package com.cuconnect.util;

import java.util.regex.Pattern;

/**
 * Utility for input validation.
 * Satisfies the Phase 1 file structure layout.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    /**
     * Checks if a string is null or empty/whitespace-only.
     */
    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Validates if a string is a valid email address.
     */
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates a password (e.g., minimum 6 characters).
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Validates a username (e.g., alphanumeric only, 3-20 characters).
     */
    public static boolean isValidUsername(String username) {
        if (isEmpty(username)) {
            return false;
        }
        return username.matches("^[a-zA-Z0-9_]{3,20}$");
    }
}
