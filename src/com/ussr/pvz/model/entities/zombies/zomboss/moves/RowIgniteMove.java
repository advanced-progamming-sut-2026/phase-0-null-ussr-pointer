package com.ussr.pvz.model.entities.zombies.zomboss.moves;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossMove;
import com.ussr.pvz.model.util.Vec2;

public class RowIgniteMove implements ZombossMove {
    private static final float BURN_DURATION_SECONDS = 2f;

    @Override
    public void execute(ZombossController controller, GameSession session) {
        for (int row : controller.getOccupiedRows()) {
            if (row >= session.getLawn().getRows()) continue;
            for (int col = 0; col < session.getLawn().getCols(); col++) {
                session.removePlantAt(col, row);
                session.igniteTileTemporarily(row, col, BURN_DURATION_SECONDS);
            }
        }
    }
}