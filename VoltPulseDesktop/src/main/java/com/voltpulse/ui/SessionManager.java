package com.voltpulse.util;

import java.util.prefs.Preferences;

public class SessionManager {
    private static final Preferences PREFS = Preferences.userRoot().node("com/voltpulse/session");
    private static final String KEY_EMAIL = "logged_in_email";

    public static void saveSession(String email) {
        if (email != null && !email.trim().isEmpty()) {
            PREFS.put(KEY_EMAIL, email.trim());
        }
    }

    public static String getSavedEmail() {
        return PREFS.get(KEY_EMAIL, null);
    }

    public static void clearSession() {
        PREFS.remove(KEY_EMAIL);
    }
}