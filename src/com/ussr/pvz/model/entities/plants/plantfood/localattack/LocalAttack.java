package com.ussr.pvz.model.entities.plants.plantfood.localattack;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.plantfood.PlantFoodEffect;

public class LocalAttack implements PlantFoodEffect {
    private final double duration;
    private final double strikeRate;
    private double strikeTimer = 0.0;

    public LocalAttack(double duration, double strikeRate) {
        this.duration = duration;
        this.strikeRate = strikeRate;
    }

    @Override
    public void triggerSuperpower(Plant user, GameSession session) {
        System.out.println("hello");
        if (user != null) {
            user.setPlantFoodTimer(this.duration);
            this.strikeTimer = 0.0;
        }
    }

    @Override
    public void applyStatusModifiers(Plant user) {
        // Instant attack superpower; no permanent stat modifiers
    }

    @Override
    public void tickDurationEffect(Plant user, GameSession session, double deltaTime) {
        if (user == null || session == null) return;

        strikeTimer += deltaTime;
        if (strikeTimer < strikeRate) return;

        strikeTimer = 0.0;

        // Option A: If using plant's active strategy to fire projectiles along vectors
        if (user.getActStrategy() != null) {
            user.getActStrategy().act(user, session);
        }
    }
}