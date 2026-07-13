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

        // Chapter-effect schedule data (sandstorms, tides, necromancy, wind)
        // is stored directly on Level and consumed by the matching
        // ChapterEffect from ChapterEffectRegistry - see GameSession.
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