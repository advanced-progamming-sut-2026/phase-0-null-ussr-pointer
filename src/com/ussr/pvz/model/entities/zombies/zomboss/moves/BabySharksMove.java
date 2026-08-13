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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BabySharksMove implements ZombossMove {
    private final int count;
    private final Random random = new Random();

    public BabySharksMove(Map<String, Object> params) {
        this.count = BehaviorSpec.getInt(params, "count", 4);
    }

    @Override
    public void execute(ZombossController controller, GameSession session) {
        Lawn lawn = session.getLawn();
        if (lawn == null) return;

        List<int[]> waterCellsWithPlant = new ArrayList<>();
        List<int[]> waterCells = new ArrayList<>();

        for (int row = 0; row < lawn.getRows(); row++) {
            for (int col = 0; col < lawn.getCols(); col++) {
                Cell cell = lawn.getCell(row, col);
                if (cell == null || cell.getTile() == null
                        || cell.getTile().getType() != TileType.Water) {
                    continue;
                }

                waterCells.add(new int[]{row, col});
                if (cell.getPlant() != null) {
                    waterCellsWithPlant.add(new int[]{row, col});
                }
            }
        }

        if (waterCells.isEmpty()) {
            return;
        }

        for (int i = 0; i < count; i++) {
            int[] target = !waterCellsWithPlant.isEmpty()
                    ? waterCellsWithPlant.remove(random.nextInt(waterCellsWithPlant.size()))
                    : waterCells.get(random.nextInt(waterCells.size()));

            int row = target[0];
            int col = target[1];

            BabySharkProjectile projectile = new BabySharkProjectile(controller.getPrimary().getPosition(), Vec2.of(col, row), row, col);
            session.addZombieProjectile(projectile);
        }
    }
}