package com.ussr.pvz.service.minigame;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.level.behavior.IZombieBehavior;
import com.ussr.pvz.model.level.behavior.LevelBehavior;

public class IZombieService {

    public String placeZombie(String zombieAlias, int x, int y) {
        GameSession session = App.getGameSession();
        if (session == null || session.getLawn() == null) return "Game session not active.";

        LevelBehavior behavior = (LevelBehavior) session.getLevel().getBehavior();
        if (!(behavior instanceof IZombieBehavior iZombieBehavior)) {
            return "Current level is not an i,Zombie minigame.";
        }

        // Validate placement area (must be to the right of the red line)
        if (x < iZombieBehavior.getRedLineColumn()) {
            return "You can only spawn zombies to the right of the red line (column " + iZombieBehavior.getRedLineColumn() + " or greater).";
        }

        // Validate Cost
        int cost = ZombieFactory.getZombieCost(zombieAlias);
        if (session.getSunCount() < cost) {
            return "Not enough sun! " + zombieAlias + " costs " + cost + " sun.";
        }

        try {
            // Spawn the zombie
            Zombie zombie = ZombieFactory.create(zombieAlias, y, x);

            // Deduct sun and add to session
            session.spendSun(cost);
            session.spawnZombie(zombie);

            return "Spawned " + zombieAlias + " at (" + x + ", " + y + ").";
        } catch (IllegalArgumentException e) {
            return "Invalid zombie type: " + zombieAlias;
        }
    }
}