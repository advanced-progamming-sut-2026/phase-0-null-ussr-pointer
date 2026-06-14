package com.ussr.pvz.model.dto;

public record RegisterRequest(
        String username,
        String password,
        String passwordConfirm,
        String nickname,
        String email,
        String gender
) {}