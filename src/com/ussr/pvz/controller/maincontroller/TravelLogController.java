package com.ussr.pvz.controller.maincontroller;

import com.ussr.pvz.controller.command.maincommand.TravelLogCommand;
import com.ussr.pvz.model.dto.TravelLogPageRequest;

import java.util.regex.Matcher;

public class TravelLogController {

    public TravelLogController() {
    }

    public String handleCommand(String command) {
        for (TravelLogCommand cmd : TravelLogCommand.values()) {
            Matcher matcher = cmd.getMatcher(command);
            if (matcher.matches()) {
                if (cmd == TravelLogCommand.PAGE) {
                    return handlePage(matcher);
                }
            }
        }
        return "";
    }

    private String handlePage(Matcher matcher) {
        TravelLogPageRequest request = new TravelLogPageRequest(matcher.group("pageName"));
        // TODO: call travelLogService.page(request) and return its message
        return "";
    }
}