package com.ussr.pvz.model.entities.zombies.zomboss.moves;

import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossMove;

public class RowIgniteMove implements ZombossMove {
    @Override
    public void execute(ZombossController controller, GameSession session) {
        for (int row : controller.getOccupiedRows()) {
            if (row >= session.getLawn().getRows()) continue;
            for (int col = 0; col < session.getLawn().getCols(); col++) {
                session.removePlantAt(col, row);
                if (session.getLawn().getTile(row, col) != null) {
                    session.getLawn().getTile(row, col).setType(TileType.Burning);
                }
            }
        }
    }
}