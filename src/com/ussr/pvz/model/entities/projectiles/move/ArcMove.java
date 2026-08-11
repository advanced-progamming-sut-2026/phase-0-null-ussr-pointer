package com.ussr.pvz.model.entities.projectiles.move;

import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.util.Vec2;

public class ArcMove implements MoveStrategy {
    private double groundY;
    private boolean landed;
    private Damageable target;
    private double startX;
    private double startHeight;
    private double landingX;
    private double elapsed;
    private double duration;

    private static final double HORIZONTAL_SPEED = 4.0;
    private static final double MINIMUM_FLIGHT_TIME = 0.55;
    private static final double ARC_HEIGHT = 2.0;
    private static final double ZOMBIE_IMPACT_HEIGHT = 0.80;


    public ArcMove(double gravity) {
        // Kept in the constructor for compatibility with existing factories.
        // The visual path is a deterministic quadratic PvZ-style lob.
    }


    @Override
    public void move(
            Projectile projectile,
            float delta
    ) {
        Vec2 position = projectile.getPosition();
        if (position == null || landed) {
            return;
        }

        if (target instanceof GameEntity targetEntity
                && target.isAlive()
                && targetEntity.getPosition() != null) {
            landingX = targetEntity.getPosition().x();
        }

        elapsed = Math.min(duration, elapsed + delta);
        double progress = duration <= 0.0 ? 1.0 : elapsed / duration;
        double impactHeight = hasLiveTarget()
                ? ZOMBIE_IMPACT_HEIGHT
                : 0.0;

        double newX = startX + (landingX - startX) * progress;
        double height = startHeight * (1.0 - progress)
                + impactHeight * progress
                + 4.0 * ARC_HEIGHT * progress * (1.0 - progress);

        double horizontalVelocity = delta > 0f
                ? (newX - position.x()) / delta
                : 0.0;
        projectile.setSpeed(Vec2.of(horizontalVelocity, 0));
        projectile.setPosition(Vec2.of(newX, groundY));
        projectile.setVisualHeight(height);

        if (progress >= 1.0) {
            landed = true;
        }
    }

    @Override
    public void initialize(Projectile projectile, Damageable target) {
        this.target = target;
        Vec2 startPos = projectile.getPosition();
        if (startPos == null) return;

        this.groundY = startPos.y();
        Vec2 launchOrigin = projectile.getVisualLaunchOrigin();
        this.startX = startPos.x() + launchOrigin.x() - 0.5;
        this.startHeight = Math.max(0.0, launchOrigin.y());
        this.landingX = startX + HORIZONTAL_SPEED * MINIMUM_FLIGHT_TIME;
        if (target instanceof GameEntity targetEntity) {
            Vec2 targetPos = targetEntity.getPosition();
            if (targetPos != null) {
                landingX = targetPos.x();
            }
        }
        duration = Math.max(
                MINIMUM_FLIGHT_TIME,
                Math.abs(landingX - startX) / HORIZONTAL_SPEED
        );
        elapsed = 0.0;
        landed = false;
        projectile.setPosition(Vec2.of(startX, groundY));
        projectile.setVisualHeight(startHeight);
    }

    private boolean hasLiveTarget() {
        return target instanceof GameEntity targetEntity
                && target.isAlive()
                && targetEntity.getPosition() != null
                && Math.abs(targetEntity.getPosition().y() - groundY) < 0.5;
    }

    public boolean hasLanded() {
        return landed;
    }

    public void setGroundY(double groundY) {
        this.groundY = groundY;
    }
}
