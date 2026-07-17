package com.ussr.pvz.model.entities.projectiles.move;

import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.util.Vec2;

public class StraightMove implements MoveStrategy {
    private double speedMagnitude = 4.0d;

    @Override
    public void move(Projectile projectile) {
        Vec2 pos = projectile.getPosition();
        Vec2 speed = projectile.getSpeed();

        if (pos != null && speed != null) {
            Vec2 newPos = pos.add(speed.scale(GameClock.SECONDS_PER_TICK));
            projectile.setPosition(newPos);
        }
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

                    double directionX = deltaX / distance;
                    double directionY = deltaY / distance;

                    projectile.setSpeed(Vec2.of(directionX * speedMagnitude, directionY * speedMagnitude));
                    return;
                }
            }
        }

        //projectile.setSpeed(Vec2.of(speedMagnitude, 0));
    }

    public void setSpeedMagnitude(double speedMagnitude) {
        this.speedMagnitude = speedMagnitude;
    }
}
