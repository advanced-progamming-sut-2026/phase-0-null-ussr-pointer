package com.ussr.pvz.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.dto.ChangeDifficultyRequest;

public class SettingService {

    public String changeDifficulty(ChangeDifficultyRequest request) {
        int newDifficulty = Integer.parseInt(request.level());

        if(newDifficulty < 1 || newDifficulty > 5)
            return "invalid difficulty level";
        App.getAccount().setDifficultyLvl(newDifficulty);
        return "new difficulty lvl applied successfully.";
    }
}
