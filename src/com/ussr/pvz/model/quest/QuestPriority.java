package com.ussr.pvz.model.quest;

public enum QuestPriority {
    LOW, MEDIUM, HIGH, CRITICAL;

    public static QuestPriority fromString(String value) {
        return valueOf(value.trim().toUpperCase());
    }
}