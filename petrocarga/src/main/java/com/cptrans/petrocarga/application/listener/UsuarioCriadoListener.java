package com.cptrans.petrocarga.application.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.cptrans.petrocarga.domain.event.UsuarioCriadoEvent;
import com.cptrans.petrocarga.infrastructure.email.EmailSender;

@Component
public class UsuarioCriadoListener {
    @Autowired
    private EmailSender emailSender;

/**
 * Envia um email de ativação para o usuario criado, contendo o código de ativação e a senha aleatória (se houver, caso contrário, apenas o código de ativação)).
 * @param event o evento disparado quando um novo usuário é criado
 */
    @TransactionalEventListener( phase = TransactionPhase.AFTER_COMMIT )
    public void onUsuarioCriado(UsuarioCriadoEvent event) {
        emailSender.sendActivationCode(event.email(), event.codigo(), event.randomPassword());
    }
}
