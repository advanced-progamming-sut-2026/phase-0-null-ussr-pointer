package com.ussr.pvz.shared.dto;

import com.ussr.pvz.shared.dto.enums.RegistrationStatus;

public record RegistrationResult(
        RegistrationStatus status,
        String message
) {
    public static RegistrationResult detailsAccepted(String message) {
        return new RegistrationResult(
                RegistrationStatus.DETAILS_ACCEPTED,
                message
        );
    }

    public static RegistrationResult completed(String message) {
        return new RegistrationResult(
                RegistrationStatus.COMPLETED,
                message
        );
    }

    public static RegistrationResult error(String message) {
        return new RegistrationResult(
                RegistrationStatus.ERROR,
                message
        );
    }
}