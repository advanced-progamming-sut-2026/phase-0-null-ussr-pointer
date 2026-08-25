package com.ussr.pvz.model.entities.plants.plantfood.localattack;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.plantfood.PlantFoodEffect;

public class PierceThroughAttack implements PlantFoodEffect {
    private final double duration;

    public PierceThroughAttack(double duration) {
        this.duration = duration;
    }

    @Override
    public void triggerSuperpower(Plant user, GameSession session) {
        if (user == null) return;
        user.setPlantFoodTimer(this.duration);
    }

    @Override
    public void applyStatusModifiers(Plant user) {
    }

    @Override
    public void tickDurationEffect(Plant user, GameSession session, double deltaTime) {
    }

    @Override
    public boolean pausesNormalAction() {
        return false;
    }
}