package com.ussr.pvz.model.entities.zombies.zomboss.moves;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.GlacierBlock;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.Tickable;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

public class GlacierColumnSweep implements Tickable {
    private static final int GLACIER_BLOCK_HP = 600;
    private static final String BURIED_ZOMBIE_ALIAS = "ZombieArmor1";

    private static final double ROW_START_DELAY_SECONDS = 2.7;
    private static final double ROW_ADVANCE_SECONDS = 0.9;

    // Delay between finishing one column and starting the next: randomized,
    // scaled by how far that column is from the boss (closer = shorter delay).
    private static final double MIN_COLUMN_DELAY = 10.0;
    private static final double MAX_COLUMN_DELAY = 30.0;
    private static final double COLUMN_DELAY_JITTER = 3.0; // +/- half this, around the scaled base

    private static final Random RAND = new Random();

    private final GameSession session;
    private final ZombossController controller;
    private final Deque<Integer> remainingColumns;
    private List<GlacierBlock> currentColumnBlocks = new ArrayList<>();
    private int currentColumn = -1;
    private int nextRowToFreeze = 0;
    private double rowTimer = 0;
    private double holdTimer = 0;
    private double columnHoldSeconds = MIN_COLUMN_DELAY;
    private boolean finished = false;

    public GlacierColumnSweep(GameSession session, List<Integer> columnsRightToLeft, ZombossController controller) {
        this.session = session;
        this.controller = controller;
        this.remainingColumns = new ArrayDeque<>(columnsRightToLeft);
        advanceToNextColumn();
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
                freezeRow(currentColumn, nextRowToFreeze);
                nextRowToFreeze++;

                if (checkFullLawnCoverage()) {
                    finish();
                    session.concludeDefeat();
                    return;
                }
            }
            return;
        }

        holdTimer += delta;
        if (holdTimer < columnHoldSeconds) return;
        holdTimer = 0;

        if (columnWasDamaged()) {
            finish();
            return;
        }

        advanceToNextColumn();
    }

    private boolean columnWasDamaged() {
        for (GlacierBlock block : currentColumnBlocks) {
            if (block.isDamaged()) {
                return true;
            }
        }
        return false;
    }

    private void advanceToNextColumn() {
        Integer col = remainingColumns.poll();
        if (col == null) {
            finish();
            currentColumnBlocks = List.of();
            return;
        }
        currentColumn = col;
        currentColumnBlocks = new ArrayList<>();
        nextRowToFreeze = 0;
        rowTimer = 0;
        holdTimer = 0;
        columnHoldSeconds = computeColumnDelay(col);
    }

    /**
     * Randomized delay before advancing past this column, scaled so columns
     * closer to the boss get a shorter delay and columns farther away get a
     * longer one — always clamped to [MIN_COLUMN_DELAY, MAX_COLUMN_DELAY].
     */
    private double computeColumnDelay(int col) {
        int bossCol = controller.getPrimaryCol();
        int maxDistance = Math.max(1, bossCol);
        int distance = Math.max(0, bossCol - col);
        double t = Math.min(1.0, distance / (double) maxDistance);

        double base = MIN_COLUMN_DELAY + t * (MAX_COLUMN_DELAY - MIN_COLUMN_DELAY);
        double jitter = (RAND.nextDouble() - 0.5) * COLUMN_DELAY_JITTER;

        return Math.max(MIN_COLUMN_DELAY, Math.min(MAX_COLUMN_DELAY, base + jitter));
    }

    private void freezeRow(int col, int row) {
        // A glacier block landing on a plant destroys it outright.
        session.removePlantAt(col, row);

        var tile = session.getLawn().getTile(row, col);
        if (tile == null) return;

        TileType previousType = tile.getType();
        tile.setType(TileType.Frozen);

        GlacierBlock block = new GlacierBlock(GLACIER_BLOCK_HP, previousType, null, BURIED_ZOMBIE_ALIAS);
        block.setPosition(Vec2.of(col, row));

        Cell cell = session.getLawn().getCell(row, col);
        if (cell != null) {
            cell.setStructure(block);
        }
        session.registerStructure(block);
        currentColumnBlocks.add(block);
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