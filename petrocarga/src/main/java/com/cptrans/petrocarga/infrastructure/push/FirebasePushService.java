package com.cptrans.petrocarga.infrastructure.push;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.application.service.PushNotificationService;
import com.cptrans.petrocarga.models.Notificacao;
import com.cptrans.petrocarga.models.PushToken;
import com.cptrans.petrocarga.repositories.PushTokenRepository;
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
            }
        }
    }
}
