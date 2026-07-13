package com.ussr.pvz.service.game;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;

public class ZombieService {

    public boolean processEating(Zombie zombie, GameSession session) {
        if (session == null || session.getLawn() == null || zombie == null) return false;

        int row = (int) zombie.getPosition().y();

        // Calculate the grid column slightly ahead of the zombie to detect collision
        int col = (int) Math.floor(zombie.getPosition().x() - 0.2);

        if (col >= 0 && col < session.getLawn().getCols()) {
            Cell cell = session.getLawn().getCell(row, col);
            if (cell != null) {
                // Calculate damage for this specific tick (0.1 seconds)
                int damagePerTick = (int) (zombie.getEatDps() * GameClock.SECONDS_PER_TICK);

                // 1. Check for Plants
                Plant plant = cell.getPlant();
                if (plant != null && plant.isAlive()) {
                    plant.takeDamage(damagePerTick);
                    session.notifyPlantDamaged(plant, damagePerTick);
                    return true; // Return true to signal the zombie is blocked and eating
                }

                // 2. Check for blocking Structures (like Vases, Ice Blocks, or Graves)
                var structure = cell.getInteractableStructure();
                if (structure != null && structure.isAlive() && structure instanceof Damageable dmgStructure) {
                    dmgStructure.takeDamage(damagePerTick);
                    return true; // Return true to signal the zombie is blocked and eating
                }
            }
        }
        return false; // The path is clear
    }
}