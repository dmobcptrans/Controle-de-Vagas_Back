package com.cptrans.petrocarga.infrastructure.messaging.push;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.application.port.out.PushNotificationService;
import com.cptrans.petrocarga.domain.entities.Notificacao;
import com.cptrans.petrocarga.domain.entities.PushToken;
import com.cptrans.petrocarga.domain.repositories.PushTokenRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;

@Service
public class FirebasePushService implements PushNotificationService {

    @Autowired
    private PushTokenRepository pushTokenRepository;

    /**
     * Envia uma notificação para todos os dispositivos de um usuário com base no seu id.
     * Só permite que notificação seja enviada por um usuário com permissão de ADMIN ou GESTOR.
     * 
     * @param notificacao a notificação a ser enviada
     */
    @Override
    public void enviarNotificacao(Notificacao notificacao) {
        List<PushToken> tokens = pushTokenRepository.findByUsuarioIdAndAtivo(notificacao.getUsuarioId(), true);
        System.out.println("tokens list is empty: " + tokens.isEmpty());
        if (!tokens.isEmpty()) {
            for (PushToken token : tokens) {
                Message message = Message.builder()
                    .setToken(token.getToken())
                    .putAllData(
                        Map.of(
                            "notificacaoId", notificacao.getId().toString(),
                            "title", notificacao.getTitulo(),
                            "body", notificacao.getMensagem(),
                            "tipo", notificacao.getTipo().name(),
                            "lida", String.valueOf(notificacao.isLida()),
                            "metadata", notificacao.getMetadata().toString()
                                
                        ))
                    .build();

                FirebaseMessaging.getInstance().sendAsync(message);
                System.out.println("push enviado com sucesso para: " + notificacao.getUsuarioId());
            }
        }
    }
}
