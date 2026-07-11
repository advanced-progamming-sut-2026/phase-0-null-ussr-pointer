package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.terrain.Tile;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.entities.items.sun.ProducedSun;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.PlantFactory;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.util.Vec2;

import java.util.*;

public class BeghouledBehavior implements LevelBehavior {

    private final int targetMatches;
    private int currentMatches = 0;

    // The 5 root plant types assigned to this level
    private final List<String> rootPlantTypes;

    // Maps the root plant type to its currently upgraded form
    private final Map<String, String> activePlantTypes = new HashMap<>();

    public BeghouledBehavior(int targetMatches, List<String> startingPlants) {
        this.targetMatches = targetMatches;
        this.rootPlantTypes = startingPlants;
        for (String plant : startingPlants) {
            activePlantTypes.put(plant, plant);
        }
    }

    @Override
    public void onStart(Level level) {
        GameSession session = App.getGameSession();
        if (session == null || session.getLawn() == null) return;

        level.setSunFalling(false);

        // Fill the board randomly with the 5 types
        fillBoard(session);

        // Prevent matches on initial generation
        while (hasMatches(session.getLawn())) {
            fillBoard(session);
        }

        // Subscribe to plant deaths to create craters if eaten by zombies
        session.getEventBus().subscribe(GameEvent.PlantDied.class, event -> {
            Cell cell = session.getLawn().getCell(event.row(), event.col());
            if (cell != null) {
                cell.setTile(new Tile(TileType.Crater));
            }
        });
    }

    @Override
    public void onWaveComplete(Level level, int waveNumber) {
        // Zombies enter like a normal level, but waves never end
    }

    @Override
    public void onComplete(Level level) {
    }

    @Override
    public boolean isFailed(Level level) {
        return false; // Handled by zombies reaching the house
    }

    public boolean isWon() {
        return currentMatches >= targetMatches;
    }

    public void checkWinCondition(GameSession session) {
        if (isWon()) {
            // After reaching the target, all existing zombies disappear and the player wins
            session.killAllZombies();
            session.getEventBus().publish(new GameEvent.GameWon());
        }
    }

    private void fillBoard(GameSession session) {
        Lawn lawn = session.getLawn();
        Random rand = new Random();

        for (int r = 0; r < lawn.getRows(); r++) {
            for (int c = 0; c < lawn.getCols(); c++) {
                Cell cell = lawn.getCell(r, c);
                if (cell.getTile().getType() != TileType.Crater && cell.getPlant() == null) {
                    String rootType = rootPlantTypes.get(rand.nextInt(rootPlantTypes.size()));
                    String activeType = activePlantTypes.get(rootType);

                    Plant plant = spawnPlant(activeType, r, c);
                    cell.setPlant(plant);
                    session.getPlants().add(plant);
                }
            }
        }
    }

    public boolean trySwap(int r1, int c1, int r2, int c2) {
        GameSession session = App.getGameSession();
        Lawn lawn = session.getLawn();

        // Validate adjacency
        if (Math.abs(r1 - r2) + Math.abs(c1 - c2) != 1) return false;

        Cell cell1 = lawn.getCell(r1, c1);
        Cell cell2 = lawn.getCell(r2, c2);

        // Check for craters
        if (cell1.getTile().getType() == TileType.Crater || cell2.getTile().getType() == TileType.Crater) {
            return false;
        }

        Plant p1 = cell1.getPlant();
        Plant p2 = cell2.getPlant();

        // Perform swap
        cell1.setPlant(p2);
        cell2.setPlant(p1);
        if (p2 != null) {
            p2.setLocation(new Plant.Location(c1, r1));
            p2.setPosition(Vec2.of(c1, r1));
        }
        if (p1 != null) {
            p1.setLocation(new Plant.Location(c2, r2));
            p1.setPosition(Vec2.of(c2, r2));
        }

        // A swap can only be made if it creates a match
        if (!hasMatches(lawn)) {
            // Revert swap
            cell1.setPlant(p1);
            cell2.setPlant(p2);
            if (p1 != null) { p1.setLocation(new Plant.Location(c1, r1)); p1.setPosition(Vec2.of(c1, r1)); }
            if (p2 != null) { p2.setLocation(new Plant.Location(c2, r2)); p2.setPosition(Vec2.of(c2, r2)); }
            return false;
        }

        // Process matches & cascades
        processMatches(session, false);
        checkWinCondition(session);
        ensurePossibleMoves(session);
        return true;
    }

