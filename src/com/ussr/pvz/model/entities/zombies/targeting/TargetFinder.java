package com.ussr.pvz.model.entities.zombies.targeting;

import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;

public interface TargetFinder {
    Damageable findTarget(Zombie self, GameSession session);
}