package com.ussr.pvz.model.entities.plants;

import com.ussr.pvz.model.entities.plants.actstrategy.ActStrategy;
import com.ussr.pvz.model.entities.plants.plantfood.PlantFoodEffect;
import com.ussr.pvz.model.entities.plants.plantfood.PlantFoodType;
import com.ussr.pvz.model.entities.plants.PlantJsonParser.PlantConfig;
import com.ussr.pvz.model.entities.plants.PlantJsonParser.UpgradeConfig;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class PlantFactory {
    //TODO: this plant factory is not complete it is just blueprint so it should get completed
    //TODO: the print lines should convert to some else or get removed (if they are for errors covert to exceptions and if not can be omitted)
    private static Map<Integer, PlantConfig> blueprints = new HashMap<>();

    public static void init(InputStream jsonStream) {
        blueprints = PlantJsonParser.loadConfigs(jsonStream);
        System.out.println("PlantFactory ready! Registered " + blueprints.size() + " type-safe plant structures.");
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

        if (config.upgrades != null) {
            for (UpgradeConfig upgrade : config.upgrades) {
                if (upgrade.level <= level) {
                    switch (upgrade.type) {
                        case BUFF_HP -> runtimeHp += upgrade.value;
                        case BUFF_COST -> runtimeCost += upgrade.value;
                        case BUFF_ACTION_INTERVAL -> runtimeInterval += upgrade.value;
                        case BUFF_DAMAGE -> runtimeDamage += upgrade.value;
                        case BUFF_RECHARGE -> runtimeRecharge += upgrade.value;
                        case SPECIAL_MECHANIC -> {
                            // If a specific plant requires unique mechanics at lvl 3 or 4,
                            // you can store it in a generic map inside Plant: plant.addCustomFlag(upgrade.specialTag, upgrade.value);
                        }
                    }
                }
            }
        }

        plant.setHp(runtimeHp);
        plant.setCost(Math.max(0, runtimeCost));
        plant.setActionInterval(Math.max(0.05, runtimeInterval));
        plant.setDamage(String.valueOf(runtimeDamage));
        plant.setRecharge((int) Math.max(0.0, runtimeRecharge));
        plant.setLevel(level);

        plant.setActStrategy(buildAbilityStrategy(config.abilityType, config.abilityValue, plant));
        plant.setPlantFoodEffect(buildPlantFoodEffect(config.plantFoodType, config.plantFoodValue, plant));

        return plant;
    }

    private static ActStrategy buildAbilityStrategy(AbilityType type, double baseValue, Plant runtimeInstance) {
        return switch (type) {
            case PRODUCE_SUN -> (user, session) -> {
                // Shared sun production loop using actionInterval
            };
            case SHOOT_PROJECTILE -> (user, session) -> {
                // Spawns baseValue count of items (1 for Pea, 2 for Repeater, 3 for Threepeater, etc.)
            };
            case DELAYED_EXPLOSIVE -> (user, session) -> {
                // Trap checking loops
            };
            case INSTANT_EXPLOSIVE -> (user, session) -> {
                // Immediate splash damage then user.isAlive = false
            };
            case MELEE_ATTACK -> (user, session) -> {
                // Check 3x3 tiles or local boxes continuously
            };
            case PASSIVE_SHIELD, MODIFIER_UTILITY, MINT_FAMILY_BOOST, INSTANT_SUN_BURST -> (user, session) -> {
                // Passive triggers managed by game grids or placement rules directly
            };
        };
    }

    private static PlantFoodEffect buildPlantFoodEffect(PlantFoodType type, double pfValue, Plant runtimeInstance) {
        return new PlantFoodEffect() {
            @Override
            public void triggerSuperpower(Plant user, com.ussr.pvz.model.engine.GameSession session) {
                switch (type) {
                    case SPAWN_SUN_ITEMS -> {
                        // Instantly drop pfValue amount of total sun items
                    }
                    case PROJECTILE_BURST -> {
                        // Execute high-speed bullet pooling loop
                    }
                    case SPAWN_CLONES -> {
                        // Place copies of runtimeInstance onto adjacent tiles
                    }
                    case LOCAL_AOE_ATTACK -> {
                        // Perform local ground stamp damage pass
                    }
                    case MAP_WIDE_FREEZE -> {
                        // Freeze or drop butter onto all active entities
                    }
                    case KNOCKBACK_BLAST -> {
                        // Apply backwards force velocities to enemies in lane
                    }
                    default -> {}
                }
            }

            @Override
            public void applyStatusModifiers(Plant user) {
                if (type == PlantFoodType.GRANT_PERMANENT_ARMOR) {
                    // Give permanent armor layer with pfValue extra health points
                }
            }
        };
    }
}