package com.ussr.pvz.model.entities.plants.plantfood;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;

public class SpawnSun implements PlantFoodEffect {
    private final int sunAmount;
    private final boolean instantMaxGrowth;

    public SpawnSun(int sunAmount, boolean instantMaxGrowth) {
        this.sunAmount = sunAmount;
        this.instantMaxGrowth = instantMaxGrowth;
    }

    @Override
    public void triggerSuperpower(Plant user, GameSession session) {
        if (this.sunAmount > 0 && session != null) {
            session.addSun(this.sunAmount);
        }
    }

    @Override
    public void applyStatusModifiers(Plant user) {
        if (this.instantMaxGrowth && user != null) {
            user.instantlyMature();
        }
    }

    @Override
    public void tickDurationEffect(Plant user, GameSession session, double deltaTime) {
        // Instant superpower effect; no duration or per-tick logic required.
    }
}