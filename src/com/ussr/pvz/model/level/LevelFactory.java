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
        BEHAVIOR_REGISTRY.put("ConveyorBehavior", ConveyorBehavior::new);
        BEHAVIOR_REGISTRY.put("SaveOurSeedsBehavior", SaveOurSeedsBehavior::new);
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

        if (data.behavior != null && !data.behavior.isBlank()) {
            Supplier<LevelBehavior> supplier = BEHAVIOR_REGISTRY.get(data.behavior);
            if (supplier != null) {
                level.setBehavior(supplier.get());
            } else {
                System.err.println("[LevelFactory] Unknown behavior: " + data.behavior);
            }
        }

        if (data.allowedZombies != null) {
            List<String> zombieIds = new ArrayList<>();
            for (JsonContainer.JsonZombieEntry entry : data.allowedZombies) {
                if (entry.id != null) zombieIds.add(entry.id);
            }
            level.setAllowedZombies(zombieIds);
        }

        if (data.waves != null) {
            List<Wave> waves = new ArrayList<>();
            for (JsonContainer.JsonWaveData waveData : data.waves) {
                Wave wave = new Wave();
                wave.setWaveNumber(waveData.waveNumber);

                if (waveData.spawnData != null) {
                    List<SpawnData> spawns = new ArrayList<>();
                    for (JsonContainer.JsonSpawnData spawnEntry : waveData.spawnData) {
                        SpawnData spawn = new SpawnData();
                        spawn.setZombieId(spawnEntry.zombieId);
                        spawn.setLane(spawnEntry.lane);
                        spawn.setDelaySeconds(spawnEntry.delaySeconds);
                        spawns.add(spawn);
                    }
                    wave.setSpawnData(spawns);
                }
                waves.add(wave);
            }
            level.setWaves(waves);
        }
        return level;
    }
}