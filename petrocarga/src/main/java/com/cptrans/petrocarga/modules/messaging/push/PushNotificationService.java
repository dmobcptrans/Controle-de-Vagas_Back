package com.cptrans.petrocarga.modules.messaging.push;

import com.cptrans.petrocarga.modules.notificacao.entity.Notificacao;

public interface PushNotificationService {
    void enviarNotificacao(Notificacao notificacao);
}
