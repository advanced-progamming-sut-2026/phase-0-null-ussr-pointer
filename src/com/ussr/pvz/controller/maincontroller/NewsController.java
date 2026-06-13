package com.ussr.pvz.controller.maincontroller;

import com.ussr.pvz.controller.command.maincommand.NewsCommand;

import java.util.regex.Matcher;

public class NewsController {

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
        // TODO: call newsService.showUnread() and return its message
        return "";
    }

    private String handleShowAll() {
        // TODO: call newsService.showAll() and return its message
        return "";
    }
}