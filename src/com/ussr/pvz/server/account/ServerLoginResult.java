package com.ussr.pvz.server.account;

import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.shared.dto.LoginResult;

public record ServerLoginResult(
        LoginResult result,
        String token,
        Account account
) {

    public static ServerLoginResult error(
            LoginResult result
    ) {
        return new ServerLoginResult(
                result,
                null,
                null
        );
    }

    public static ServerLoginResult success(
            LoginResult result,
            String token,
            Account account
    ) {
        return new ServerLoginResult(
                result,
                token,
                account
        );
    }
}