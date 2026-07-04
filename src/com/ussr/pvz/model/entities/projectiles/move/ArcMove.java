package com.ussr.pvz.model.entities.projectiles.move;

import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.util.Vec2;

public class ArcMove implements MoveStrategy {
    private final double gravity;

    // e.g., gravity = 9.8 (You may need to tweak this to match your grid scale)
    public ArcMove(double gravity) {
        this.gravity = gravity;
    }


    @Override
    public void move(Projectile projectile) {
        Vec2 pos = projectile.getPosition();
        Vec2 speed = projectile.getSpeed();

        if (pos != null && speed != null) {
            // Apply gravity to vertical speed (assuming +Y goes down your screen)
            double newSpeedY = speed.y() + (gravity * GameClock.SECONDS_PER_TICK);
            Vec2 newSpeed = Vec2.of(speed.x(), newSpeedY);

            // Update position based on the new speed
            Vec2 newPos = pos.add(newSpeed.scale(GameClock.SECONDS_PER_TICK));

            projectile.setSpeed(newSpeed);
            projectile.setPosition(newPos);
        }
    }
}