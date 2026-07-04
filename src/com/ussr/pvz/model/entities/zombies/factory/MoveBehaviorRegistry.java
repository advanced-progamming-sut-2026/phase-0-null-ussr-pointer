package com.ussr.pvz.model.entities.zombies.factory;

import com.ussr.pvz.model.entities.zombies.move.MoveBehavior;
import com.ussr.pvz.model.entities.zombies.move.NormalWalk;
import com.ussr.pvz.model.entities.zombies.move.SprintMove;
import com.ussr.pvz.model.entities.zombies.move.PusherMove;
import com.ussr.pvz.model.entities.zombies.move.ProspectorMove;
import com.ussr.pvz.model.entities.zombies.move.JumpMove;
import com.ussr.pvz.model.entities.zombies.move.SnorkelMove;

import java.util.HashMap;
import java.util.Map;

public final class MoveBehaviorRegistry {

    private static final Map<String, MoveBehaviorFactory> FACTORIES = new HashMap<>();

    static {
        register("NormalWalk", params -> new NormalWalk());

        register("SprintMove", params -> params.containsKey("baseSprintMultiplier")
                ? new SprintMove(BehaviorSpec.getDouble(params, "baseSprintMultiplier", 1.0))
                : new SprintMove());

        register("PusherMove", params -> new PusherMove());

        register("ProspectorMove", params -> new ProspectorMove());

        register("JumpMove", params -> new JumpMove());

        register("SnorkelMove", params -> new SnorkelMove());
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