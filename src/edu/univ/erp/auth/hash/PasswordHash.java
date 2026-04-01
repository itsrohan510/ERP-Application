package edu.univ.erp.auth.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;


// Password hashing using SHA-256 + salt

public class PasswordHash {
    private static final SecureRandom random = new SecureRandom();
    private static final int SALT_LENGTH = 16;

    //Hashed password with random salt returns salt:hash (both base64 encoded)
    public static String hashPassword(String password) {
        try {
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            byte[] hash = hashWithSalt(password, salt);
            String saltStr = Base64.getEncoder().encodeToString(salt);
            String hashStr = Base64.getEncoder().encodeToString(hash);
            return saltStr + ":" + hashStr;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not available", e);
        }
    }

    //Verifying password against a stored hash (storedHash format: salt:hash)
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            if (storedHash == null || !storedHash.contains(":")) {
                return false;
            }
            String[] parts = storedHash.split(":", 2);
            if (parts.length != 2) return false;
            
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] storedHashBytes = Base64.getDecoder().decode(parts[1]);
            byte[] computedHash = hashWithSalt(password, salt);
            
            return MessageDigest.isEqual(storedHashBytes, computedHash);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] hashWithSalt(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        md.update(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return md.digest();
    }
}

