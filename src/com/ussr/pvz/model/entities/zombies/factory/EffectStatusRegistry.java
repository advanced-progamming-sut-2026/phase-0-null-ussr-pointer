package com.ussr.pvz.model.entities.zombies.factory;

import com.ussr.pvz.model.entities.zombies.effect.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EffectStatusRegistry {

    private static final Map<String, EffectStatusFactory> FACTORIES = new HashMap<>();

    static {
        register("TombRaiserEffect", (params, data) -> new TombRaiserEffect(
                BehaviorSpec.getDouble(params, "cooldown", 6.0),
                BehaviorSpec.getInt(params, "numTombsToSpawn", 2)
        ));

        register("SunThief", (params, data) -> new SunThief(
                BehaviorSpec.getBoolean(params, "isBankThief", false),
                BehaviorSpec.getInt(params, "maxSunsToSteal", 250),
                BehaviorSpec.getDouble(params, "dropRatioOnDeath", 1.0),
                BehaviorSpec.getDouble(params, "chargingTime", 5.0),
                BehaviorSpec.getInt(params, "laserDamage", 1800)
        ));

        register("SpinEffect", (params, data) -> new SpinEffect());

        register("FishermanEffect", (params, data) -> new FishermanEffect(
                BehaviorSpec.getDouble(params, "delayBetweenCasting", 2.5)
        ));

        register("OctopusThrowEffect", (params, data) -> new OctopusThrowEffect(
                BehaviorSpec.getDouble(params, "throwCooldown", 5.0)
        ));

        register("IceAgeHunterEffect", (params, data) -> new IceAgeHunterEffect(
                BehaviorSpec.getDouble(params, "throwCooldown", 4.0)
        ));

        register("FireEffect", (params, data) -> new FireEffect(
                BehaviorSpec.getDouble(params, "reach", 1.0)
        ));

        register("WizardEffect", (params, data) -> new WizardEffect(
                BehaviorSpec.getDouble(params, "transformInterval", 8.0)
        ));

        register("KingBuffEffect", (params, data) -> new KingBuffEffect(
                BehaviorSpec.getDouble(params, "delayBetweenKnighting", 2.5)
        ));

        register("PianistEffect", (params, data) -> new PianistEffect(
                BehaviorSpec.getDouble(params, "danceInterval", 2.5)
        ));

        register("GargantuarImpThrowEffect", (params, data) ->
                new GargantuarImpThrowEffect(
                        readHealthPercentThrowImp(data, params),
                        BehaviorSpec.getDouble(data, "ImpApex", 250.0),
                        BehaviorSpec.getDouble(data, "ImpFlightTime", 1.5),
                        BehaviorSpec.getInt(data, "ImpTargetColumn", 2),
                        BehaviorSpec.getDouble(data, "MinPosXThrowImp", 0.0),
                        BehaviorSpec.getString(params, "impAlias", "ZombieImp")
                ));

        register("JalapenoZombieEffect", (params, data) -> new JalapenoZombieEffect());

        register("PeashooterZombieEffect", (params, data) -> new PeashooterZombieEffect(
                BehaviorSpec.getInt(params, "damage", 20),
                BehaviorSpec.getDouble(params, "fireRate", 1.5)
        ));
    }

    private EffectStatusRegistry() {
    }

    public static void register(String type, EffectStatusFactory factory) {
        FACTORIES.put(type, factory);
    }

    public static EffectStatus createOrNull(Object rawSpecOrNull, Map<String, Object> zombieData) {
        if (rawSpecOrNull == null) return null;
        BehaviorSpec spec = BehaviorSpec.parse(rawSpecOrNull);
        EffectStatusFactory factory = FACTORIES.get(spec.getType());
        if (factory == null) {
            throw new IllegalArgumentException("Unknown effect status type: " + spec.getType());
        }
        return factory.create(spec.params(), zombieData);
    }

    @SuppressWarnings("unchecked")
    private static double readHealthPercentThrowImp(Map<String, Object> data, Map<String, Object> params) {
        if (params.containsKey("healthPercentThrowImp")) {
            return BehaviorSpec.getDouble(params, "healthPercentThrowImp", 0.5);
        }

        Object raw = data.get("HealthThresholdToImpAmmoLayers");
        if (raw instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> firstEntry) {
            Object value = ((Map<String, Object>) firstEntry).get("HealthPercentThrowImp");
            if (value instanceof Number n) {
                return n.doubleValue();
            }
        }
        return 0.5;
    }
}