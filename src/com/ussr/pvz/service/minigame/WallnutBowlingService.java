package com.ussr.pvz.service.minigame;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.level.behavior.LevelBehavior;
import com.ussr.pvz.model.level.behavior.WallnutBowlingBehavior;

public class WallnutBowlingService {

    public String rollWallnut(String nutType, int x, int y) {
        GameSession session = App.getGameSession();
        if (session == null) return "Game session not active.";

        LevelBehavior behavior = (LevelBehavior) session.getLevel().getBehavior();
        if (!(behavior instanceof WallnutBowlingBehavior bowlingBehavior)) {
            return "Current level is not a Wallnut Bowling minigame.";
        }

        // The behavior handles the red-line validation and projectile creation
        return bowlingBehavior.rollNut(nutType, x, y);
    }
}