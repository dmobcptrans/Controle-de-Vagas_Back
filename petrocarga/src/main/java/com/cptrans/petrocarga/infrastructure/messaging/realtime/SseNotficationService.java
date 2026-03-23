package com.cptrans.petrocarga.infrastructure.messaging.realtime;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.cptrans.petrocarga.application.port.out.RealTimeNotificationService;
import com.cptrans.petrocarga.domain.entities.Notificacao;

import jakarta.annotation.PreDestroy;

@Service
public class SseNotficationService implements RealTimeNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SseNotficationService.class);

    private final Map<UUID, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final Map<SseEmitter, ScheduledFuture<?>> heartbeatTasks = new ConcurrentHashMap<>();
    private final AtomicInteger totalConexoes = new AtomicInteger(0);
    private final Set<SseEmitter> activeEmitters = ConcurrentHashMap.newKeySet();
    private static final int MAX_CONEXOES_POR_USUARIO = 3;
    private static final int MAX_CONEXOES_GLOBAL = 1000;

    private final ScheduledExecutorService heartbeatScheduler =
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SSE-Heartbeat");
            t.setDaemon(true);
            return t;
        });

    /**
     * Conecta um usuário ao serviço de notificações em tempo real.
     * @param usuarioId o id do usuário a ser conectado
     * @return o SseEmitter para o usuário conectado
     */
    public SseEmitter connect(UUID usuarioId) {
        SseEmitter emitter = new SseEmitter(TimeUnit.MINUTES.toMillis(15));

        Set<SseEmitter> userEmitters = emitters.computeIfAbsent(usuarioId, id -> ConcurrentHashMap.newKeySet());

        synchronized (userEmitters) {

            if (userEmitters.size() >= MAX_CONEXOES_POR_USUARIO) {
                throw new RuntimeException("Muitas conexões simultâneas");
            }

            if (totalConexoes.get() >= MAX_CONEXOES_GLOBAL) {
                throw new RuntimeException("Servidor sobrecarregado");
            }

            userEmitters.add(emitter);
            activeEmitters.add(emitter);
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
                    .name("INIT")
                    .data("connected")
            );
        } catch (IOException e) {
            log.debug("Failed to send INIT to user {}: {}", usuarioId, e.getMessage());
            cleanupEmitter(usuarioId, emitter);
            return emitter;
        }

        startHeartbeat(usuarioId, emitter);
        return emitter;
    }

    /**
     * Inicia um task para enviar um heartbeat para o usuário em um intervalo de 15 segundos.
     * O task é cancelado e removido se o usuário desconectar.
     * @param usuarioId o id do usuário a ser conectado
     * @param emitter o SseEmitter do usuário conectado
     */
    private void startHeartbeat(UUID usuarioId, SseEmitter emitter) {
        ScheduledFuture<?> task = heartbeatScheduler.scheduleAtFixedRate(() -> {
            try {
                emitter.send(
                    SseEmitter.event()
                        .name("heartbeat")
                        .data("ping")
                );
            } catch (IOException e) {
                cleanupEmitter(usuarioId, emitter);
            }
        }, 15, 15, TimeUnit.SECONDS);

        heartbeatTasks.put(emitter, task);
    }

    /**
     * Remove o SseEmitter do usuário e cancela o task de heartbeat associado.
     *
     * 
     * @param usuarioId o id do usuário a ser desconectado
     * @param emitter o SseEmitter do usuário a ser desconectado
     */
    private void cleanupEmitter(UUID usuarioId, SseEmitter emitter) {
        if (!activeEmitters.remove(emitter)) {
            return;
        }

        ScheduledFuture<?> task = heartbeatTasks.remove(emitter);
        if (task != null) {
            task.cancel(false);
        }

        Set<SseEmitter> set = emitters.get(usuarioId);
        if (set != null) {
            synchronized (set) {
                set.remove(emitter);
                if (set.isEmpty()) {
                    emitters.remove(usuarioId);
                }
            }
        }

        totalConexoes.decrementAndGet();

        try {
            emitter.complete();
        } catch (Exception ignored) {}
    }

    /**
     * Verifica se a exceção foi causada por uma desconexão do lado do cliente.
     * 
     * @param e a exceção a ser verificada
     * @return true se a exceção for causada por uma desconexão do lado do cliente, false caso contrário
     */
    private boolean isClientDisconnect(Throwable e) {
        String message = e.getMessage();
        if (message == null) return false;

        return message.contains("Broken pipe")
            || message.contains("Connection reset")
            || message.contains("ClientAbortException");
    }

    /**
     * Envia uma notificação para todos os sse emitters conectados do usuário com base no seu id.
     * 
     * @param notificacao a notificação a ser enviada
     */
    @Override
    public void enviarNotificacao(Notificacao notificacao) {
        Set<SseEmitter> userEmitters = emitters.get(notificacao.getUsuarioId());

        if (userEmitters == null || userEmitters.isEmpty()) return;

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

    /**
     * Verifica se o usuário está ativo no serviço de notificações em tempo real.
     * Um usuário é considerado ativo se ele tiver pelo menos um SseEmitter conectado.
     * 
     * @param usuarioId o id do usuário a ser verificado
     * @return true se o usuário estiver ativo, false caso contrário
     */
    @Override
    public boolean isAtivo(UUID usuarioId) {
        return emitters.containsKey(usuarioId)
            && !emitters.get(usuarioId).isEmpty();
    }

    /**
     * Realiza a limpeza dos recursos quando a aplicação for encerrada.
     * 
     * Este método é anotado com @PreDestroy para garantir que seja chamado antes da destruição do bean, permitindo que o serviço de notificações em tempo real seja encerrado de forma ordenada, evitando vazamentos de recursos e garantindo a desconexão.
     */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down SSE service...");
        heartbeatScheduler.shutdownNow();
        heartbeatTasks.clear();
        emitters.clear();
        activeEmitters.clear();
    }
}