package com.ussr.pvz.model.level;

import java.util.List;

public class JsonContainer {

    public static class JsonWorldData {
        public List<JsonChapterData> chapters;
    }

    public static class JsonChapterData {
        public String id;
        public String name;
        public String gameMode;
        public List<String> allowedPlants;
        public List<JsonLevelData> levels;
        public List<String> allowedTiles;
    }

    public static class JsonLevelData {
        public String id;
        public int order;
        public String deliveryStrategy;

        public boolean sunFalling = true;
        public int timeLimitSeconds = 0;
        public int deadlineColumn = -1;
        public int allowedPlantsLost = -1;
        public String environment;
        public List<String> lockedPlants;
        public List<String> seedPlants;
        public String behavior;

        // === Timed War Parameters ===
        public String timedWarType;
        public int timedWarTarget;

        public List<JsonZombieEntry> allowedZombies;
        public List<JsonWaveData> waves;

        // === Ancient Egypt Parameters ===
        public List<JsonSandstormEvent> sandstorms;

        // === Big Wave Beach Parameters ===
        public int startingTideColumn = 9;
        public List<JsonTideEvent> tides;

        // === Dark Ages Parameters ===
        public String necromancyZombieAlias;
        public int zombiesPerNecromancyWave = 0;

        // === Frostbite Caves Parameters ===
        public double windIntervalSeconds = 0.0;
        public int freezeStacksPerWind = 0;

        // === Minigame Strategy Parameters ===
        public int redLineColumn = 2;
        public int startingSun = 150;
        public int targetMatches = 20;
        public List<String> startingPlants;
        public List<JsonPrePlacedPlant> plantLayout;
    }

    public static class JsonZombieEntry {
        public String id;
        public int weight;
    }

    public static class JsonWaveData {
        public int waveNumber;
        public int cost;
        public List<JsonSpawnData> spawnData;
    }

    public static class JsonSpawnData {
        public String zombieId;
        public int lane;
        public float delaySeconds;
    }

    public static class JsonSandstormEvent {
        public double triggerTimeSeconds;
        public String zombieAlias;
    }

    public static class JsonTideEvent {
        public double triggerTimeSeconds;
        public int targetColumn;
    }

    public static class JsonPrePlacedPlant {
        public String plantName;
        public int row;
        public int col;
    }
}