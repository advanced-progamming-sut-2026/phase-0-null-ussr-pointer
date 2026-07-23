package com.ussr.pvz.model.account;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.entities.plants.Plant;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class AdventureProgress {
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
                             int gem, Map<String, Integer> rawPlantLvls, List<String> completedLevels) {
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
                    this.plantLvls.put(normalizeKey(entry.getKey()), entry.getValue());
                }
            }
        }

        this.seedPackets = new HashMap<>();
        this.seenZombies = new ArrayList<>();
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
                        Plant plantInstance = new Plant();
                        plantInstance.setName(displayName);
                        plantInstance.setLevel(currentLevel);
                        setDefaults(plantInstance, plantData);
                        Object upgradesObj = plantData.get("upgrades");
                        if (upgradesObj instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> upgradesList = (List<Map<String, Object>>) upgradesObj;
                            upgradesList.sort(Comparator.comparingInt(u -> ((Number) u.get("level")).intValue()));
                            for (Map<String, Object> upgrade : upgradesList) {
                                int upgradeLevel = ((Number) upgrade.get("level")).intValue();
                                if (upgradeLevel <= currentLevel) {
                                    String type = (String) upgrade.get("type");
                                    double value = ((Number) upgrade.get("value")).doubleValue();
                                    applyBuffi(type, plantInstance, value);
                                }
                            }
                        }
                        this.accountPlants.add(plantInstance);
                    }
                }
            }
        }
    }

    private void applyBuffi(String type, Plant plantInstance, double value) {
        switch (type.toUpperCase()) {
            case "HP":
                plantInstance.setHp(plantInstance.getHp() + (int) value);
                break;
            case "DAMAGE":
                plantInstance.setDamage(plantInstance.getDamage() + (int) value);
                break;
            case "COST":
                plantInstance.setCost(plantInstance.getCost() + (int) value);
                break;
            case "RECHARGE":
                plantInstance.setMaxRecharge(Math.max(0.0, plantInstance.getMaxRecharge() + value));
                break;
            case "ACTION_INTERVAL":
                plantInstance.setActionInterval(plantInstance.getActionInterval() + value);
                break;
        }
    }

    private void setDefaults(Plant plantInstance, Map<String, Object> plantData) {
        if (plantData.get("id") != null)
            plantInstance.setId(((Number) plantData.get("id")).intValue());

        if (plantData.get("baseHp") != null)
            plantInstance.setHp(((Number) plantData.get("baseHp")).intValue());

        if (plantData.get("cost") != null)
            plantInstance.setCost(((Number) plantData.get("cost")).intValue());
        if (plantData.get("damage") != null)
            plantInstance.setDamage(((Number) plantData.get("damage")).intValue());
        if (plantData.get("actionInterval") != null)
            plantInstance.setActionInterval(((Number) plantData.get("actionInterval")).doubleValue());
        if (plantData.get("recharge") != null)
            plantInstance.setRecharge(((Number) plantData.get("recharge")).intValue());
        if (plantData.get("abilityValue") != null)
            plantInstance.setAbilityValue(((Number) plantData.get("abilityValue")).doubleValue());
        if (plantData.get("wramp-up") != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> wrampUpList = (List<Map<String, Object>>) plantData.get("wramp-up");
            plantInstance.setWrampUp(wrampUpList);
        }
    }

    public void upgradePlant(String plantName) {
        String key = normalizeKey(plantName);
        if (plantLvls.containsKey(key)) {
            plantLvls.put(key, plantLvls.get(key) + 1);
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
        Gson gson = new Gson();

        File defaultUnlockedFile = new File("default_unlocked_plants.json");

        List<String> starterPlantNames = new ArrayList<>();
        if (defaultUnlockedFile.exists()) {
            try (FileReader reader = new FileReader(defaultUnlockedFile)) {
                Type simpleListType = new TypeToken<List<String>>() {}.getType();
                List<String> loadedStarters = gson.fromJson(reader, simpleListType);
                if (loadedStarters != null) {
                    starterPlantNames = loadedStarters.stream()
                            .map(AdventureProgress::normalizeKey)
                            .toList();
                }
            } catch (IOException e) {
                System.err.println("Error reading default_unlocked_plants.json: " + e.getMessage());
            }
        }

        if (App.getCachedPlantsData() == null) {
            App.loadPlantsDataToMemory();
        }

        List<Map<String, Object>> complexPlantsList = App.getCachedPlantsData();
        if (complexPlantsList != null) {
            for (Map<String, Object> plantData : complexPlantsList) {
                Object nameObj = plantData.get("name");
                if (nameObj != null) {
                    String plantName = normalizeKey(nameObj.toString());
                    defaultPlantLevels.put(plantName, 1);
                }
            }
        }
        return defaultPlantLevels;
    }
}