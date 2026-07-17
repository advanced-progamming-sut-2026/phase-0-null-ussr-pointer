package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.board.structures.InteractableStructure;
import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.hit.*;
import com.ussr.pvz.model.entities.projectiles.move.BounceMove;
import com.ussr.pvz.model.entities.projectiles.move.MoveStrategy;
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

            // Kept at 6.0 to prevent the bullet from tunneling over the target!
            Vec2 velocity = direction.normalize().scale(6.0);
            MoveStrategy moveStrategy = buildMoveStrategy(user);

            session.addProjectile(new Projectile(
                    (Damageable) target, // Cast the GameEntity to Damageable
                    user.getPosition(),
                    velocity,
                    user.getDamage(),
                    moveStrategy,
                    hitEffect
            ));
        }
    }

    private HitEffectStrategy buildHitEffect(Plant user) {
        if (user.getTags().contains(Tag.FIRE)) return new FireHit(1);
        if (user.getTags().contains(Tag.ICE)) return new IceHit(1);
        if (user.getTags().contains(Tag.POISON)) return new PoisonHit(1);
        if (user.getName().equalsIgnoreCase("bowling bulb")) return new PierceHit(Integer.MAX_VALUE);
        return new NormalHit(1);
    }

    private MoveStrategy buildMoveStrategy(Plant user) {
        if(user.getName().equalsIgnoreCase("bowling bulb"))
            return new BounceMove();
        return new StraightMove();
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
            if (!isParallelSameDirection(relX, relY, dx, dy)) continue;

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
                if (!isParallelSameDirection(relX, relY, dx, dy)) continue;

                double dist = Math.sqrt(relX * relX + relY * relY);
                if (dist < bestDist) {
                    bestDist = dist;
                    nearest = structure;
                }
            }
        }

        return nearest;
    }

    private boolean isParallelSameDirection(double relX, double relY, double dx, double dy) {
        double relLen = Math.sqrt(relX * relX + relY * relY);
        double dirLen = Math.sqrt(dx * dx + dy * dy);

        // Cannot compare zero-length vectors
        if (relLen == 0 || dirLen == 0) {
            return false;
        }

        // Cross product == 0 => parallel
        double cross = relX * dy - relY * dx;

        // Use an epsilon because of floating-point precision
        double EPSILON = 0.3;
        if (Math.abs(cross) > EPSILON) {
            return false;
        }

        // Dot product > 0 => same direction
        double dot = relX * dx + relY * dy;
        return dot > 0;
    }
}