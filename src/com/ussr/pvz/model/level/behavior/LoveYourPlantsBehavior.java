package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.level.Level;

public class LoveYourPlantsBehavior extends LevelBehavior {
    private int counter = 0;
    private final int maxAllowedDeaths;
    private boolean missionFailed = false;

    // Passing the limit via the constructor makes this behavior reusable for different levels!
    public LoveYourPlantsBehavior(int maxAllowedDeaths) {
        this.maxAllowedDeaths = maxAllowedDeaths > 0 ? maxAllowedDeaths : 5;
    }

    public LoveYourPlantsBehavior(){
        this.maxAllowedDeaths = 5;
    }

    @Override
    public void tick(GameSession session, double deltaTime) {
        super.tick(session, deltaTime);

        // Do not process if the game status is already concluded
        if (levelCompleted || session.isGameOver() || missionFailed) return;

        // Automatically catch the failure condition state change during game ticks
        if (isFailed(session.getLevel())) {
            this.missionFailed = true;
            session.getEventBus().publish(new GameEvent.GameOver());
        }
    }

    @Override
    public boolean isFailed(Level level) {
        return counter >= maxAllowedDeaths;
    }

    /**
     * Call this hook from your gameplay manager/plant entity code whenever a plant is destroyed.
     */
    public void triggerPlantDied() {
        counter++;
    }

    public int getCounter() {
        return counter;
    }

    public int getMaxAllowedDeaths() {
        return maxAllowedDeaths;
    }
}