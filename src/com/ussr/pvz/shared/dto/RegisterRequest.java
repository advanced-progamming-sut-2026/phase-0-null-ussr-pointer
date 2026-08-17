package com.ussr.pvz.shared.dto;

public record RegisterRequest(
        String username,
        String password,
        String passwordConfirm,
        String nickname,
        String email,
        String gender
) {
}