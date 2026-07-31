package com.ussr.pvz.controller;

import com.ussr.pvz.model.dto.PickQuestionRequest;
import com.ussr.pvz.model.dto.RegisterRequest;
import com.ussr.pvz.model.dto.RegistrationResult;
import com.ussr.pvz.service.RegisterService;

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