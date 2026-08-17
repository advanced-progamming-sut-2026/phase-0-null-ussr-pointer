package com.ussr.pvz.shared.dto;

import com.ussr.pvz.shared.dto.enums.LoginStatus;

public record LoginResult(LoginStatus status, String message) {
    public static LoginResult of(LoginStatus status, String message) {
        return new LoginResult(status, message);
    }

    public static LoginResult error(String message) {
        return new LoginResult(LoginStatus.ERROR, message);
    }
}
