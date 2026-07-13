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
        // TODO(vasebreaker-wiring): VaseBreakerBehavior exists (model/level/behavior/VaseBreakerBehavior.java)
        //  but is never constructed anywhere. Register it here (or add an explicit case in
        //  parseBehavior() below, mirroring WallnutBowlingBehavior/BeghouledBehavior) and add the
        //  JSON fields it needs (vase layout, guaranteed plant/gargantuar vase positions) to
        //  JsonContainer.JsonLevelData + levels.json before it can be reached from a real level.
        BEHAVIOR_REGISTRY.put("BossBehavior", BossBehavior::new);
        BEHAVIOR_REGISTRY.put("ConveyorBehavior", ConveyorBehavior::new);
        BEHAVIOR_REGISTRY.put("DeadlineBehavior", DeadlineBehavior::new);
        BEHAVIOR_REGISTRY.put("LoveYourPlantsBehavior", LoveYourPlantsBehavior::new);
        BEHAVIOR_REGISTRY.put("PlantWhatYouGetBehavior", PlantWhatYouGetBehavior::new);
        BEHAVIOR_REGISTRY.put("ZombotanyBehavior", ZombotanyBehavior::new);
        BEHAVIOR_REGISTRY.put("NormalBehavior", NormalBehavior::new);
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
                schedule.add(new Level.SandstormEvent(e.triggerTimeSeconds, e.zombieAlias));
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

        level.setNecromancyZombieAlias(data.necromancyZombieAlias);
        level.setZombiesPerNecromancyWave(data.zombiesPerNecromancyWave);
        level.setWindIntervalSeconds(data.windIntervalSeconds);
        level.setFreezeStacksPerWind(data.freezeStacksPerWind);
    }

    private static void parseZombiesAndWaves(Level level, JsonContainer.JsonLevelData data) {
        if (data.allowedZombies != null) {
            List<Level.AllowedZombie> allowed = new ArrayList<>();
            for (JsonContainer.JsonZombieEntry entry : data.allowedZombies) {
                if (entry.id != null) {
                    allowed.add(new Level.AllowedZombie(entry.id, entry.weight > 0 ? entry.weight : 1000));
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
                        spawns.add(new Level.SpawnData(spawnEntry.zombieId, spawnEntry.lane, spawnEntry.delaySeconds));
                    }
                }
                waves.add(new Level.Wave(waveData.waveNumber, waveData.cost, spawns));
            }
            level.setWaves(waves);
        }
    }
}
