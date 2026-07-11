package com.ussr.pvz.model.entities.plants.plantfood;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RandomHypnotize implements PlantFoodEffect {
    private final int maxTargets;

    public RandomHypnotize(int maxTargets) {
        this.maxTargets = maxTargets;
    }

    @Override
    public void triggerSuperpower(Plant user, GameSession session) {
        if (session.getZombies() == null || session.getZombies().isEmpty()) return;

        List<Zombie> validTargets = new ArrayList<>();
        for (Zombie zombie : session.getZombies()) {
            if (zombie != null && zombie.isAlive() && zombie.getStatus() != Zombie.Status.HYPNOTIZED) {
                validTargets.add(zombie);
            }
        }

        if (validTargets.isEmpty()) return;

        Collections.shuffle(validTargets);
        int count = Math.min(this.maxTargets, validTargets.size());
        List<Zombie> selectedZombies = validTargets.subList(0, count);

        for (Zombie target : selectedZombies) {
            target.setStatus(Zombie.Status.HYPNOTIZED);
            target.hypnotize();
        }
    }

    @Override
    public void applyStatusModifiers(Plant user) {

    }

    @Override
    public void tickDurationEffect(Plant user, GameSession session, double deltaTime) {

    }
}
