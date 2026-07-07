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

    private final double DESIRED_APEX_HEIGHT = 2.5;


    public ArcMove(double gravity) {
        this.gravity = gravity;
    }


    public void move(Projectile projectile) {
        if (landed) return;

        Vec2 pos = projectile.getPosition();
        Vec2 speed = projectile.getSpeed();

        if (pos != null && speed != null) {
            double newSpeedY = speed.y() - (gravity * GameClock.SECONDS_PER_TICK);
            Vec2 newSpeed = Vec2.of(speed.x(), newSpeedY);

            Vec2 newPos = pos.add(newSpeed.scale(GameClock.SECONDS_PER_TICK));

            boolean isFalling = newSpeedY < 0;
            boolean hitTheGroundLevel = newPos.y() <= groundY;

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

                double initialSpeedY = Math.sqrt(2 * gravity * DESIRED_APEX_HEIGHT);

                double timeOfFlight = 2 * (initialSpeedY / gravity);

                if (timeOfFlight > 0) {

                    double distanceX = targetPos.x() - startPos.x();

                    double initialSpeedX = (distanceX / timeOfFlight) + targetSpeed.x();

                    projectile.setSpeed(Vec2.of(initialSpeedX, initialSpeedY));
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