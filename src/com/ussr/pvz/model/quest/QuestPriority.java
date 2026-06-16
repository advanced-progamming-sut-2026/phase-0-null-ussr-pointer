package com.ussr.pvz.model.quest;

public enum QuestPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public static QuestPriority fromString(String value) {
        return switch (value.trim().toLowerCase()) {
            case "low" -> LOW;
            case "medium" -> MEDIUM;
            case "high" -> HIGH;
            case "critical" -> CRITICAL;
            default -> throw new IllegalArgumentException("Unknown priority: " + value);
        };
    }
}