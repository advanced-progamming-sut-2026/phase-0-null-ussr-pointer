package com.ussr.pvz.model.entities.zombies.zomboss.moves;

import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossMove;

public class ColumnIgniteMove implements ZombossMove {
    private static final int COLUMNS_TO_BURN = 2;

    @Override
    public void execute(ZombossController controller, GameSession session) {
        int cols = session.getLawn().getCols();
        int lastCol = Math.min(cols - COLUMNS_TO_BURN, cols);

        for (int col = 8; col >= lastCol; col--) {
            for (int row = 4; row >= 0; row--) {
                session.removePlantAt(col, row);
                if (session.getLawn().getTile(row, col) != null) {
                    session.getLawn().getTile(row, col).setType(TileType.Burning);
                }
            }
        }
    }
}