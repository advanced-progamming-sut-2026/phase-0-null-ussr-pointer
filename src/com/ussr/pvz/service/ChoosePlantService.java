package com.ussr.pvz.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.account.AdventureProgress;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.level.TerrainFactory;
import com.ussr.pvz.model.dto.PlantTypeRequest;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.level.Chapter;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.view.CliGameEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChoosePlantService {

    private static final int MAX_SEED_SLOTS = 8;
    private static final int INITIAL_SUN = 50;
    private static final int LAWN_ROWS = 5;
    private static final int LAWN_COLS = 9;

    private final List<String> selectedPlants = new ArrayList<>();

    public static String normalizePlantKey(String rawName) {
        if (rawName == null) return "";
        String strippedInput = rawName.replaceAll("[\\s_\\-]", "").toUpperCase();

        List<Map<String, Object>> allPlants = App.getCachedPlantsData();
        if (allPlants != null) {
            for (Map<String, Object> p : allPlants) {
                String officialName = p.get("name").toString();
                if (officialName.replaceAll("[\\s_]", "").toUpperCase().equals(strippedInput)) {
                    return strippedInput;
                }
            }
        }
        return rawName.trim().toUpperCase().replace('_', ' ');
    }

    public String showAllPlants() {
        AdventureProgress adv = App.getAccount().getAdventureProgress();
        Map<String, Integer> plantLvls = adv.getPlantLvls();

        StringBuilder sb = new StringBuilder("--- Your Plants ---\n");
        boolean any = false;
        for (Map.Entry<String, Integer> entry : plantLvls.entrySet()) {
            if (entry.getValue() > 0) {
                sb.append("- ").append(entry.getKey())
                        .append(" (lvl ").append(entry.getValue()).append(")\n");
                any = true;
            }
        }
        if (!any) return "you have no unlocked plants yet";
        return sb.toString().trim();
    }

    public String showAvailablePlants() {
        Chapter chapter = App.getLevelManager().getCurrentChapter();
        if (chapter == null) return "no chapter selected";

        List<String> allowed = chapter.getAllowedPlants();
        if (allowed == null || allowed.isEmpty()) return "no plants allowed in this chapter";

        AdventureProgress adv = App.getAccount().getAdventureProgress();
        StringBuilder sb = new StringBuilder("--- Available Plants ---\n");
        for (String name : allowed) {
            String key = normalizePlantKey(name);
            int lvl = adv.getPlantLvls().getOrDefault(key, 0);
            if (lvl > 0) {
                boolean selected = selectedPlants.contains(key);
                sb.append(selected ? "[✓] " : "[ ] ")
                        .append(key)
                        .append(" (lvl ").append(lvl).append(")\n");
            }
        }
        sb.append("\nSlots: ").append(selectedPlants.size()).append("/").append(MAX_SEED_SLOTS);
        return sb.toString().trim();
    }

    public String addPlant(PlantTypeRequest request) {
        if (selectedPlants.size() >= MAX_SEED_SLOTS)
            return "seed slots full (" + MAX_SEED_SLOTS + "/" + MAX_SEED_SLOTS + ")";

        String canonicalName = normalizePlantKey(request.type());
        AdventureProgress adv = App.getAccount().getAdventureProgress();
        int lvl = adv.getPlantLvls().getOrDefault(canonicalName, 0);
        if (lvl == 0) return "you don't have " + canonicalName + " unlocked.";

        Chapter chapter = App.getLevelManager().getCurrentChapter();
        if (chapter != null && chapter.getAllowedPlants() != null) {
            boolean allowed = chapter.getAllowedPlants().stream()
                    .anyMatch(p -> normalizePlantKey(p).equals(canonicalName));
            if (!allowed) return canonicalName + " is not allowed in this chapter";
        }

        Level level = App.getLevelManager().getCurrentLevel();
        if (level != null && level.getLockedPlants() != null) {
            boolean locked = level.getLockedPlants().stream()
                    .anyMatch(p -> normalizePlantKey(p).equals(canonicalName));
            if (locked) return canonicalName + " is locked in this level";
        }

        if (selectedPlants.contains(canonicalName)) return canonicalName + " is already selected";

        selectedPlants.add(canonicalName);
        return canonicalName + " added (" + selectedPlants.size() + "/" + MAX_SEED_SLOTS + ")";
    }

    public String removePlant(PlantTypeRequest request) {
        String canonicalName = normalizePlantKey(request.type());
        if (!selectedPlants.remove(canonicalName))
            return canonicalName + " is not in your selection";
        return canonicalName + " removed (" + selectedPlants.size() + "/" + MAX_SEED_SLOTS + ")";
    }

    public String boostPlant(PlantTypeRequest request) {
        String canonicalName = normalizePlantKey(request.type());
        if (!selectedPlants.contains(canonicalName))
            return canonicalName + " is not in your selection";

        AdventureProgress adv = App.getAccount().getAdventureProgress();
        Map<String, Integer> seeds = adv.getSeedPackets();
        int available = seeds.getOrDefault(canonicalName, 0);
        if (available <= 0)
            return "no seed packets available for " + canonicalName;

        adv.spendSeedPacket(canonicalName);
        return "seed packet used for " + canonicalName + " (" + (available - 1) + " remaining)";
    }

    public String startGame() {
        Level level = App.getLevelManager().getCurrentLevel();
        if (level == null) return "no level selected";

        boolean requiresSelection = !(level.getDeliveryStrategy() instanceof com.ussr.pvz.model.level.delivery.ConveyorDeliveryStrategy)
                && !(level.getBehavior() instanceof com.ussr.pvz.model.level.behavior.BeghouledBehavior)
                && !(level.getBehavior() instanceof com.ussr.pvz.model.level.behavior.WallnutBowlingBehavior)
                && !(level.getBehavior() instanceof com.ussr.pvz.model.level.behavior.VaseBreakerBehavior)
                && !(level.getBehavior() instanceof com.ussr.pvz.model.level.behavior.IZombieBehavior);

        if (requiresSelection && selectedPlants.isEmpty()) {
            return "select at least one plant before starting";
        }

        Lawn lawn = buildLawn(LAWN_ROWS, LAWN_COLS);

        GameSession session = new GameSession();
        session.setLawn(lawn);
        session.setPlants(new ArrayList<>());
        session.setZombies(new ArrayList<>());
        session.setItems(new ArrayList<>());
        session.setLevel(level);
        session.addSun(INITIAL_SUN);
        session.setProgressTracked(!App.isCheatedLevel());
        App.setGameSession(session);

        // Register the CLI View layer to listen to engine events
        new CliGameEventListener(session);

        ZombieFactory.init();
        level.onStart();
        session.initClock();
        App.setMenuState(MenuState.GAME);
        session.setSelectedPlants(selectedPlants);
        selectedPlants.clear();
        return "game started! sun: " + INITIAL_SUN;
    }

    private Lawn buildLawn(int rows, int cols) {
        Chapter chapter = App.getLevelManager().getCurrentChapter();
        String chapterId = chapter != null ? chapter.getId() : null;
        return TerrainFactory.build(chapterId, rows, cols);
    }
}