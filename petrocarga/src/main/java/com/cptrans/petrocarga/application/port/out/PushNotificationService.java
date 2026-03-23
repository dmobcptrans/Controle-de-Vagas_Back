package com.cptrans.petrocarga.application.port.out;

import com.cptrans.petrocarga.domain.entities.Notificacao;

public interface PushNotificationService {
    void enviarNotificacao(Notificacao notificacao);
}
