package com.ussr.pvz.notification;

public record Notification(
        String text,
        NotificationType type,
        float durationSeconds
) {
}