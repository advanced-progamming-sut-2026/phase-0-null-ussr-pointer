package com.ussr.pvz.service.minigame;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.level.behavior.CouchIZombieBehavior;
import com.ussr.pvz.model.level.behavior.IZombieBehavior;
import com.ussr.pvz.model.level.behavior.LevelBehavior;

public class IZombieService {

    public String placeZombie(String zombieAlias, int x, int y) {
        GameSession session = App.getGameSession();
        if (session == null || session.getLawn() == null) return "Game session not active.";

        LevelBehavior behavior = session.getLevel().getBehavior();
        int redLineColumn;
        if (behavior instanceof IZombieBehavior iZombieBehavior) {
            redLineColumn = iZombieBehavior.getRedLineColumn();
        } else if (behavior instanceof CouchIZombieBehavior couchBehavior) {
            redLineColumn = couchBehavior.getRedLineColumn();
        } else {
            return "Current level is not an i,Zombie minigame.";
        }

        // 1. Prevent spawning the special Sun-Producing Zombie
        if (zombieAlias.equalsIgnoreCase("SunProducerZombie")) {
            return "You cannot manually place the Sun-Producing Zombie!";
        }

        // Optional: Ensure the zombie is part of the 5 allowed zombies for this specific stage

        boolean isAllowed = session.getLevel().getAllowedZombies().stream()
                .anyMatch(z -> z.id().equalsIgnoreCase(zombieAlias));
        if (!isAllowed) {
            return zombieAlias + " is not available in this stage.";
        }


        // 2. Validate placement area (must be to the right of the red line)
        if (x < redLineColumn) {
            return "You can only spawn zombies to the right of the red line (column " +
                    redLineColumn + " or greater).";
        }

        // 3. Validate Cost
        int cost = ZombieFactory.getZombieCost(zombieAlias);
        if (behavior instanceof CouchIZombieBehavior couchBehavior) {
            if (couchBehavior.getZombieSun() < cost) {
                return "Not enough sun! " + zombieAlias + " costs " + cost + " sun.";
            }
        } else if (session.getSunCount() < cost) {
            return "Not enough sun! " + zombieAlias + " costs " + cost + " sun.";
        }

        try {
            // 4. Spawn the zombie
            Zombie zombie = ZombieFactory.create(zombieAlias, y, x);

            // 5. Deduct sun and add to session
            if (behavior instanceof CouchIZombieBehavior couchBehavior) {
                couchBehavior.spendZombieSun(cost);
            } else {
                session.spendSun(cost);
            }
            session.spawnZombie(zombie);

            return "Spawned " + zombieAlias + " at (" + x + ", " + y + ").";
        } catch (IllegalArgumentException e) {
            return "Invalid zombie type: " + zombieAlias;
        }
    }
}