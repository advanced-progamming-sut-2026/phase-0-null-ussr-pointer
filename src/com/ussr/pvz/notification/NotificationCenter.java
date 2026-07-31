package com.ussr.pvz.notification;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class NotificationCenter {
    private static final float DEFAULT_DURATION = 3f;

    private static final Queue<Notification> QUEUE =
            new ConcurrentLinkedQueue<>();

    private NotificationCenter() {
    }

    public static void show(String text) {
        info(text);
    }

    public static void info(String text) {
        publish(text, NotificationType.INFO, DEFAULT_DURATION);
    }

    public static void success(String text) {
        publish(text, NotificationType.SUCCESS, DEFAULT_DURATION);
    }

    public static void warning(String text) {
        publish(text, NotificationType.WARNING, 4f);
    }

    public static void error(String text) {
        publish(text, NotificationType.ERROR, 5f);
    }

    public static void publish(
            String text,
            NotificationType type,
            float durationSeconds
    ) {
        if (text == null || text.isBlank()) {
            return;
        }

        NotificationType safeType =
                type == null ? NotificationType.INFO : type;

        float safeDuration = Math.max(0.5f, durationSeconds);

        QUEUE.offer(new Notification(
                text.trim(),
                safeType,
                safeDuration
        ));
    }

    public static Notification poll() {
        return QUEUE.poll();
    }

    public static void clear() {
        QUEUE.clear();
    }
}