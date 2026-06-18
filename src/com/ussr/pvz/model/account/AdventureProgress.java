package com.ussr.pvz.model.account;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdventureProgress {
    private int coin;
    private int gem;
    private int currentLvl;
    private final Map<String, Integer> plantLvls;

    public AdventureProgress(int currentLvl, int coin, int gem, Map<String, Integer> plantLvls) {
        this.currentLvl = currentLvl;
        this.coin = coin;
        this.gem = gem;
        this.plantLvls = plantLvls;
    }

    //getters
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

    //helper
    public static Map<String , Integer> initializePlantsLvl() {
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

