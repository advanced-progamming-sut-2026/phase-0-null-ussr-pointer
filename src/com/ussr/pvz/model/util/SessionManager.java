package com.ussr.pvz.model.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SessionManager {
    private static final Path SESSION_FILE = Path.of("src/resources/current_session.txt");

    public static void saveSession(String username) {
        try {
            Files.writeString(SESSION_FILE, username);
        } catch (IOException e) {
            System.err.println("Failed to save auto-login session: " + e.getMessage());
        }
    }

    public static String getAutoLoginUsername() {
        if (!Files.exists(SESSION_FILE)) return null;
        try {
            return Files.readString(SESSION_FILE).trim();
        } catch (IOException e) {
            return null;
        }
    }

    public static void clearSession() {
        try {
            Files.deleteIfExists(SESSION_FILE);
        } catch (IOException e) {
            System.err.println("Failed to clear session: " + e.getMessage());
        }
    }
}