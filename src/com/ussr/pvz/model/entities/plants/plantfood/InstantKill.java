package com.ussr.pvz.model.entities.plants.plantfood;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InstantKill implements PlantFoodEffect{
    private final int targetCount;

    public InstantKill(int targetCount) {
        this.targetCount = targetCount;
    }

    @Override
    public void triggerSuperpower(Plant user, GameSession session) {
        if (session.getZombies() == null || session.getZombies().isEmpty()) return;

        List<Zombie> activeZombies = new ArrayList<>();
        for (Zombie zombie : session.getZombies()) {
            if (zombie != null && zombie.isAlive()) {
                activeZombies.add(zombie);
            }
        }

        Collections.shuffle(activeZombies);
        int targetsToKill = Math.min(this.targetCount, activeZombies.size());

        for (int i = 0; i < targetsToKill; i++) {
            activeZombies.get(i).takeDamage(9999, user);
        }
    }

    @Override
    public void applyStatusModifiers(Plant user) {

    }

    @Override
    public void tickDurationEffect(Plant user, GameSession session, double deltaTime) {

    }
}
