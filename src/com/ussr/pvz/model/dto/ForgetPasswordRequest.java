package com.ussr.pvz.model.dto;

public record ForgetPasswordRequest(
        String username,
        String email
) {
}