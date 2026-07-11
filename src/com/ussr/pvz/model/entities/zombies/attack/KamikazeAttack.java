package com.ussr.pvz.model.entities.zombies.attack;

import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;

public class KamikazeAttack implements AttackBehavior {
    private final int damage;

    public KamikazeAttack(int damage) {
        this.damage = damage;
    }

    @Override
    public void attack(Zombie zombie, GameSession session) {
        Damageable target = zombie.acquireTarget(session);

        if (target != null && target.isAlive()) {
            if (target instanceof Plant p) {
                p.takeDamage(damage, zombie);
            } else {
                target.takeDamage(damage);
            }

            // Squash zombie destroys itself immediately after attacking
            zombie.takeDamage(zombie.getHp(), false);
        }
    }
}