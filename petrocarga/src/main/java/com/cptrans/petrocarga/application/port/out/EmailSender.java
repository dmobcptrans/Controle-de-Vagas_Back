package com.cptrans.petrocarga.application.port.out;

/**
 * Interface para serviços de envio de email.
 * Permite alternar entre implementações (SMTP, Resend API, etc.)
 */
public interface EmailSender {
    
    /**
     * Envia código de ativação de conta.
     * @param to email do destinatário
     * @param code código de ativação
     */
    void sendActivationCode(String to, String code, String randomPassword);
    
    /**
     * Envia código de recuperação de senha.
     * @param to email do destinatário
     * @param code código de recuperação
     */
    void sendPasswordResetCode(String to, String code);
}
