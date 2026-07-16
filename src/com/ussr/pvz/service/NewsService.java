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
            return """
                    ============================================================
                                      No unread news right now.                \s
                    ============================================================
                    """;
        }
        return output.toString();
    }

    public static String showAll() {
        StringBuilder output = new StringBuilder();
        App.getAccount().getPersonalNews().forEach(newsItem -> output.append(formatNewsCard(newsItem)));

        if (output.isEmpty()) {
            return """
                    ============================================================
                                         Your inbox is empty.                  \s
                    ============================================================
                    """;
        }
        return output.toString();
    }

    private static String formatNewsCard(NewsItem item) {
        return "============================================================\n" +
                String.format(" %-35s | %s\n", item.getTitle(), item.getFormattedDate()) +
                "------------------------------------------------------------\n" +
                item.getContent() + "\n" +
                "============================================================\n\n";
    }
}