package com.ussr.pvz.controller;

import com.ussr.pvz.shared.dto.PickQuestionRequest;
import com.ussr.pvz.shared.dto.RegisterRequest;
import com.ussr.pvz.shared.dto.RegistrationResult;
import com.ussr.pvz.server.account.RegisterService;

public final class RegisterController {
    private final RegisterService service;

    public RegisterController() {
        service = new RegisterService();
    }

    public RegistrationResult register(RegisterRequest request) {
        return service.register(request);
    }

    public RegistrationResult pickQuestion(
            PickQuestionRequest request
    ) {
        return service.pickQuestion(request);
    }
}