package com.ussr.pvz.model.entities.plants.factory;

import com.ussr.pvz.model.entities.plants.PlantType;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.entities.plants.PlantJsonParser.PlantConfig;
import com.ussr.pvz.model.entities.plants.plantfood.*;
import com.ussr.pvz.model.util.Vec2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class PlantFoodEffectRegistry {
    private static final Map<PlantFoodType, PlantFoodEffectFactory> FACTORIES = new HashMap<>();

    static {
        register(PlantFoodType.NONE, config -> null);

        register(PlantFoodType.SPAWN_SUN_ITEMS, config -> new SpawnSun((int) config.plantFoodValue, false));

        // Reroutes the generic PROJECTILE_BURST to the flawless implementations
        register(PlantFoodType.PROJECTILE_BURST, config -> {
            if (config.category == PlantType.LOBBER) {
                return new LobberBarrage((int) config.plantFoodValue, 1.0, -1);
            }

            switch (config.name) {
                case "Peashooter":
                case "Fire Peashooter":
                case "Goo Peashooter":
                case "Cactus":
                case "Rotobaga":
                case "Starfruit":
                case "Cat-tail":
                    // 3 sec duration, fires every 0.1s. (Starfruit/Rotobaga will inherit their base multi-directional vectors)
                    return new TimedProjectileBurst(3.0, 0.1, 0, 1.0, false, null, null);
                case "Repeater":
                    return new TimedProjectileBurst(3.0, 0.1, 1, 20.0, false, null, null);
                case "Mega Gatling Pea":
                    return new TimedProjectileBurst(3.0, 0.1, 4, 20.0, false, null, null);
                case "Threepeater":
                    return new TimedProjectileBurst(3.0, 0.1, 0, 1.0, false, null, Arrays.asList(
                            Vec2.of(1, -1), Vec2.of(1, 0), Vec2.of(1, 1), Vec2.of(1, -2), Vec2.of(1, 2)
                    ));
                case "Split Pea":
                    return new TimedProjectileBurst(3.0, 0.1, 0, 1.0, false, null, Arrays.asList(
                            Vec2.of(1, 0), Vec2.of(-1, 0)
                    ));
                case "Snow Pea":
                    return new TimedProjectileBurst(3.0, 0.1, 0, 1.0, true, null, null);
                case "Sea-shroom":
                case "Puff-shroom":
                    return new TimedProjectileBurst(3.0, 0.1, 0, 1.0, false, Tag.SHROOM, null);
                case "Pea Pod":
                    return new InstantMassiveBlast(20.0, true);
                case "Torchwood":
                    return new ModifierEffect(false, 3); // 3x damage multiplier
                default:
                    // Citron, Bowling Bulb
                    return new InstantMassiveBlast(config.plantFoodValue / Math.max(1.0, config.damage), false);
            }
        });

        register(PlantFoodType.SPAWN_CLONES, config -> {
            if ("Lily Pad".equals(config.name)) {
                return new ModifierEffect(true, 1);
            }
            return new SpawnClones((int) config.plantFoodValue);
        });

        register(PlantFoodType.LOCAL_AOE_ATTACK, config -> new LocalAttack(5.0, 0.5, (int) config.plantFoodValue));

        register(PlantFoodType.GRANT_PERMANENT_ARMOR, config -> new GrantArmor((int) config.plantFoodValue, 0, false, false, false, true));

        register(PlantFoodType.RANDOM_HYPNOTIZE, config -> new RandomHypnotize((int) config.plantFoodValue));

        register(PlantFoodType.KNOCKBACK_BLAST, config -> {
            if ("Magnet-shroom".equals(config.name)) {
                return new MetalAbsorb((int) config.plantFoodValue);
            }
            return new KnockBackBlast((int) config.plantFoodValue, 2.0);
        });

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