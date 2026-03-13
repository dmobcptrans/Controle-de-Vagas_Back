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
@ConfigurationProperties(prefix = "app.security.aes-criptography")
public class CriptoService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    private Map<Integer, String> keys;
    private Integer activeKeyVersion;

    /**
     * Encripta uma string usando o algoritmo AES com uma chave fornecida.
     * A string encriptada é retornada em formato Base64.
     * A chave deve ser armazenada no mapa de chaves com a versão ativa como chave
     * O IV é gerado aleatoriamente e armazenado nos primeiros 12 bytes do resultado.
     * A string encriptada é armazenada nos bytes restantes do resultado.
     * @param string a string a ser encriptada
     * @return a string encriptada em formato Base64
     * @throws RuntimeException se ocorrer um erro durante o processo de encriptação
     */
    public String encrypt(String string) {
        try {
            
            String key = keys.get(activeKeyVersion);

            byte[] keyBytes = Base64.getDecoder().decode(key);
            
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
            
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);
            
            byte[] encrypted = cipher.doFinal(string.getBytes(StandardCharsets.UTF_8));

            byte[] result = new byte[IV_LENGTH + encrypted.length];

            System.arraycopy(iv, 0, result, 0, IV_LENGTH);

            System.arraycopy(encrypted, 0, result, IV_LENGTH, encrypted.length);

            return Base64.getEncoder().encodeToString(result);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar String", e);
        }
    }


    /**
     * Descriptografa uma string previamente encriptada com AES/GCM.
     * A string encriptada é recebida como parâmetro.
     * O IV é extraido dos primeiros 12 bytes da string encriptada.
     * A chave de descriptografia é buscada no mapa de chaves com base no valor do parâmetro "version".
     * A string descriptografada é retornada em formato String.
     * @param encryptedString a string encriptada
     * @param version a versão da chave de descriptografia
     * @return a string descriptografada
     * @throws RuntimeException se ocorrer um erro durante o processo de descriptografia
     */
    public String decrypt(String encryptedString, Integer version) {
        try {

            String key = keys.get(version);

            byte[] decoded = Base64.getDecoder().decode(encryptedString);

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
            throw new RuntimeException("Erro ao descriptografar String", e);
        }
    }

    /**
     * Retorna um mapa contendo as chaves de descriptografia,
     * onde a chave é a versão da chave e o valor é a chave
     * em si mesma.
     * @return um mapa contendo as chaves de descriptografia
     */
    public Map<Integer, String> getKeys() {
        return keys;
    }

    /**
     * Define o mapa de chaves de criptografia, onde a chave
     * é a versão da chave e o valor é a chave em si mesma.
     * @param keys o mapa de chaves de criptografia
     */
    public void setKeys(Map<Integer, String> keys) {
        this.keys = keys;
    }

    /**
     * Retorna a versão ativa da chave de criptografia.

     * @return a versão ativa da chave de criptografia
     */
    public Integer getActiveKeyVersion() {
        return activeKeyVersion;
    }

    /**
     * Define a versão ativa da chave de criptografia.
     * 
     * @param activeKeyVersion a versão ativa da chave de criptografia
     */
    public void setActiveKeyVersion(Integer activeKeyVersion) {
        this.activeKeyVersion = activeKeyVersion;
    }
}