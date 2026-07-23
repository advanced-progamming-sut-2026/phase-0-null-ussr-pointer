package com.ussr.pvz.model.account;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class NewsItem {
    private final String title;
    private final String content;
    private final int date;
    private boolean isRead;

    public NewsItem(String title, String content, int date) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.isRead = false;
    }

    public String getTitle() {
        return this.title;
    }

    public String getContent() {
        return this.content;
    }

    public int getDate() {
        return this.date;
    }

    public String getFormattedDate() {
        try {
            Instant instant = Instant.ofEpochSecond(this.date);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault());
            return formatter.format(instant);
        } catch (Exception e) {
            return "Unknown Date";
        }
    }

    public boolean isRead() {
        return this.isRead;
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public static NewsItem initialNews() {
        String topic = "Welcome to Plants vs. Zombies!";
        String context = "Thank you for registering your account. Protect your lawn, " +
                "plant your defenses, and don't let the zombies eat your brains!";
        int currentTimestamp = (int) (System.currentTimeMillis() / 1000);
        return new NewsItem(topic, context, currentTimestamp);
    }
}