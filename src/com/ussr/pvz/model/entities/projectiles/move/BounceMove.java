package com.ussr.pvz.model.entities.projectiles.move;

import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.util.Vec2;

public class BounceMove implements MoveStrategy {
    private final double gravity;
    private final double bounceDamping;
    private Double floorY = null;

    /**
     * @param gravity       The downward pull (e.g., 9.8)
     * @param bounceDamping How much energy it retains per bounce (e.g., 0.6 retains 60% height)
     */
    public BounceMove(double gravity, double bounceDamping) {
        this.gravity = gravity;
        this.bounceDamping = bounceDamping;
    }

    @Override
    public void move(Projectile projectile) {
        Vec2 pos = projectile.getPosition();
        Vec2 speed = projectile.getSpeed();

        if (pos != null && speed != null) {
            // Record the initial firing height as the floor to bounce off of
            if (floorY == null) {
                floorY = pos.y();
            }

            double newSpeedY = speed.y() + (gravity * GameClock.SECONDS_PER_TICK);
            double newY = pos.y() + (newSpeedY * GameClock.SECONDS_PER_TICK);
            double newX = pos.x() + (speed.x() * GameClock.SECONDS_PER_TICK);

            // If it hits or passes the floor while falling, BOUNCE!
            if (newY >= floorY && newSpeedY > 0) {
                newY = floorY; // Snap back to the floor line
                newSpeedY = -newSpeedY * bounceDamping; // Reverse direction and dampen speed
            }

            Vec2 newSpeed = Vec2.of(speed.x(), newSpeedY);
            Vec2 newPos = Vec2.of(newX, newY);

            projectile.setSpeed(newSpeed);
            projectile.setPosition(newPos);
        }
    }
}