package br.edu.ifrs.tcc_novo.utils;

import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashSenha {
    public static String sha256(String senha) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(senha.getBytes("UTF-8"));

            // Converte bytes para hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e("HashingUtils", "Algoritmo SHA-256 não encontrado", e);
            return null;
        } catch (java.io.UnsupportedEncodingException e) {
            Log.e("HashingUtils", "Codificação UTF-8 não suportada", e);
            return null;
        }
    }
}
