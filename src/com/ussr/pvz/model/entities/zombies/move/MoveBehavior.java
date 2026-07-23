package com.ussr.pvz.model.entities.zombies.move;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

public interface MoveBehavior {
    void move(Zombie zombie, GameSession session);

    default Vec2 applySlipperyShift(Vec2 pos, GameSession session) {
        com.ussr.pvz.model.board.Lawn lawn = session.getLawn();
        if (lawn == null) return pos;

        int row = (int) pos.y();
        int col = (int) pos.x();
        com.ussr.pvz.model.board.Cell cell = lawn.getCell(row, col);
        if (cell == null || cell.getTile() == null) return pos;

        com.ussr.pvz.model.board.terrain.Tile tile = cell.getTile();
        if (tile.getType() != com.ussr.pvz.model.board.terrain.TileType.Slippery
                || tile.getSlipperyDirection() == null) {
            return pos;
        }

        double rowDelta = tile.getSlipperyDirection() == com.ussr.pvz.model.board.terrain.Tile.SlipperyDirection.UP
                ? -1.0 : 1.0;
        double newRow = row + rowDelta;

        // Prevent sliding off the map bounds
        if (newRow < 0 || newRow >= lawn.getRows()) return pos;

        return new Vec2(pos.x(), newRow);
    }
}