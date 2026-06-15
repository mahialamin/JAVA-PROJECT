package com.cuconnect.util;

import com.cuconnect.model.User;

/**
 * Utility for keeping track of the currently logged-in user.
 * Satisfies the Phase 1 file structure layout.
 */
public class SessionManager {
    private static User currentUser = null;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void clearSession() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
