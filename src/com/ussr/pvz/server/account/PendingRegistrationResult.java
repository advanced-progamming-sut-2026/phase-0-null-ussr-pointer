package com.ussr.pvz.server.account;

import com.ussr.pvz.shared.account.AccountState;
import com.ussr.pvz.shared.dto.RegistrationResult;

public record PendingRegistrationResult(
        RegistrationResult result,
        AccountState pendingAccount
) {

    public static PendingRegistrationResult error(
            String message
    ) {
        return new PendingRegistrationResult(
                RegistrationResult.error(message),
                null
        );
    }

    public static PendingRegistrationResult success(
            RegistrationResult result,
            AccountState pendingAccount
    ) {
        return new PendingRegistrationResult(
                result,
                pendingAccount
        );
    }
}