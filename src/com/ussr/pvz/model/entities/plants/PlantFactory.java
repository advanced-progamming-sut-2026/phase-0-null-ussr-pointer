package com.ussr.pvz.model.entities.plants;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.entities.plants.factory.ActStrategyRegistry;
import com.ussr.pvz.model.entities.plants.factory.PlantFoodEffectRegistry;
import com.ussr.pvz.model.entities.plants.factory.ShootingVectorRegistry;
import com.ussr.pvz.model.entities.plants.plantfood.PlantFoodType;

import java.util.List;
import java.util.Map;

public class PlantFactory {

    private static String normalizeForLookup(String name) {
        if (name == null) return "";
        return name.trim().toLowerCase().replaceAll("[\\s_-]+", "");
    }

    public static Map<String, Object> getPlantData(String name) {
        if (name == null || App.getCachedPlantsData() == null) return null;
        String searchName = normalizeForLookup(name);
        for (Map<String, Object> data : App.getCachedPlantsData()) {
            String plantName = normalizeForLookup((String) data.get("name"));
            if (plantName.equals(searchName)) {
                return data;
            }
        }
        return null;
    }

    public static int findIdByName(String name) {
        Map<String, Object> data = getPlantData(name);
        if (data != null && data.containsKey("id")) {
            return ((Number) data.get("id")).intValue();
        }
        return -1;
    }

    public static Plant createPlantByName(String name, int level) {
        Map<String, Object> data = getPlantData(name);
        if (data == null) {
            throw new IllegalArgumentException("Plant name not found in registry: " + name);
        }

        Plant plant = new Plant();
        plant.setId(((Number) data.getOrDefault("id", 0)).intValue());
        plant.setName((String) data.get("name"));

        String catStr = (String) data.get("category");
        if (catStr != null) plant.setType(PlantType.valueOf(catStr));

        @SuppressWarnings("unchecked")
        List<String> tagsList = (List<String>) data.get("tags");
        if (tagsList != null) {
            for (String tagStr : tagsList) {
                Tag t = Tag.getByName(tagStr);
                if (t != null) plant.getTags().add(t);
            }
        }

        int runtimeHp = ((Number) data.getOrDefault("baseHp", 0)).intValue();
        int runtimeCost = ((Number) data.getOrDefault("cost", 0)).intValue();
        double runtimeInterval = ((Number) data.getOrDefault("actionInterval", 0.0)).doubleValue();
        int runtimeDamage = ((Number) data.getOrDefault("damage", 0)).intValue();
        double runtimeRecharge = ((Number) data.getOrDefault("recharge", 0.0)).doubleValue();
        double runtimeAbility = ((Number) data.getOrDefault("abilityValue", 0.0)).doubleValue();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> upgrades = (List<Map<String, Object>>) data.get("upgrades");
        if (upgrades != null) {
            for (Map<String, Object> upgrade : upgrades) {
                int upLevel = ((Number) upgrade.getOrDefault("level", 0)).intValue();
                if (upLevel <= level) {
                    String upType = (String) upgrade.get("type");
                    double upVal = ((Number) upgrade.getOrDefault("value", 0.0)).doubleValue();
                    String specialTag = (String) upgrade.get("specialTag");

                    if (upType != null) {
                        switch (upType) {
                            case "BUFF_HP" -> runtimeHp += (int) upVal;
                            case "BUFF_COST" -> runtimeCost += (int) upVal;
                            case "BUFF_ACTION_INTERVAL" -> runtimeInterval += upVal;
                            case "BUFF_DAMAGE" -> runtimeDamage += (int) upVal;
                            case "BUFF_RECHARGE" -> runtimeRecharge += upVal;
                            case "SPECIAL_MECHANIC" -> {
                                if (specialTag != null && !specialTag.isEmpty()) {
                                    plant.getRawUpgrades().add(specialTag);
                                }
                            }
                        }
                    }
                }
            }
        }

        plant.setHp(Math.max(0, runtimeHp));
        plant.setCost(Math.max(0, runtimeCost));
        plant.setActionInterval(Math.max(0.05, runtimeInterval));
        plant.setDamage(runtimeDamage);

        // Adjusted to maintain your setMaxRecharge logic
        try {
            plant.getClass().getMethod("setMaxRecharge", double.class).invoke(plant, Math.max(0.0, runtimeRecharge));
            plant.getClass().getMethod("setRecharge", double.class).invoke(plant, 0.0);
        } catch (Exception e) {
            plant.setRecharge((int) Math.max(0.0, runtimeRecharge)); // Fallback if setMaxRecharge doesn't exist yet
        }

        plant.setAbilityValue(runtimeAbility);
        plant.setLevel(level);

        String pfType = (String) data.get("plantFoodType");
        if (pfType != null && !pfType.trim().isEmpty() && !pfType.trim().equalsIgnoreCase("NONE")) {
            try {
                plant.setPlantFoodType(PlantFoodType.valueOf(pfType.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                plant.setPlantFoodType(PlantFoodType.NONE);
            }
        } else {
            plant.setPlantFoodType(PlantFoodType.NONE);
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> wrampUp = (List<Map<String, Object>>) data.get("wramp-up");
        plant.setWrampUp(wrampUp);

        plant.setShootingVectors(ShootingVectorRegistry.getVectors(data));
        plant.setActStrategy(ActStrategyRegistry.create(data));

        plant.setPlantFoodEffect(PlantFoodEffectRegistry.create(data));

        return plant;
    }

    public static Plant createPlant(int id, int level) {
        if (App.getCachedPlantsData() != null) {
            for (Map<String, Object> data : App.getCachedPlantsData()) {
                if (((Number) data.get("id")).intValue() == id) {
                    return createPlantByName((String) data.get("name"), level);
                }
            }
        }
        throw new IllegalArgumentException("Plant ID " + id + " not found.");
    }
}