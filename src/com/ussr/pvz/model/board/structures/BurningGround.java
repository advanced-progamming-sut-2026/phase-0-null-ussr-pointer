package com.ussr.pvz.model.board.structures;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.session.GameSession;

public class BurningGround extends InteractableStructure {
    private double secondsRemaining;

    public BurningGround(double duration) {
        this.secondsRemaining = duration;
        this.setAlive(true);
    }

    @Override
    public void takeDamage(int damage) {
    }

    @Override
    public void update(float delta) {
        secondsRemaining -= delta;
        if (secondsRemaining <= 0) {
            setAlive(false);
        }
    }

    @Override
    public void onDestroy(GameSession session) {
        int row = (int) this.getPosition().y();
        int col = (int) this.getPosition().x();
        Cell cell = session.getLawn().getCell(row, col);
        if (cell != null && cell.getTile() != null) {
            cell.getTile().setType(TileType.Normal);
        }
    }
}