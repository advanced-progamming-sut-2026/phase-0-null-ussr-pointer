package com.ussr.pvz.model.entities.plants.factory;

import com.ussr.pvz.model.entities.plants.AbilityType;
import com.ussr.pvz.model.entities.plants.PlantJsonParser.PlantConfig;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ShootingVectorRegistry {
    private static final Map<String, List<Vec2>> REGISTRY = new HashMap<>();

    static {
        List<String> singleShots = List.of(
                "Peashooter", "Snow Pea", "Fire Peashooter", "Goo Peashooter", "Sea-shroom",
                "Puff-shroom", "Cactus", "Citron", "Bowling Bulb", "Cabbage-pult", "Kernel-pult",
                "Melon-pult", "Winter Melon", "Pepper-pult"
        );
        for (String name : singleShots) {
            register(name, List.of(Vec2.of(1, 0)));
        }

        register("Repeater", List.of(Vec2.of(1, 0), Vec2.of(1, 0)));
        register("Mega Gatling Pea", List.of(Vec2.of(1, 0), Vec2.of(1, 0), Vec2.of(1, 0), Vec2.of(1, 0)));
        register("Threepeater", List.of(Vec2.of(1, -1), Vec2.of(1, 0), Vec2.of(1, 1)));
        register("Split Pea", List.of(Vec2.of(1, 0), Vec2.of(-1, 0), Vec2.of(-1, 0)));
        register("Rotobaga", List.of(Vec2.of(1, 1), Vec2.of(1, -1), Vec2.of(-1, 1), Vec2.of(-1, -1)));
        register("Starfruit", List.of(Vec2.of(-1.000, 0.000), Vec2.of(0.000, 1.000), Vec2.of(0.000, -1.000), Vec2.of(0.894, 0.447), Vec2.of(0.894, -0.447)));
    }

    private ShootingVectorRegistry() {}

    public static void register(String name, List<Vec2> vectors) {
        REGISTRY.put(name, vectors);
    }

    public static List<Vec2> getVectors(PlantConfig config) {
        if (config.abilityType != AbilityType.SHOOT_PROJECTILE) {
            return List.of();
        }

        // Handle dynamically stacked vectors, e.g. Pea Pod
        if ("Pea Pod".equals(config.name)) {
            int count = Math.max(1, (int) config.abilityValue);
            List<Vec2> vectors = new ArrayList<>();
            for (int i = 0; i < count; i++) vectors.add(Vec2.of(1, 0));
            return vectors;
        }

        return REGISTRY.getOrDefault(config.name, List.of(Vec2.of(1, 0)));
    }
}