    private void processMatches(GameSession session, boolean isCascade) {
        Lawn lawn = session.getLawn();
        Set<Plant> matchedPlants = new HashSet<>();
        int matchCount = 0;
        int maxGroupSize = 0;

        // Horizontal matches
        for (int r = 0; r < lawn.getRows(); r++) {
            for (int c = 0; c < lawn.getCols() - 2; c++) {
                Plant p1 = lawn.getCell(r, c).getPlant();
                if (p1 == null) continue;

                int matchLength = 1;
                while (c + matchLength < lawn.getCols()) {
                    Plant nextP = lawn.getCell(r, c + matchLength).getPlant();
                    if (nextP != null && nextP.getName().equals(p1.getName())) {
                        matchLength++;
                    } else {
                        break;
                    }
                }

                if (matchLength >= 3) {
                    matchCount++;
                    maxGroupSize = Math.max(maxGroupSize, matchLength);
                    for (int i = 0; i < matchLength; i++) {
                        matchedPlants.add(lawn.getCell(r, c + i).getPlant());
                    }
                    c += matchLength - 1;
                }
            }
        }

        // Vertical matches
        for (int c = 0; c < lawn.getCols(); c++) {
            for (int r = 0; r < lawn.getRows() - 2; r++) {
                Plant p1 = lawn.getCell(r, c).getPlant();
                if (p1 == null) continue;

                int matchLength = 1;
                while (r + matchLength < lawn.getRows()) {
                    Plant nextP = lawn.getCell(r + matchLength, c).getPlant();
                    if (nextP != null && nextP.getName().equals(p1.getName())) {
                        matchLength++;
                    } else {
                        break;
                    }
                }

                if (matchLength >= 3) {
                    matchCount++;
                    maxGroupSize = Math.max(maxGroupSize, matchLength);
                    for (int i = 0; i < matchLength; i++) {
                        matchedPlants.add(lawn.getCell(r + i, c).getPlant());
                    }
                    r += matchLength - 1;
                }
            }
        }

        if (matchedPlants.isEmpty()) return;

        // Apply Rewards
        int baseSun = 1;
        if (maxGroupSize == 4) baseSun = 2;
        if (maxGroupSize >= 5) baseSun = 3;

        // Cascades give one extra sun
        if (isCascade) baseSun += 1;

        for (int i = 0; i < baseSun; i++) {
            session.getItems().add(new ProducedSun(5, 2, 50));
        }

        currentMatches += matchCount;

        // Remove matched plants silently (don't trigger craters)
        for (Plant p : matchedPlants) {
            p.setAlive(false);
            session.getPlants().remove(p);
            lawn.getCell(p.getLocation().y(), p.getLocation().x()).setPlant(null);
        }

        // Drop plants down
        dropPlants(session);
        fillBoard(session);

        // Check for cascades
        if (hasMatches(lawn)) {
            processMatches(session, true);
        }
    }

    private void dropPlants(GameSession session) {
        Lawn lawn = session.getLawn();
        for (int c = 0; c < lawn.getCols(); c++) {
            for (int r = lawn.getRows() - 1; r >= 0; r--) {
                Cell cell = lawn.getCell(r, c);
                if (cell.getTile().getType() == TileType.Crater || cell.getPlant() != null) continue;

                // Find the nearest plant above
                for (int aboveR = r - 1; aboveR >= 0; aboveR--) {
                    Cell aboveCell = lawn.getCell(aboveR, c);
                    if (aboveCell.getTile().getType() == TileType.Crater) break; // Crater blocks falling
                    if (aboveCell.getPlant() != null) {
                        Plant fallingPlant = aboveCell.getPlant();
                        aboveCell.setPlant(null);
                        cell.setPlant(fallingPlant);
                        fallingPlant.setLocation(new Plant.Location(c, r));
                        fallingPlant.setPosition(Vec2.of(c, r));
                        break;
                    }
                }
            }
        }
    }

