package com.ussr.pvz.model.entities.zombies.attack;

import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;

public class ChompAttack implements AttackBehavior {
    private double pendingDamage;

    @Override
    public void attack(
            Zombie zombie,
            GameSession session,
            float delta
    ) {
        Damageable target =
                zombie.acquireTarget(session);

        if (target == null || !target.isAlive()) {
            pendingDamage = 0;
            return;
        }

        pendingDamage +=
                zombie.getEatDps() * delta;

        int damage = (int) pendingDamage;

        if (damage <= 0) {
            return;
        }

        target.takeDamage(damage);
        pendingDamage -= damage;
    }
}