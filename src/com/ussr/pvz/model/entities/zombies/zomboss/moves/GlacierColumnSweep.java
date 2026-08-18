package com.ussr.pvz.model.entities.zombies.zomboss.moves;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.GlacierBlock;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.Tickable;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.model.util.Vec2;

public class GlacierColumnSweep implements Tickable {
    private static final int GLACIER_BLOCK_HP = 100;
    private static final String BURIED_ZOMBIE_ALIAS = "ZombieArmor1";

    private static final double ROW_START_DELAY_SECONDS = 2.7;
    private static final double ROW_ADVANCE_SECONDS = 0.9;

    private final GameSession session;
    private final ZombossController controller;
    private final int column;
    private int nextRowToFreeze = 0;
    private double rowTimer = 0;
    private boolean finished = false;

    public GlacierColumnSweep(GameSession session, int column, ZombossController controller) {
        this.session = session;
        this.controller = controller;
        this.column = column;
    }

    @Override
    public void update(float delta) {
        if (finished || session.isGameOver()) return;

        int totalRows = session.getLawn().getRows();

        if (nextRowToFreeze < totalRows) {
            rowTimer += delta;
            double threshold = nextRowToFreeze == 0 ? ROW_START_DELAY_SECONDS : ROW_ADVANCE_SECONDS;
            if (rowTimer >= threshold) {
                rowTimer = 0;
                freezeRow(column, nextRowToFreeze);
                nextRowToFreeze++;

                if (checkFullLawnCoverage()) {
                    finish();
                    session.concludeDefeat();
                    return;
                }
            }
            return;
        }

        finish();
    }

    private void freezeRow(int col, int row) {
        Cell cell = session.getLawn().getCell(row, col);
        if (cell != null && cell.getInteractableStructure() instanceof GlacierBlock existing
                && existing.isAlive() && !existing.isDamaged()) {
            return; // already whole, leave it
        }

        session.removePlantAt(col, row);

        var tile = session.getLawn().getTile(row, col);
        if (tile == null) return;

        TileType previousType = tile.getType();
        tile.setType(TileType.Frozen);

        GlacierBlock block = new GlacierBlock(GLACIER_BLOCK_HP, previousType, null, BURIED_ZOMBIE_ALIAS);
        block.setPosition(Vec2.of(col, row));

        if (cell != null) {
            cell.setStructure(block);
        }
        session.registerStructure(block);
    }

    private boolean checkFullLawnCoverage() {
        int rows = session.getLawn().getRows();
        int cols = session.getLawn().getCols();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Cell cell = session.getLawn().getCell(row, col);
                if (cell == null
                        || !(cell.getInteractableStructure() instanceof GlacierBlock block)
                        || !block.isAlive()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void finish() {
        finished = true;
        controller.unlockMoves();
    }
}