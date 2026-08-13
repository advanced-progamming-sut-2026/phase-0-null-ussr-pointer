package com.ussr.pvz.model.entities.zombies.zomboss.moves;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossMove;

import java.util.Random;

public class RowSwitchMove implements ZombossMove {
    @Override
    public void execute(ZombossController controller, GameSession session) {
        if (!controller.canSwitchRows()) return;

        Random random = new Random();
        int maxRow = session.getLawn().getRows() - 2;
        if (maxRow < 0) return;

        int newRow = random.nextInt(maxRow + 1);
        controller.relocateRows(newRow, session);
    }
}