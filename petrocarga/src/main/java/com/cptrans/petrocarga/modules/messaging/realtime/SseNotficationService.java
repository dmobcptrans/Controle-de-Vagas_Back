package com.cptrans.petrocarga.modules.messaging.realtime;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.cptrans.petrocarga.modules.notificacao.entity.Notificacao;
import com.cptrans.petrocarga.shared.exceptions.GlobalHandlerExceptions;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class SseNotficationService implements RealTimeNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SseNotficationService.class);

    private static final int MAX_CONEXOES_POR_USUARIO = 3;
    private static final int MAX_CONEXOES_GLOBAL = 10_000;

    private static final Duration SSE_TIMEOUT = Duration.ofMinutes(15);
    private static final Duration HEARTBEAT_INTERVAL = Duration.ofSeconds(15);

    private final Map<UUID, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final Map<SseEmitter, UUID> emitterOwners = new ConcurrentHashMap<>();
    private final Set<SseEmitter> activeEmitters = ConcurrentHashMap.newKeySet();

    private final AtomicInteger totalConexoes = new AtomicInteger();
    private final AtomicBoolean heartbeatRunning = new AtomicBoolean(false);

    private final int heartbeatWorkers = Math.max(2, Runtime.getRuntime().availableProcessors());

    /**
     * Apenas agenda o heartbeat.
     */
    private final ScheduledExecutorService heartbeatScheduler =
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SSE-Heartbeat-Scheduler");
            t.setDaemon(true);
            return t;
        });

    /**
     * Executa os envios em paralelo.
     */
    private final ExecutorService heartbeatExecutor =
        Executors.newFixedThreadPool(
            heartbeatWorkers,
            r -> {
                Thread t = new Thread(r, "SSE-Heartbeat-Worker");
                t.setDaemon(true);
                return t;
            }
        );

    @PostConstruct
    public void startHeartbeat() {

        heartbeatScheduler.scheduleAtFixedRate(
            this::sendHeartbeatToAll,
            HEARTBEAT_INTERVAL.toSeconds(),
            HEARTBEAT_INTERVAL.toSeconds(),
            TimeUnit.SECONDS
        );

    }

    @Override
    public SseEmitter connect(UUID usuarioId) {

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT.toMillis());

        Set<SseEmitter> userEmitters =
            emitters.computeIfAbsent(usuarioId, id -> ConcurrentHashMap.newKeySet());

        synchronized (userEmitters) {

            if (userEmitters.size() >= MAX_CONEXOES_POR_USUARIO) throw new GlobalHandlerExceptions.MuitasConexoesSimultaneasException();

            if (totalConexoes.get() >= MAX_CONEXOES_GLOBAL) throw new GlobalHandlerExceptions.ServidorSobrecarregadoException();

            userEmitters.add(emitter);
            activeEmitters.add(emitter);
            emitterOwners.put(emitter, usuarioId);

            totalConexoes.incrementAndGet();
        }

        Runnable cleanup = () -> cleanupEmitter(usuarioId, emitter);

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);

        emitter.onError(e -> {

            if (!isClientDisconnect(e)) {
                log.warn("SSE error for user {}: {}", usuarioId, e.getMessage());
            }

            cleanup.run();

        });

        try {

            emitter.send(
                SseEmitter.event()
                    .id(UUID.randomUUID().toString())
                    .name("INIT")
                    .data("connected")
            );

        } catch (IOException e) {

            log.debug("Failed to send INIT to user {}: {}", usuarioId, e.getMessage());

            cleanupEmitter(usuarioId, emitter);

            return emitter;
        }

        return emitter;
    }

    /**
     * Dispara o envio dos heartbeats em paralelo.
     */
    private void sendHeartbeatToAll() {
        if (!heartbeatRunning.compareAndSet(false, true)) return;

        try {
            SseEmitter[] snapshot = activeEmitters.toArray(SseEmitter[]::new);

            if (snapshot.length == 0) return;

            int chunkSize = Math.max(
                1,
                (int) Math.ceil((double) snapshot.length / heartbeatWorkers)
            );

            int totalLotes = (int) Math.ceil((double) snapshot.length / chunkSize);

            CountDownLatch latch = new CountDownLatch(totalLotes);

            for (int from = 0; from < snapshot.length; from += chunkSize) {
                final int inicio = from;
                final int fim = Math.min(snapshot.length, inicio + chunkSize);
                try {
                    heartbeatExecutor.execute(() -> {
                        try {
                            for (int i = inicio; i < fim; i++) {
                                SseEmitter emitter = snapshot[i];
                                try {
                                    emitter.send(
                                        SseEmitter.event()
                                            .name("heartbeat")
                                                .data("ping")
                                    );
                                } catch (IOException e) {
                                    UUID usuarioId = emitterOwners.get(emitter);
                                    if (usuarioId != null) cleanupEmitter(usuarioId, emitter);
                                }
                            }
                        } finally {
                            latch.countDown();
                        }
                    });
                } catch (RejectedExecutionException e) {
                   latch.countDown();
                }
            }

            if (!latch.await(10, TimeUnit.SECONDS)) log.warn("Heartbeat cycle did not finish within the expected time.");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                heartbeatRunning.set(false);
            }
    }

    private void cleanupEmitter(UUID usuarioId, SseEmitter emitter) {
        if (!activeEmitters.remove(emitter)) return;

        emitterOwners.remove(emitter);

        Set<SseEmitter> set = emitters.get(usuarioId);

        if (set != null) {
            synchronized (set) {
                set.remove(emitter);
                if (set.isEmpty()) emitters.remove(usuarioId, set);
            }
        }

        totalConexoes.decrementAndGet();

        try {
            emitter.complete();
        } catch (Exception ignored){

        }
    }

    private boolean isClientDisconnect(Throwable e) {

        String message = e.getMessage();

        if (message == null) {
            return false;
        }

        return message.contains("Broken pipe")
            || message.contains("Connection reset")
            || message.contains("ClientAbortException");
    }

    @Override
    public void enviarNotificacao(Notificacao notificacao) {

        Set<SseEmitter> userEmitters = emitters.get(notificacao.getUsuarioId());

        if (userEmitters == null || userEmitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : Set.copyOf(userEmitters)) {

            try {

                emitter.send(
                    SseEmitter.event()
                        .id(notificacao.getId().toString())
                        .name("notificacao")
                        .data(notificacao)
                        .reconnectTime(3000)
                );

            } catch (IOException e) {

                cleanupEmitter(notificacao.getUsuarioId(), emitter);

            }

        }

    }

    @Override
    public boolean isAtivo(UUID usuarioId) {

        Set<SseEmitter> set = emitters.get(usuarioId);

        return set != null && !set.isEmpty();

    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down SSE service...");

        heartbeatScheduler.shutdown();
        heartbeatExecutor.shutdown();

        try {
            if (!heartbeatScheduler.awaitTermination(5, TimeUnit.SECONDS)) heartbeatScheduler.shutdownNow();
            if (!heartbeatExecutor.awaitTermination(5, TimeUnit.SECONDS)) heartbeatExecutor.shutdownNow();
        } catch (InterruptedException e) {
            heartbeatScheduler.shutdownNow();
            heartbeatExecutor.shutdownNow();

            Thread.currentThread().interrupt();
        }

        emitters.clear();
        emitterOwners.clear();
        activeEmitters.clear();

    }
}