package com.ussr.pvz.model.level;

import com.ussr.pvz.model.level.behavior.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class LevelFactory {

    private static final Map<String, Supplier<LevelBehavior>> BEHAVIOR_REGISTRY = new HashMap<>();

    static {
        BEHAVIOR_REGISTRY.put("BossBehavior", BossBehavior::new);
        BEHAVIOR_REGISTRY.put("ConveyorBehavior", ConveyorBehavior::new);
        BEHAVIOR_REGISTRY.put("DeadlineBehavior", DeadlineBehavior::new);
        BEHAVIOR_REGISTRY.put("LoveYourPlantsBehavior", LoveYourPlantsBehavior::new);
        BEHAVIOR_REGISTRY.put("PlantWhatYouGetBehavior", PlantWhatYouGetBehavior::new);
        BEHAVIOR_REGISTRY.put("ZombotanyBehavior", ZombotanyBehavior::new);
        BEHAVIOR_REGISTRY.put("NormalBehavior", NormalBehavior::new);
        BEHAVIOR_REGISTRY.put("VaseBreakerBehavior", VaseBreakerBehavior::new);
    }

    public static Level create(JsonContainer.JsonLevelData data) {
        if (data == null) return null;

        Level level = new Level();
        parseBaseAttributes(level, data);
        level.setBehavior(parseBehavior(data));
        parseEnvironmentConfig(level, data);
        parseZombiesAndWaves(level, data);

        return level;
    }

    private static void parseBaseAttributes(Level level, JsonContainer.JsonLevelData data) {
        level.setId(data.id);
        level.setOrder(data.order);
        level.setSunFalling(data.sunFalling);
        level.setTimeLimitSeconds(data.timeLimitSeconds);
        level.setDeadlineColumn(data.deadlineColumn);
        level.setAllowedPlantsLost(data.allowedPlantsLost);
        level.setLockedPlants(data.lockedPlants != null ? data.lockedPlants : new ArrayList<>());
        level.setSeedPlants(data.seedPlants != null ? data.seedPlants : new ArrayList<>());
    }

    private static LevelBehavior parseBehavior(JsonContainer.JsonLevelData data) {
        if (data.behavior == null || data.behavior.isBlank()) {
            return (data.order == 4) ? new BossBehavior() : null;
        }

        return switch (data.behavior.trim()) {
            case "TimedWarBehavior" -> {
                TimedWarBehavior.LimitationType type = TimedWarBehavior.LimitationType.ZOMBIE;
                if (data.timedWarType != null && data.timedWarType.equalsIgnoreCase("SUN")) {
                    type = TimedWarBehavior.LimitationType.SUN;
                }
                yield new TimedWarBehavior(type, data.timedWarTarget);
            }
            case "SaveOurSeedsBehavior" -> {
                List<SaveOurSeedsBehavior.TargetSeed> seeds = new ArrayList<>();
                if (data.plantLayout != null) {
                    for (JsonContainer.JsonPrePlacedPlant p : data.plantLayout) {
                        seeds.add(new SaveOurSeedsBehavior.TargetSeed(p.plantName, p.row, p.col));
                    }
                }
                yield new SaveOurSeedsBehavior(seeds);
            }
            case "WallnutBowlingBehavior" -> new WallnutBowlingBehavior(data.redLineColumn);
            case "BeghouledBehavior" -> new BeghouledBehavior(data.targetMatches, data.startingPlants != null ? data.startingPlants : new ArrayList<>());
            case "IZombieBehavior" -> new IZombieBehavior(data.redLineColumn, data.startingSun);
            case "PlantWhatYouGetBehavior" -> {
                PlantWhatYouGetBehavior behavior = new PlantWhatYouGetBehavior();
                behavior.setStartingSun(data.startingSun);
                yield behavior;
            }
            case "LoveYourPlantsBehavior" -> {
                // Dynamically apply the level's specific death limit, falling back to 5 if not specified
                int limit = data.allowedPlantsLost > 0 ? data.allowedPlantsLost : 5;
                yield new LoveYourPlantsBehavior(limit);
            }
            default -> {
                Supplier<LevelBehavior> supplier = BEHAVIOR_REGISTRY.get(data.behavior.trim());
                if (supplier != null) yield supplier.get();
                System.err.println("[LevelFactory] Unknown campaign behavior: " + data.behavior);
                yield (data.order == 4) ? new BossBehavior() : null;
            }
        };
    }

    private static void parseEnvironmentConfig(Level level, JsonContainer.JsonLevelData data) {
        if (data.sandstorms != null) {
            List<Level.SandstormEvent> schedule = new ArrayList<>();
            for (JsonContainer.JsonSandstormEvent e : data.sandstorms) {
                schedule.add(new Level.SandstormEvent(e.triggerTimeSeconds, resolveZombieAlias(e.zombieAlias)));
            }
            level.setSandstormSchedule(schedule);
        }

        level.setStartingTideColumn(data.startingTideColumn);
        if (data.tides != null) {
            List<Level.TideEvent> schedule = new ArrayList<>();
            for (JsonContainer.JsonTideEvent e : data.tides) {
                schedule.add(new Level.TideEvent(e.triggerTimeSeconds, e.targetColumn));
            }
            level.setTideSchedule(schedule);
        }

        level.setNecromancyZombieAlias(resolveZombieAlias(data.necromancyZombieAlias));
        level.setZombiesPerNecromancyWave(data.zombiesPerNecromancyWave);
        level.setWindIntervalSeconds(data.windIntervalSeconds);
        level.setFreezeStacksPerWind(data.freezeStacksPerWind);
    }

    private static void parseZombiesAndWaves(Level level, JsonContainer.JsonLevelData data) {
        if (data.allowedZombies != null) {
            List<Level.AllowedZombie> allowed = new ArrayList<>();
            for (JsonContainer.JsonZombieEntry entry : data.allowedZombies) {
                if (entry.id != null) {
                    allowed.add(new Level.AllowedZombie(resolveZombieAlias(entry.id), entry.weight > 0 ? entry.weight : 1000));
                }
            }
            level.setAllowedZombies(allowed);
        }

        if (data.waves != null) {
            List<Level.Wave> waves = new ArrayList<>();
            for (JsonContainer.JsonWaveData waveData : data.waves) {
                List<Level.SpawnData> spawns = new ArrayList<>();
                if (waveData.spawnData != null) {
                    for (JsonContainer.JsonSpawnData spawnEntry : waveData.spawnData) {
                        spawns.add(new Level.SpawnData(resolveZombieAlias(spawnEntry.zombieId), spawnEntry.lane, spawnEntry.delaySeconds));
                    }
                }
                waves.add(new Level.Wave(waveData.waveNumber, waveData.cost, spawns));
            }
            level.setWaves(waves);
        }
    }

    /**
     * Bridges legacy JSON zombie nomenclature to strict internal zombie aliases from zombies.json.
     */
    private static String resolveZombieAlias(String rawAlias) {
        if (rawAlias == null || rawAlias.isBlank()) return null;
        return switch (rawAlias.trim().toLowerCase()) {
            case "mummy_normal", "beach_normal", "peasant_normal", "cave_normal", "zombie" -> "ZombieDefault";
            case "mummy_conehead", "conehead" -> "ZombieArmor1";
            case "buckethead" -> "ZombieArmor2";
            case "peasant_knight", "zombie_peasant_knight" -> "ZombieDarkArmor3";
            case "surfer_zombie" -> "ZombieBeachSnorkel";
            case "yeti_imp", "imp_zombie" -> "ZombieImp";
            case "gargantuar" -> "ZombieGargantuar";
            default -> rawAlias;
        };
    }
}