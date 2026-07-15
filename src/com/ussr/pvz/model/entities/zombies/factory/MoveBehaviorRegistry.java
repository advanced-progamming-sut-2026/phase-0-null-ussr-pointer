package com.ussr.pvz.model.entities.zombies.factory;

import com.ussr.pvz.model.entities.zombies.move.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MoveBehaviorRegistry {

    private static final Map<String, MoveBehaviorFactory> FACTORIES = new HashMap<>();

    static {
        register("NormalWalk", (params, data) -> new NormalWalk());

        register("SprintMove", (params, data) -> {
            if (params == null || params.isEmpty()) return new SprintMove();
            double baseSprintMultiplier = BehaviorSpec.getDouble(params, "baseSprintMultiplier", 1.0);
            double enrageMultiplier = BehaviorSpec.getDouble(params, "enrageMultiplier", 4.0);
            boolean enragesOnArmorLoss = BehaviorSpec.getBoolean(params, "enragesOnArmorLoss", false);
            return new SprintMove(baseSprintMultiplier, enrageMultiplier, enragesOnArmorLoss);
        });

        register("PusherMove", (params, data) -> new PusherMove());
        register("ProspectorMove", (params, data) -> new ProspectorMove());

        register("JumpMove", (params, data) -> {
            double addChance = BehaviorSpec.getDouble(data, "AddRandomChanceForJumpPerGridWalked", 0.0);
            double cooldown = BehaviorSpec.getDouble(data, "CooldownSecondsUntilNextJumpAvailable", 0.0);
            double initChance = BehaviorSpec.getDouble(data, "InitialSetRandomChanceForJump", 0.0);
            double resetChance = BehaviorSpec.getDouble(data, "LandedResetRandomChanceForJump", 0.0);

            List<String> plantsToFlyOver = null;
            if (data.containsKey("PlantsToFlyOver")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) data.get("PlantsToFlyOver");
                plantsToFlyOver = (List<String>) map.get("List");
            }

            return new JumpMove(addChance, cooldown, initChance, resetChance, plantsToFlyOver);
        });

        register("SnorkelMove", (params, data) -> new SnorkelMove());
        register("StationaryMove", (params, data) -> new StationaryMove());
    }

    private MoveBehaviorRegistry() {}

    public static void register(String type, MoveBehaviorFactory factory) {
        FACTORIES.put(type, factory);
    }

    public static MoveBehavior create(Object rawSpec, Map<String, Object> zombieData) {
        BehaviorSpec spec = BehaviorSpec.parse(rawSpec);
        MoveBehaviorFactory factory = FACTORIES.get(spec.getType());
        if (factory == null) {
            throw new IllegalArgumentException("Unknown move behavior type: " + spec.getType());
        }
        return factory.create(spec.params(), zombieData);
    }
}