package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.hit.PierceHit;
import com.ussr.pvz.model.entities.projectiles.move.StraightMove;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;


public class StrikeStrategy implements ActStrategy {

    @Override
    public void act(Plant user, GameSession session) {
        Zombie target = findNearestInLane(user, session);
        if (target == null) return;

        user.setInternalTimer(0.0);

        int pierceCount = (int) user.getAbilityValue();
        if(user.isBuffed()) pierceCount = Integer.MAX_VALUE;
        session.addProjectile(new Projectile(
                user.getPosition(),
                new Vec2(6, 0), target,
                user.getDamage(),
                new StraightMove(),
                new PierceHit(pierceCount)
        ));
    }


    private Zombie findNearestInLane(Plant user, GameSession session) {
        double plantRow = user.getPosition().y();
        double plantCol = user.getPosition().x();

        Zombie nearest = null;
        double minDist = Double.MAX_VALUE;

        for (Zombie zombie : session.getZombies()) {
            if (zombie == null || !zombie.isAlive()) continue;
            Vec2 zp = zombie.getPosition();

            if (Math.abs(zp.y() - plantRow) < 0.5 && zp.x() > plantCol) {
                double dist = zp.x() - plantCol;
                if (dist < minDist) {
                    minDist = dist;
                    nearest = zombie;
                }
            }
        }
        return nearest;
    }
}