package com.cptrans.petrocarga.modules.messaging.realtime;

import java.util.UUID;

import com.cptrans.petrocarga.modules.notificacao.entity.Notificacao;

public interface RealTimeNotificationService {
    void enviarNotificacao(Notificacao notificacao);
    boolean isAtivo(UUID usuarioId);
}
