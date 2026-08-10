package com.ussr.pvz.model.entities.projectiles.move;

import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.entities.projectiles.Projectile;

public interface MoveStrategy {
    void move(
            Projectile projectile,
            float delta
    );

    void initialize(
            Projectile projectile,
            Damageable target
    );

    /** Stops tracking a target once this projectile has already hit it. */
    default void onTargetHit(
            Projectile projectile,
            Damageable hitTarget
    ) {
    }
}
