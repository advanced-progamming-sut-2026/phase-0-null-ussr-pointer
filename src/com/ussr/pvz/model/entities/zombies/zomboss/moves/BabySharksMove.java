package com.ussr.pvz.model.entities.zombies.zomboss.moves;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.projectiles.BabySharkProjectile;
import com.ussr.pvz.model.entities.zombies.factory.BehaviorSpec;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossMove;
import com.ussr.pvz.model.util.Vec2;

import java.util.*;

public class BabySharksMove implements ZombossMove {
    private final Map<Integer, BabySharkProjectile> rowSharks = new HashMap<>();

    public BabySharksMove(Map<String, Object> params) {
    }

    @Override
    public void execute(ZombossController controller, GameSession session) {
        Lawn lawn = session.getLawn();
        if (lawn == null) return;

        int rows = lawn.getRows();
        int cols = lawn.getCols();

        for (int row = 0; row < rows; row++) {
            BabySharkProjectile shark = rowSharks.get(row);

            if (shark == null || !shark.isAlive()) {
                int restCol = rightmostWaterColumn(lawn, row, cols);
                if (restCol < 0) continue; // no water in this row
                shark = new BabySharkProjectile(Vec2.of(restCol, row), row);
                rowSharks.put(row, shark);
                session.addZombieProjectile(shark);
            }

            if (!shark.isIdle()) continue; // already mid-attack

            int[] target = findWaterPlantInRow(lawn, row, cols);
            if (target == null) continue; // nothing to hit — stays idle

            int targetCol = target[1];
            shark.activate(Vec2.of(targetCol, row), row, targetCol);
        }
    }

    private int rightmostWaterColumn(Lawn lawn, int row, int cols) {
        for (int col = cols - 1; col >= 0; col--) {
            Cell cell = lawn.getCell(row, col);
            if (cell != null && cell.getTile() != null
                    && cell.getTile().getType() == TileType.Water) {
                return col;
            }
        }
        return -1;
    }

    private int[] findWaterPlantInRow(Lawn lawn, int row, int cols) {
        for (int col = 0; col < cols; col++) {
            Cell cell = lawn.getCell(row, col);
            if (cell == null || cell.getTile() == null
                    || cell.getTile().getType() != TileType.Water) {
                continue;
            }
            if (cell.getPlant() != null) {
                return new int[]{row, col};
            }
        }
        return null;
    }
}