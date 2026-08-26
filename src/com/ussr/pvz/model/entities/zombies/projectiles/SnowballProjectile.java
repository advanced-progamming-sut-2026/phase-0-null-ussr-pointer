package com.ussr.pvz.model.entities.zombies.projectiles;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.PlantFreezer;
import com.ussr.pvz.model.util.Vec2;

public class SnowballProjectile extends ZombieProjectile {

    private final String hitPam;

    public SnowballProjectile(Vec2 startPosition, Vec2 targetPosition, double flightTime) {
        this(startPosition, targetPosition, flightTime, null);
    }

    public SnowballProjectile(Vec2 startPosition, Vec2 targetPosition, double flightTime, String hitPam) {
        super(startPosition, targetPosition, flightTime, "IceAgeHunter");
        this.hitPam = hitPam;
    }

    @Override
    public String getHitPam() {
        return hitPam;
    }

    @Override
    protected void updateFlightPath(double progress) {
        double currentX = startPosition.x() + (targetPosition.x() - startPosition.x()) * progress;
        double currentY = startPosition.y() + (targetPosition.y() - startPosition.y()) * progress;

        this.setPosition(Vec2.of(currentX, currentY));
    }

    @Override
    protected void onDestinationReached(GameSession session) {
        int targetRow = (int) targetPosition.y();
        int targetCol = (int) targetPosition.x();

        Cell targetCell = session.getLawn().getCell(targetRow, targetCol);
        if (targetCell != null && targetCell.getPlant() != null && targetCell.getPlant().isAlive()) {
            Plant targetPlant = targetCell.getPlant();
            PlantFreezer.applyFreeze(session, targetPlant, 1);
        }
    }

    @Override
    public void onDestinationReached() {

    }

    @Override
    public String getPamLocation() {
        return "768/INITIAL/EFFECTS/T_SNOW_PEA/T_SNOW_PEA.PAM";
    }
}