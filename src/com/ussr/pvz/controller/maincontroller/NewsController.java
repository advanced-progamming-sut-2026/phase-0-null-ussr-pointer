package com.ussr.pvz.controller.maincontroller;

import com.ussr.pvz.controller.command.maincommand.NewsCommand;
import com.ussr.pvz.service.NewsService;

import java.util.regex.Matcher;

public class NewsController {
    //TODO: Debug
    public NewsController() {
    }

    public String handleCommand(String command) {
        for (NewsCommand cmd : NewsCommand.values()) {
            Matcher matcher = cmd.getMatcher(command);
            if (matcher.matches()) {
                return switch (cmd) {
                    case SHOW_UNREAD -> handleShowUnread();
                    case SHOW_ALL -> handleShowAll();
                };
            }
        }
        return "";
    }

    private String handleShowUnread() {
        return NewsService.showUnread();
    }

    private String handleShowAll() {
        return NewsService.showAll();
    }
}