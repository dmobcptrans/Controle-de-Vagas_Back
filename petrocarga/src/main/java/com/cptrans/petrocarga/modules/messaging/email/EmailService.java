package com.cptrans.petrocarga.modules.messaging.email;


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

import com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.entity.ConviteMotoristaEmpresa;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService implements EmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String from;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${app.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    @Value("${app.mailSender.enabled:true}")
    private Boolean enabled;

    private void logMailEndpointInfo() {
        if(enabled.equals(Boolean.FALSE)){
            LOGGER.info("Mail sender is not enabled");
            return;
        }
        try {
            if (mailSender instanceof JavaMailSenderImpl) {
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
        validarMailSender();

        String text;
        if (randomPassword == null){
            text = "Seu código de ativação é: " + code + "\n\n" +
            "Clique no link abaixo para ativar sua conta:\n" +
            frontendBaseUrl + "/autorizacao/login?ativar-conta=true\n\n" +
            "Se vocé nao solicitou, ignore este e-mail.";
        } else {
            text = "Seu código de ativação é: " + code + "\n\n" +
            "Sua senha de acesso é: " + randomPassword + "\n\n" +
            "Lembre-se de alterar sua senha posterioremente através do 'esqueci minha senha'." + "\n\n" +
            "Clique no link abaixo para ativar sua conta:\n" +
            frontendBaseUrl + "/autorizacao/login?ativar-conta=true\n\n" +
            "Se vocé nao solicitou, ignore este e-mail.";
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("Código de Ativação - PetroCarga");
            message.setText(text);
            mailSender.send(message);
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
        validarMailSender();

        try {
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
        } catch (MailException e) {
            LOGGER.error("[{}] MailException ao enviar reset para {}: {}", Thread.currentThread().getName(), to, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("[{}] Erro inesperado ao enviar reset para {}: {}", Thread.currentThread().getName(), to, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    @Async("taskExecutor")
    public void sendConviteMotoristaEmpresa(String to, ConviteMotoristaEmpresa convite, String nomeMotorista, String token) {
        validarMailSender();
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("Novo Convite");
            String text;
            
            if (convite.getMotorista() != null){
                text = """
                    Ola, ${motorista.nome}!\n\n
                    A empresa '${empresa.nome}' quer se vincular à você!\n\n
                    Acesse sua conta para aceitar ou recusar o convite: ${frontendBaseUrl}/convite-motorista-empresa?convite=${token}\n\n
                    O convite expira em 7 dias.\n\n
                    Se você nao reconhece este convite, ignore este e-mail.\n
                """.replace("${motorista.nome}", convite.getMotorista().getUsuario().getNome());
            } else {
                text = """
                    Ola, ${nomeMotorista}!\n\n
                    A empresa '${empresa.nome}' quer se vincular à você!\n\n
                    Acesse este link para confirmar ou recusar o convite: ${frontendBaseUrl}/convite-motorista-empresa?convite=${token}\n\n\n
                    O convite expira em 7 dias.\n\n
                    Se você não reconhece este convite, ignore este e-mail.\n
                """.replace("${nomeMotorista}", nomeMotorista);
            }   

            message.setText(
                text.replace("${empresa.nome}", convite.getEmpresa().getUsuario().getNome())
                .replace("${frontendBaseUrl}", frontendBaseUrl)
                .replace("${token}", token)
            );

            mailSender.send(message);

        } catch (MailException e) {
            LOGGER.error("[{}] MailException ao enviar reset para {}: {}", Thread.currentThread().getName(), to, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("[{}] Erro inesperado ao enviar reset para {}: {}", Thread.currentThread().getName(), to, e.getMessage(), e);
            throw new RuntimeException(e);
        }

    }


    private void validarMailSender(){
        if(enabled.equals(Boolean.FALSE)){
            LOGGER.info("Mail sender is not enabled");
            return;
        }
        if ((from == null || from.isBlank()) && mailUsername != null && !mailUsername.isBlank()) {
            from = mailUsername;
        }

        logMailEndpointInfo();
    }
}