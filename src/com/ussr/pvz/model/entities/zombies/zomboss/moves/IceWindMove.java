package com.ussr.pvz.model.entities.zombies.zomboss.moves;

import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.PlantFreezer;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossMove;

import java.util.Random;

public class IceWindMove implements ZombossMove {
    @Override
    public void execute(ZombossController controller, GameSession session) {
        Random random = new Random();
        int numRows = session.getLawn().getRows();

        int r1 = random.nextInt(numRows);
        int r2 = random.nextInt(numRows);
        while (r2 == r1 && numRows > 1) {
            r2 = random.nextInt(numRows);
        }

        for (Plant plant : session.getPlants()) {
            if (plant.getLocation().y() == r1 || plant.getLocation().y() == r2) {
                PlantFreezer.applyFreeze(session, plant, 1);
            }
        }

        for (int col = 0; col < session.getLawn().getCols(); col++) {
            if (session.getLawn().getTile(r1, col) != null) session.getLawn().getTile(r1, col).setType(TileType.Frozen);
            if (session.getLawn().getTile(r2, col) != null) session.getLawn().getTile(r2, col).setType(TileType.Frozen);
        }
    }
}