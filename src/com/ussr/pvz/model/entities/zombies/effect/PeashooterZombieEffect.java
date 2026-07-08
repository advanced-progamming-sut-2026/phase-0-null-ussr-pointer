package com.ussr.pvz.model.entities.zombies.effect;

import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.projectiles.ZombiePeaProjectile;

public class PeashooterZombieEffect implements EffectStatus {
    private double timer = 0.0;
    private final double fireRate;
    private final int damage;

    public PeashooterZombieEffect(int damage, double fireRate) {
        this.damage = damage;
        this.fireRate = fireRate;
    }

    @Override
    public void effect(Zombie zombie, GameSession session) {
        if (!zombie.isAlive()) return;

        timer += GameClock.SECONDS_PER_TICK;
        if (timer >= fireRate) {
            timer = 0;
            session.addZombieProjectile(new ZombiePeaProjectile(zombie.getPosition(), damage));
        }
    }
}