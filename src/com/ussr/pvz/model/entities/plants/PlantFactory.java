package com.ussr.pvz.model.entities.plants;

import com.ussr.pvz.model.entities.plants.PlantJsonParser.PlantConfig;
import com.ussr.pvz.model.entities.plants.PlantJsonParser.UpgradeConfig;
import com.ussr.pvz.model.entities.plants.actstrategy.*;
import com.ussr.pvz.model.entities.plants.plantfood.*;
import com.ussr.pvz.model.util.Vec2;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
                        }
                    }
                }
            }
        }

        plant.setHp(Math.max(0, runtimeHp));
        plant.setCost(Math.max(0, runtimeCost));
        plant.setActionInterval(Math.max(0.05, runtimeInterval));
        plant.setDamage(runtimeDamage);
        plant.setRecharge((int) Math.max(0.0, runtimeRecharge));
        plant.setAbilityValue(runtimeAbility);
        plant.setLevel(level);
        plant.setPlantFoodType(config.plantFoodType);
        plant.setWrampUp(config.wrampUp);

        plant.setShootingVectors(buildShootingVectors(config));
        plant.setActStrategy(buildActStrategy(config));
        plant.setPlantFoodEffect(buildPlantFoodEffect(config.plantFoodType));

        return plant;
    }


    private static List<Vec2> buildShootingVectors(PlantConfig config) {
        if (config.abilityType != AbilityType.SHOOT_PROJECTILE) {
            return List.of();
        }

        String name = config.name;
        List<Vec2> v = new ArrayList<>();

        switch (name) {

            case "Peashooter", "Snow Pea", "Fire Peashooter",
                 "Goo Peashooter", "Sea-shroom", "Puff-shroom",
                 "Cactus", "Citron", "Bowling Bulb" -> v.add(Vec2.of(1, 0));

            case "Repeater" -> {
                v.add(Vec2.of(1, 0));
                v.add(Vec2.of(1, 0));
            }
            case "Mega Gatling Pea" -> {
                for (int i = 0; i < 4; i++) v.add(Vec2.of(1, 0));
            }
            case "Pea Pod" -> {
                int count = Math.max(1, (int) config.abilityValue);
                for (int i = 0; i < count; i++) v.add(Vec2.of(1, 0));
            }

            case "Threepeater" -> {
                v.add(Vec2.of(1, -1));
                v.add(Vec2.of(1, 0));
                v.add(Vec2.of(1, 1));
            }

            case "Split Pea" -> {
                v.add(Vec2.of(1, 0));
                v.add(Vec2.of(-1, 0));
                v.add(Vec2.of(-1, 0));
            }

            case "Rotobaga" -> {
                v.add(Vec2.of(1, 1));
                v.add(Vec2.of(1, -1));
                v.add(Vec2.of(-1, 1));
                v.add(Vec2.of(-1, -1));
            }

            case "Starfruit" -> {
                v.add(Vec2.of(-1.000, 0.000));
                v.add(Vec2.of(0.000, 1.000));
                v.add(Vec2.of(0.000, -1.000));
                v.add(Vec2.of(0.894, 0.447));
                v.add(Vec2.of(0.894, -0.447));
            }

            default -> v.add(Vec2.of(1, 0));
        }

        return v;
    }


    private static ActStrategy buildActStrategy(PlantConfig config) {
        return switch (config.abilityType) {
            case PRODUCE_SUN -> new SunProduceStrategy();
            case INSTANT_SUN_BURST -> new SunProduceStrategy();
            case SHOOT_PROJECTILE -> buildShootActStrategy(config);
            case DELAYED_EXPLOSIVE -> new ExplodeStrategy();
            case INSTANT_EXPLOSIVE -> new ExplodeStrategy();
            case MELEE_ATTACK -> new MeleeStrategy();
            case PASSIVE_SHIELD -> new WallNutStrategy();
            case MODIFIER_UTILITY -> new ModifyStrategy();
            case MINT_FAMILY_BOOST -> new MintStrategy();
        };
    }

    private static ActStrategy buildShootActStrategy(PlantConfig config) {
        if (config.category == null) return new ShootStrategy();
        return switch (config.category) {
            case HOMING -> new HomingStrategy();
            case STRIKE_THROUGH -> new StrikeStrategy();
            case LOBBER -> new LobberStrategy();
            default -> new ShootStrategy();
        };
    }


    private static PlantFoodEffect buildPlantFoodEffect(PlantFoodType type) {
        if (type == null || type == PlantFoodType.NONE) return null;
        return switch (type) {
            case SPAWN_SUN_ITEMS -> new SpawnSun();
            case PROJECTILE_BURST -> new ProjectileBurs();
            case SPAWN_CLONES -> new SpawnClones();
            case LOCAL_AOE_ATTACK -> new LocalAttack();
            case GRANT_PERMANENT_ARMOR -> new GrantArmor();
            case RANDOM_HYPNOTIZE -> new RandomHypnotize();
            case KNOCKBACK_BLAST -> new KnockBackBlast();
            case PULL_UNDERWATER -> new PullUnderWater();
            case MAP_WIDE_FREEZE -> new MapWideFreeze();
            case NONE -> null;
        };
    }
}