package com.ussr.pvz.model.entities.zombies.projectiles;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.Grave;
import com.ussr.pvz.model.board.terrain.Tile;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.GameSession;
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

        Cell targetCell = session.getLawn().getCell(targetRow, targetCol);

        if (targetCell != null && targetCell.getPlant() == null && targetCell.getInteractableStructure() == null) {
            Grave newGrave = new Grave();
            newGrave.setPosition(Vec2.of(targetCol, targetRow));
            targetCell.setStructure(newGrave);
            targetCell.setTile(new Tile(TileType.Grave));
        }
    }
}