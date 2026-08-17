package com.ussr.pvz.shared.dto;

public record ForgetPasswordRequest(
        String username,
        String email
) {
}