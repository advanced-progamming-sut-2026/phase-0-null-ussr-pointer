package com.ussr.pvz.model.dto;

public record LoginRequest(
        String username,
        String password,
        boolean stayLoggedIn
) {}