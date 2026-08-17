package com.ussr.pvz.shared.dto;

public record LoginRequest(
        String username,
        String password,
        boolean stayLoggedIn
) {
}