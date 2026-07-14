package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.hit.*;
import com.ussr.pvz.model.entities.projectiles.move.ArcMove;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;


public class LobberStrategy implements ActStrategy {
    private static final double GRAVITY = 15.0;
    private static final double HORIZONTAL_SPEED = 4.0;

    @Override
    public void act(Plant user, GameSession session) {
        if (user.getIntervalTimer() > 0) return;

        user.setInternalTimer(user.getActionInterval());

        Zombie target = findNearestInLane(user, session);
        if (target == null) return;
        Vec2 startPos = user.getPosition();
        Vec2 targetPos = target.getPosition();
        double distanceX = targetPos.x() - startPos.x();
        double timeOfFlight = distanceX / HORIZONTAL_SPEED;
        double initialVelocityY = -0.5 * GRAVITY * timeOfFlight;

        Vec2 initialVelocity = new Vec2(HORIZONTAL_SPEED, initialVelocityY);
        HitEffectStrategy hitEffect = buildHitEffect(user);
        session.addProjectile(new Projectile(
                user.getPosition(),
                initialVelocity, target,
                user.getDamage(),
                new ArcMove(GRAVITY),
                hitEffect
        ));
    }


    private HitEffectStrategy buildHitEffect(Plant user) {
        int areaLength = user.getTags().contains(Tag.AOE) ? 3 : 1;
        if (user.getTags().contains(Tag.FIRE)) return new FireHit(areaLength);
        if (user.getTags().contains(Tag.ICE)) return new IceHit(areaLength);
        if (user.getTags().contains(Tag.POISON)) return new PoisonHit(areaLength);
        return new NormalHit(areaLength);
    }


    private Zombie findNearestInLane(Plant user, GameSession session) {
        double plantRow = user.getPosition().y();
        Zombie nearest = null;
        double minX = Double.MAX_VALUE;

        for (Zombie zombie : session.getZombies()) {
            if (zombie == null || !zombie.isAlive()) continue;
            Vec2 zp = zombie.getPosition();

            if (Math.abs(zp.y() - plantRow) < 0.5 && zp.x() > user.getPosition().x()) {
                if (zp.x() < minX) {
                    minX = zp.x();
                    nearest = zombie;
                }
            }
        }
        return nearest;
    }
}