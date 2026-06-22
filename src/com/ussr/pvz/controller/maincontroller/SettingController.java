package com.ussr.pvz.controller.maincontroller;

import com.ussr.pvz.controller.command.maincommand.SettingCommand;
import com.ussr.pvz.model.dto.ChangeDifficultyRequest;
import com.ussr.pvz.service.SettingService;

import java.util.regex.Matcher;

public class SettingController {
    private final SettingService settingService = new SettingService();
    public SettingController() {
    }

    public String handleCommand(String command) {
        for (SettingCommand cmd : SettingCommand.values()) {
            Matcher matcher = cmd.getMatcher(command);
            if (matcher.matches()) {
                if (cmd == SettingCommand.CHANGE_DIFFICULTY) {
                    return handleChangeDifficulty(matcher);
                }
                return "";
            }
        }
        return "";
    }

    private String handleChangeDifficulty(Matcher matcher) {
        ChangeDifficultyRequest request = new ChangeDifficultyRequest(matcher.group("level"));
        return settingService.changeDifficulty(request);
    }
}