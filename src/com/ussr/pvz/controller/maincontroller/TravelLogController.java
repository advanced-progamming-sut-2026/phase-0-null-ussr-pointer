package com.ussr.pvz.controller.maincontroller;

import com.ussr.pvz.controller.command.maincommand.TravelLogCommand;
import com.ussr.pvz.model.dto.TravelLogPageRequest;
import com.ussr.pvz.service.QuestService;

import java.util.regex.Matcher;

public class TravelLogController {

    // CHANGED: Added service instance
    private final QuestService questService;

    public TravelLogController() {
        this.questService = new QuestService();
    }

    public String handleCommand(String command) {
        for (TravelLogCommand cmd : TravelLogCommand.values()) {
            Matcher matcher = cmd.getMatcher(command);
            if (matcher.matches()) {
                return switch (cmd) {
                    case PAGE -> handlePage(matcher);
                    case PLAY_MINIGAME -> handlePlayMinigame(matcher); // Route it
                };
            }
        }
        return "Invalid Travel Log command.";
    }

    private String handlePlayMinigame(Matcher matcher) {
        return questService.playMinigame(matcher.group("levelId"));
    }

    private String handlePage(Matcher matcher) {
        TravelLogPageRequest request = new TravelLogPageRequest(matcher.group("pageName"));
        return questService.getPage(request.pageName());
    }
}