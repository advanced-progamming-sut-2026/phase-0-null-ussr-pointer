package com.ussr.pvz.model.account;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.PlantFactory;
import com.ussr.pvz.model.level.Chapter;
import com.ussr.pvz.model.level.GameMode;
import com.ussr.pvz.model.level.Level;

import java.util.*;

public class AdventureProgress {
    public static final int MAX_PLANT_LEVEL = 4;
    private int coin;
    private int gem;
    private int currentChapter;
    private int currentLvl;
    private int minigamesWon;
    private int questsCompleted;
    private int plantFoodCount;
    private final Map<String, Integer> plantLvls;
    private final List<String> seenZombies;
    private final Map<String, Integer> seedPackets;
    private final List<Plant> accountPlants;
    private final List<String> completedLevels; // <--- ADDED FIELD

    // A static normalizer so it's fully self-contained and accessible anywhere
    public static String normalizeKey(String rawKey) {
        if (rawKey == null) return "";
        return rawKey.trim().toUpperCase().replaceAll("[\\s_]", "");
    }

    // UPDATED CONSTRUCTOR to accept completedLevels
    public AdventureProgress(int currentChapter, int currentLvl, int minigamesWon, int questsCompleted, int coin,
                             int gem, Map<String, Integer> rawPlantLvls, List<String> completedLevels
            ,Map<String, Integer> seedPackets, List<String> seenZombies) {
        this.currentChapter = currentChapter;
        this.currentLvl = currentLvl;
        this.minigamesWon = minigamesWon;
        this.questsCompleted = questsCompleted;
        this.coin = coin;
        this.gem = gem;
        this.plantFoodCount = 0;

        // Rebuild the map to normalize any raw keys
        this.plantLvls = new HashMap<>();
        if (rawPlantLvls != null) {
            for (Map.Entry<String, Integer> entry : rawPlantLvls.entrySet()) {
                if (entry.getKey() != null) {
                    int savedLevel = entry.getValue() == null ? 0 : entry.getValue();
                    this.plantLvls.put(
                            normalizeKey(entry.getKey()),
                            Math.max(0, Math.min(savedLevel, MAX_PLANT_LEVEL))
                    );
                }
            }
        }

        this.seedPackets = seedPackets;
        this.seenZombies = seenZombies;
        this.accountPlants = new ArrayList<>();

        // Initialize completed levels list
        this.completedLevels = new ArrayList<>();
        if (completedLevels != null) {
            this.completedLevels.addAll(completedLevels);
        }

        populateAccountPlants();
    }

    private void populateAccountPlants() {
        this.accountPlants.clear();

        if (App.getCachedPlantsData() == null) {
            App.loadPlantsDataToMemory();
        }
        List<Map<String, Object>> complexPlantsList = App.getCachedPlantsData();
        if (complexPlantsList != null) {
            for (Map<String, Object> plantData : complexPlantsList) {
                Object nameObj = plantData.get("name");
                if (nameObj == null) continue;
                String displayName = nameObj.toString();
                String plantName = normalizeKey(displayName);
                if (plantLvls.containsKey(plantName)) {
                    int currentLevel = plantLvls.get(plantName);
                    if (currentLevel > 0) {
                        this.accountPlants.add(
                                PlantFactory.createPlantByName(displayName, currentLevel)
                        );
                    }
                }
            }
        }
    }

    public void upgradePlant(String plantName) {
        String key = normalizeKey(plantName);
        if (plantLvls.containsKey(key)) {
            int currentLevel = plantLvls.getOrDefault(key, 0);
            plantLvls.put(key, Math.min(currentLevel + 1, MAX_PLANT_LEVEL));
            populateAccountPlants();
        }
    }

    // --- ADDED HELPERS FOR COMPLETED LEVELS ---
    public List<String> getCompletedLevels() {
        return this.completedLevels;
    }

    public void addCompletedLevel(String levelId) {
        if (levelId != null && !this.completedLevels.contains(levelId)) {
            this.completedLevels.add(levelId);
        }
    }

    public boolean isLevelCompleted(String levelId) {
        return levelId != null && this.completedLevels.contains(levelId);
    }

