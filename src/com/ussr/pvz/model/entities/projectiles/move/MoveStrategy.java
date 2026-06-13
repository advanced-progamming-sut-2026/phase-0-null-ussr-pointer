package com.ussr.pvz.model.entities.projectiles.move;

import com.ussr.pvz.model.entities.projectiles.Projectile;

public interface MoveStrategy {
    void move(Projectile projectile);
}
