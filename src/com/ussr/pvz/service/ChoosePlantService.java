package com.ussr.pvz.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.account.AdventureProgress;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.level.TerrainFactory;
import com.ussr.pvz.model.dto.PlantTypeRequest;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.level.Chapter;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.level.behavior.BeghouledBehavior;
import com.ussr.pvz.model.level.behavior.IZombieBehavior;
import com.ussr.pvz.model.level.behavior.VaseBreakerBehavior;
import com.ussr.pvz.model.level.behavior.WallnutBowlingBehavior;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChoosePlantService {

    private static final int MAX_SEED_SLOTS = 8;
    private static final int INITIAL_SUN = 50;
    private static final int LAWN_ROWS = 5;
    private static final int LAWN_COLS = 9;

    private final List<String> selectedPlants = new ArrayList<>();
    private final List<String> boostedPlants = new ArrayList<>();

    public ChoosePlantService() {
        // On init, pre-load any saved boosts from the account into boostedPlants
        if (App.getAccount() != null) {
            com.ussr.pvz.model.account.SavedBoosts saved = App.getAccount().getSavedBoosts();
            if (saved != null) {
                for (String boost : saved.getBoosts()) {
                    String key = normalizePlantKey(boost);
                    if (!boostedPlants.contains(key)) {
                        boostedPlants.add(key);
                    }
                }
            }
        }
    }

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

    public static String getCleanedUppercaseName(String rawName) {
        if (rawName == null) {
            return "";
        }
        // Removes all spaces, underscores, and hyphens, then capitalizes
        return rawName.replaceAll("[\\s_\\-]", "").toUpperCase();
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
        boostedPlants.add(canonicalName);
        return "seed packet used for " + canonicalName + " (" + (available - 1) + " remaining)";
    }

    public static boolean isConveyorLevel(Level level) {
        return level != null && level.getDeliveryStrategy() instanceof com.ussr.pvz.model.level.delivery
                .ConveyorDeliveryStrategy;
    }

    /**
     * Called right after a level is (re)started. Conveyor-delivery levels don't use a
     * pre-game loadout, so we skip the choose-plant menu and jump straight into the game.
     */
    public static void proceedPastLevelStart() {
        Level level = App.getLevelManager().getCurrentLevel();
        if (isConveyorLevel(level) || level.getBehavior() instanceof BeghouledBehavior
                || level.getBehavior() instanceof WallnutBowlingBehavior
                || level.getBehavior() instanceof VaseBreakerBehavior
                || level.getBehavior() instanceof IZombieBehavior) {
            new ChoosePlantService().startGame();
        } else {
            App.setMenuState(MenuState.CHOOSE_PLANT);
        }
    }

    public String startGame() {
        Level level = App.getLevelManager().getCurrentLevel();
        if (level == null) return "no level selected";
        boolean requiresSelection = !(level.getDeliveryStrategy() instanceof com.ussr.pvz.model.level.delivery
                .ConveyorDeliveryStrategy)
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
        // A new level always starts with every chosen packet ready. This also
        // repairs accounts loaded before max/current recharge were separated.
        session.removeAllCooldowns();
        session.setBoostedPlants(new ArrayList<>(boostedPlants));
        ZombieFactory.init();
        level.onStart();
        session.initClock();
        App.setMenuState(MenuState.GAME);
        session.setSelectedPlants(new ArrayList<>(selectedPlants));
        selectedPlants.clear();
        return "game started! sun: " + INITIAL_SUN;
    }

    private Lawn buildLawn(int rows, int cols) {
        Chapter chapter = App.getLevelManager().getCurrentChapter();
        String chapterId = chapter != null ? chapter.getId() : null;
        return TerrainFactory.build(chapterId, rows, cols);
    }

    // ── ChoosePlantService additions ──────────────────────────────────────────────
// Add these methods to the existing ChoosePlantService:

    public List<CollectionService.PlantData> getSelectablePlants() {
        Chapter chapter = App.getLevelManager().getCurrentChapter();
        List<String> allowed = chapter != null ? chapter.getAllowedPlants() : null;
        AdventureProgress adv = App.getAccount().getAdventureProgress();

        return new CollectionService().getPlantDataForGUI().stream()
                .filter(p -> {
                    if (allowed != null && !allowed.isEmpty()) {
                        return allowed.stream().anyMatch(a -> normalizePlantKey(a).equals(p.id));
                    }
                    return true;
                })
                .peek(p -> {
                    // sync boost state from boostedPlants list
                    p.isBoosted = boostedPlants.contains(p.id);
                })
                .collect(java.util.stream.Collectors.toList());
    }

    public String toggleBoost(String plantId) {
        String key = normalizePlantKey(plantId);
        if (!selectedPlants.contains(key)) return key + " is not selected";

        AdventureProgress adv = App.getAccount().getAdventureProgress();
        if (boostedPlants.contains(key)) {
            // un-boost: refund the packet
            boostedPlants.remove(key);
            adv.getSeedPackets().merge(key, 1, Integer::sum);
            return "boost removed for " + key;
        } else {
            int available = adv.getSeedPackets().getOrDefault(key, 0);
            if (available <= 0) return "no seed packets for " + key;
            adv.spendSeedPacket(key);
            boostedPlants.add(key);
            return "boosted " + key;
        }
    }

    public boolean isSelected(String plantId) {
        return selectedPlants.contains(normalizePlantKey(plantId));
    }

    public boolean isBoosted(String plantId) {
        return boostedPlants.contains(normalizePlantKey(plantId));
    }

    public int selectedCount() {
        return selectedPlants.size();
    }

    public int maxSlots() {
        return MAX_SEED_SLOTS;
    }

    // Add to ChoosePlantService:
    public void applyBoost(String plantId) {
        String key = normalizePlantKey(plantId);
        if (!boostedPlants.contains(key)) {
            boostedPlants.add(key);
        }
    }
}
