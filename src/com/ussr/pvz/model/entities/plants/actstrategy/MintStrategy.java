package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.PlantType;

public class MintStrategy implements ActStrategy {
    @Override
    public void act(Plant user, GameSession session) {
        PlantType userType = user.getType();
        session.getPlants().stream()
                .filter(plant -> plant != null && plant.isAlive() && plant.getType() == user.getType())
                .forEach(plant -> {
                    // Instantly ready the plant's attack interval
                    if(userType.equals(plant.getType())) {
                        plant.setInternalTimer(plant.getActionInterval());
                        plant.setBuffed(true);
                    }
                });

        user.setAlive(false);
    }
}