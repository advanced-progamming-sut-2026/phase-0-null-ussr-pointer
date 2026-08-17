package com.ussr.pvz.controller;

import com.ussr.pvz.service.LoginService;
import com.ussr.pvz.shared.dto.AnswerRequest;
import com.ussr.pvz.shared.dto.ForgetPasswordRequest;
import com.ussr.pvz.shared.dto.LoginRequest;
import com.ussr.pvz.shared.dto.LoginResult;

public final class LoginController {

    private final LoginService service =
            new LoginService();

    public LoginResult login(
            LoginRequest request
    ) {
        return service.login(request);
    }

    public LoginResult forgetPassword(
            ForgetPasswordRequest request
    ) {
        return service.forgetPassword(request);
    }

    public LoginResult answer(
            AnswerRequest request
    ) {
        return service.answer(request);
    }

    public LoginResult resetPassword(
            String newPassword
    ) {
        return service.resetPassword(
                newPassword
        );
    }
}