    /**
     * Adventure chapters form one ordered road. The first chapter is open;
     * every later chapter requires every level in the previous chapter.
     */
    public boolean isChapterUnlocked(Chapter chapter, List<Chapter> allChapters) {
        if (chapter == null) return false;
        if (chapter.getGameMode() != GameMode.ADVENTURE) return true;
        if (allChapters == null) return false;

        List<Chapter> adventureChapters = allChapters.stream()
                .filter(candidate -> candidate.getGameMode() == GameMode.ADVENTURE)
                .toList();
        int chapterIndex = adventureChapters.indexOf(chapter);
        if (chapterIndex < 0) return false;
        if (chapterIndex == 0) return true;

        Chapter previousChapter = adventureChapters.get(chapterIndex - 1);
        return !previousChapter.getLevels().isEmpty()
                && previousChapter.getLevels().stream()
                .allMatch(level -> isLevelCompleted(level.getId()));
    }

    /** The first level is open with its chapter; later levels require the previous level. */
    public boolean isLevelUnlocked(Chapter chapter, Level level, List<Chapter> allChapters) {
        if (chapter == null || level == null || !isChapterUnlocked(chapter, allChapters)) {
            return false;
        }
        if (isLevelCompleted(level.getId())) return true;

        List<Level> orderedLevels = chapter.getLevels().stream()
                .sorted(Comparator.comparingInt(Level::getOrder))
                .toList();
        int levelIndex = orderedLevels.indexOf(level);
        if (levelIndex < 0) return false;
        return levelIndex == 0
                || isLevelCompleted(orderedLevels.get(levelIndex - 1).getId());
    }
    // ------------------------------------------

    public List<String> getSeenZombies() {
        return this.seenZombies;
    }

    public void addSeenZombies(String newSeen) {
        seenZombies.add(newSeen);
    }

    public int getCurrentChapter() {
        return currentChapter;
    }

    public void setCurrentChapter(int chapter) {
        this.currentChapter = chapter;
    }

    public int getCurrentLvl() {
        return this.currentLvl;
    }

    public void setCurrentLvl(int level) {
        this.currentLvl = level;
    }

    public int getMinigamesWon() {
        return minigamesWon;
    }

    public void incrementMinigamesWon() {
        this.minigamesWon++;
    }

    public void setMinigamesWon(int count) {
        this.minigamesWon = count;
    }

    public int getQuestsCompleted() {
        return questsCompleted;
    }

    public void incrementQuestsCompleted() {
        this.questsCompleted++;
    }

    public void setQuestsCompleted(int count) {
        this.questsCompleted = count;
    }

    public int getCoin() {
        return this.coin;
    }

    public void addCoin(int amount) {
        this.coin += amount;
    }

    public int getGem() {
        return this.gem;
    }

    public void addGem(int amount) {
        this.gem += amount;
    }

    public Map<String, Integer> getPlantLvls() {
        return this.plantLvls;
    }

    public List<Plant> getAccountPlants() {
        return this.accountPlants;
    }

    public int getPlantFoodCount() {
        return plantFoodCount;
    }

    public void addPlantFood(int amount) {
        plantFoodCount = Math.min(plantFoodCount + amount, 3);
    }

    public boolean spendPlantFood() {
        if (plantFoodCount <= 0) return false;
        plantFoodCount--;
        return true;
    }

    public Map<String, Integer> getSeedPackets() {
        return seedPackets;
    }

    public void addSeedPackets(String plantName, int amount) {
        seedPackets.merge(normalizeKey(plantName), amount, Integer::sum);
    }

    public boolean spendSeedPacket(String plantName) {
        String normalizedKey = normalizeKey(plantName);
        int current = seedPackets.getOrDefault(normalizedKey, 0);
        if (current <= 0) return false;
        seedPackets.put(normalizedKey, current - 1);
        return true;
    }

    public static Map<String, Integer> initializePlantsLvl() {
        Map<String, Integer> defaultPlantLevels = new HashMap<>();

        if (App.getCachedPlantsData() == null) {
            App.loadPlantsDataToMemory();
        }

        List<Map<String, Object>> complexPlantsList = App.getCachedPlantsData();
        if (complexPlantsList != null) {
            for (Map<String, Object> plantData : complexPlantsList) {
                Object nameObj = plantData.get("name");
                if (nameObj != null) {
                    String plantName = normalizeKey(nameObj.toString());
                    defaultPlantLevels.put(plantName, 0);
                }
            }
        }
        return defaultPlantLevels;
    }
}
