package com.ussr.pvz.model.entities.zombies.zomboss;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.entities.zombies.factory.BehaviorSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ZombossFactory {

    private ZombossFactory() {
    }

    public static ZombossController spawn(String alias, int primaryRow, int col, GameSession session) {
        Map<String, Object> data = ZombieFactory.getBlueprint(alias);
        if (data == null) {
            throw new IllegalArgumentException("Unknown zomboss alias: " + alias);
        }

        int occupiedRows = Math.max(1, BehaviorSpec.getInt(data, "ZombossOccupiedRows", 2));
        int occupiedCols = Math.max(1, BehaviorSpec.getInt(data, "ZombossOccupiedCols", 2));

        Zombie primary = ZombieFactory.create(alias, primaryRow, col);

        List<Zombie> mirrors = new ArrayList<>();
        for (int r = 0; r < occupiedRows; r++) {
            for (int c = 0; c < occupiedCols; c++) {
                Zombie mirror = ZombieFactory.create(alias, primaryRow + r, col - c);
                mirror.setBossMirror(true);
                mirrors.add(mirror);
            }
        }

        ZombossController controller = new ZombossController(primary, mirrors, data);
        primary.setEffectStatus(controller);
        primary.setDefenseBehavior(new ZombossDefense(controller));
        primary.setZombossController(controller);
        session.spawnZombie(primary);

        for (Zombie mirror : mirrors) {
            mirror.setDefenseBehavior(new ZombossDefense(controller));
            mirror.setZombossController(controller);
            session.spawnZombie(mirror);
        }

        controller.spawnGlacierShield(session);

        return controller;
    }
}