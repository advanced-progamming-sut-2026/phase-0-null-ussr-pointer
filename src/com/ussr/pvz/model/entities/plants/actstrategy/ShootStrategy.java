package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.board.structures.InteractableStructure;
import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.hit.*;
import com.ussr.pvz.model.entities.projectiles.move.StraightMove;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

import java.util.List;

public class ShootStrategy implements ActStrategy {

    @Override
    public void act(Plant user, GameSession session) {
        List<Vec2> vectors = user.getShootingVectors();

        if (vectors == null || vectors.isEmpty()) return;

        boolean anyTarget = vectors.stream()
                .anyMatch(v -> findTargetAlongVector(user, v, session) != null);
        if (!anyTarget) return;

        user.setInternalTimer(0.0);

        HitEffectStrategy hitEffect = buildHitEffect(user);

        for (Vec2 direction : vectors) {
            GameEntity target = findTargetAlongVector(user, direction, session);
            if (target == null) continue;

            // Kept at 6.0 to prevent the bullet from tunneling over the target!
            Vec2 velocity = direction.normalize().scale(6.0);

            session.addProjectile(new Projectile(
                    (Damageable) target, // Cast the GameEntity to Damageable
                    user.getPosition(),
                    velocity,
                    user.getDamage(),
                    new StraightMove(),
                    hitEffect
            ));
        }
    }

    private HitEffectStrategy buildHitEffect(Plant user) {
        if (user.getTags().contains(Tag.FIRE)) return new FireHit(1);
        if (user.getTags().contains(Tag.ICE)) return new IceHit(1);
        if (user.getTags().contains(Tag.POISON)) return new PoisonHit(1);
        return new NormalHit(1);
    }

    private GameEntity findTargetAlongVector(Plant user, Vec2 direction, GameSession session) {
        Vec2 origin = user.getPosition();
        double dx = direction.x();
        double dy = direction.y();
        GameEntity nearest = null;
        double bestDist = Double.MAX_VALUE;

        for (Zombie zombie : session.getZombies()) {
            if (zombie == null || !zombie.isAlive()) continue;
            Vec2 zp = zombie.getPosition();

            double relX = zp.x() - origin.x();
            double relY = zp.y() - origin.y();
            if (!isInCone(relX, relY, dx, dy)) continue;

            double dist = Math.sqrt(relX * relX + relY * relY);
            if (dist < bestDist) {
                bestDist = dist;
                nearest = zombie;
            }
        }

        if (session.getLawn() != null) {
            for (InteractableStructure structure : session.getLawn().getAllInteractable()) {
                if (structure == null || !structure.isAlive()) continue;
                Vec2 sp = structure.getPosition();

                double relX = sp.x() - origin.x();
                double relY = sp.y() - origin.y();
                if (!isInCone(relX, relY, dx, dy)) continue;

                double dist = Math.sqrt(relX * relX + relY * relY);
                if (dist < bestDist) {
                    bestDist = dist;
                    nearest = structure;
                }
            }
        }

        return nearest;
    }

    private boolean isInCone(double relX, double relY, double dx, double dy) {
        double dirLen = Math.sqrt(dx * dx + dy * dy);
        if (dirLen == 0) return false;

        if (dy == 0) {
            return Math.abs(relY) < 0.75 && Math.signum(relX) == Math.signum(dx);
        }

        if (dx != 0 && Math.abs(dy) <= 1.5) {
            boolean correctXDir = Math.signum(relX) == Math.signum(dx) && Math.abs(relX) > 0;
            boolean correctRow = Math.abs(relY - dy) < 0.75;
            return correctXDir && correctRow;
        }

        double relLen = Math.sqrt(relX * relX + relY * relY);
        if (relLen == 0) return false;
        double ndx = dx / dirLen;
        double ndy = dy / dirLen;
        double dot = (relX / relLen) * ndx + (relY / relLen) * ndy;
        return dot > 0.6;
    }
}