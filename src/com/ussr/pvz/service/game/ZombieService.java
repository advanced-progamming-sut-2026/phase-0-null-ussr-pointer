package com.ussr.pvz.service.game;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;

public class ZombieService {

    // Removed processEating() as it is fully handled by Zombie.tick() and ChompAttack.java

    public void processSpecialAbilities(Zombie zombie, GameSession session) {
        // Special abilities are fully delegated to the Zombie's EffectStatus
        // which evaluate and trigger autonomously during the Zombie's tick() cycle.
    }
}