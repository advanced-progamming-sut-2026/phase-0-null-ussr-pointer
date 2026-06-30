package com.ussr.pvz.model.entities.zombies.projectiles;

import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.util.Vec2;

public abstract class ZombieProjectile extends GameEntity {
    protected Vec2 startPosition;
    protected Vec2 targetPosition;
    protected double flightTime;
    protected double elapsedTimer = 0.0;

    protected String sourceZombieAlias;

    public ZombieProjectile(Vec2 startPosition, Vec2 targetPosition, double flightTime, String sourceZombieAlias) {
        this.startPosition = startPosition;
        this.targetPosition = targetPosition;
        this.setPosition(startPosition);
        this.flightTime = flightTime;
        this.sourceZombieAlias = sourceZombieAlias;
    }

    @Override
    public void tick() {
        if (!isAlive) return;

        elapsedTimer += GameClock.SECONDS_PER_TICK;
        double progress = elapsedTimer / flightTime;

        if (progress >= 1.0) {
            this.setPosition(targetPosition);
            onDestinationReached(com.ussr.pvz.model.App.getGameSession());
            this.isAlive = false;
        } else {
            updateFlightPath(progress);
        }
    }

    protected abstract void updateFlightPath(double progress);

    protected abstract void onDestinationReached(GameSession session);
}