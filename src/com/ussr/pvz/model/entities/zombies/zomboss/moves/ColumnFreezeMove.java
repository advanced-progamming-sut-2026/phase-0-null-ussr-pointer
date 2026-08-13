package com.ussr.pvz.model.entities.zombies.zomboss.moves;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossMove;

import java.util.ArrayList;
import java.util.List;

public class ColumnFreezeMove implements ZombossMove {

    private static final int START_COLUMN = 5;
    private static final int END_COLUMN = 0;

    @Override
    public void execute(ZombossController controller, GameSession session, List<String> playingClips) {
        int numCols = session.getLawn().getCols();
        List<Integer> columns = new ArrayList<>();
        for (int col = START_COLUMN; col >= END_COLUMN; col--) {
            if (col >= 0 && col < numCols) {
                columns.add(col);
            }
        }
        if (columns.isEmpty()) return;

        GlacierColumnSweep sweep = new GlacierColumnSweep(session, columns);
        session.registerTickable(sweep);
    }

    @Override
    public void execute(ZombossController controller, GameSession session) {
        execute(controller, session, List.of());
    }
}