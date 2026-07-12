package com.ussr.pvz.model.level;

import com.ussr.pvz.model.level.behavior.*;
import com.ussr.pvz.model.level.environment.*;

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
    }

    public static Level create(JsonContainer.JsonLevelData data) {
        if (data == null) return null;

        Level level = new Level();
        level.setId(data.id);
        level.setOrder(data.order);
        level.setSunFalling(data.sunFalling);
        level.setTimeLimitSeconds(data.timeLimitSeconds);
        level.setDeadlineColumn(data.deadlineColumn);
        level.setAllowedPlantsLost(data.allowedPlantsLost);
        level.setLockedPlants(data.lockedPlants != null ? data.lockedPlants : new ArrayList<>());
        level.setSeedPlants(data.seedPlants != null ? data.seedPlants : new ArrayList<>());

        LevelBehavior selectedBehavior = null;

        if (data.behavior != null && !data.behavior.isBlank()) {
            String behaviorKey = data.behavior.trim();
            switch (behaviorKey) {
                case "TimedWarBehavior" -> {
                    TimedWarBehavior.LimitationType type = TimedWarBehavior.LimitationType.ZOMBIE;
                    if (data.timedWarType != null && data.timedWarType.equalsIgnoreCase("SUN")) {
                        type = TimedWarBehavior.LimitationType.SUN;
                    }
                    selectedBehavior = new TimedWarBehavior(type, data.timedWarTarget);
                }
                case "SaveOurSeedsBehavior" -> {
                    List<SaveOurSeedsBehavior.TargetSeed> seeds = new ArrayList<>();
                    if (data.plantLayout != null) {
                        for (JsonContainer.JsonPrePlacedPlant p : data.plantLayout) {
                            seeds.add(new SaveOurSeedsBehavior.TargetSeed(p.plantName, p.row, p.col));
                        }
                    }
                    selectedBehavior = new SaveOurSeedsBehavior(seeds);
                }
                default -> {
                    Supplier<LevelBehavior> supplier = BEHAVIOR_REGISTRY.get(behaviorKey);
                    if (supplier != null) {
                        selectedBehavior = supplier.get();
                    } else {
                        System.err.println("[LevelFactory] Unknown campaign behavior: " + data.behavior);
                    }
                }
            }
        }

        if (selectedBehavior == null && data.order == 4) {
            selectedBehavior = new BossBehavior();
        }

        level.setBehavior(selectedBehavior);

        if (data.environment != null && !data.environment.isBlank()) {
            switch (data.environment.trim()) {
                case "AncientEgyptEnvironment" -> {
                    List<AncientEgyptEnvironment.SandstormEvent> schedule = new ArrayList<>();
                    if (data.sandstorms != null) {
                        for (JsonContainer.JsonSandstormEvent e : data.sandstorms) {
                            schedule.add(new AncientEgyptEnvironment.SandstormEvent(e.triggerTimeSeconds, e.zombieAlias));
                        }
                    }
                    level.setEnvironment(new AncientEgyptEnvironment(schedule));
                }

                case "BigWaveBeachEnvironment" -> {
                    List<BigWaveBeachEnvironment.TideEvent> schedule = new ArrayList<>();
                    if (data.tides != null) {
                        for (JsonContainer.JsonTideEvent e : data.tides) {
                            schedule.add(new BigWaveBeachEnvironment.TideEvent(e.triggerTimeSeconds, e.targetColumn));
                        }
                    }
                    level.setEnvironment(new BigWaveBeachEnvironment(data.startingTideColumn, schedule));
                }

                case "DarkAgesEnvironment" ->
                        level.setEnvironment(new DarkAgesEnvironment(data.necromancyZombieAlias, data.zombiesPerNecromancyWave));

                case "FrostbiteEnvironment" ->
                        level.setEnvironment(new FrostbiteEnvironment(data.windIntervalSeconds, data.freezeStacksPerWind));

                default ->
                        System.err.println("[LevelFactory] Unknown environment: " + data.environment);
            }
        }

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
            level.setWaves(getWaves(data));
        }

        return level;
    }

    private static List<Level.Wave> getWaves(JsonContainer.JsonLevelData data) {
        List<Level.Wave> waves = new ArrayList<>();
        if (data.waves == null) return waves;

        for (JsonContainer.JsonWaveData waveData : data.waves) {
            List<Level.SpawnData> spawns = new ArrayList<>();

            if (waveData.spawnData != null) {
                for (JsonContainer.JsonSpawnData spawnEntry : waveData.spawnData) {
                    spawns.add(new Level.SpawnData(
                            spawnEntry.zombieId,
                            spawnEntry.lane,
                            spawnEntry.delaySeconds
                    ));
                }
            }
            waves.add(new Level.Wave(waveData.waveNumber, waveData.cost, spawns));
        }

        return waves;
    }
}