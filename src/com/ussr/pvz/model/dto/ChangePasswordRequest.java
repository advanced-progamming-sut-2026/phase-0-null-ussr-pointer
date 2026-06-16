package com.ussr.pvz.model.dto;

public record ChangePasswordRequest(String newPassword, String oldPassword) {
}