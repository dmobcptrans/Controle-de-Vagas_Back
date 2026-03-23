package com.cptrans.petrocarga.shared.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class GenerateUtils {
    public static void main(String[] args) {
        byte[] key = new byte[256]; // AES-256
        new SecureRandom().nextBytes(key);

        System.out.println(Base64.getEncoder().encodeToString(key));
    }
}
