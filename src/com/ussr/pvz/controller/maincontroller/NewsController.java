package com.ussr.pvz.controller.maincontroller;

import com.ussr.pvz.shared.account.NewsItem;
import com.ussr.pvz.service.NewsService;

import java.util.List;

public final class NewsController {

    public List<NewsItem> getAllNews() {
        return NewsService.getAllNews();
    }

    public List<NewsItem> getUnreadNews() {
        return NewsService.getUnreadNews();
    }

    public void markAsRead(NewsItem item) {
        NewsService.markAsRead(item);
    }
}