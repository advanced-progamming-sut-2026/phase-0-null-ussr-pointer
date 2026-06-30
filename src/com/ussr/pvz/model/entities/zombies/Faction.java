package com.ussr.pvz.model.entities.zombies;

import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.targeting.PlantSideTargetFinder;
import com.ussr.pvz.model.entities.zombies.targeting.TargetFinder;
import com.ussr.pvz.model.entities.zombies.targeting.ZombieSideTargetFinder;

public enum Faction {
    ZOMBIES(new PlantSideTargetFinder()),
    PLANTS(new ZombieSideTargetFinder());

    private final TargetFinder targetFinder;

    Faction(TargetFinder targetFinder) {
        this.targetFinder = targetFinder;
    }

    public Damageable findTarget(Zombie self, GameSession session) {
        return targetFinder.findTarget(self, session);
    }
}