package com.ussr.pvz.model.entities.zombies.move;

import com.ussr.pvz.model.board.terrain.Tile;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

public interface MoveBehavior {
    void move(
            Zombie zombie,
            GameSession session,
            float delta
    );

    default Vec2 applySlipperyShift(Zombie zombie, Vec2 previousPosition, Vec2 newPosition, GameSession session) {
        com.ussr.pvz.model.board.Lawn lawn = session.getLawn();
        if (lawn == null || previousPosition == null || newPosition == null || zombie.isSlidingBetweenRows()) {
            return newPosition;
        }
        int row = (int) Math.round(newPosition.y());
        int col = (int) Math.round(newPosition.x());
        if (row < 0 || row >= lawn.getRows() || col < 0 || col >= lawn.getCols()) {
            return newPosition;
        }
        double previousX = previousPosition.x();
        double newX = newPosition.x();
        double tileCenterX = col;

        boolean crossedCenter = newX < previousX ? previousX >= tileCenterX && newX <= tileCenterX : newX > previousX
                && previousX <= tileCenterX && newX >= tileCenterX;
        if (!crossedCenter) {
            return newPosition;
        }

        com.ussr.pvz.model.board.Cell cell = lawn.getCell(row, col);
        if (cell == null || cell.getTile() == null) return newPosition;

        com.ussr.pvz.model.board.terrain.Tile tile = cell.getTile();
        if (tile.getType() != TileType.Slippery
                || tile.getSlipperyDirection() == null) {
            return newPosition;
        }

        double rowDelta =
                tile.getSlipperyDirection()
                        == Tile.SlipperyDirection.UP
                        ? -1.0
                        : 1.0;

        double targetRow = row + rowDelta;

        if (targetRow < 0 || targetRow >= lawn.getRows()) {
            return newPosition;
        }

        Vec2 centeredPosition = Vec2.of(tileCenterX, row);
        zombie.setPosition(centeredPosition);
        zombie.startSlipperySlide(targetRow);

        return centeredPosition;
    }
}
