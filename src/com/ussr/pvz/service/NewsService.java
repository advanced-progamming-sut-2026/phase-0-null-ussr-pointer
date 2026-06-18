package com.ussr.pvz.service;

import com.ussr.pvz.model.App;

public class NewsService {
    public static String showUnread() {
        StringBuilder output = new StringBuilder();
        App.getAccount().getPersonalNews().forEach(
                newsItem -> {
                    if (!newsItem.isRead()) {
                        output.append(newsItem.getTitle()).append("\n");
                        output.append(newsItem.getDate()).append("\n");
                        output.append(newsItem.getContent()).append("\n");
                        newsItem.markAsRead();
                    }
                }
        );
        return output.toString();
    }

    public static String showAll() {
        StringBuilder output = new StringBuilder();
        App.getAccount().getPersonalNews().forEach(
                newsItem -> {
                    output.append(newsItem.getTitle()).append("\n");
                    output.append(newsItem.getDate()).append("\n");
                    output.append(newsItem.getContent()).append("\n");
                }
        );
        return output.toString();
    }
}