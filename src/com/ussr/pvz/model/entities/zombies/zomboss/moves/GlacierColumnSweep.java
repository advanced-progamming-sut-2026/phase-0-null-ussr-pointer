package com.ussr.pvz.model.entities.zombies.zomboss.moves;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.GlacierBlock;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.Tickable;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.plants.PlantFreezer;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class GlacierColumnSweep implements Tickable {
    private static final int FREEZE_STACKS = 3;
    private static final int GLACIER_BLOCK_HP = 600;
    private static final String BURIED_ZOMBIE_ALIAS = "ZombieArmor1";

    private static final double ROW_ADVANCE_SECONDS = 1.26;
    private static final double COLUMN_HOLD_SECONDS = 3.5;

    private final GameSession session;
    private final Deque<Integer> remainingColumns;
    private List<GlacierBlock> currentColumnBlocks = new ArrayList<>();
    private int currentColumn = -1;
    private int nextRowToFreeze = 0;
    private double rowTimer = 0;
    private double holdTimer = 0;
    private boolean finished = false;

    public GlacierColumnSweep(GameSession session, List<Integer> columnsRightToLeft) {
        this.session = session;
        this.remainingColumns = new ArrayDeque<>(columnsRightToLeft);
        advanceToNextColumn();
    }

    @Override
    public void update(float delta) {
        if (finished) return;

        int totalRows = session.getLawn().getRows();

        if (nextRowToFreeze < totalRows) {
            rowTimer += delta;
            if (rowTimer >= ROW_ADVANCE_SECONDS) {
                rowTimer = 0;
                freezeRow(currentColumn, nextRowToFreeze);
                nextRowToFreeze++;
            }
            return;
        }

        holdTimer += delta;
        if (holdTimer < COLUMN_HOLD_SECONDS) return;
        holdTimer = 0;

        if (columnWasDamaged()) {
            finished = true;
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
            finished = true;
            currentColumnBlocks = List.of();
            return;
        }
        currentColumn = col;
        currentColumnBlocks = new ArrayList<>();
        nextRowToFreeze = 0;
        rowTimer = 0;
        holdTimer = 0;
    }

    private void freezeRow(int col, int row) {
        Cell cell = session.getLawn().getCell(row, col);
        if (cell != null && cell.getPlant() != null) {
            PlantFreezer.applyFreeze(session, cell.getPlant(), FREEZE_STACKS);
        }

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
        currentColumnBlocks.add(block);
    }
}