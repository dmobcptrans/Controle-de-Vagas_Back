package com.cptrans.petrocarga.application.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.cptrans.petrocarga.application.port.out.PushNotificationService;
import com.cptrans.petrocarga.application.port.out.RealTimeNotificationService;
import com.cptrans.petrocarga.domain.events.NotificacaoCriadaEvent;
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
