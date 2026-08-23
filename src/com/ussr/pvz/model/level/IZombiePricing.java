package com.ussr.pvz.model.level;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.level.behavior.MultiplayerIZombieBehavior;

/**
 * Resolves the sun price used when a player buys a zombie in i,Zombie.
 * Multiplayer levels configure their own prices through the allowed-zombie
 * entries; offline modes continue to use the zombie blueprint cost.
 */
public final class IZombiePricing {

    private IZombiePricing() {
    }

    public static int getCost(GameSession session, String zombieAlias) {
        if (zombieAlias == null) {
            return Integer.MAX_VALUE;
        }

        if (session != null
                && session.getLevel() != null
                && session.getLevel().getBehavior()
                instanceof MultiplayerIZombieBehavior) {
            return session.getLevel().getAllowedZombies().stream()
                    .filter(allowed -> allowed.id().equalsIgnoreCase(zombieAlias))
                    .mapToInt(Level.AllowedZombie::weight)
                    .filter(cost -> cost > 0)
                    .findFirst()
                    .orElse(Integer.MAX_VALUE);
        }

        return ZombieFactory.getZombieCost(zombieAlias);
    }
}
