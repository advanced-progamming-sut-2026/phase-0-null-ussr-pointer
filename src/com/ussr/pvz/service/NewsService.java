package com.ussr.pvz.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.NewsItem;

import java.util.List;

public final class NewsService {
    private NewsService() {
    }

    public static List<NewsItem> getAllNews() {
        Account account = App.getAccount();

        if (account == null) {
            return List.of();
        }

        return List.copyOf(account.getPersonalNews());
    }

    public static List<NewsItem> getUnreadNews() {
        return getAllNews().stream()
                .filter(item -> !item.isRead())
                .toList();
    }

    public static void markAsRead(NewsItem item) {
        if (item != null) {
            item.markAsRead();
        }
    }
}