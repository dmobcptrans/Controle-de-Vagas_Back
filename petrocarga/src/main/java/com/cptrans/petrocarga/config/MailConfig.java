
package com.cptrans.petrocarga.config;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Configuração do JavaMailSender real.
 * Ativada quando spring.mail.host OU SMTP_HOST está definido.
 */
@Configuration
@ConditionalOnProperty(name = {"spring.mail.host", "SMTP_HOST"}, matchIfMissing = false)
public class MailConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailConfig.class);

    // Tenta spring.mail.host primeiro, depois SMTP_HOST como fallback
    @Value("${spring.mail.host:${SMTP_HOST:}}")
    private String host;

    @Value("${spring.mail.port:${SMTP_PORT:465}}")
    private int port;

    @Value("${spring.mail.username:${SMTP_USERNAME:}}")
    private String username;

    @Value("${spring.mail.password:${SMTP_PASSWORD:}}")
    private String password;

    @Value("${spring.mail.from:${SMTP_FROM:}}")
    private String smtpFrom;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable:${SMTP_STARTTLS:false}}")
    private boolean starttls;

    @Value("${spring.mail.properties.mail.smtp.ssl.enable:${SMTP_SSL:true}}")
    private boolean ssl;

    @Value("${spring.mail.properties.mail.smtp.timeout:${SMTP_TIMEOUT:10000}}")
    private int timeoutMillis;

/**
 *
 * Esse bean só é criado se spring.mail.host OU SMTP_HOST estiverem presentes. 
 * Ele configura um JavaMailSenderImpl com as informações SMTP.
 *
 * Os timeouts são configurados para evitar bloqueios indeterminados em ambientes nuvem.
 *
 */
    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl impl = new JavaMailSenderImpl();

        // SMTP_HOST is guaranteed to be present due to @ConditionalOnProperty
        impl.setHost(host);
        impl.setPort(port);

        if (username != null && !username.isBlank()) {
            impl.setUsername(username);
        }
        if (password != null && !password.isBlank()) {
            impl.setPassword(password);
        }

        Properties props = impl.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", (username != null && !username.isBlank()) ? "true" : "false");
        props.put("mail.smtp.starttls.enable", Boolean.toString(starttls));
        props.put("mail.smtp.ssl.enable", Boolean.toString(ssl));

        // Timeouts to avoid indefinite blocking in cloud environments
        props.put("mail.smtp.connectiontimeout", Integer.toString(timeoutMillis));
        props.put("mail.smtp.timeout", Integer.toString(timeoutMillis));
        props.put("mail.smtp.writetimeout", Integer.toString(timeoutMillis));

        LOGGER.info("========================================================");
        LOGGER.info("  REAL JavaMailSender CONFIGURED SUCCESSFULLY");
        LOGGER.info("  Host: {}, Port: {}", host, port);
        LOGGER.info("  Username present: {}", (username != null && !username.isBlank()));
        LOGGER.info("  STARTTLS: {}, SSL: {}", starttls, ssl);
        LOGGER.info("  Timeout: {}ms", timeoutMillis);
        LOGGER.info("========================================================");

        // Note: 'from' concept is handled by application properties / EmailService
        return impl;
    }
}
