package com.cptrans.petrocarga.modules.events.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.cptrans.petrocarga.modules.events.NotificacaoCriadaEvent;
import com.cptrans.petrocarga.modules.messaging.push.PushNotificationService;
import com.cptrans.petrocarga.modules.messaging.realtime.RealTimeNotificationService;
import com.google.firebase.FirebaseApp;

@Component
public class NotificacaoCriadaListener {
    
    @Autowired
    private RealTimeNotificationService realTimeNotificationService;
    @Autowired
    private PushNotificationService pushNotificationService;

/**
 * Essa função é responsável por enviar notificações quando uma nova notificação é criada.
 * 
 * Ela usa a RealTimeNotificationService para enviar a notificação em tempo real e caso o FirebaseApp não esteja vazio, usa o PushNotificationService para enviar a notificação push.
.
 
 * @param event o evento disparado quando uma nova notificação é criada, contendo a notificação a ser enviada
 */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotificacaoEnviada(NotificacaoCriadaEvent event) {
        realTimeNotificationService.enviarNotificacao(event.notificacao());
        if (!FirebaseApp.getApps().isEmpty()){
            pushNotificationService.enviarNotificacao(event.notificacao());
        }
    }
    
}
