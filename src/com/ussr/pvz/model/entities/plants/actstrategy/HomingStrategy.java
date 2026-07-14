package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.hit.NormalHit;
import com.ussr.pvz.model.entities.projectiles.hit.PierceHit;
import com.ussr.pvz.model.entities.projectiles.move.StraightMove;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class HomingStrategy implements ActStrategy {

    @Override
    public void act(Plant user, GameSession session) {
        if (user.getIntervalTimer() > 0) return;

        List<Zombie> zombies = session.getZombies();
        if (zombies.isEmpty()) return;

        boolean isMagic = user.getTags().contains(Tag.MAGIC);
        Zombie target = isMagic ? randomTarget(zombies) : nearestTarget(user, zombies);
        if (target == null) return;

        session.addProjectile(buildProjectile(user, target, isMagic));
        user.setInternalTimer(user.getActionInterval());
    }

    private Projectile buildProjectile(Plant user, Zombie target, boolean isMagic) {
        Vec2 velocity = new Vec2(20, 0);

        if (isMagic) {
            int pierceCount = (int) user.getAbilityValue();
            Projectile p = new Projectile(
                    user.getPosition(), velocity, target,
                    user.getDamage(), new StraightMove(), new PierceHit(pierceCount)
            );
            p.setStunning(true);
            return p;
        }

        return new Projectile(
                user.getPosition(), velocity, target,
                user.getDamage(), new StraightMove(), new NormalHit(1)
        );
    }

    private Zombie randomTarget(List<Zombie> zombies) {
        List<Zombie> alive = zombies.stream().filter(z -> z != null && z.isAlive()).toList();
        if (alive.isEmpty()) return null;
        return alive.get(ThreadLocalRandom.current().nextInt(alive.size()));
    }

    private Zombie nearestTarget(Plant user, List<Zombie> zombies) {
        Zombie nearest = null;
        double shortest = Double.MAX_VALUE;
        for (Zombie z : zombies) {
            if (z == null || !z.isAlive()) continue;
            double dist = z.getPosition().distanceTo(user.getPosition());
            if (dist < shortest) {
                shortest = dist;
                nearest = z;
            }
        }
        return nearest;
    }
}