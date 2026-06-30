package com.ussr.pvz.model.entities.zombies.projectiles;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.Grave;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.util.Vec2;

public class BoneProjectile extends ZombieProjectile {

    private final double arcHeight = 2.0; // How high the bone arcs visually

    public BoneProjectile(Vec2 startPosition, Vec2 targetPosition, double flightTime) {
        super(startPosition, targetPosition, flightTime, "TombRaiser");
    }

    @Override
    protected void updateFlightPath(double progress) {
        // Standard Linear Interpolation (Lerp) for X and Y grid movement
        double currentX = startPosition.x() + (targetPosition.x() - startPosition.x()) * progress;
        double currentY = startPosition.y() + (targetPosition.y() - startPosition.y()) * progress;

        // Add a visual arc to the Y position (or Z if your graphics engine uses it).
        // Math.sin(progress * Math.PI) goes from 0 -> 1 -> 0, creating a perfect parabola.
        //todo NOTE: In PvZ, Y is visually down, so we subtract to make it go "up" on screen.
        double visualY = currentY - (arcHeight * Math.sin(progress * Math.PI));

        this.setPosition(Vec2.of(currentX, visualY));
    }

    @Override
    protected void onDestinationReached(GameSession session) {
        int targetRow = (int) Math.round(targetPosition.y());
        int targetCol = (int) Math.round(targetPosition.x());

        Cell targetCell = session.getLawn().getCell(targetRow, targetCol);

        // Double check the cell is STILL empty when the bone lands
        if (targetCell != null && targetCell.getPlant() == null && targetCell.getInteractableStructure() == null) {
            Grave newGrave = new Grave();
            newGrave.setPosition(Vec2.of(targetCol, targetRow));
            targetCell.setStructure(newGrave);
        }
    }
}