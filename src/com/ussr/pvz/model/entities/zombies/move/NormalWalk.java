package com.ussr.pvz.model.entities.zombies.move;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.terrain.Tile;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

public class NormalWalk implements MoveBehavior {

    @Override
    public void move(Zombie zombie, GameSession session) {
        Vec2 pos = zombie.getPosition();
        Vec2 vel = zombie.getSpeed();

        Vec2 newPos = pos.add(vel.scale(GameClock.SECONDS_PER_TICK));

        int oldCol = (int) pos.x();
        int newCol = (int) newPos.x();
        if (newCol != oldCol) {
            newPos = applySlipperyShift(newPos, session);
        }

        zombie.setPosition(newPos);

        if (newPos.x() < 0) {
            session.onZombieReachedEnd();
        }
    }

    private Vec2 applySlipperyShift(Vec2 pos, GameSession session) {
        Lawn lawn = session.getLawn();
        if (lawn == null) return pos;

        int row = (int) pos.y();
        int col = (int) pos.x();
        Cell cell = lawn.getCell(row, col);
        if (cell == null || cell.getTile() == null) return pos;

        Tile tile = cell.getTile();
        if (tile.getType() != TileType.Slippery || tile.getSlipperyDirection() == null) {
            return pos;
        }

        double rowDelta = tile.getSlipperyDirection() == Tile.SlipperyDirection.UP ? -1.0 : 1.0;
        if (rowDelta < 0 || rowDelta >= lawn.getRows()) return pos;

        return new Vec2(pos.x(), rowDelta);
    }
}