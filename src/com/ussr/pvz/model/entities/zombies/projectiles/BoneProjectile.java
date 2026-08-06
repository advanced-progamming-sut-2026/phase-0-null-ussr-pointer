package com.ussr.pvz.model.entities.zombies.projectiles;

import com.ussr.pvz.model.board.structures.GraveSpawner;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.util.Vec2;

public class BoneProjectile extends ZombieProjectile {

    private final double arcHeight = 2.0;

    public BoneProjectile(Vec2 startPosition, Vec2 targetPosition, double flightTime) {
        super(startPosition, targetPosition, flightTime, "TombRaiser");
    }

    @Override
    protected void updateFlightPath(double progress) {
        double currentX = startPosition.x() + (targetPosition.x() - startPosition.x()) * progress;
        double currentY = startPosition.y() + (targetPosition.y() - startPosition.y()) * progress;

        double visualY = currentY - (arcHeight * Math.sin(progress * Math.PI));

        this.setPosition(Vec2.of(currentX, visualY));
    }

    @Override
    protected void onDestinationReached(GameSession session) {
        int targetRow = (int) targetPosition.y();
        int targetCol = (int) targetPosition.x();

        GraveSpawner.spawnGraveIfEmpty(session, targetRow, targetCol);
    }

    @Override
    public void onDestinationReached() {

    }
}