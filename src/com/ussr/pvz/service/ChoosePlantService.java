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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChoosePlantService {

    private static final int MAX_SEED_SLOTS = 8;
    private static final int INITIAL_SUN = 50;
    private static final int LAWN_ROWS = 5;
    private static final int LAWN_COLS = 9;

    private final List<String> selectedPlants = new ArrayList<>();

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

    private static String normalizePlantKey(String rawName) {
        if (rawName == null) return "";
        return rawName.trim().toUpperCase().replace('_', ' ');
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
                        .append(name)
                        .append(" (lvl ").append(lvl).append(")\n");
            }
        }
        sb.append("\nSlots: ").append(selectedPlants.size()).append("/").append(MAX_SEED_SLOTS);
        return sb.toString().trim();
    }

    public String addPlant(PlantTypeRequest request) {
        if (selectedPlants.size() >= MAX_SEED_SLOTS)
            return "seed slots full (" + MAX_SEED_SLOTS + "/" + MAX_SEED_SLOTS + ")";

        String name = normalizePlantKey(request.type());
        AdventureProgress adv = App.getAccount().getAdventureProgress();
        int lvl = adv.getPlantLvls().getOrDefault(name, 0);
        if (lvl == 0) return "you don't have " + name;

        Chapter chapter = App.getLevelManager().getCurrentChapter();
        if (chapter != null && chapter.getAllowedPlants() != null) {
            boolean allowed = chapter.getAllowedPlants().stream()
                    .anyMatch(p -> normalizePlantKey(p).equals(name));
            if (!allowed) return name + " is not allowed in this chapter";
        }

        Level level = App.getLevelManager().getCurrentLevel();
        if (level != null && level.getLockedPlants() != null) {
            boolean locked = level.getLockedPlants().stream()
                    .anyMatch(p -> normalizePlantKey(p).equals(name));
            if (locked) return name + " is locked in this level";
        }

        if (selectedPlants.contains(name)) return name + " is already selected";

        selectedPlants.add(name);
        return name + " added (" + selectedPlants.size() + "/" + MAX_SEED_SLOTS + ")";
    }

    public String removePlant(PlantTypeRequest request) {
        String name = normalizePlantKey(request.type());
        if (!selectedPlants.remove(name))
            return name + " is not in your selection";
        return name + " removed (" + selectedPlants.size() + "/" + MAX_SEED_SLOTS + ")";
    }

    public String boostPlant(PlantTypeRequest request) {
        String name = normalizePlantKey(request.type());
        if (!selectedPlants.contains(name))
            return name + " is not in your selection";

        AdventureProgress adv = App.getAccount().getAdventureProgress();
        Map<String, Integer> seeds = adv.getSeedPackets();
        int available = seeds.getOrDefault(name, 0);
        if (available <= 0)
            return "no seed packets available for " + name;

        adv.spendSeedPacket(name);
        return "seed packet used for " + name + " (" + (available - 1) + " remaining)";
    }

    public String startGame() {
        Level level = App.getLevelManager().getCurrentLevel();
        if (level == null) return "no level selected";

        // Check if it's a standard level that requires plant selection
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
        session.setLevel(level);
        session.addSun(INITIAL_SUN);

        // Bind session globally BEFORE behavior onStart
        App.setGameSession(session);

        // Ensure zombies are loaded before minigames (like IZombie or Vasebreaker) attempt to spawn them
        ZombieFactory.init();

        // Now safe to initialize behavior grids (Vasebreaker, Beghouled, IZombie)
        level.onStart();

        session.initClock();
        App.setMenuState(MenuState.GAME);

        selectedPlants.clear();
        return "game started! sun: " + INITIAL_SUN;
    }

    private Lawn buildLawn(int rows, int cols) {
        Chapter chapter = App.getLevelManager().getCurrentChapter();
        String chapterId = chapter != null ? chapter.getId() : null;
        return TerrainFactory.build(chapterId, rows, cols);
    }
}