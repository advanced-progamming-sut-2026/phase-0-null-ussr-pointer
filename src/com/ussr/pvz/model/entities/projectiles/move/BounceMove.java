package com.ussr.pvz.model.entities.projectiles.move;

import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.util.Vec2;

public class BounceMove implements MoveStrategy {
    private double speedMagnitude = 4.0d;
    private final double MIN_X = 1d;
    private final double MIN_Y = 1d;
    private final double MAX_X = 9d;
    private final double MAX_Y = 5d;

    private int hitCount = 0;


    public BounceMove() {};

    @Override
    public void move(Projectile projectile) {
        Vec2 pos = projectile.getPosition();
        Vec2 speed = projectile.getSpeed();

        if (pos != null && speed != null) {
            Vec2 newPos = pos.add(speed.scale(GameClock.SECONDS_PER_TICK));

            if (newPos.x() < MIN_X || newPos.x() > MAX_X) {
                projectile.setPosition(newPos);
                return;
            }
            boolean hitWallY = false;
            double clampedY = newPos.y();

            if (clampedY <= MIN_Y) { clampedY = MIN_Y; hitWallY = true; }
            if (clampedY >= MAX_Y) { clampedY = MAX_Y; hitWallY = true; }

            if (hitWallY) {
                projectile.setPosition(Vec2.of(newPos.x(), clampedY));
                //next line may have a bug will check in the debug process
                bounce(projectile);
            } else {
                projectile.setPosition(newPos);
            }
        }
    }

    public void bounce(Projectile projectile) {
        Vec2 currentSpeed = projectile.getSpeed();
        Vec2 pos = projectile.getPosition();
        if (currentSpeed == null || pos == null) return;

        if (hitCount == 0) {
            double xSign = currentSpeed.x() >= 0 ? 1.0 : -1.0;
            double randomSign = Math.random() < 0.5 ? 1.0 : -1.0;

            double targetX = xSign * 0.7071 * speedMagnitude;
            double targetY = randomSign * 0.7071 * speedMagnitude;

            projectile.setSpeed(Vec2.of(targetX, targetY));
        }
        else {
            projectile.setSpeed(new Vec2(currentSpeed.x() , -currentSpeed.y()));
        }
        hitCount++;

    }

    @Override
    public void initialize(Projectile projectile, Damageable target) {
        Vec2 startPos = projectile.getPosition();
        if (startPos == null) return;

        if (target instanceof GameEntity targetEntity) {
            Vec2 targetPos = targetEntity.getPosition();
            if (targetPos != null) {
                double deltaX = targetPos.x() - startPos.x();
                double deltaY = targetPos.y() - startPos.y();
                double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

                if (distance > 0) {
                    double dirX = deltaX / distance;
                    double dirY = deltaY / distance;
                    projectile.setSpeed(Vec2.of(dirX * speedMagnitude, dirY * speedMagnitude));
                    return;
                }
            }
        }

        projectile.setSpeed(Vec2.of(speedMagnitude, 0));
    }

    public void setSpeedMagnitude(double speedMagnitude) {
        this.speedMagnitude = speedMagnitude;
    }
}