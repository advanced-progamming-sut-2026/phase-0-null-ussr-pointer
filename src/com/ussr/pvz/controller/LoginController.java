package com.ussr.pvz.controller;

import com.ussr.pvz.model.dto.AnswerRequest;
import com.ussr.pvz.model.dto.ForgetPasswordRequest;
import com.ussr.pvz.model.dto.LoginRequest;
import com.ussr.pvz.model.dto.LoginResult;
import com.ussr.pvz.service.LoginService;

public final class LoginController {
    private final LoginService service = new LoginService();

    public LoginResult login(LoginRequest request) {
        return service.login(request);
    }

    public LoginResult forgetPassword(ForgetPasswordRequest request) {
        return service.forgetPassword(request);
    }

    public LoginResult answer(AnswerRequest request) {
        return service.answer(request);
    }

    public LoginResult resetPassword(String newPassword) {
        return service.resetPassword(newPassword);
    }
}
