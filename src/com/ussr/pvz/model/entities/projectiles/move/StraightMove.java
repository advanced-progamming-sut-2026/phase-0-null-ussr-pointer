package com.ussr.pvz.model.entities.projectiles.move;

import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.util.Vec2;

public class StraightMove implements MoveStrategy {

    @Override
    public void move(Projectile projectile) {
        Vec2 pos = projectile.getPosition();
        Vec2 speed = projectile.getSpeed();

        if (pos != null && speed != null) {
            Vec2 newPos = pos.add(speed.scale(GameClock.SECONDS_PER_TICK));
            projectile.setPosition(newPos);
        }
    }
}
