package com.ussr.pvz.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.AdventureProgress;
import com.ussr.pvz.model.dto.CollectionShowZombieRequest;
import com.ussr.pvz.model.dto.PlantTypeRequest;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CollectionService {
    private final Gson gson = new Gson();
    private static final String PLANTS_PATH = "src/resources/plants.json";
    private static final String ZOMBIES_PATH = "src/resources/zombies.json";

    public String showPlants() {
        Account current = App.getAccount();
        if (current == null)
            return "Please login first";

        Map<String, Integer> userPlants = current.getAdventureProgress().getPlantLvls();
        List<Map<String, Object>> allPlants = loadConfigFromDisk(PLANTS_PATH);

        StringBuilder sb = new StringBuilder("--- Earned Plants ---\n");
        boolean foundAny = false;

        for (Map<String, Object> plant : allPlants) {
            String name = plant.get("name").toString().toUpperCase();
            if (userPlants.containsKey(name) && userPlants.get(name) > 0) {
                sb.append("- ").append(plant.get("name"))
                        .append(" (Lvl: ").append(userPlants.get(name)).append(")\n");
                foundAny = true;
            }
        }
        if (!foundAny) return "You haven't unlocked any plants yet!";
        return sb.toString().trim();
    }

    public String showAllPlants() {
        List<Map<String, Object>> allPlants = loadConfigFromDisk(PLANTS_PATH);
        StringBuilder sb = new StringBuilder("--- All Game Plants ---\n");

        for (Map<String, Object> plant : allPlants) {
            sb.append("- ").append(plant.get("name")).append("\n");
        }
        return sb.toString().trim();
    }

    public String showPlant(String plantName) {
        List<Map<String, Object>> allPlants = loadConfigFromDisk(PLANTS_PATH);
        String targetName = plantName.trim().toUpperCase();

        for (Map<String, Object> plant : allPlants) {
            if (plant.get("name").toString().toUpperCase().equals(targetName)) {
                String sb = "Plant Name: " + plant.get("name") + "\n" +
                        "HP: " + plant.get("baseHp") + "\n" +
                        "Sun Cost: " + plant.get("cost") + "\n" +
                        "Cool Down: " + plant.get("recharge") + "\n" +
                        "Damage: " + plant.get("damage") + "\n";
                return sb.trim();
            }
        }
        return "Plant not found in game data.";
    }

    @SuppressWarnings("unchecked")
    public String showZombies() {
        Account currentUser = App.getAccount();
        if (currentUser == null) return "Please login first.";

        List<String> seenZombies = currentUser.getAdventureProgress().getSeenZombies().stream()
                .map(String::toUpperCase)
                .toList();
        List<Map<String, Object>> allZombies = loadConfigFromDisk(ZOMBIES_PATH);

        StringBuilder sb = new StringBuilder("--- Encountered Zombies ---\n");
        boolean foundAny = false;

        for (Map<String, Object> zombie : allZombies) {
            List<String> aliases = (List<String>) zombie.get("aliases");
            if (aliases == null || aliases.isEmpty()) continue;

            String name = aliases.getFirst();
            if (seenZombies.contains(name.toUpperCase())) {
                sb.append("- ").append(name).append("\n");
                foundAny = true;
            } else {
                sb.append("- [ Empty Frame / Unseen Zombie ]\n");
            }
        }
        if (!foundAny) return "--- Encountered Zombies ---\nYou haven't met any zombies in combat yet!";
        return sb.toString().trim();
    }

    @SuppressWarnings("unchecked")
    public String showAllZombies() {
        List<Map<String, Object>> allZombies = loadConfigFromDisk(ZOMBIES_PATH);
        if (allZombies.isEmpty()) return "No game configuration found for zombies.";

        StringBuilder sb = new StringBuilder("--- Encyclopedia of All Zombies ---\n");
        for (Map<String, Object> zombie : allZombies) {
            List<String> aliases = (List<String>) zombie.get("aliases");
            if (aliases != null && !aliases.isEmpty()) {
                sb.append("- ").append(aliases.getFirst()).append("\n");
            }
        }
        return sb.toString().trim();
    }

    @SuppressWarnings("unchecked")
    public String showZombie(CollectionShowZombieRequest request) {
        Account currentUser = App.getAccount();
        if (currentUser == null) return "Please login first.";

        String targetName = request.zombieName().trim().toUpperCase();
        List<String> seenUpper = currentUser.getAdventureProgress().getSeenZombies().stream()
                .map(String::toUpperCase)
                .toList();

        if (!seenUpper.contains(targetName)) {
            return "You cannot view details for a zombie you haven't encountered yet!";
        }

        List<Map<String, Object>> allZombies = loadConfigFromDisk(ZOMBIES_PATH);
        for (Map<String, Object> zombie : allZombies) {
            List<String> aliases = (List<String>) zombie.get("aliases");
            if (aliases != null && !aliases.isEmpty() && aliases.getFirst().toUpperCase().equals(targetName)) {
                Map<String, Object> objdata = (Map<String, Object>) zombie.get("objdata");

                String sb = "=== " + aliases.getFirst() + " ===\n" +
                        "HP: " + objdata.get("Hitpoints") + "\n" +
                        "Speed: " + objdata.get("Speed") + "\n" +
                        "Attack Power (DPS): " + objdata.get("EatDPS") + "\n";
                return sb.trim();
            }
        }
        return "Zombie type not registered in game parameters.";
    }

    //todo check the seed pack cost and etc base on doc
    public String upgradePlant(PlantTypeRequest request) {
        Account account = App.getAccount();
        if (account == null) return "Please login first.";
        AdventureProgress progress = account.getAdventureProgress();

        String plantName = request.type().toUpperCase();
        int currentLevel = progress.getPlantLvls().getOrDefault(plantName, 0);

        if (currentLevel == 0) return "Error: You do not own this plant yet.";
        if (currentLevel >= 4) return "Error: Plant is already at max level.";

        int coinCost = currentLevel * 1000;
        int packetCost = currentLevel * 10;

        if (progress.getCoin() < coinCost) return "Error: Not enough coins. Need " + coinCost;
        int currentPackets = progress.getSeedPackets().getOrDefault(plantName, 0);
        if (currentPackets < packetCost) return "Error: Not enough seed packets. Need " + packetCost;

        progress.addCoin(-coinCost);
        progress.getSeedPackets().put(plantName, currentPackets - packetCost);
        progress.upgradePlant(plantName);

        return "Success! " + plantName + " upgraded to level " + (currentLevel + 1) + ".";
    }

    public String purchasePlant(PlantTypeRequest request) {
        Account account = App.getAccount();
        if (account == null) return "Please login first.";
        AdventureProgress progress = account.getAdventureProgress();

        String plantName = request.type().toUpperCase();
        int currentLevel = progress.getPlantLvls().getOrDefault(plantName, 0);

        if (currentLevel > 0) return "Error: You already own this plant.";
        if (progress.getCoin() < 2000) return "Error: Not enough coins to purchase. Cost is 2,000 coins.";

        progress.addCoin(-2000);

        progress.getPlantLvls().put(plantName, 0);
        progress.upgradePlant(plantName);

        return "Success! " + plantName + " purchased and added to your collection.";
    }

    // UTILITY
    private List<Map<String, Object>> loadConfigFromDisk(String path) {
        File file = new File(path);
        if (!file.exists()) return new ArrayList<>();
        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<Map<String, Object>>>() {
            }.getType();
            List<Map<String, Object>> data = gson.fromJson(reader, listType);
            return data != null ? data : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
}