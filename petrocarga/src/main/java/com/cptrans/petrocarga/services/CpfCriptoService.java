package com.cptrans.petrocarga.services;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.security.cpf")
public class CpfCriptoService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    private Map<Integer, String> keys;
    private Integer activeKeyVersion;

    public String encrypt(String cpf) {
        try {
            
            String key = keys.get(activeKeyVersion);

            byte[] keyBytes = Base64.getDecoder().decode(key);
            
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
            
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);
            
            byte[] encrypted = cipher.doFinal(cpf.getBytes(StandardCharsets.UTF_8));

            byte[] result = new byte[IV_LENGTH + encrypted.length];

            System.arraycopy(iv, 0, result, 0, IV_LENGTH);

            System.arraycopy(encrypted, 0, result, IV_LENGTH, encrypted.length);

            return Base64.getEncoder().encodeToString(result);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar CPF", e);
        }
    }

    public String decrypt(String encryptedCpf, Integer version) {
        try {

            String key = keys.get(version);

            byte[] decoded = Base64.getDecoder().decode(encryptedCpf);

            byte[] keyBytes = Base64.getDecoder().decode(key);


            byte[] iv = new byte[IV_LENGTH];
            byte[] cipherText = new byte[decoded.length - IV_LENGTH];

            System.arraycopy(decoded, 0, iv, 0, IV_LENGTH);
            System.arraycopy(decoded, IV_LENGTH, cipherText, 0, cipherText.length);

            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");


            Cipher cipher = Cipher.getInstance(ALGORITHM);

            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);

            byte[] decrypted = cipher.doFinal(cipherText);

            return new String(decrypted);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao descriptografar CPF", e);
        }
    }

    public Map<Integer, String> getKeys() {
        return keys;
    }

    public void setKeys(Map<Integer, String> keys) {
        this.keys = keys;
    }

    public Integer getActiveKeyVersion() {
        return activeKeyVersion;
    }

    public void setActiveKeyVersion(Integer activeKeyVersion) {
        this.activeKeyVersion = activeKeyVersion;
    }
}