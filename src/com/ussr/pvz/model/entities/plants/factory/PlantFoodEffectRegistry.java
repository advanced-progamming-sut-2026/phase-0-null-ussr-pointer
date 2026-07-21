package com.ussr.pvz.model.entities.plants.factory;

import com.ussr.pvz.model.entities.plants.PlantType;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.entities.plants.plantfood.*;
import com.ussr.pvz.model.entities.plants.plantfood.localattack.LocalAttack;
import com.ussr.pvz.model.util.Vec2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class PlantFoodEffectRegistry {
    private static final Map<PlantFoodType, PlantFoodEffectFactory> FACTORIES = new HashMap<>();

    static {
        register(PlantFoodType.NONE, data -> null);

        register(PlantFoodType.SPAWN_SUN_ITEMS, data -> new SpawnSun(((Number) data.getOrDefault("plantFoodValue", 0)).intValue(), false));

        register(PlantFoodType.PROJECTILE_BURST, data -> {
            String catStr = (String) data.get("category");
            PlantType category = catStr != null ? PlantType.valueOf(catStr) : null;
            double pfValue = ((Number) data.getOrDefault("plantFoodValue", 0)).doubleValue();
            int damage = ((Number) data.getOrDefault("damage", 1)).intValue();
            String name = (String) data.get("name");

            if (category == PlantType.LOBBER) {
                return new LobberBarrage();
            }

            switch (name) {
                case "Peashooter":
                case "Fire Peashooter":
                case "Goo Peashooter":
                case "Cactus":
                case "Rotobaga":
                case "Starfruit":
                case "Cat-tail":
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
                    return new ModifierEffect(false, 3);
                default:
                    return new InstantMassiveBlast(pfValue / Math.max(1.0, damage), false);
            }
        });

        register(PlantFoodType.SPAWN_CLONES, data -> {
            if ("Lily Pad".equals(data.get("name"))) {
                return new ModifierEffect(true, 1);
            }
            return new SpawnClones(((Number) data.getOrDefault("plantFoodValue", 0)).intValue());
        });

        register(PlantFoodType.LOCAL_AOE_ATTACK, data -> new LocalAttack(5.0, 0.5));

        register(PlantFoodType.GRANT_PERMANENT_ARMOR, data -> new GrantArmor(((Number) data.getOrDefault("plantFoodValue", 0)).intValue(), 0, false, false, false, true));

        register(PlantFoodType.RANDOM_HYPNOTIZE, data -> new RandomHypnotize(((Number) data.getOrDefault("plantFoodValue", 0)).intValue()));

        register(PlantFoodType.KNOCKBACK_BLAST, data -> {
            if ("Magnet-shroom".equals(data.get("name"))) {
                return new MetalAbsorb(((Number) data.getOrDefault("plantFoodValue", 0)).intValue());
            }
            return new KnockBackBlast(3.0);
        });

        register(PlantFoodType.PULL_UNDERWATER, data -> new PullUnderWater(((Number) data.getOrDefault("plantFoodValue", 0)).intValue()));

        register(PlantFoodType.MAP_WIDE_FREEZE, data -> new MapWideFreeze());

        register(PlantFoodType.INSTANT_KILL, data -> new InstantKill(((Number) data.getOrDefault("plantFoodValue", 0)).intValue()));

        register(PlantFoodType.LOBBER_BARRAGE, data -> new LobberBarrage());
    }

    private PlantFoodEffectRegistry() {}

    public static void register(PlantFoodType type, PlantFoodEffectFactory factory) {
        FACTORIES.put(type, factory);
    }

    public static PlantFoodEffect create(Map<String, Object> data) {
        String pfTypeStr = (String) data.get("plantFoodType");
        if (pfTypeStr == null || pfTypeStr.equals("NONE")) return null;

        PlantFoodType type = PlantFoodType.valueOf(pfTypeStr);
        PlantFoodEffectFactory factory = FACTORIES.get(type);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown PlantFoodEffect type: " + type);
        }
        return factory.create(data);
    }
}