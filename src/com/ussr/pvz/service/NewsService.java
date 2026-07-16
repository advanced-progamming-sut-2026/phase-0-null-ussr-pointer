package com.ussr.pvz.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.NewsItem;

public class NewsService {

    public static String showUnread() {
        StringBuilder output = new StringBuilder();
        App.getAccount().getPersonalNews().forEach(newsItem -> {
            if (!newsItem.isRead()) {
                output.append(formatNewsCard(newsItem));
                newsItem.markAsRead();
            }
        });
        if (output.isEmpty()) {
            return "============================================================\n" +
                    "                  No unread news right now.                 \n" +
                    "============================================================\n";
        }
        return output.toString();
    }

    public static String showAll() {
        StringBuilder output = new StringBuilder();
        App.getAccount().getPersonalNews().forEach(newsItem -> {
            output.append(formatNewsCard(newsItem));
        });

        if (output.isEmpty()) {
            return "============================================================\n" +
                    "                     Your inbox is empty.                   \n" +
                    "============================================================\n";
        }
        return output.toString();
    }

    private static String formatNewsCard(NewsItem item) {
        StringBuilder card = new StringBuilder();
        card.append("============================================================\n");
        card.append(String.format(" %-35s | %s\n", item.getTitle(), item.getFormattedDate()));
        card.append("------------------------------------------------------------\n");
        card.append(item.getContent()).append("\n");
        card.append("============================================================\n\n");
        return card.toString();
    }
}