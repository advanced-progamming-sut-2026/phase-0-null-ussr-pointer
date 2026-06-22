package com.ussr.pvz.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.dto.CollectionShowZombieRequest;

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
                        .append(" (Lvl: ").append(userPlants.get(name)).append(")\n").append(plant.get("description")).append("\n");
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
            sb.append("- ").append(plant.get("name")).append("\n").append(plant.get("description")).append("\n");
        }
        return sb.toString().trim();
    }

    public String showPlant(String plantName) {
        List<Map<String, Object>> allPlants = loadConfigFromDisk(PLANTS_PATH);
        String targetName = plantName.trim().toUpperCase();

        for (Map<String, Object> plant : allPlants) {
            if (plant.get("name").toString().toUpperCase().equals(targetName)) {
                StringBuilder sb = new StringBuilder();
                sb.append("Plant Name: ").append(plant.get("name")).append("\n");
                sb.append("HP: ").append(plant.get("hp")).append("\n");
                sb.append("Sun Cost: ").append(plant.get("sunCost")).append("\n");
                sb.append("Cool Down: ").append(plant.get("coolDown")).append("\n");
                if (plant.containsKey("description")) {
                    sb.append("Description: ").append(plant.get("description")).append("\n");
                }
                return sb.toString().trim();
            }
        }
        return "Plant not found in game data.";
    }

    public String showZombies() {
        Account currentUser = App.getAccount();
        if (currentUser == null) return "Please login first.";

        List<String> seenZombies = currentUser.getAdventureProgress().getSeenZombies();
        List<Map<String, Object>> allZombies = loadConfigFromDisk(ZOMBIES_PATH);

        StringBuilder sb = new StringBuilder("--- Encountered Zombies ---\n");
        boolean foundAny = false;
        for (Map<String, Object> zombie : allZombies) {
            String name = zombie.get("name").toString();
            if (seenZombies.contains(name.toUpperCase())) {
                sb.append("- ").append(name);
                if (zombie.containsKey("description")) {
                    sb.append(": ").append(zombie.get("description"));
                }
                sb.append("\n");
                foundAny = true;
            } else {
                sb.append("- [ Empty Frame / Unseen Zombie ]\n");
            }
        }
        if (!foundAny) return "--- Encountered Zombies ---\nYou haven't met any zombies in combat yet!";
        return sb.toString().trim();
    }

    public String showAllZombies() {
        List<Map<String, Object>> allZombies = loadConfigFromDisk(ZOMBIES_PATH);
        if (allZombies.isEmpty()) return "No game configuration found for zombies.";

        StringBuilder sb = new StringBuilder("--- Encyclopedia of All Zombies ---\n");
        for (Map<String, Object> zombie : allZombies) {
            sb.append("- ").append(zombie.get("name")).append("\n");
        }
        return sb.toString().trim();
    }

    public String showZombie(CollectionShowZombieRequest request) {
        Account currentUser = App.getAccount();
        if (currentUser == null) return "Please login first.";

        String targetName = request.zombieName().trim().toUpperCase();
        if (!currentUser.getAdventureProgress().getSeenZombies().contains(targetName)) {
            return "You cannot view details for a zombie you haven't encountered yet!";
        }

        List<Map<String, Object>> allZombies = loadConfigFromDisk(ZOMBIES_PATH);
        for (Map<String, Object> zombie : allZombies) {
            if (zombie.get("name").toString().toUpperCase().equals(targetName)) {
                StringBuilder sb = new StringBuilder();
                sb.append("=== ").append(zombie.get("name")).append(" ===\n");
                sb.append("HP: ").append(zombie.get("hp")).append("\n");
                sb.append("Speed: ").append(zombie.get("speed")).append("\n");
                sb.append("Attack Power: ").append(zombie.get("attackPower")).append("\n");
                sb.append("Description: ").append(zombie.getOrDefault("description", "No description available."));
                return sb.toString();
            }
        }
        return "Zombie type not registered in game parameters.";
    }

    // UTILITY
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

