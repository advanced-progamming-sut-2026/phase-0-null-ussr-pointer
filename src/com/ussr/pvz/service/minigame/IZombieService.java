package com.ussr.pvz.service.minigame;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.level.IZombiePricing;
import com.ussr.pvz.model.level.behavior.CouchIZombieBehavior;
import com.ussr.pvz.model.level.behavior.IZombieBehavior;
import com.ussr.pvz.model.level.behavior.LevelBehavior;
import com.ussr.pvz.model.level.behavior.MultiplayerIZombieBehavior;

public class IZombieService {

    public String placeZombie(String zombieAlias, int x, int y) {
        GameSession session = App.getGameSession();
        if (session == null || session.getLawn() == null) return "Game session not active.";
        LevelBehavior behavior = session.getLevel().getBehavior();
        int redLineColumn;
        switch (behavior) {
            case IZombieBehavior iZombieBehavior -> redLineColumn = iZombieBehavior.getRedLineColumn();
            case CouchIZombieBehavior couchBehavior -> redLineColumn = couchBehavior.getRedLineColumn();
            case MultiplayerIZombieBehavior multiplayerBehavior ->
                    redLineColumn = multiplayerBehavior.getRedLineColumn();
            case null, default -> {
                return "Current level is not an i,Zombie minigame.";
            }
        }
        if (zombieAlias.equalsIgnoreCase("SunProducerZombie")) {
            return "You cannot manually place the Sun-Producing Zombie!";
        }
        boolean isAllowed = session.getLevel().getAllowedZombies().stream()
                .anyMatch(z -> z.id().equalsIgnoreCase(zombieAlias));
        if (!isAllowed) {
            return zombieAlias + " is not available in this stage.";
        }
        if (x < redLineColumn) {
            return "You can only spawn zombies to the right of the red line (column " +
                    redLineColumn + " or greater).";
        }
        int cost = IZombiePricing.getCost(session, zombieAlias);
        if (behavior instanceof CouchIZombieBehavior couchBehavior) {
            if (couchBehavior.getZombieSun() < cost) {
                return "Not enough sun! " + zombieAlias + " costs " + cost + " sun.";
            }
        } else if (session.getSunCount() < cost) {
            return "Not enough sun! " + zombieAlias + " costs " + cost + " sun.";
        }
        try {
            Zombie zombie = ZombieFactory.create(zombieAlias, y, x);
            if (behavior instanceof CouchIZombieBehavior couchBehavior)
                couchBehavior.spendZombieSun(cost);
            else
                session.spendSun(cost);

            session.spawnZombie(zombie);
            return "Spawned " + zombieAlias + " at (" + x + ", " + y + ").";
        } catch (IllegalArgumentException e) {
            return "Invalid zombie type: " + zombieAlias;
        }
    }
}
