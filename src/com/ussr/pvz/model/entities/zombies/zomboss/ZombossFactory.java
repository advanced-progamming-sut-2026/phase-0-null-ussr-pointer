package com.ussr.pvz.model.entities.zombies.zomboss;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;

import java.util.Map;

public final class ZombossFactory {

    private ZombossFactory() {
    }

    public static ZombossController spawn(String alias, int primaryRow, int col, GameSession session) {
        Map<String, Object> data = ZombieFactory.getBlueprint(alias);
        if (data == null) {
            throw new IllegalArgumentException("Unknown zomboss alias: " + alias);
        }

        Zombie primary = ZombieFactory.create(alias, primaryRow, col);
        Zombie mirror = ZombieFactory.create(alias, primaryRow + 1, col);

        ZombossController controller = new ZombossController(primary, mirror, data);
        primary.setEffectStatus(controller);
        primary.setDefenseBehavior(new ZombossDefense(controller));
        mirror.setDefenseBehavior(new ZombossDefense(controller));

        session.spawnZombie(primary);
        session.spawnZombie(mirror);

        return controller;
    }
}