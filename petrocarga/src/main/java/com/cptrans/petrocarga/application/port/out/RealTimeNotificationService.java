package com.cptrans.petrocarga.application.port.out;

import java.util.UUID;

import com.cptrans.petrocarga.domain.entities.Notificacao;

public interface RealTimeNotificationService {
    void enviarNotificacao(Notificacao notificacao);
    boolean isAtivo(UUID usuarioId);
}
