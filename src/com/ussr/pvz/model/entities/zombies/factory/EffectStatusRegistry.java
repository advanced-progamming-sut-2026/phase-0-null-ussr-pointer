package com.ussr.pvz.model.entities.zombies.factory;

import com.ussr.pvz.model.entities.zombies.effect.*;

import java.util.HashMap;
import java.util.Map;

public final class EffectStatusRegistry {

    private static final Map<String, EffectStatusFactory> FACTORIES = new HashMap<>();

    static {
        register("TombRaiserEffect", params -> new TombRaiserEffect(
                BehaviorSpec.getDouble(params, "cooldown", 6.0),
                BehaviorSpec.getInt(params, "numTombsToSpawn", 2)
        ));

        register("SunThief", params -> new SunThief(
                BehaviorSpec.getBoolean(params, "isBankThief", false),
                BehaviorSpec.getInt(params, "maxSunsToSteal", 250),
                BehaviorSpec.getDouble(params, "dropRatioOnDeath", 1.0),
                BehaviorSpec.getDouble(params, "chargingTime", 5.0),
                BehaviorSpec.getInt(params, "laserDamage", 1800)
        ));

        register("SpinEffect", params -> new SpinEffect());

        register("FishermanEffect", params -> new FishermanEffect(
                BehaviorSpec.getDouble(params, "delayBetweenCasting", 2.5)
        ));

        register("OctopusThrowEffect", params -> new OctopusThrowEffect(
                BehaviorSpec.getDouble(params, "throwCooldown", 5.0)
        ));

        register("IceAgeHunterEffect", params -> new IceAgeHunterEffect(
                BehaviorSpec.getDouble(params, "throwCooldown", 4.0)
        ));

        register("FireEffect", params -> new FireEffect(
                BehaviorSpec.getDouble(params, "reach", 1.0)
        ));

        register("WizardEffect", params -> new WizardEffect(
                BehaviorSpec.getDouble(params, "transformInterval", 8.0)
        ));

        register("KingBuffEffect", params -> new KingBuffEffect(
                BehaviorSpec.getDouble(params, "delayBetweenKnighting", 2.5)
        ));

        register("PianistEffect", params -> new PianistEffect(
                BehaviorSpec.getDouble(params, "danceInterval", 2.5)
        ));
    }

    private EffectStatusRegistry() {
    }

    public static void register(String type, EffectStatusFactory factory) {
        FACTORIES.put(type, factory);
    }

    public static EffectStatus createOrNull(Object rawSpecOrNull) {
        if (rawSpecOrNull == null) return null;
        BehaviorSpec spec = BehaviorSpec.parse(rawSpecOrNull);
        EffectStatusFactory factory = FACTORIES.get(spec.getType());
        if (factory == null) {
            throw new IllegalArgumentException("Unknown effect status type: " + spec.getType());
        }
        return factory.create(spec.params());
    }
}