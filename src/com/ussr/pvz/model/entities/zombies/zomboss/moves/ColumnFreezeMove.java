package com.ussr.pvz.model.entities.zombies.zomboss.moves;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.plants.PlantFreezer;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossMove;

import java.util.Random;

public class ColumnFreezeMove implements ZombossMove {
    @Override
    public void execute(ZombossController controller, GameSession session) {
        Random random = new Random();
        int col = random.nextInt(session.getLawn().getCols());

        for (int row = 0; row < session.getLawn().getRows(); row++) {
            Cell cell = session.getLawn().getCell(row, col);
            if (cell != null && cell.getPlant() != null) {
                PlantFreezer.applyFreeze(session, cell.getPlant(), 3);
            }
            if (session.getLawn().getTile(row, col) != null) {
                session.getLawn().getTile(row, col).setType(TileType.Frozen);
            }
            try {
                Zombie frozenZombie = ZombieFactory.create("ZombieArmor1", row, col);
                session.spawnZombie(frozenZombie);
            } catch (Exception ignored) {}
        }
    }
}