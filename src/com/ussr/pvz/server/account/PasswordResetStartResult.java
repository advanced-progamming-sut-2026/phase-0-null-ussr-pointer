package com.ussr.pvz.server.account;

import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.shared.dto.LoginResult;
import com.ussr.pvz.shared.dto.enums.LoginStatus;

public record PasswordResetStartResult(
        LoginResult result,
        Account account
) {

    public static PasswordResetStartResult error(
            String message
    ) {

        return new PasswordResetStartResult(
                LoginResult.error(message),
                null
        );
    }

    public static PasswordResetStartResult success(
            String question,
            Account account
    ) {

        return new PasswordResetStartResult(
                LoginResult.of(
                        LoginStatus.SECURITY_QUESTION,
                        question
                ),
                account
        );
    }
}