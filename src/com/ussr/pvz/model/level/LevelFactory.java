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
        BEHAVIOR_REGISTRY.put("ConveyorBehavior", ConveyorBehavior::new);
        BEHAVIOR_REGISTRY.put("PlantWhatYouGetBehavior", PlantWhatYouGetBehavior::new);
        BEHAVIOR_REGISTRY.put("BossBehavior", BossBehavior::new);
    }

    public static Level create(JsonContainer.JsonLevelData data) {
        Level level = new Level();
        level.setId(data.id);
        level.setOrder(data.order);
        level.setSunFalling(data.sunFalling);
        level.setTimeLimitSeconds(data.timeLimitSeconds);
        level.setDeadlineColumn(data.deadlineColumn);
        level.setAllowedPlantsLost(data.allowedPlantsLost);
        level.setLockedPlants(data.lockedPlants != null ? data.lockedPlants : new ArrayList<>());
        level.setSeedPlants(data.seedPlants != null ? data.seedPlants : new ArrayList<>());

        // Dynamic Behavior & Minigame Router
        if (data.behavior != null && !data.behavior.isBlank()) {
            switch (data.behavior) {
                case "WallnutBowlingBehavior" ->
                        level.setBehavior(new WallnutBowlingBehavior(data.redLineColumn));

                case "VaseBreakerBehavior" ->
                        level.setBehavior(new VaseBreakerBehavior());

                case "BeghouledBehavior" -> {
                    List<String> startingPlants = data.startingPlants != null ? data.startingPlants : new ArrayList<>();
                    level.setBehavior(new BeghouledBehavior(data.targetMatches, startingPlants));
                }

                case "IZombieBehavior" -> {
                    List<IZombieBehavior.PrePlacedPlant> layouts = new ArrayList<>();
                    if (data.plantLayout != null) {
                        for (JsonContainer.JsonPrePlacedPlant p : data.plantLayout) {
                            layouts.add(new IZombieBehavior.PrePlacedPlant(p.plantName, p.row, p.col));
                        }
                    }
                    level.setBehavior(new IZombieBehavior(data.redLineColumn, data.startingSun, layouts));
                }

                default -> {
                    Supplier<LevelBehavior> supplier = BEHAVIOR_REGISTRY.get(data.behavior);
                    if (supplier != null) {
                        level.setBehavior(supplier.get());
                    } else {
                        System.err.println("[LevelFactory] Unknown behavior: " + data.behavior);
                    }
                }
            }
        }

        //Context-Driven Environment Builder
        if (data.environment != null && !data.environment.isBlank()) {
            switch (data.environment) {
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

        //Allowed Zombies Loader
        if (data.allowedZombies != null) {
            List<Level.AllowedZombie> allowed = new ArrayList<>();
            for (JsonContainer.JsonZombieEntry entry : data.allowedZombies) {
                if (entry.id != null) {
                    allowed.add(new Level.AllowedZombie(entry.id, entry.weight > 0 ? entry.weight : 1000));
                }
            }
            level.setAllowedZombies(allowed);
        }

        //Wave & Spawn Schedule Loader
        if (data.waves != null) {
            level.setWaves(getWaves(data));
        }

        return level;
    }

    /**
     * Maps incoming wave timeline metadata directly into immutable Java Record structures.
     */
    private static List<Level.Wave> getWaves(JsonContainer.JsonLevelData data) {
        List<Level.Wave> waves = new ArrayList<>();

        for (JsonContainer.JsonWaveData waveData : data.waves) {
            List<Level.SpawnData> spawns = new ArrayList<>();

            if (waveData.spawnData != null) {
                for (JsonContainer.JsonSpawnData spawnEntry : waveData.spawnData) {
                    // Safe injection using the Record constructor
                    spawns.add(new Level.SpawnData(
                            spawnEntry.zombieId,
                            spawnEntry.lane,
                            spawnEntry.delaySeconds
                    ));
                }
            }

            // Safe instantiation of the Level.Wave record container
            waves.add(new Level.Wave(waveData.waveNumber, waveData.cost, spawns));
        }

        return waves;
    }
}