package com.ussr.pvz.model.entities.zombies.factory;

import com.ussr.pvz.model.entities.zombies.move.MoveBehavior;
import com.ussr.pvz.model.entities.zombies.move.NormalWalk;
import com.ussr.pvz.model.entities.zombies.move.SprintMove;
import com.ussr.pvz.model.entities.zombies.move.PusherMove;
import com.ussr.pvz.model.entities.zombies.move.ProspectorMove;
import com.ussr.pvz.model.entities.zombies.move.JumpMove;
import com.ussr.pvz.model.entities.zombies.move.SnorkelMove;
import com.ussr.pvz.model.entities.zombies.move.StationaryMove;

import java.util.HashMap;
import java.util.Map;

public final class MoveBehaviorRegistry {

    private static final Map<String, MoveBehaviorFactory> FACTORIES = new HashMap<>();

    static {
        register("NormalWalk", params -> new NormalWalk());

        register("SprintMove", params -> {
            if (params == null || params.isEmpty()) return new SprintMove();

            double baseSprintMultiplier = BehaviorSpec.getDouble(params, "baseSprintMultiplier", 1.0);
            double enrageMultiplier = BehaviorSpec.getDouble(params, "enrageMultiplier", 4.0);
            boolean enragesOnArmorLoss = BehaviorSpec.getBoolean(params, "enragesOnArmorLoss", false);

            return new SprintMove(baseSprintMultiplier, enrageMultiplier, enragesOnArmorLoss);
        });

        register("PusherMove", params -> new PusherMove());

        register("ProspectorMove", params -> new ProspectorMove());

        register("JumpMove", params -> new JumpMove());

        register("SnorkelMove", params -> new SnorkelMove());

        register("StationaryMove", params -> new StationaryMove());
    }

    private MoveBehaviorRegistry() {
    }

    public static void register(String type, MoveBehaviorFactory factory) {
        FACTORIES.put(type, factory);
    }

    public static MoveBehavior create(Object rawSpec) {
        BehaviorSpec spec = BehaviorSpec.parse(rawSpec);
        MoveBehaviorFactory factory = FACTORIES.get(spec.getType());
        if (factory == null) {
            throw new IllegalArgumentException("Unknown move behavior type: " + spec.getType());
        }
        return factory.create(spec.params());
    }
}