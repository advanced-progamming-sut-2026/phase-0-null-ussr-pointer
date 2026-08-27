package com.ussr.pvz.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.dto.ChangeDifficultyRequest;

public class SettingService {

    public String changeDifficulty(ChangeDifficultyRequest request) {
        int newDifficulty;
        try {
            newDifficulty = Integer.parseInt(request.level());
        } catch (NumberFormatException e) {
            return "invalid difficulty level format";
        }

        // Validate the 1-5 range limit
        if (newDifficulty < 1 || newDifficulty > 5) {
            return "invalid difficulty level";
        }

        // Save the raw integer choice to the profile session
        App.getAccount().setDifficultyLvl(newDifficulty);
        AccountSyncService.sync();
        return "new difficulty lvl applied successfully.";
    }

    public float getGameSpeed() {
        if (App.getAccount() == null) {
            return 1f;
        }

        return App.getAccount().getGameSpeed();
    }

    public String changeGameSpeed(float speed) {
        if (App.getAccount() == null) {
            return "you are not logged in";
        }

        if (!Float.isFinite(speed)
                || speed < 1f
                || speed > 3f) {
            return "invalid game speed";
        }

        App.getAccount().setGameSpeed(speed);
        AccountSyncService.sync();
        return "game speed applied successfully.";
    }
}