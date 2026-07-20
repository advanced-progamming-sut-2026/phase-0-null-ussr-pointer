package com.ussr.pvz.model.entities.zombies.attack;

import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;

public class ChompAttack implements AttackBehavior {

    @Override
    public void attack(Zombie zombie, GameSession session) {
        Damageable target = zombie.acquireTarget(session);
        if (target == null || !target.isAlive()) return;

        int damage = (int) (zombie.getEatDps() * GameClock.SECONDS_PER_TICK);
        damage = Math.max(1, damage);
        if(target instanceof Plant plant)
            plant.takeDamage(damage , zombie);
        else
            target.takeDamage(damage);
    }
}