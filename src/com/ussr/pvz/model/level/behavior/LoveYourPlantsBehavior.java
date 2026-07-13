package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.level.Level;

public class LoveYourPlantsBehavior extends LevelBehavior {
    private int counter = 0;
    private final int maxAllowedDeaths;
    private boolean missionFailed = false;

    public LoveYourPlantsBehavior(int maxAllowedDeaths) {
        this.maxAllowedDeaths = maxAllowedDeaths > 0 ? maxAllowedDeaths : 5;
    }

    public LoveYourPlantsBehavior(){
        this.maxAllowedDeaths = 5;
    }

    @Override
    public void tick(GameSession session, double deltaTime) {
        super.tick(session, deltaTime);

        if (levelCompleted || session.isGameOver() || missionFailed) return;

        if (isFailed(session.getLevel())) {
            this.missionFailed = true;
            session.getEventBus().publish(new GameEvent.GameOver());
        }
    }

    @Override
    public void onPlantDied(GameSession session, Plant plant) {
        counter++;
        if (isFailed(session.getLevel())) {
            this.missionFailed = true;
            session.getEventBus().publish(new GameEvent.GameOver());
        }
    }

    @Override
    public boolean isFailed(Level level) {
        return counter >= maxAllowedDeaths;
    }

    public int getCounter() {
        return counter;
    }

    public int getMaxAllowedDeaths() {
        return maxAllowedDeaths;
    }
}