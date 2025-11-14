package com.example.projeto.utils; // Mude se criar em outra pasta

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtils {

    /**
     * Gera um hash SHA-256 para uma string (senha).
     */
    public static String sha256(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // Usar StandardCharsets.UTF_8 é importante para consistência
            byte[] encodedhash = digest.digest(s.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null; // Ou lançar uma exceção
        }
    }

    /**
     * Converte um array de bytes para sua representação em String hexadecimal.
     */
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}