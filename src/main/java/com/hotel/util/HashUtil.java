package com.hotel.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Utilidad para convertir contraseñas en hash (así no se guardan en texto plano). */
public class HashUtil {

    /** Devuelve el hash SHA-256 del texto, en hexadecimal (64 caracteres). */
    public static String sha256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(texto.getBytes(StandardCharsets.UTF_8));

            // Convierte cada byte a su representación hexadecimal de 2 caracteres
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 existe en todo Java; este error no debería ocurrir
            throw new RuntimeException(e);
        }
    }
}