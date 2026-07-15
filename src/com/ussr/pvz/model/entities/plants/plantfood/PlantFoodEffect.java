package com.ussr.pvz.model.entities.plants.plantfood;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;

public interface PlantFoodEffect {
    void triggerSuperpower(Plant user, GameSession session);

    void applyStatusModifiers(Plant user);

    void tickDurationEffect(Plant user, GameSession session, double deltaTime);
}
