package com.ussr.pvz.model.entities.projectiles;

import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.entities.projectiles.hit.HitEffectStrategy;
import com.ussr.pvz.model.entities.projectiles.move.ArcMove;
import com.ussr.pvz.model.entities.projectiles.move.MoveStrategy;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

public class Projectile extends GameEntity {
    private int damage;
    private Damageable target;
    private boolean isStunning;

    private MoveStrategy moveStrategy;
    private HitEffectStrategy hitEffectStrategy;

    public Projectile(Vec2 position, Vec2 velocity, Zombie zombie, int damage, MoveStrategy moveStrategy, HitEffectStrategy hitEffectStrategy) {
        this((Damageable) zombie, position, velocity, damage, moveStrategy, hitEffectStrategy);
    }

    public Projectile(Damageable target, Vec2 position, Vec2 velocity, int damage, MoveStrategy moveStrategy, HitEffectStrategy hitEffectStrategy) {
        this.setPosition(position);
        this.setSpeed(velocity);
        this.target = target;
        this.damage = damage;
        this.moveStrategy = moveStrategy;
        this.hitEffectStrategy = hitEffectStrategy;
        this.isStunning = false;
    }

    public void setStunning(boolean isStunning) {
        this.isStunning = isStunning;
    }

    @Override
    public void tick() {
        if (!isAlive) return;

        if (moveStrategy != null) {
            moveStrategy.move(this);
        }

        boolean hasHitTarget = checkCollision(target);

        if (hasHitTarget) {
            if (target instanceof Zombie zombie) {
                zombie.takeDamage(damage, this);
            } else {
                target.takeDamage(damage);
            }

            if (hitEffectStrategy != null && target instanceof Zombie zombie) {
                hitEffectStrategy.apply(zombie);
            }
            this.isAlive = false;
        }
    }

    private boolean checkCollision(Damageable target) {
        if (target == null || !target.isAlive()) return false;
        Vec2 targetPos = resolveTargetPosition(target);
        if (targetPos == null) return false;
        // Simple 1D collision check based on X coordinate proximity
        return Math.abs(this.getPosition().x() - targetPos.x()) <= 0.5;
    }

    private Vec2 resolveTargetPosition(Damageable target) {
        if (target instanceof Zombie zombie) {
            return zombie.getPosition();
        }
        if (target instanceof com.ussr.pvz.model.entities.plants.Plant plant) {
            var loc = plant.getLocation();
            return loc == null ? null : Vec2.of(loc.x(), loc.y());
        }
        return null;
    }

    public void setHitEffectStrategy(HitEffectStrategy strategy) {
        this.hitEffectStrategy = strategy;
    }

    public HitEffectStrategy getHitEffectStrategy() {
        return this.hitEffectStrategy;
    }

    public Object getMoveStrategy() {
        return moveStrategy;
    }

    public int getDamage() {
        return damage;
    }
}