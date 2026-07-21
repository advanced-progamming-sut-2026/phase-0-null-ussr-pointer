package com.ussr.pvz.model.entities.plants.plantfood;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MetalAbsorb implements PlantFoodEffect {
    private final int maxTargets;

    public MetalAbsorb(int maxTargets) {
        this.maxTargets = maxTargets;
    }

    public MetalAbsorb() {
        this(15);
    }

    @Override
    public void triggerSuperpower(Plant user, GameSession session) {
        if (user == null || session == null || session.getZombies() == null || session.getZombies().isEmpty()) return;

        List<Zombie> metallicZombies = new ArrayList<>();

        for (Zombie zombie : session.getZombies()) {
            if (zombie != null && zombie.isAlive() && zombie.getArmor() != null) {
                // Safeguard against unassigned armor types
                if (zombie.getArmor().getArmorType() != null && zombie.getArmor().getArmorType().isMetal()) {
                    metallicZombies.add(zombie);
                }
            }
        }

        if (metallicZombies.isEmpty()) return;

        Collections.shuffle(metallicZombies);
        int targetsToStrip = Math.min(this.maxTargets, metallicZombies.size());

        for (int i = 0; i < targetsToStrip; i++) {
            Zombie target = metallicZombies.get(i);
            target.setArmor(null);
        }
    }

    @Override
    public void applyStatusModifiers(Plant user) {
        // Instant trigger; no permanent stat modifiers
    }

    @Override
    public void tickDurationEffect(Plant user, GameSession session, double deltaTime) {
        // Instant trigger; no duration ticking needed
    }
}