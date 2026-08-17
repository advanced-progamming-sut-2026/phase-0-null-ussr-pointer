package com.ussr.pvz.server.account;

import com.ussr.pvz.model.account.Account;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServerSessionManager {

    private final Map<String, Account> sessions =
            new ConcurrentHashMap<>();

    public String createSession(
            Account account
    ) {

        if (account == null) {
            throw new IllegalArgumentException(
                    "Account cannot be null."
            );
        }

        String token =
                UUID.randomUUID()
                        .toString();

        sessions.put(
                token,
                account
        );

        return token;
    }

    public Account getAccount(
            String token
    ) {

        if (token == null ||
                token.isBlank()) {

            return null;
        }

        return sessions.get(token);
    }

    public boolean isValid(
            String token
    ) {

        return getAccount(token)
                != null;
    }

    public void removeSession(
            String token
    ) {

        if (token == null ||
                token.isBlank()) {

            return;
        }

        sessions.remove(token);
    }
}