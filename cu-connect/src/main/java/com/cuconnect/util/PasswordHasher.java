package com.cuconnect.util;

import java.security.MessageDigest;
import java.util.Base64;

public class PasswordHasher {
    public static String hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password: " + e.getMessage(), e);
        }
    }
}
