package com.ussr.pvz.shared.dto;

public record ChangePasswordRequest(String newPassword, String oldPassword) {
}