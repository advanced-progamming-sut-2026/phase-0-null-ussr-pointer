package com.ussr.pvz.model.board.structures;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.terrain.Tile;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.util.Vec2;

/**
 * Shared grave-placement logic, extracted from ZombieTombRaiser's
 * BoneProjectile so any other source of graves (e.g. the Egypt zomboss'
 * rocket) reuses the exact same rule instead of reimplementing it.
 */
public final class GraveSpawner {

    private GraveSpawner() {
    }

    public static boolean spawnGraveIfEmpty(GameSession session, int row, int col) {
        Cell targetCell = session.getLawn().getCell(row, col);
        if (targetCell == null || targetCell.getPlant() != null || targetCell.getInteractableStructure() != null) {
            return false;
        }

        Grave newGrave = new Grave();
        newGrave.setPosition(Vec2.of(col, row));
        targetCell.setStructure(newGrave);
        targetCell.setTile(new Tile(TileType.Grave));
        return true;
    }
}