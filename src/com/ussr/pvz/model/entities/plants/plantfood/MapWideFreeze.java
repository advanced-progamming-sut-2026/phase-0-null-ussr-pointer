package com.ussr.pvz.model.entities.plants.plantfood;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;

public class MapWideFreeze implements PlantFoodEffect {
    @Override
    public void triggerSuperpower(Plant user, GameSession session) {
        if (session.getZombies() == null) return;

        for (Zombie zombie : session.getZombies()) {
            if (zombie != null && zombie.isAlive()) {
                zombie.setStatus(Zombie.Status.FREEZE);
            }
        }
    }

    @Override
    public void applyStatusModifiers(Plant user) {

    }

    @Override
    public void tickDurationEffect(Plant user, GameSession session, double deltaTime) {

    }
}
