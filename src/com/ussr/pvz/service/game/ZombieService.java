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
        int col = (int) Math.floor(zombie.getPosition().x() - 0.2);

        if (col >= 0 && col < session.getLawn().getCols()) {
            Cell cell = session.getLawn().getCell(row, col);
            if (cell != null) {
                int damagePerTick = (int) (zombie.getEatDps() * GameClock.SECONDS_PER_TICK);

                Plant plant = cell.getPlant();
                if (plant != null && plant.isAlive()) {
                    plant.takeDamage(damagePerTick);
                    session.notifyPlantDamaged(plant, damagePerTick);
                    return true;
                }

                var structure = cell.getInteractableStructure();
                if (structure != null && structure.isAlive() && structure instanceof Damageable dmgStructure) {
                    dmgStructure.takeDamage(damagePerTick);
                    return true;
                }
            }
        }
        return false;
    }

    public void processSpecialAbilities(Zombie zombie, GameSession session) {
        if (session == null || session.getLawn() == null || zombie == null || !zombie.isAlive()) return;

        // TODO: [TURQUOISE ZOMBIE]
        // 1. If zombie has SunThiefEffect, check internal accumulator timer.
        // 2. Check radius (4 tiles). If plant present, drain 25 sun/sec (2.5 sun/tick) from session.spendSun().
        // 3. After 5 seconds of active draining, execute laser raycast logic: instantly kill all Plant entities in 4 tiles ahead.

        // TODO: [OCTOPUS ZOMBIE]
        // 1. Check throw cooldown timer.
        // 2. Select random alive Plant entity on the board.
        // 3. Instantiate ArcMove Projectile toward Plant.
        // 4. On ProjectileHit resolution, wrap target Plant in an OctopusWrap InteractableStructure.

        // TODO: [TOMB RAISER]
        // 1. Check internal bone-throw cooldown timer.
        // 2. Select two random empty Cells on the Lawn.
        // 3. Instantiate and register Grave structures at those coordinates.

        // TODO: [FISHERMAN ZOMBIE]
        // 1. Check hook cooldown timer.
        // 2. Locate right-most Plant in the current lane.
        // 3. Update the Plant's Location/Vec2 state by +1 X (right) and update the Lawn's Cell pointers.
        // 4. If the plant is pulled off the board or into the Fisherman's hitbox, destroy the plant.
    }
}