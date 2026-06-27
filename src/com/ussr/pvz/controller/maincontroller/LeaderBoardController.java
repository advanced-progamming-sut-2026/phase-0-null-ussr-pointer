package com.ussr.pvz.controller.maincontroller;

import com.ussr.pvz.controller.command.maincommand.LeaderBoardCommand;
import com.ussr.pvz.model.dto.LeaderBoardSortRequest;
import com.ussr.pvz.service.LeaderBoardService;

import java.util.regex.Matcher;

public class LeaderBoardController {
    private LeaderBoardService leaderBoardService;
    public LeaderBoardController() {
    }

    public String handleCommand(String command) {
        for (LeaderBoardCommand cmd : LeaderBoardCommand.values()) {
            Matcher matcher = cmd.getMatcher(command);
            if (matcher.matches()) {
                return switch (cmd) {
                    case SHOW -> handleShow();
                    case SORT -> handleSort(matcher);
                };
            }
        }
        return "";
    }

    private String handleShow() {
        // TODO: call leaderBoardService.show() and return its message
        return "";
    }

    private String handleSort(Matcher matcher) {
        LeaderBoardSortRequest request = new LeaderBoardSortRequest(
                matcher.group("column"),
                matcher.group("order")
        );
        // TODO: call leaderBoardService.sort(request) and return its message
        return "";
    }
}