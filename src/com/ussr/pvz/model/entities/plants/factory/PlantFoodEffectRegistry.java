package com.ussr.pvz.model.entities.plants.factory;

import com.ussr.pvz.model.entities.plants.PlantType;
import com.ussr.pvz.model.entities.plants.PlantJsonParser.PlantConfig;
import com.ussr.pvz.model.entities.plants.plantfood.*;

import java.util.HashMap;
import java.util.Map;

public final class PlantFoodEffectRegistry {
    private static final Map<PlantFoodType, PlantFoodEffectFactory> FACTORIES = new HashMap<>();

    static {
        register(PlantFoodType.NONE, config -> null);
        register(PlantFoodType.SPAWN_SUN_ITEMS, config -> new SpawnSun((int) config.plantFoodValue, false));
        register(PlantFoodType.PROJECTILE_BURST, config -> {
            if (config.category == PlantType.LOBBER) {
                return new LobberBarrage((int) config.plantFoodValue, 1.0, -1);
            } else {
                return new InstantMassiveBlast(config.plantFoodValue / Math.max(1.0, config.damage), false);
            }
        });
        register(PlantFoodType.SPAWN_CLONES, config -> new SpawnClones((int) config.plantFoodValue));
        register(PlantFoodType.LOCAL_AOE_ATTACK, config -> new LocalAttack(5.0, 0.5, (int) config.plantFoodValue));
        register(PlantFoodType.GRANT_PERMANENT_ARMOR, config -> new GrantArmor((int) config.plantFoodValue, 0, false, false, false, true));
        register(PlantFoodType.RANDOM_HYPNOTIZE, config -> new RandomHypnotize((int) config.plantFoodValue));
        register(PlantFoodType.KNOCKBACK_BLAST, config -> new KnockBackBlast((int) config.plantFoodValue, 2.0));
        register(PlantFoodType.PULL_UNDERWATER, config -> new PullUnderWater((int) config.plantFoodValue));
        register(PlantFoodType.MAP_WIDE_FREEZE, config -> new MapWideFreeze());
        register(PlantFoodType.INSTANT_KILL, config -> new InstantKill((int) config.plantFoodValue));
        register(PlantFoodType.LOBBER_BARRAGE, config -> new LobberBarrage((int) config.plantFoodValue, 1.0, -1));
    }

    private PlantFoodEffectRegistry() {}

    public static void register(PlantFoodType type, PlantFoodEffectFactory factory) {
        FACTORIES.put(type, factory);
    }

    public static PlantFoodEffect create(PlantConfig config) {
        if (config.plantFoodType == null || config.plantFoodType == PlantFoodType.NONE) return null;

        PlantFoodEffectFactory factory = FACTORIES.get(config.plantFoodType);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown PlantFoodEffect type: " + config.plantFoodType);
        }
        return factory.create(config);
    }
}