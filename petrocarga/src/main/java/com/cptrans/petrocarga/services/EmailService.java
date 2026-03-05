package com.cptrans.petrocarga.services;

/*
 * EmailService (SMTP-based)
 *
 * IMPORTANTE: O Railway bloqueia TODAS as portas SMTP (25, 465, 587, 2525).
 * Em produção no Railway, use ResendEmailService (API HTTP) ao invés deste.
 * 
 * Este serviço só é ativado quando:
 * 1. RESEND_API_KEY NÃO está configurado (ResendEmailService não está ativo)
 * 2. OU em ambiente local onde SMTP funciona normalmente
 *
 * Para Railway, configure:
 * - RESEND_API_KEY: sua API key do Resend
 * - RESEND_FROM: onboarding@resend.dev (tier gratuito)
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.infrastructure.email.EmailSender;

@Service
public class EmailService implements EmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${SMTP_FROM:}")
    private String from;

    @Value("${SMTP_USERNAME:}")
    private String mailUsername;

    @Value("${FRONTEND_URL:http://localhost:3000}")
    private String frontendBaseUrl;

    @Value("${app.mailSender.enabled:true}")
    private Boolean enabled;


    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        LOGGER.warn("========================================================");
        LOGGER.warn("  EmailService (SMTP) initialized");
        LOGGER.warn("  ATENCAO: Railway BLOQUEIA todas as portas SMTP!");
        LOGGER.warn("  Para emails funcionarem, configure RESEND_API_KEY");
        LOGGER.warn("========================================================");
    }

    private void logMailEndpointInfo() {
        if(enabled.equals(Boolean.FALSE)){
            LOGGER.info("Mail sender is not enabled");
            return;
        }
        try {
            if (mailSender instanceof JavaMailSenderImpl) {
                JavaMailSenderImpl impl = (JavaMailSenderImpl) mailSender;
                String host = impl.getHost();
                int port = impl.getPort();
                // Do NOT log password or sensitive props
                LOGGER.info("Mail sender config - host: {}, port: {}, from: {}", host, port, from);
            } else {
                LOGGER.info("Mail sender is not JavaMailSenderImpl, cannot read host/port. from={}", from);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to inspect JavaMailSender implementation: {}", e.getMessage());
        }
    }

    @Override
    @Async("taskExecutor")
    public void sendActivationCode(String to, String code, String randomPassword) {
        if(enabled.equals(Boolean.FALSE)){
            LOGGER.info("Mail sender is not enabled");
            return;
        }
        // Ensure 'from' uses configured username when available
        if ((from == null || from.isBlank()) && mailUsername != null && !mailUsername.isBlank()) {
            from = mailUsername;
        }

        logMailEndpointInfo();
        String text;
        if(randomPassword == null){
            text = "Seu código de ativação é: " + code + "\n\n" +
            "Clique no link abaixo para ativar sua conta:\n" +
            frontendBaseUrl + "/autorizacao/login?ativar-conta=true\n\n" +
            "Se vocé nao solicitou, ignore este e-mail.";
        }else{
            text = "Seu código de ativação é: " + code + "\n\n" +
            "Sua senha de acesso é: " + randomPassword + "\n\n" +
            "Lembre-se de alterar sua senha posterioremente através do 'esqueci minha senha'." + "\n\n" +
            "Clique no link abaixo para ativar sua conta:\n" +
            frontendBaseUrl + "/autorizacao/login?ativar-conta=true\n\n" +
            "Se vocé nao solicitou, ignore este e-mail.";
        }

        try {
            LOGGER.info("[{}] Sending activation code to {}", Thread.currentThread().getName(), to);
            LOGGER.debug("Activation code for {}: {}", to, code);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("Código de Ativação - PetroCarga");
            message.setText(text);
            mailSender.send(message);
            LOGGER.info("[{}] Email de ativação enviado com sucesso para: {}", Thread.currentThread().getName(), to);
        } catch (MailException e) {
            LOGGER.error("[{}] MailException ao enviar ativação para {}: {}", Thread.currentThread().getName(), to, e.getMessage(), e);
            // Não silenciar: rethrow para que o AsyncUncaughtExceptionHandler trate o erro e registre stacktrace
            throw e;
        } catch (Exception e) {
            LOGGER.error("[{}] Erro inesperado ao enviar ativação para {}: {}", Thread.currentThread().getName(), to, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    @Async("taskExecutor")
    public void sendPasswordResetCode(String to, String code) {
        if(enabled.equals(Boolean.FALSE)){
            LOGGER.info("Mail sender is not enabled");
            return;
        }
        if ((from == null || from.isBlank()) && mailUsername != null && !mailUsername.isBlank()) {
            from = mailUsername;
        }

        logMailEndpointInfo();

        try {
            LOGGER.info("[{}] Sending password reset to {}", Thread.currentThread().getName(), to);
            LOGGER.debug("Password reset code for {}: {}", to, code);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("Recuperação de Senha - PetroCarga");
            message.setText("Você solicitou a recuperação de senha.\n\n" +
                    "Seu código de recuperação é: " + code + "\n\n" +
                    "Clique no link abaixo para redefinir sua senha:\n" +
                    frontendBaseUrl + "/autorizacao/nova-senha/\n\n" +
                    "Este código expira em 10 minutos.\n" +
                    "Se você não solicitou esta recuperação, ignore este e-mail.");

            mailSender.send(message);
            LOGGER.info("[{}] Email de reset de senha enviado com sucesso para: {}", Thread.currentThread().getName(), to);
        } catch (MailException e) {
            LOGGER.error("[{}] MailException ao enviar reset para {}: {}", Thread.currentThread().getName(), to, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("[{}] Erro inesperado ao enviar reset para {}: {}", Thread.currentThread().getName(), to, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
