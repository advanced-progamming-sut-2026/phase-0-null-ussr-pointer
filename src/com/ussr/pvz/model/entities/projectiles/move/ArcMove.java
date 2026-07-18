package com.ussr.pvz.model.entities.projectiles.move;

import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.util.Vec2;

//todo : since we assume that the y+ is in down direction we should negate all the y s in this class

public class ArcMove implements MoveStrategy {
    private final double gravity;
    private double groundY;
    private boolean landed = false;

    private static final double HORIZONTAL_SPEED = 4.0;


    public ArcMove(double gravity) {
        this.gravity = gravity;
    }


    public void move(Projectile projectile) {
        if (landed) return;

        Vec2 pos = projectile.getPosition();
        Vec2 speed = projectile.getSpeed();

        if (pos != null && speed != null) {
            double newSpeedY = speed.y() + (gravity * GameClock.SECONDS_PER_TICK);
            Vec2 newSpeed = Vec2.of(speed.x(), newSpeedY);

            Vec2 newPos = pos.add(newSpeed.scale(GameClock.SECONDS_PER_TICK));

            boolean isFalling = newSpeedY > 0;
            boolean hitTheGroundLevel = newPos.y() - groundY > -0.15;

            if (isFalling && hitTheGroundLevel) {
                newPos = Vec2.of(newPos.x(), groundY);
                newSpeed = Vec2.of(newSpeed.x(), 0);
                this.landed = true;
            }

            projectile.setSpeed(newSpeed);
            projectile.setPosition(newPos);
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