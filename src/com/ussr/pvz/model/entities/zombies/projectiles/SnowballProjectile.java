package com.ussr.pvz.model.entities.zombies.projectiles;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.IceBlock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.util.Vec2;

public class SnowballProjectile extends ZombieProjectile {

    public SnowballProjectile(Vec2 startPosition, Vec2 targetPosition, double flightTime) {
        super(startPosition, targetPosition, flightTime, "IceAgeHunter");
    }

    @Override
    protected void updateFlightPath(double progress) {
        double currentX = startPosition.x() + (targetPosition.x() - startPosition.x()) * progress;
        double currentY = startPosition.y() + (targetPosition.y() - startPosition.y()) * progress;

        this.setPosition(Vec2.of(currentX, currentY));
    }

    @Override
    protected void onDestinationReached(GameSession session) {
        int targetRow = (int) Math.round(targetPosition.y());
        int targetCol = (int) Math.round(targetPosition.x());

        Cell targetCell = session.getLawn().getCell(targetRow, targetCol);

        if (targetCell != null && targetCell.getPlant() != null && targetCell.getPlant().isAlive()) {
            Plant targetPlant = targetCell.getPlant();

            if (targetPlant.getState() != Plant.PlantState.INCAPACITATED) {
                targetPlant.setChillLevel(targetPlant.getChillLevel() + 1);

                // If it hits 3 stacks, freeze it completely
                if (targetPlant.getChillLevel() >= 3) {
                    targetCell.setStructure(new IceBlock(targetPlant, 500));
                }
            }
        }
    }
}