package com.ussr.pvz.model.entities.plants;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ussr.pvz.model.entities.plants.plantfood.PlantFoodType;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlantJsonParser {

    public static class UpgradeConfig {
        public int level;
        public UpgradeType type;
        public double value;
        public String specialTag;
    }

    public static class PlantConfig {
        public int id;
        public String name;
        public PlantType category;
        public List<Tag> tags;
        public int cost;
        public int baseHp;
        public int damage;
        public double actionInterval;
        public double recharge;

        public AbilityType abilityType;
        public double abilityValue;
        public PlantFoodType plantFoodType;
        public double plantFoodValue;

        public List<UpgradeConfig> upgrades;
    }

    public static Map<Integer, PlantConfig> loadConfigs(InputStream stream) {
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<PlantConfig>>() {
            }.getType();
            List<PlantConfig> list = new Gson().fromJson(reader, listType);

            Map<Integer, PlantConfig> configMap = new HashMap<>();
            if (list != null) {
                for (PlantConfig c : list) {
                    configMap.put(c.id, c);
                }
            }
            return configMap;
        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to parse structural plants dataset.");
            e.printStackTrace();
            return new HashMap<>();
        }
    }
}