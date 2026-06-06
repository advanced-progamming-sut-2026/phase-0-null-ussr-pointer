package model.level;

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
    }

    public static class JsonLevelData {
        public String id;
        public int order;
        public String type;
        public String deliveryStrategy;
        public List<JsonZombieEntry> allowedZombies;
        public List<JsonWaveData> waves;
    }

    public static class JsonZombieEntry {
        public String id;
        public int weight;
    }

    public static class JsonWaveData {
        public int waveNumber;
        public List<JsonSpawnData> spawnData;
    }

    public static class JsonSpawnData {
        public String zombieId;
        public int lane;
        public float delaySeconds;
    }
}
