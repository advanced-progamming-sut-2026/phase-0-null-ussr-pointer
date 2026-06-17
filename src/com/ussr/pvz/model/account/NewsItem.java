package com.ussr.pvz.model.account;

public class NewsItem {
    private String title;
    private String content;
    private int date;
    private boolean isRead;

    //constructor
    public NewsItem(String title, String content, int date) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.isRead = false;
    }

    //getter
    public String getTitle() {
        return this.title;
    }

    public String getContent() {
        return this.content;
    }

    public int getDate() {
        return this.date;
    }

    public boolean isRead() {
        return this.isRead;
    }

    //helper
    public static NewsItem initialNews() {
        String topic = "Welcome to Plants vs. Zombies!";
        String context = "Thank you for registering your account. Protect your lawn, plant your defenses, and don't let the zombies eat your brains!";
        int currentTimestamp = (int) (System.currentTimeMillis() / 1000);
        return new NewsItem(topic, context, currentTimestamp);
    }

}