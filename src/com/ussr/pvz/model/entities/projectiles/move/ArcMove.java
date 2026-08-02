package com.ussr.pvz.model.entities.projectiles.move;

import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.util.Vec2;

public class ArcMove implements MoveStrategy {
    private final double gravity;
    private double groundY;
    private boolean landed = false;

    private static final double HORIZONTAL_SPEED = 4.0;


    public ArcMove(double gravity) {
        this.gravity = gravity;
    }


    @Override
    public void move(
            Projectile projectile,
            float delta
    ) {
        Vec2 position = projectile.getPosition();
        Vec2 speed = projectile.getSpeed();

        if (position == null || speed == null) {
            return;
        }

        double newSpeedY =
                speed.y() + gravity * delta;

        Vec2 newSpeed = new Vec2(
                speed.x(),
                newSpeedY
        );

        Vec2 newPosition = position.add(
                newSpeed.scale(delta)
        );

        projectile.setSpeed(newSpeed);
        projectile.setPosition(newPosition);

        if (newPosition.y() >= groundY) {
            landed = true;
        }
    }

    @Override
    public void initialize(Projectile projectile, Damageable target) {
        Vec2 startPos = projectile.getPosition();
        if (startPos == null) return;

        this.groundY = startPos.y();

        if (target instanceof GameEntity targetEntity) {
            Vec2 targetPos = targetEntity.getPosition();
            Vec2 targetSpeed = targetEntity.getSpeed();

            if (targetPos != null && targetSpeed != null) {

                double distanceX = targetPos.x() - startPos.x();

                double timeOfFlight = distanceX / HORIZONTAL_SPEED;

                if (timeOfFlight > 0) {

                    double initialVelocityY = -0.5 * gravity * timeOfFlight;

                    Vec2 initialVelocity = new Vec2(HORIZONTAL_SPEED, initialVelocityY);
                    projectile.setSpeed(initialVelocity);
                }
            }
        }
    }

    public boolean hasLanded() {
        return landed;
    }

    public void setGroundY(double groundY) {
        this.groundY = groundY;
    }
}