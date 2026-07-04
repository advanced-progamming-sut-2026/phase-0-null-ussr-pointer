package com.ussr.pvz.model.entities.projectiles;

import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.entities.projectiles.hit.HitEffectStrategy;
import com.ussr.pvz.model.entities.projectiles.move.ArcMove;
import com.ussr.pvz.model.entities.projectiles.move.MoveStrategy;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

public class Projectile extends GameEntity {
    private int damage;
    private Zombie target;
    private boolean isStunning;

    private MoveStrategy moveStrategy;
    private HitEffectStrategy hitEffectStrategy;

    public Projectile(Vec2 position, Vec2 velocity, Zombie zombie, int damage, MoveStrategy moveStrategy, HitEffectStrategy hitEffectStrategy) {
        this.setPosition(position);
        this.setSpeed(velocity);
        this.target = zombie;
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
            target.takeDamage(damage, this);

            if (hitEffectStrategy != null) {
                hitEffectStrategy.apply(target);
            }
            this.isAlive = false;
        }
    }

    private boolean checkCollision(Zombie target) {
        if (target == null || !target.isAlive()) return false;
        // Simple 1D collision check based on X coordinate proximity
        return Math.abs(this.getPosition().x() - target.getPosition().x()) <= 0.5;
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
}