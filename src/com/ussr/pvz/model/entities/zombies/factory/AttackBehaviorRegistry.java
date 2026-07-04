package com.ussr.pvz.model.entities.zombies.factory;

import com.ussr.pvz.model.entities.zombies.attack.AttackBehavior;
import com.ussr.pvz.model.entities.zombies.attack.ChompAttack;
import com.ussr.pvz.model.entities.zombies.attack.CrushAttack;
import com.ussr.pvz.model.entities.zombies.attack.SmashAttack;

import java.util.HashMap;
import java.util.Map;

public final class AttackBehaviorRegistry {

    private static final Map<String, AttackBehaviorFactory> FACTORIES = new HashMap<>();

    static {
        register("ChompAttack", (params, data) -> new ChompAttack());

        register("CrushAttack", (params, data) -> {
            int fallbackDamage = BehaviorSpec.getInt(data, "EatDPS", 0);
            int crushDamage = BehaviorSpec.getInt(params, "crushDamage", fallbackDamage);
            return new CrushAttack(crushDamage);
        });

        register("SmashAttack", (params, data) -> {
            int fallbackDamage = BehaviorSpec.getInt(data, "SmashDamage", 0);
            int smashDamage = BehaviorSpec.getInt(params, "smashDamage", fallbackDamage);
            double windupDuration = BehaviorSpec.getDouble(params, "windupDuration", 0.0);
            boolean isOneTime = BehaviorSpec.getBoolean(params, "isOneTime", false);
            double speedScaleAfter = BehaviorSpec.getDouble(params, "speedScaleAfter", 1.0);
            return new SmashAttack(smashDamage, windupDuration, isOneTime, speedScaleAfter);
        });
    }

    private AttackBehaviorRegistry() {
    }

    public static void register(String type, AttackBehaviorFactory factory) {
        FACTORIES.put(type, factory);
    }

    public static AttackBehavior create(Object rawSpec, Map<String, Object> zombieData) {
        BehaviorSpec spec = BehaviorSpec.parse(rawSpec);
        AttackBehaviorFactory factory = FACTORIES.get(spec.getType());
        if (factory == null) {
            throw new IllegalArgumentException("Unknown attack behavior type: " + spec.getType());
        }
        return factory.create(spec.params(), zombieData);
    }
}