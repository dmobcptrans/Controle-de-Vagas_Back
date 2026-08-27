package com.cptrans.petrocarga.modules.cripto;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.cripto.exceptions.CriptoExceptions;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "app.security.aes-criptography")
@Getter
@Setter
public class HashService {

    private static final String ALGORITHM = "HmacSHA256";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private Map<Integer, String> peppers;
    private Integer activePepperVersion;

    /**
     * Gera um hash SHA256 da string fornecida.
     * O hash é gerado com base na chave secreta definida na configuração do aplicativo.
     * O hash é retornado em formato hexadecimal.
     * Se ocorrer um erro durante o processo de geração do hash, uma exceção de tipo RuntimeException é lançada.
     * @param string a string a ser hasheada
     * @return o hash da string em formato hexadecimal
     * @throws RuntimeException se ocorrer um erro durante o processo de geração do hash
     */
    public String hash(String string, Integer pepperVersion) {
        try {
            if (pepperVersion == null) throw new CriptoExceptions.HashException();
            String pepper = peppers.get(pepperVersion);
            if (pepper == null) throw new CriptoExceptions.HashException();

            Mac mac = Mac.getInstance(ALGORITHM);

            SecretKeySpec key = new SecretKeySpec(
                    peppers.get(pepperVersion).getBytes(StandardCharsets.UTF_8),
                    ALGORITHM);

            mac.init(key);

            byte[] result = mac.doFinal(string.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(result);

        } catch (Exception e) {
            throw new CriptoExceptions.HashException();
        }
    }

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
            if (activePepperVersion == null) throw new CriptoExceptions.HashException();
            String pepper = peppers.get(activePepperVersion);
            if (pepper == null) throw new CriptoExceptions.HashException();

            Mac mac = Mac.getInstance(ALGORITHM);

            SecretKeySpec key = new SecretKeySpec(
                    peppers.get(activePepperVersion).getBytes(StandardCharsets.UTF_8),
                    ALGORITHM);

            mac.init(key);

            byte[] result = mac.doFinal(string.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(result);

        } catch (Exception e) {
            throw new CriptoExceptions.HashException();
        }
    }
    
    public static String gerarToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}