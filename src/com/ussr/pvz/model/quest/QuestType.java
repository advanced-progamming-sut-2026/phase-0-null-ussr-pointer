package com.ussr.pvz.model.quest;

public enum QuestType {
    DAILY("daily"),
    CHALLENGE("challenge"),
    EPIC("epic");

    private final String name;

    QuestType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static QuestType fromString(String value) {
        return switch (value) {
            case "daily" -> DAILY;
            case "challenge" -> CHALLENGE;
            case "epic" -> EPIC;
            default -> throw new IllegalArgumentException("Unknown quest type: " + value);
        };
    }
}
