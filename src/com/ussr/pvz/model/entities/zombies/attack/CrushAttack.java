package com.ussr.pvz.model.entities.zombies.attack;

import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;

public class CrushAttack implements AttackBehavior {
    private final int crushDamage;

    // Passed from the factory parsing the "EatDPS": 4000 property
    public CrushAttack(int crushDamage) {
        this.crushDamage = crushDamage;
    }

    @Override
    public void attack(Zombie zombie, GameSession session) {
        Damageable target = zombie.acquireTarget(session);
        if (target == null || !target.isAlive()) return;

        // Apply instant fatal damage without scaling by GameClock.
        // If it's a plant, pass the zombie instance so counter-effects (like spikes) can trigger.
        if (target instanceof Plant p) {
            p.takeDamage(crushDamage, zombie);
        } else {
            target.takeDamage(crushDamage);
        }
    }
}