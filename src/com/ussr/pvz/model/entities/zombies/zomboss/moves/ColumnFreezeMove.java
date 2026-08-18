package com.ussr.pvz.model.entities.zombies.zomboss.moves;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.GlacierBlock;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossMove;

import java.util.List;

public class ColumnFreezeMove implements ZombossMove {

    private static final int START_COLUMN = 5;
    private static final int END_COLUMN = 0;

    @Override
    public void execute(ZombossController controller, GameSession session, List<String> playingClips) {
        int numCols = session.getLawn().getCols();

        // The decision of "repair or build" is made exactly once, right here,
        // at the moment the boss commits to this move. The sweep it spawns
        // just carries out that single decision — it never re-evaluates.
        int repairTarget = findDamagedColumn(session, numCols);
        int targetColumn = repairTarget >= 0 ? repairTarget : findUnbuiltColumn(session, numCols);

        if (targetColumn < 0) {
            // Nothing damaged, nothing left to build — skip this move entirely.
            return;
        }

        String sweepClip = playingClips.isEmpty() ? null : playingClips.get(0);
        controller.lockMoves(sweepClip);

        GlacierColumnSweep sweep = new GlacierColumnSweep(session, targetColumn, controller);
        session.registerTickable(sweep);
    }

    @Override
    public void execute(ZombossController controller, GameSession session) {
        execute(controller, session, List.of());
    }

    /** Right-to-left: first column that has existing ice but isn't fully intact. */
    private int findDamagedColumn(GameSession session, int numCols) {
        int totalRows = session.getLawn().getRows();
        for (int col = Math.min(START_COLUMN, numCols - 1); col >= END_COLUMN; col--) {
            if (col < 0) continue;
            boolean hasAnyBlock = false;
            boolean needsRepair = false;
            for (int row = 0; row < totalRows; row++) {
                Cell cell = session.getLawn().getCell(row, col);
                if (cell != null && cell.getInteractableStructure() instanceof GlacierBlock block && block.isAlive()) {
                    hasAnyBlock = true;
                    if (block.isDamaged()) needsRepair = true;
                } else {
                    needsRepair = true; // a missing cell in a built column also needs attention
                }
            }
            if (hasAnyBlock && needsRepair) return col;
        }
        return -1;
    }

    private int findUnbuiltColumn(GameSession session, int numCols) {
        int totalRows = session.getLawn().getRows();
        for (int col = Math.min(START_COLUMN, numCols - 1); col >= END_COLUMN; col--) {
            if (col < 0) continue;
            boolean anyBlock = false;
            for (int row = 0; row < totalRows; row++) {
                Cell cell = session.getLawn().getCell(row, col);
                if (cell != null && cell.getInteractableStructure() instanceof GlacierBlock block && block.isAlive()) {
                    anyBlock = true;
                    break;
                }
            }
            if (!anyBlock) return col;
        }
        return -1;
    }
}