package com.ussr.pvz.model.account;

public enum Gender {
    MALE, FEMALE;

    public static Gender from(String value) {
        return switch (value.toLowerCase()) {
            case "male" -> MALE;
            case "female" -> FEMALE;
            default -> throw new IllegalArgumentException("invalid gender");
        };
    }
}