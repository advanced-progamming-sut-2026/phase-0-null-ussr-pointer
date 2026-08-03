package com.ussr.pvz.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.AdventureProgress;
import com.ussr.pvz.model.dto.PlantTypeRequest;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CollectionService {
    private final Gson gson = new Gson();
    private static final String PLANTS_PATH = "src/resources/plants.json";
    private static final String ZOMBIES_PATH = "src/resources/zombies.json";

    public static class PlantData {
        public String id;
        public String name;
        public String category;
        public int level;
        public int ownedPackets;
        public int cost;
        public int damage;
        public int baseHp;
        public int recharge;
        public String pamPath;
    }

    public static class ZombieData {
        public String name;
        public boolean encountered;
        public int hitpoints;
        public double speed;
        public int eatDPS;
        public String pamPath;
        public String textureRegion; // Added property
    }

    @SuppressWarnings("unchecked")
    public List<ZombieData> getZombieDataForGUI() {
        Account current = App.getAccount();
        List<String> seenZombies = current != null ?
                current.getAdventureProgress().getSeenZombies().stream().map(String::toUpperCase).toList() : new ArrayList<>();

        List<Map<String, Object>> allZombies = loadConfigFromDisk(ZOMBIES_PATH);
        List<ZombieData> result = new ArrayList<>();

        for (Map<String, Object> zombieMap : allZombies) {
            List<String> aliases = (List<String>) zombieMap.get("aliases");
            if (aliases == null || aliases.isEmpty()) continue;

            ZombieData data = new ZombieData();
            data.name = aliases.getFirst();
            data.encountered = seenZombies.contains(data.name.toUpperCase());

            // Check for Texture Region
            if (zombieMap.containsKey("textureRegion")) {
                data.textureRegion = zombieMap.get("textureRegion").toString();
            }

            // Check for PAM Location with fallback
            if (zombieMap.containsKey("pamLocation") && !zombieMap.get("pamLocation").toString().isEmpty()) {
                data.pamPath = zombieMap.get("pamLocation").toString();
            } else if (zombieMap.containsKey("pamPath") && !zombieMap.get("pamPath").toString().isEmpty()) {
                data.pamPath = zombieMap.get("pamPath").toString();
            } else {
                String sanitizedName = data.name.toUpperCase().replace(" ", "_");
                data.pamPath = "768/INITIAL/ZOMBIE/" + sanitizedName + "/" + sanitizedName + ".PAM";
            }

            Map<String, Object> objData = (Map<String, Object>) zombieMap.get("objdata");
            if (objData != null) {
                data.hitpoints = ((Double) objData.getOrDefault("Hitpoints", 0.0)).intValue();
                data.speed = (Double) objData.getOrDefault("Speed", 0.0);
                data.eatDPS = ((Double) objData.getOrDefault("EatDPS", 0.0)).intValue();
            }

            result.add(data);
        }
        return result;
    }

    public List<PlantData> getPlantDataForGUI() {
        Account current = App.getAccount();
        Map<String, Integer> userPlants = current != null ? current.getAdventureProgress().getPlantLvls() : null;
        Map<String, Integer> userPackets = current != null ? current.getAdventureProgress().getSeedPackets() : null;

        List<Map<String, Object>> allPlants = loadConfigFromDisk(PLANTS_PATH);
        List<PlantData> result = new ArrayList<>();

        for (Map<String, Object> plantMap : allPlants) {
            PlantData data = new PlantData();
            data.name = plantMap.get("name").toString();
            data.id = ChoosePlantService.normalizePlantKey(data.name);
            data.category = plantMap.getOrDefault("category", "UNKNOWN").toString();

            if (userPlants != null && userPlants.containsKey(data.id) && userPlants.get(data.id) > 0) {
                data.level = userPlants.get(data.id);
            } else {
                data.level = 0;
            }

            data.ownedPackets = (userPackets != null) ? userPackets.getOrDefault(data.id, 0) : 0;
            data.cost = ((Double) plantMap.getOrDefault("cost", 0.0)).intValue();
            data.damage = ((Double) plantMap.getOrDefault("damage", 0.0)).intValue();
            data.baseHp = ((Double) plantMap.getOrDefault("baseHp", 0.0)).intValue();
            data.recharge = ((Double) plantMap.getOrDefault("recharge", 0.0)).intValue();

            if (plantMap.containsKey("pamLocation")) {
                data.pamPath = plantMap.get("pamLocation").toString();
            } else {
                String sanitizedName = data.name.toUpperCase().replace(" ", "_").replace("-", "");
                data.pamPath = "768/INITIAL/PLANTS/" + sanitizedName + "/" + sanitizedName + ".PAM";
            }

            result.add(data);
        }
        return result;
    }

    public String upgradePlant(PlantTypeRequest request) {
        Account account = App.getAccount();
        if (account == null) return "Please login first.";
        AdventureProgress progress = account.getAdventureProgress();

        String canonicalName = ChoosePlantService.normalizePlantKey(request.type());
        int currentLevel = progress.getPlantLvls().getOrDefault(canonicalName, 0);

        if (currentLevel == 0 || currentLevel >= 4) return "Cannot upgrade.";

        int coinCost = currentLevel * 1000;
        int packetCost = currentLevel * 10;

        if (progress.getCoin() < coinCost || progress.getSeedPackets().getOrDefault(canonicalName, 0) < packetCost) {
            return "Not enough resources.";
        }

        progress.addCoin(-coinCost);
        progress.getSeedPackets().put(canonicalName, progress.getSeedPackets().get(canonicalName) - packetCost);
        progress.upgradePlant(canonicalName);

        return "Upgraded!";
    }

    public String purchasePlant(PlantTypeRequest request) {
        Account account = App.getAccount();
        if (account == null) return "Please login first.";
        AdventureProgress progress = account.getAdventureProgress();

        String canonicalName = ChoosePlantService.normalizePlantKey(request.type());

        if (progress.getPlantLvls().getOrDefault(canonicalName, 0) > 0) return "Already owned.";
        if (progress.getCoin() < 2000) return "Not enough coins.";

        progress.addCoin(-2000);
        progress.getPlantLvls().put(canonicalName, 0);
        progress.upgradePlant(canonicalName);

        return "Purchased!";
    }

    private List<Map<String, Object>> loadConfigFromDisk(String path) {
        File file = new File(path);
        if (!file.exists()) return new ArrayList<>();
        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
            List<Map<String, Object>> data = gson.fromJson(reader, listType);
            return data != null ? data : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
}