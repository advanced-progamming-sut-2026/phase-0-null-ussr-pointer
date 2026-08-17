package com.ussr.pvz.model.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SessionManager {

    private static final Path SESSION_FILE =
            Path.of(
                    "src/resources/current_session.txt"
            );

    private static String token;


    private SessionManager() {
    }


    public static void saveSession(
            String sessionToken
    ) {

        token =
                sessionToken;

        if (sessionToken == null ||
                sessionToken.isBlank()) {

            clearSession();
            return;
        }

        try {

            Files.writeString(
                    SESSION_FILE,
                    sessionToken
            );

        } catch (IOException e) {

            System.err.println(
                    "Failed to save session token: "
                            + e.getMessage()
            );
        }
    }


    public static String getToken() {

        if (token != null &&
                !token.isBlank()) {

            return token;
        }

        if (!Files.exists(
                SESSION_FILE
        )) {

            return null;
        }

        try {

            String savedToken =
                    Files.readString(
                            SESSION_FILE
                    ).trim();

            if (savedToken.isBlank()) {
                return null;
            }

            token =
                    savedToken;

            return token;

        } catch (IOException e) {

            System.err.println(
                    "Failed to load session token: "
                            + e.getMessage()
            );

            return null;
        }
    }


    public static boolean isLoggedIn() {

        String currentToken =
                getToken();

        return currentToken != null &&
                !currentToken.isBlank();
    }


    public static void clearSession() {

        token =
                null;

        try {

            Files.deleteIfExists(
                    SESSION_FILE
            );

        } catch (IOException e) {

            System.err.println(
                    "Failed to clear session: "
                            + e.getMessage()
            );
        }
    }
}