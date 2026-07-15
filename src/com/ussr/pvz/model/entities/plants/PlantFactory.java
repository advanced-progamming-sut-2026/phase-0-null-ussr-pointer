package com.ussr.pvz.model.entities.plants;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.entities.plants.PlantJsonParser.PlantConfig;
import com.ussr.pvz.model.entities.plants.PlantJsonParser.UpgradeConfig;
import com.ussr.pvz.model.entities.plants.factory.ActStrategyRegistry;
import com.ussr.pvz.model.entities.plants.factory.PlantFoodEffectRegistry;
import com.ussr.pvz.model.entities.plants.factory.ShootingVectorRegistry;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class PlantFactory {

    private static Map<Integer, PlantConfig> blueprints = new HashMap<>();

    public static void init(InputStream jsonStream) {
        blueprints = PlantJsonParser.loadConfigs(jsonStream);
    }

    public static Plant createPlant(int id, int level) {
        PlantConfig config = blueprints.get(id);
        if (config == null) {
            throw new IllegalArgumentException("Plant ID " + id + " does not exist in dataset.");
        }

        Plant plant = new Plant();
        plant.setId(config.id);
        plant.setName(config.name);
        plant.setType(config.category);
        if (config.tags != null) {
            plant.getTags().addAll(config.tags);
        }

        int runtimeHp = config.baseHp;
        int runtimeCost = config.cost;
        double runtimeInterval = config.actionInterval;
        int runtimeDamage = config.damage;
        double runtimeRecharge = config.recharge;
        double runtimeAbility = config.abilityValue;

        if (config.upgrades != null) {
            for (UpgradeConfig upgrade : config.upgrades) {
                if (upgrade.level <= level) {
                    switch (upgrade.type) {
                        case BUFF_HP -> runtimeHp += (int) upgrade.value;
                        case BUFF_COST -> runtimeCost += (int) upgrade.value;
                        case BUFF_ACTION_INTERVAL -> runtimeInterval += upgrade.value;
                        case BUFF_DAMAGE -> runtimeDamage += (int) upgrade.value;
                        case BUFF_RECHARGE -> runtimeRecharge += upgrade.value;
                        case SPECIAL_MECHANIC -> {
                            if (upgrade.specialTag != null && !upgrade.specialTag.isEmpty()) {
                                plant.getRawUpgrades().add(upgrade.specialTag);
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
        plant.setMaxRecharge(Math.max(0.0, runtimeRecharge));
        plant.setRecharge(0.0);
        plant.setAbilityValue(runtimeAbility);
        plant.setLevel(level);
        plant.setPlantFoodType(config.plantFoodType);
        plant.setWrampUp(config.wrampUp);

        plant.setShootingVectors(ShootingVectorRegistry.getVectors(config));
        plant.setActStrategy(ActStrategyRegistry.create(config));
        plant.setPlantFoodEffect(PlantFoodEffectRegistry.create(config));


        return plant;
    }

    private static String normalizeForLookup(String name) {
        if (name == null) return "";
        return name.trim().toLowerCase().replaceAll("[\\s_-]+", "");
    }

    public static int findIdByName(String name) {
        if (name == null || App.getCachedPlantsData() == null) return -1;

        String searchName = normalizeForLookup(name);

        for (int i = 0; i < App.getCachedPlantsData().size(); i++) {
            var data = App.getCachedPlantsData().get(i);
            String plantName = normalizeForLookup((String) data.get("name"));
            if (plantName.equals(searchName)) {
                return i + 1; // Assuming 1-based indexing for IDs
            }
        }
        return -1;
    }

    public static Plant createPlantByName(String name, int level) {
        int id = findIdByName(name);
        if (id == -1) {
            throw new IllegalArgumentException("Plant name not found in registry: " + name);
        }
        return createPlant(id, level);
    }
}