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
    private Damageable target;
    private double verticalVelocity;
    private double flightHeight;
    private boolean launched;

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

        double horizontalSpeed = speed.x();
        double trackedTargetX = Double.NaN;
        double remainingFlightTime = Double.POSITIVE_INFINITY;
        if (target instanceof GameEntity targetEntity
                && target.isAlive()
                && targetEntity.getPosition() != null) {
            remainingFlightTime = calculateTimeToGround();
            trackedTargetX = targetEntity.getPosition().x();
            if (remainingFlightTime > 0.001) {
                horizontalSpeed =
                        (trackedTargetX - position.x()) / remainingFlightTime;
            }
        }

        verticalVelocity -= gravity * delta;
        flightHeight += verticalVelocity * delta;
        double newX = !Double.isNaN(trackedTargetX) && delta >= remainingFlightTime
                ? trackedTargetX
                : position.x() + horizontalSpeed * delta;

        projectile.setSpeed(Vec2.of(horizontalSpeed, 0));
        projectile.setPosition(Vec2.of(newX, groundY));
        projectile.setVisualHeight(flightHeight);

        if (launched && flightHeight <= 0) {
            flightHeight = 0;
            projectile.setVisualHeight(0);
            landed = true;
        }
    }

    @Override
    public void initialize(Projectile projectile, Damageable target) {
        this.target = target;
        Vec2 startPos = projectile.getPosition();
        if (startPos == null) return;

        this.groundY = startPos.y();

        if (target instanceof GameEntity targetEntity) {
            Vec2 targetPos = targetEntity.getPosition();
            if (targetPos != null) {

                double distanceX = targetPos.x() - startPos.x();

                double timeOfFlight = distanceX / HORIZONTAL_SPEED;

                if (timeOfFlight > 0) {

                    verticalVelocity = 0.5 * gravity * timeOfFlight;
                    flightHeight = 0;
                    launched = true;
                    projectile.setVisualHeight(0);
                    projectile.setSpeed(Vec2.of(HORIZONTAL_SPEED, 0));
                }
            }
        }
        if (!launched) {
            verticalVelocity = gravity * 0.5;
            flightHeight = 0;
            launched = true;
            projectile.setSpeed(Vec2.of(HORIZONTAL_SPEED, 0));
        }
    }

    private double calculateTimeToGround() {
        if (gravity == 0) return 0;
        double discriminant = verticalVelocity * verticalVelocity
                + 2.0 * gravity * Math.max(0.0, flightHeight);
        return (verticalVelocity + Math.sqrt(discriminant)) / gravity;
    }

    public boolean hasLanded() {
        return landed;
    }

    public void setGroundY(double groundY) {
        this.groundY = groundY;
    }
}
