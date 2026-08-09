package com.ussr.pvz.model.entities.zombies.projectiles;

import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.util.Vec2;

public abstract class ZombieProjectile extends GameEntity {
    protected Vec2 startPosition;
    protected Vec2 targetPosition;
    protected double flightTime;
    protected double elapsedTimer = 0.0;
    private double visualHeight;

    protected String sourceZombieAlias;

    public ZombieProjectile(Vec2 startPosition, Vec2 targetPosition, double flightTime, String sourceZombieAlias) {
        this.startPosition = startPosition;
        this.targetPosition = targetPosition;
        this.setPosition(startPosition);
        this.flightTime = flightTime;
        this.sourceZombieAlias = sourceZombieAlias;
    }

    @Override
    public void update(float delta) {
        if (!isAlive) return;

        elapsedTimer += delta;
        double progress = elapsedTimer / flightTime;

        if (progress >= 1.0) {
            this.setPosition(targetPosition);
            this.visualHeight = 0.0;
            onDestinationReached(com.ussr.pvz.model.App.getGameSession());
            this.isAlive = false;
        } else {
            updateFlightPath(progress);
        }
    }

    public double getVisualHeight() {
        return visualHeight;
    }

    protected void setVisualHeight(double visualHeight) {
        this.visualHeight = Math.max(0.0, visualHeight);
    }

    protected abstract void updateFlightPath(double progress);

    protected abstract void onDestinationReached(GameSession session);

    public abstract void onDestinationReached();
    public abstract String getPamLocation();
}
