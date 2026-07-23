package com.ussr.pvz.model.entities.plants.factory;

import com.ussr.pvz.model.entities.plants.PlantType;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.entities.plants.plantfood.*;
import com.ussr.pvz.model.entities.plants.plantfood.localattack.*;
import com.ussr.pvz.model.util.Vec2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class PlantFoodEffectRegistry {
    // Map using String keys straight from JSON to bypass the incomplete PlantFoodType enum
    private static final Map<String, PlantFoodEffectFactory> FACTORIES = new HashMap<>();

    static {
        register("NONE", data -> null);

        register("SPAWN_SUN", data -> {
            int pfValue = ((Number) data.getOrDefault("plantFoodValue", 0)).intValue();
            boolean instantGrow = Boolean.TRUE.equals(data.get("instantMaxGrowth"));
            return new SpawnSun(pfValue, instantGrow);
        });

        register("PROJECTILE_BURST", data -> {
            //String name = (String) data.get("name");
            double duration = ((Number) data.getOrDefault("plantFoodDuration", 4.0)).doubleValue();
            double strikeRate = ((Number) data.getOrDefault("plantFoodStrikeRate", 0.1)).doubleValue();
            return new LocalAttack(duration , strikeRate);

        });

        register("REPEATER_ATTACK", data -> {
            double duration = ((Number) data.getOrDefault("plantFoodDuration", 4.0)).doubleValue();
            double strikeRate = ((Number) data.getOrDefault("plantFoodStrikeRate", 0.1)).doubleValue();
            return new RepeaterAttack(duration, strikeRate);
        });

        // JSON has a trailing space for Snow Pea ("SNOW_PEA_ATTACK "), but it gets trimmed by our loader logic
        register("SNOW_PEA_ATTACK", data -> {
            double duration = ((Number) data.getOrDefault("plantFoodDuration", 4.0)).doubleValue();
            double strikeRate = ((Number) data.getOrDefault("plantFoodStrikeRate", 0.1)).doubleValue();
            return new SnowPeaAttack(duration, strikeRate);
        });

        register("PLASMA_WIPE", data -> {
            double duration = ((Number) data.getOrDefault("plantFoodDuration", 0.0)).doubleValue();
            double strikeRate = ((Number) data.getOrDefault("plantFoodStrikeRate", 0.0)).doubleValue();
            return new PlasmaWipe(duration, strikeRate);
        });

        register("BOWLING_BULB_ATTACK", data -> new BowlingBulbAttack());

        register("GLOBAL_SHROOM_ATTACK", data -> {
            double duration = ((Number) data.getOrDefault("plantFoodDuration", 4.0)).doubleValue();
            double strikeRate = ((Number) data.getOrDefault("plantFoodStrikeRate", 0.1)).doubleValue();
            return new GlobalShroomAttack(duration, strikeRate, (String) data.get("name"));
        });

        register("AREA_3X3_ATTACK", data -> {
            double duration = ((Number) data.getOrDefault("plantFoodDuration", 4.0)).doubleValue();
            double strikeRate = ((Number) data.getOrDefault("plantFoodStrikeRate", 0.1)).doubleValue();
            return new Area3x3Attack(duration, strikeRate);
        });

        register("SPAWN_CLONES", data -> {
            int pfValue = ((Number) data.getOrDefault("plantFoodValue", 0)).intValue();
            if ("Lily Pad".equals(data.get("name"))) {
                return SpawnClones.forLilyPad(pfValue);
            }
            return new SpawnClones(pfValue);
        });

        register("GRANT_PERMANENT_ARMOR", data -> {
            String name = (String) data.get("name");
            return switch (name) {
                case "Wall-nut" -> GrantArmor.forWallNut();
                case "Tall-nut" -> GrantArmor.forTallNut();
                case "Endurian" -> GrantArmor.forEndurian();
                case "Garlic" -> GrantArmor.forGarlic();
                case "Sweet Potato" -> GrantArmor.forSweetPotato();
                case "Explode-o-nut" -> GrantArmor.forExplodeONut();
                case "Pumpkin" -> GrantArmor.forPumpkin();
                case "Sun Bean" -> GrantArmor.forSunBean();
                default -> new GrantArmor(((Number) data.getOrDefault("plantFoodValue", 0)).intValue(),
                        0, false, false, false, true);
            };
        });

        register("RANDOM_HYPNOTIZE", data -> new RandomHypnotize(((Number)
                data.getOrDefault("plantFoodValue", 0)).intValue()));

        register("KNOCKBACK_BLAST", data -> {
            if ("Magnet-shroom".equals(data.get("name"))) {
                return new MetalAbsorb(((Number) data.getOrDefault("plantFoodValue", 15)).intValue());
            }
            return new KnockBackBlast(3.0);
        });

        register("METAL_ABSORB", data -> new MetalAbsorb(((Number)
                data.getOrDefault("plantFoodValue", 15)).intValue()));

        register("PULL_UNDERWATER", data -> new PullUnderWater(((Number)
                data.getOrDefault("plantFoodValue", 0)).intValue()));

        register("MAP_WIDE_FREEZE", data -> new MapWideFreeze());

        register("INSTANT_KILL", data -> new InstantKill(((Number)
                data.getOrDefault("plantFoodValue", 0)).intValue()));

        register("LOBBER_BARRAGE", data -> new LobberBarrage());
    }

    private PlantFoodEffectRegistry() {}

    public static void register(String typeName, PlantFoodEffectFactory factory) {
        FACTORIES.put(typeName.trim().toUpperCase(), factory);
    }

    public static PlantFoodEffect create(Map<String, Object> data) {
        String pfTypeStr = (String) data.get("plantFoodType");

        if (pfTypeStr == null) return null;

        // Cleanse spacing issues commonly found in the JSON (like "SNOW_PEA_ATTACK ")
        pfTypeStr = pfTypeStr.trim().toUpperCase();
        if (pfTypeStr.equals("NONE")) return null;

        PlantFoodEffectFactory factory = FACTORIES.get(pfTypeStr);
        if (factory == null) {
            System.err.println("Warning: Unknown PlantFoodEffect string from JSON: " + pfTypeStr);
            return null;
        }
        return factory.create(data);
    }
}