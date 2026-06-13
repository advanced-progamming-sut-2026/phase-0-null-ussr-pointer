package com.ussr.pvz.model.account;
public class NewsItem {
    private String title;
    private String content;
    private int date;
    private boolean isRead;

    //constructor
    public NewsItem(String title , String content , int date) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.isRead = false;
    }

    //getter
    public String getTitle() { return this.title; }
    public String getContent() { return this.content; }
    public int getDate() { return this.date; }
    public boolean isRead() { return this.isRead; }

}