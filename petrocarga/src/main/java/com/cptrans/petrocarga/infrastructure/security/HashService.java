package com.cptrans.petrocarga.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HashService {

    private static final String ALGORITHM = "HmacSHA256";

    @Value("${app.security.aes-criptography.pepper}")
    private String secret;

    /**
     * Gera um hash SHA256 da string fornecida.
     * O hash é gerado com base na chave secreta definida na configuração do aplicativo.
     * O hash é retornado em formato hexadecimal.
     * Se ocorrer um erro durante o processo de geração do hash, uma exceção de tipo RuntimeException é lançada.
     * @param string a string a ser hasheada
     * @return o hash da string em formato hexadecimal
     * @throws RuntimeException se ocorrer um erro durante o processo de geração do hash
     */
    public String hash(String string) {
        try {

            Mac mac = Mac.getInstance(ALGORITHM);

            SecretKeySpec key = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    ALGORITHM);

            mac.init(key);

            byte[] result = mac.doFinal(string.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(result);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar hash da String", e);
        }
    }
}