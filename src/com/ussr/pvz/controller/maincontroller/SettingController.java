package com.ussr.pvz.controller.maincontroller;

import com.ussr.pvz.model.dto.ChangeDifficultyRequest;
import com.ussr.pvz.service.SettingService;


public class SettingController {
    private final SettingService settingService = new SettingService();
    public SettingController() {
    }

    public String changeDifficulty(int level) {
        ChangeDifficultyRequest request =
                new ChangeDifficultyRequest(String.valueOf(level));

        return settingService.changeDifficulty(request);
    }

    public float getGameSpeed() {
        return settingService.getGameSpeed();
    }

    public String changeGameSpeed(float speed) {
        return settingService.changeGameSpeed(speed);
    }
}
