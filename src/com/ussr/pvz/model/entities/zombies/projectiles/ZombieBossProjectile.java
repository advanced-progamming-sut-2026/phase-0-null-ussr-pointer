package com.ussr.pvz.model.entities.zombies.projectiles;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.util.Vec2;

public abstract class ZombieBossProjectile extends ZombieProjectile {

    public ZombieBossProjectile(Vec2 startPosition, Vec2 targetPosition, double flightTime, String sourceZombieAlias) {
        super(startPosition, targetPosition, flightTime, sourceZombieAlias);
    }

    @Override
    protected void updateFlightPath(double progress) {
        double curX = startPosition.x() + (targetPosition.x() - startPosition.x()) * progress;
        double curY = startPosition.y() + (targetPosition.y() - startPosition.y()) * progress;

        // Arc offset for parabolic trajectory
        double arc = 4.0 * progress * (1.0 - progress) * 2.0;
        this.setPosition(Vec2.of(curX, curY + arc));
    }

    @Override
    protected void onDestinationReached(GameSession session) {
        if (session != null) {
            applyDestinationEffect(session);
        }
    }

    protected abstract void applyDestinationEffect(GameSession session);
}