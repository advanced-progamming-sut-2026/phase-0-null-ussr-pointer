package com.ussr.pvz.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.account.AdventureProgress;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.Row;
import com.ussr.pvz.model.board.terrain.Tile;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.dto.PlantTypeRequest;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.PlantFactory;
import com.ussr.pvz.model.level.Chapter;
import com.ussr.pvz.model.level.Level;

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

    public String showAvailablePlants() {
        Chapter chapter = App.getLevelManager().getCurrentChapter();
        if (chapter == null) return "no chapter selected";

        List<String> allowed = chapter.getAllowedPlants();
        if (allowed == null || allowed.isEmpty()) return "no plants allowed in this chapter";

        AdventureProgress adv = App.getAccount().getAdventureProgress();
        StringBuilder sb = new StringBuilder("--- Available Plants ---\n");
        for (String name : allowed) {
            int lvl = adv.getPlantLvls().getOrDefault(name.toUpperCase(), 0);
            if (lvl > 0) {
                boolean selected = selectedPlants.contains(name.toUpperCase());
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

        String name = request.type().trim().toUpperCase();
        AdventureProgress adv = App.getAccount().getAdventureProgress();
        int lvl = adv.getPlantLvls().getOrDefault(name, 0);
        if (lvl == 0) return "you don't have " + name;

        Chapter chapter = App.getLevelManager().getCurrentChapter();
        if (chapter != null && chapter.getAllowedPlants() != null) {
            boolean allowed = chapter.getAllowedPlants().stream()
                    .anyMatch(p -> p.equalsIgnoreCase(name));
            if (!allowed) return name + " is not allowed in this chapter";
        }

        Level level = App.getLevelManager().getCurrentLevel();
        if (level != null && level.getLockedPlants() != null) {
            boolean locked = level.getLockedPlants().stream()
                    .anyMatch(p -> p.equalsIgnoreCase(name));
            if (locked) return name + " is locked in this level";
        }

        if (selectedPlants.contains(name)) return name + " is already selected";

        selectedPlants.add(name);
        return name + " added (" + selectedPlants.size() + "/" + MAX_SEED_SLOTS + ")";
    }

    public String removePlant(PlantTypeRequest request) {
        String name = request.type().trim().toUpperCase();
        if (!selectedPlants.remove(name))
            return name + " is not in your selection";
        return name + " removed (" + selectedPlants.size() + "/" + MAX_SEED_SLOTS + ")";
    }

    public String boostPlant(PlantTypeRequest request) {
        String name = request.type().trim().toUpperCase();
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
        if (selectedPlants.isEmpty())
            return "select at least one plant before starting";

        Level level = App.getLevelManager().getCurrentLevel();
        if (level == null) return "no level selected";

        Lawn lawn = buildLawn(LAWN_ROWS, LAWN_COLS);

        GameSession session = new GameSession();
        session.setLawn(lawn);
        session.setPlants(new ArrayList<>());
        session.setZombies(new ArrayList<>());
        session.setLevel(level);
        session.addSun(INITIAL_SUN);

        App.setGameSession(session);
        session.initClock();
        App.setMenuState(MenuState.GAME);

        selectedPlants.clear();
        return "game started! sun: " + INITIAL_SUN;
    }

    private Lawn buildLawn(int rows, int cols) {
        Lawn lawn = new Lawn(rows, cols);
        for (int r = 0; r < rows; r++) {
            Row row = new Row(r);
            for (int c = 0; c < cols; c++) {
                Cell cell = new Cell();
                cell.setRow(r);
                cell.setCol(c);
                cell.setTile(new Tile(TileType.Normal));
                row.addCell(cell);
            }
            lawn.addRow(row);
        }
        return lawn;
    }
}