    private boolean hasMatches(Lawn lawn) {
        // Horizontal
        for (int r = 0; r < lawn.getRows(); r++) {
            for (int c = 0; c < lawn.getCols() - 2; c++) {
                Plant p1 = lawn.getCell(r, c).getPlant();
                Plant p2 = lawn.getCell(r, c + 1).getPlant();
                Plant p3 = lawn.getCell(r, c + 2).getPlant();
                if (p1 != null && p2 != null && p3 != null
                        && p1.getName().equals(p2.getName()) && p2.getName().equals(p3.getName())) {
                    return true;
                }
            }
        }
        // Vertical
        for (int c = 0; c < lawn.getCols(); c++) {
            for (int r = 0; r < lawn.getRows() - 2; r++) {
                Plant p1 = lawn.getCell(r, c).getPlant();
                Plant p2 = lawn.getCell(r + 1, c).getPlant();
                Plant p3 = lawn.getCell(r + 2, c).getPlant();
                if (p1 != null && p2 != null && p3 != null
                        && p1.getName().equals(p2.getName()) && p2.getName().equals(p3.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void ensurePossibleMoves(GameSession session) {
        // Note: A full backtracking algorithm to check for potential moves is extensive.
        // For standard Beghouled rules: If no match is possible, the whole board resets.
        if (!hasPossibleMoves(session.getLawn())) {
            resetBoard(session);
        }
    }

    private boolean hasPossibleMoves(Lawn lawn) {
        // Simplified check: iterate and conceptually swap horizontally/vertically to check hasMatch
        for (int r = 0; r < lawn.getRows(); r++) {
            for (int c = 0; c < lawn.getCols(); c++) {
                if (c < lawn.getCols() - 1 && testSimulatedSwap(lawn, r, c, r, c + 1)) return true;
                if (r < lawn.getRows() - 1 && testSimulatedSwap(lawn, r, c, r + 1, c)) return true;
            }
        }
        return false;
    }

    private boolean testSimulatedSwap(Lawn lawn, int r1, int c1, int r2, int c2) {
        Cell cell1 = lawn.getCell(r1, c1);
        Cell cell2 = lawn.getCell(r2, c2);
        if (cell1.getTile().getType() == TileType.Crater || cell2.getTile().getType() == TileType.Crater) return false;

        Plant p1 = cell1.getPlant();
        Plant p2 = cell2.getPlant();

        cell1.setPlant(p2);
        cell2.setPlant(p1);
        boolean match = hasMatches(lawn);

        cell1.setPlant(p1);
        cell2.setPlant(p2);
        return match;
    }

    private void resetBoard(GameSession session) {
        // Clear non-crater plants and re-fill
        Lawn lawn = session.getLawn();
        for (int r = 0; r < lawn.getRows(); r++) {
            for (int c = 0; c < lawn.getCols(); c++) {
                Cell cell = lawn.getCell(r, c);
                if (cell.getPlant() != null) {
                    cell.getPlant().setAlive(false);
                    session.getPlants().remove(cell.getPlant());
                    cell.setPlant(null);
                }
            }
        }
        fillBoard(session);
        while (hasMatches(lawn) || !hasPossibleMoves(lawn)) {
            // Clear and retry if generated board is invalid
            for (int r = 0; r < lawn.getRows(); r++) {
                for (int c = 0; c < lawn.getCols(); c++) {
                    if (lawn.getCell(r,c).getPlant() != null) lawn.getCell(r,c).setPlant(null);
                }
            }
            fillBoard(session);
        }
    }

    public void upgradePlantType(String baseType, String newType, GameSession session) {
        activePlantTypes.put(baseType, newType);

        // Convert all existing plants on the board
        Lawn lawn = session.getLawn();
        for (int r = 0; r < lawn.getRows(); r++) {
            for (int c = 0; c < lawn.getCols(); c++) {
                Cell cell = lawn.getCell(r, c);
                if (cell.getPlant() != null && cell.getPlant().getName().equalsIgnoreCase(baseType)) {
                    cell.getPlant().setAlive(false);
                    session.getPlants().remove(cell.getPlant());

                    Plant upgraded = spawnPlant(newType, r, c);
                    cell.setPlant(upgraded);
                    session.getPlants().add(upgraded);
                }
            }
        }
    }

    private Plant spawnPlant(String alias, int row, int col) {
        // ID lookup mock - replace with actual PlantFactory ID lookup if needed
        int id = 1;
        Plant p = PlantFactory.createPlant(id, 1);
        p.setName(alias); // Force name for logic
        p.setLocation(new Plant.Location(col, row));
        p.setPosition(Vec2.of(col, row));
        return p;
    }

    public int getTargetMatches() { return targetMatches; }
    public int getCurrentMatches() { return currentMatches; }
}