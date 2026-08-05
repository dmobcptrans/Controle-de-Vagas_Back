package com.cptrans.petrocarga.modules.messaging.realtime;

import java.util.UUID;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.cptrans.petrocarga.modules.notificacao.entity.Notificacao;

public interface RealTimeNotificationService {
    void enviarNotificacao(Notificacao notificacao);
    boolean isAtivo(UUID usuarioId);
    SseEmitter connect(UUID usuarioId);
}