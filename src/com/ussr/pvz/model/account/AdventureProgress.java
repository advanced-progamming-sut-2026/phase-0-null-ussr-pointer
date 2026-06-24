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
    private int currentLvl;
    private int plantFoodCount;
    private final Map<String, Integer> plantLvls;
    private final List<String> seenZombies;
    private final Map<String , Integer> seedPackets;
    private final List<Plant> accountPlants;

    public AdventureProgress(int currentLvl, int coin, int gem, Map<String, Integer> plantLvls) {
        this.currentLvl = currentLvl;
        this.coin = coin;
        this.gem = gem;
        this.plantFoodCount = 0;
        this.plantLvls = plantLvls;
        this.seedPackets = new HashMap<>();
        this.seenZombies = new ArrayList<>();
        this.accountPlants = new ArrayList<>();
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
                String plantName = nameObj.toString().trim().toUpperCase();
                if (plantLvls.containsKey(plantName)) {
                    int currentLevel = plantLvls.get(plantName);
                    if (currentLevel > 0) {
                        Plant plantInstance = new Plant();
                        plantInstance.setName(plantName);
                        plantInstance.setLevel(currentLevel);
                        setDefaults(plantInstance , plantData);
                        Object upgradesObj = plantData.get("upgrades");
                        if (upgradesObj instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> upgradesList = (List<Map<String, Object>>) upgradesObj;
                            upgradesList.sort(Comparator.comparingInt(u -> ((Double) u.get("level")).intValue()));
                            for (Map<String, Object> upgrade : upgradesList) {
                                int upgradeLevel = ((Double) upgrade.get("level")).intValue();
                                if (upgradeLevel <= currentLevel) {
                                    String type = (String) upgrade.get("type");
                                    double value = (Double) upgrade.get("value");
                                    applyBuffi(type , plantInstance , value);
                                }
                            }
                        }
                        this.accountPlants.add(plantInstance);
                    }
                }
            }
        }
    }

    private void applyBuffi(String type , Plant plantInstance , double value) {
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
                plantInstance.setRecharge(plantInstance.getRecharge() + (int) value);
                break;
            case "ACTION_INTERVAL":
                plantInstance.setActionInterval(plantInstance.getActionInterval() + value);
                break;
        }
    }

    private void setDefaults(Plant plantInstance , Map<String , Object> plantData) {
        if (plantData.get("id") != null)
            plantInstance.setId(((Double) plantData.get("id")).intValue());
        if (plantData.get("hp") != null)
            plantInstance.setHp(((Double) plantData.get("hp")).intValue());
        if (plantData.get("cost") != null)
            plantInstance.setCost(((Double) plantData.get("cost")).intValue());
        if (plantData.get("damage") != null)
            plantInstance.setDamage(((Double) plantData.get("damage")).intValue());
        if (plantData.get("actionInterval") != null)
            plantInstance.setActionInterval(((Double) plantData.get("actionInterval")));
        if (plantData.get("recharge") != null)
            plantInstance.setRecharge(((Double) plantData.get("recharge")).intValue());
    }

    public void upgradePlant(String plantName) {
        String key = plantName.trim().toUpperCase();
        if (plantLvls.containsKey(key)) {
            plantLvls.put(key, plantLvls.get(key) + 1);
            populateAccountPlants();
        }
    }

    public List<String> getSeenZombies() { return this.seenZombies; }

    public void addSeenZombies(String newSeen) { seenZombies.add(newSeen); }

    public int getCurrentLvl() {
        return this.currentLvl;
    }

    public int getCoin() {
        return this.coin;
    }

    public int getGem() {
        return this.gem;
    }

    public Map<String, Integer> getPlantLvls() {
        return this.plantLvls;
    }

    public List<Plant> getAccountPlants() {
        return this.accountPlants;
    }

    public void addCoin(int amount) {
        this.coin += amount;
    }

    public void addGem(int amount) {
        this.gem += amount;
    }

    public void setCurrentLvl(int level) {
        this.currentLvl = level;
    }

    public int getPlantFoodCount() { return plantFoodCount; }

    public void addPlantFood(int amount) {
        plantFoodCount = Math.min(plantFoodCount + amount, 3);
    }

    public boolean spendPlantFood() {
        if (plantFoodCount <= 0) return false;
        plantFoodCount--;
        return true;
    }

    public Map<String, Integer> getSeedPackets() { return seedPackets; }

    public void addSeedPackets(String plantName, int amount) {
        seedPackets.merge(plantName, amount, Integer::sum);
    }

    public boolean spendSeedPacket(String plantName) {
        int current = seedPackets.getOrDefault(plantName, 0);
        if (current <= 0) return false;
        seedPackets.put(plantName, current - 1);
        return true;
    }

    public static Map<String, Integer> initializePlantsLvl() {
        Map<String, Integer> defaultPlantLevels = new HashMap<>();
        Gson gson = new Gson();

        File allPlantsFile = new File("src/resources/plants.json");
        File defaultUnlockedFile = new File("default_unlocked_plants.json");

        if (!allPlantsFile.exists()) {
            System.err.println("Critical Error: plants.json not found!");
            return defaultPlantLevels;
        }

        List<String> starterPlantNames = new ArrayList<>();
        if (defaultUnlockedFile.exists()) {
            try (FileReader reader = new FileReader(defaultUnlockedFile)) {
                Type simpleListType = new TypeToken<List<String>>() {}.getType();
                List<String> loadedStarters = gson.fromJson(reader, simpleListType);
                if (loadedStarters != null) {
                    starterPlantNames = loadedStarters.stream()
                            .map(name -> name.trim().toUpperCase())
                            .toList();
                }
            } catch (IOException e) {
                System.err.println("Error reading default_unlocked_plants.json: " + e.getMessage());
            }
        }
        try (FileReader reader = new FileReader(allPlantsFile)) {
            Type complexListType = new TypeToken<List<Map<String, Object>>>() {}.getType();
            List<Map<String, Object>> complexPlantsList = gson.fromJson(reader, complexListType);

            if (complexPlantsList != null) {
                for (Map<String, Object> plantData : complexPlantsList) {
                    Object nameObj = plantData.get("name");
                    if (nameObj != null) {
                        String plantName = nameObj.toString().trim().toUpperCase();
                        if (starterPlantNames.contains(plantName)) {
                            defaultPlantLevels.put(plantName, 1);
                        } else {
                            defaultPlantLevels.put(plantName, 0);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading plants.json: " + e.getMessage());
        }
        return defaultPlantLevels;
    }
}