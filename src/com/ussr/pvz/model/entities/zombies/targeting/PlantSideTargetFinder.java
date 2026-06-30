package com.ussr.pvz.model.entities.zombies.targeting;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;

public class PlantSideTargetFinder implements TargetFinder {

    @Override
    public Damageable findTarget(Zombie self, GameSession session) {
        Cell cell = self.getCurrentCell(session);
        if (cell == null) return null;

        Plant plant = cell.getPlant();
        if (plant != null && plant.isAlive() && !plant.isCat()) return plant;

        return null;
    }
}