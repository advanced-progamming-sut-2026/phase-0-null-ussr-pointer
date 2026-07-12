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
        return "new difficulty lvl applied successfully.";
    }
}