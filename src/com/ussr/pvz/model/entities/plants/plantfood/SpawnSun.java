package com.ussr.pvz.model.entities.plants.plantfood;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.items.sun.ProducedSun;
import com.ussr.pvz.model.entities.plants.Plant;

public class SpawnSun implements PlantFoodEffect {
    private final int sunAmount;
    private final boolean instantMaxGrowth;
    private final double duration;

    public SpawnSun(int sunAmount, boolean instantMaxGrowth) {
        this.sunAmount = sunAmount;
        this.instantMaxGrowth = instantMaxGrowth;
        this.duration = 0.0;
    }

    @Override
    public void triggerSuperpower(Plant user, GameSession session) {
        if (this.sunAmount > 0 && session != null) {
            ProducedSun sun = new ProducedSun((int) user.getPosition().x() ,
                    (int) user.getPosition().y() , sunAmount , user.getName());
            session.addItem(sun);
        }
        user.setPlantFoodTimer(duration);
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