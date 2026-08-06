package com.ussr.pvz.model.entities.zombies.zomboss;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.defense.DefenseBehavior;

public class ZombossDefense implements DefenseBehavior {
    private final ZombossController controller;

    public ZombossDefense(ZombossController controller) {
        this.controller = controller;
    }

    @Override
    public int handleDamage(Zombie zombie, int rawDamage, Object damageSource, GameSession session) {
        controller.applyDamage(rawDamage, session);
        return 0;
    }
}