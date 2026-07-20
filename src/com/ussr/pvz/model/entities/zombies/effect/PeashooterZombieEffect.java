package com.ussr.pvz.model.entities.zombies.effect;

import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.hit.NormalHit;
import com.ussr.pvz.model.entities.projectiles.move.StraightMove;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

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

            // Find a target plant in the same lane
            int lane = (int) zombie.getPosition().y();
            Plant target = null;
            double closestDist = Double.MAX_VALUE;

            if (session.getPlants() != null) {
                for (Plant p : session.getPlants()) {
                    if (p.isAlive() && p.getLocation() != null && p.getLocation().y() == lane) {
                        double dist = zombie.getPosition().x() - p.getLocation().x();
                        if (dist > 0 && dist < closestDist) {
                            closestDist = dist;
                            target = p;
                        }
                    }
                }
            }

            if (target != null) {
                Projectile pea = new Projectile(
                        (Damageable) target,
                        Vec2.of(zombie.getPosition().x() - 0.5, zombie.getPosition().y()),
                        Vec2.of(-4.0, 0), // Shoot left
                        damage,
                        new StraightMove(),
                        new NormalHit(1)
                );
                session.addProjectile(pea);
            }

            timer = 0;
        }
    }
}