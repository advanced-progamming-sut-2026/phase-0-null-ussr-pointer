package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;

public class MintStrategy implements ActStrategy {
    @Override
    public void act(Plant user, GameSession session) {
        session.getPlants().stream()
                .filter(plant -> plant != null && plant.isAlive() && plant.getType() == user.getType())
                .forEach(plant -> {
                    if (plant.getPlantFoodEffect() != null) {
                        plant.getPlantFoodEffect().applyStatusModifiers(plant);
                        //todo check and update the triggers
                        plant.getPlantFoodEffect().triggerSuperpower(plant, session);
                    }
                });

        user.setAlive(false);
    }
}