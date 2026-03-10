package com.cptrans.petrocarga.services;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CpfHashService {

    private static final String ALGORITHM = "HmacSHA256";

    @Value("${app.security.cpf.pepper}")
    private String secret;

    public String hash(String cpf) {
        try {

            Mac mac = Mac.getInstance(ALGORITHM);

            SecretKeySpec key = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    ALGORITHM);

            mac.init(key);

            byte[] result = mac.doFinal(cpf.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(result);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar hash do CPF", e);
        }
    }
}