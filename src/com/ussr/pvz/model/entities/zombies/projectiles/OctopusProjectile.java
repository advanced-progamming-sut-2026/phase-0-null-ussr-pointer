package com.ussr.pvz.model.entities.zombies.projectiles;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.OctopusWrap;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.util.Vec2;

public class OctopusProjectile extends ZombieProjectile {

    private final double arcHeight = 2.5;

    public OctopusProjectile(Vec2 startPosition, Vec2 targetPosition, double flightTime) {
        super(startPosition, targetPosition, flightTime, "OctopusZombie");
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

        Cell targetCell = session.getLawn().getCell(targetRow, targetCol);

        if (targetCell != null && targetCell.getPlant() != null && targetCell.getPlant().isAlive()) {
            Plant targetPlant = targetCell.getPlant();

            // Only attach an octopus if the plant isn't already incapacitated by something else
            if (targetPlant.getState() != Plant.PlantState.INCAPACITATED) {
                OctopusWrap wrap = new OctopusWrap(targetPlant, 800);
                wrap.setPosition(Vec2.of(targetCol, targetRow));
                targetCell.setStructure(wrap);
                session.registerStructure(wrap);
            }
        }
    }
}