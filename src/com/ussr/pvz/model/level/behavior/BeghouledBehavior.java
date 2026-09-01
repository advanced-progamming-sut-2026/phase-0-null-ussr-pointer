package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.terrain.Tile;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.entities.items.sun.ProducedSun;
import com.ussr.pvz.model.entities.plants.Location;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.PlantFactory;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.util.Vec2;

import java.util.*;

public class BeghouledBehavior extends LevelBehavior {

    private static final double STEP_DELAY = 0.35;

    private final int targetMatches;
    private int currentMatches = 0;

    // The 5 root plant types assigned to this level
    private final List<String> rootPlantTypes;

    // Maps the root plant type to its currently upgraded form
    private final Map<String, String> activePlantTypes = new HashMap<>();

    private final Deque<Runnable> pendingSteps = new ArrayDeque<>();
    private double stepTimer = 0;

    public BeghouledBehavior(int targetMatches, List<String> startingPlants) {
        this.targetMatches = targetMatches;
        this.rootPlantTypes = startingPlants;
        // Beghouled is won by reaching the match objective, never merely by
        // clearing the current zombie waves.
        this.autoWinOnWavesClear = false;
        for (String plant : startingPlants) {
            activePlantTypes.put(plant.toLowerCase(), plant.toLowerCase());
        }
    }

    @Override
    public void onStart(Level level) {
        super.onStart(level);

        GameSession session = App.getGameSession();
        if (session == null || session.getLawn() == null) return;

        level.setSunFalling(false);

        fillBoard(session);

        Lawn lawn = session.getLawn();
        while (hasMatches(lawn)) {
            clearBoard(session, lawn);
            fillBoard(session);
        }

        // According to the doc: When a zombie eats a plant, a crater is created there.
        session.getEventBus().subscribe(GameEvent.PlantDied.class, event -> {
            Cell cell = session.getLawn().getCell(event.row(), event.col());
            if (cell != null) {
                cell.setTile(new Tile(TileType.Crater));
            }
        });
    }

    private void clearBoard(GameSession session, Lawn lawn) {
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
    }

    @Override
    public boolean isFailed(Level level) {
        return false;
    }

    @Override
    public void tick(GameSession session, double deltaTime) {
        super.tick(session, deltaTime);
        processStepQueue(session, deltaTime);
        if (!levelCompleted && !session.isGameOver()) {
            checkWinCondition(session);
        }
    }

    private void processStepQueue(GameSession session, double deltaTime) {
        if (pendingSteps.isEmpty()) return;

        stepTimer -= deltaTime;
        if (stepTimer > 0) return;

        Runnable step = pendingSteps.poll();
        if (step != null) step.run();
        stepTimer = STEP_DELAY;
    }

    public boolean isResolving() {
        return !pendingSteps.isEmpty();
    }

    public boolean isWon() {
        return currentMatches >= targetMatches;
    }

    public void checkWinCondition(GameSession session) {
        if (isWon()) {
            session.killAllZombies();
            onComplete(session.getLevel());
        }
    }

    private void fillBoard(GameSession session) {
        Lawn lawn = session.getLawn();
        Random rand = new Random();

        for (int r = 0; r < lawn.getRows(); r++) {
            for (int c = 0; c < lawn.getCols(); c++) {
                Cell cell = lawn.getCell(r, c);
                if (cell.getTile().getType() != TileType.Crater && cell.getPlant() == null) {
                    String rootType = rootPlantTypes.get(rand.nextInt(rootPlantTypes.size())).toLowerCase();
                    Plant plant = spawnPlant(activePlantTypes.get(rootType), r, c);
                    cell.setPlant(plant);
                    session.getPlants().add(plant);
                }
            }
        }
    }

    public boolean trySwap(int r1, int c1, int r2, int c2) {
        GameSession session = App.getGameSession();
        Lawn lawn = session.getLawn();

        if (isResolving()) return false;

        // Validate adjacency
        if (Math.abs(r1 - r2) + Math.abs(c1 - c2) != 1) return false;

        Cell cell1 = lawn.getCell(r1, c1);
        Cell cell2 = lawn.getCell(r2, c2);

        // Cannot move plants into craters
        if (cell1.getTile().getType() == TileType.Crater || cell2.getTile().getType() == TileType.Crater) {
            return false;
        }

        Plant p1 = cell1.getPlant();
        Plant p2 = cell2.getPlant();

        // Both cells must have a plant — cannot swap with an empty cell.
        if (p1 == null || p2 == null) return false;

        swapCells(cell1, cell2, p1, p2, r1, c1, r2, c2);

        // A swap can only be made if it creates a match
        if (!hasMatches(lawn)) {
            swapCells(cell1, cell2, p2, p1, r1, c1, r2, c2); // Revert
            return false;
        }

        queueResolvePass(session, false);
        return true;
    }

    private void swapCells(Cell cell1, Cell cell2, Plant p1, Plant p2, int r1, int c1, int r2, int c2) {
        cell1.setPlant(p2);
        cell2.setPlant(p1);
        if (p2 != null) { p2.setLocation(new Location(c1, r1)); p2.setPosition(Vec2.of(c1, r1)); }
        if (p1 != null) { p1.setLocation(new Location(c2, r2)); p1.setPosition(Vec2.of(c2, r2)); }
    }

    private void queueResolvePass(GameSession session, boolean isCascade) {
        Lawn lawn = session.getLawn();

        List<MatchGroup> groups = findAllMatchGroups(lawn);
        if (groups.isEmpty()) {
            pendingSteps.add(() -> ensurePossibleMoves(session));
            return;
        }

        for (MatchGroup group : groups) {
            pendingSteps.add(() -> removeMatchGroup(session, group, isCascade));
        }

        pendingSteps.add(() -> dropPlants(session));
        pendingSteps.add(() -> fillBoard(session));

        pendingSteps.add(() -> {
            if (hasMatches(session.getLawn())) {
                queueResolvePass(session, true);
            } else {
                ensurePossibleMoves(session);
            }
        });
    }

    private void removeMatchGroup(GameSession session, MatchGroup group, boolean isCascade) {
        Lawn lawn = session.getLawn();

        applyMatchRewards(session, isCascade, group.plants());
        currentMatches++;

        for (Plant p : group.plants()) {
            if (!p.isAlive()) continue; // may already be cleared by an overlapping group
            p.setAlive(false);
            session.getPlants().remove(p);
            Cell cell = lawn.getCell(p.getLocation().y(), p.getLocation().x());
            if (cell.getPlant() == p) cell.setPlant(null);
        }
    }

    /**
     * Finds all distinct horizontal and vertical match groups (≥3) on the board.
     * A plant can belong to multiple groups (e.g. an L-shape), but each linear
     * run is its own group for scoring purposes.
     */
    private List<MatchGroup> findAllMatchGroups(Lawn lawn) {
        List<MatchGroup> groups = new ArrayList<>();

        // Horizontal runs
        for (int r = 0; r < lawn.getRows(); r++) {
            int c = 0;
            while (c < lawn.getCols()) {
                Plant p1 = lawn.getCell(r, c).getPlant();
                if (p1 == null) { c++; continue; }

                int len = 1;
                while (c + len < lawn.getCols()) {
                    Plant pn = lawn.getCell(r, c + len).getPlant();
                    if (pn == null || !pn.getName().equals(p1.getName())) break;
                    len++;
                }
                if (len >= 3) {
                    Set<Plant> plants = new HashSet<>();
                    for (int i = 0; i < len; i++) plants.add(lawn.getCell(r, c + i).getPlant());
                    groups.add(new MatchGroup(plants, len));
                }
                c += len;
            }
        }

        // Vertical runs
        for (int c = 0; c < lawn.getCols(); c++) {
            int r = 0;
            while (r < lawn.getRows()) {
                Plant p1 = lawn.getCell(r, c).getPlant();
                if (p1 == null) { r++; continue; }

                int len = 1;
                while (r + len < lawn.getRows()) {
                    Plant pn = lawn.getCell(r + len, c).getPlant();
                    if (pn == null || !pn.getName().equals(p1.getName())) break;
                    len++;
                }
                if (len >= 3) {
                    Set<Plant> plants = new HashSet<>();
                    for (int i = 0; i < len; i++) plants.add(lawn.getCell(r + i, c).getPlant());
                    groups.add(new MatchGroup(plants, len));
                }
                r += len;
            }
        }

        return groups;
    }

    private record MatchGroup(Set<Plant> plants, int size) {}

    private void applyMatchRewards(GameSession session, boolean isCascade, Set<Plant> matchedPlants) {
        // Each match is worth a flat 1 sun token, regardless of how many
        // plants it removes (a 3-match and a 5-match both count as one match).
        int baseSun = 1;

        // Cascades add 1 bonus sun token
        if (isCascade) baseSun += 1;

        int sumX = 0, sumY = 0;
        for (Plant p : matchedPlants) {
            sumX += p.getLocation().x();
            sumY += p.getLocation().y();
        }
        int avgX = sumX / Math.max(1, matchedPlants.size());
        int avgY = sumY / Math.max(1, matchedPlants.size());

        for (int i = 0; i < baseSun; i++) {
            // Each token is worth 50 sun
            session.addItem(new ProducedSun(avgX, avgY, 50, "Beghouled Match"));
        }
    }

    private void dropPlants(GameSession session) {
        Lawn lawn = session.getLawn();
        for (int c = 0; c < lawn.getCols(); c++) {
            int writeRow = 0;
            for (int r = 0; r < lawn.getRows(); r++) {
                Cell cell = lawn.getCell(r, c);
                if (cell.getTile().getType() == TileType.Crater) {
                    writeRow = r + 1; // reset write head above this crater
                    continue;
                }
                if (cell.getPlant() != null) {
                    if (r != writeRow) {
                        Plant p = cell.getPlant();
                        cell.setPlant(null);
                        Cell dest = lawn.getCell(writeRow, c);
                        dest.setPlant(p);
                        p.setLocation(new Location(c, writeRow));
                        p.setPosition(Vec2.of(c, writeRow));
                    }
                    writeRow++;
                }
            }
        }
    }

    private boolean hasMatches(Lawn lawn) {
        return !findAllMatchGroups(lawn).isEmpty();
    }

    private void ensurePossibleMoves(GameSession session) {
        if (!hasPossibleMoves(session.getLawn())) resetBoard(session);
    }

    private boolean hasPossibleMoves(Lawn lawn) {
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
        // Document: "If a match-3 cannot be created, the whole board must be reset
        // and a random plant is placed where a plant was in the garden."
        Lawn lawn = session.getLawn();
        clearBoard(session, lawn);
        fillBoard(session);
        while (hasMatches(lawn) || !hasPossibleMoves(lawn)) {
            clearBoard(session, lawn);
            fillBoard(session);
        }
    }

    public void upgradePlantType(String baseType, String newType, GameSession session) {
        String rootToUpdate = null;
        for (Map.Entry<String, String> entry : activePlantTypes.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(baseType)) {
                rootToUpdate = entry.getKey();
                break;
            }
        }
        if (rootToUpdate != null) {
            activePlantTypes.put(rootToUpdate, newType.toLowerCase());
        }

        Lawn lawn = session.getLawn();
        for (int r = 0; r < lawn.getRows(); r++) {
            for (int c = 0; c < lawn.getCols(); c++) {
                Cell cell = lawn.getCell(r, c);
                if (cell.getPlant() != null && cell.getPlant().getName().toLowerCase().equals(baseType.toLowerCase())) {
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
        Plant p = PlantFactory.createPlantByName(alias, 1);
        p.setLocation(new Location(col, row));
        p.setPosition(Vec2.of(col, row));
        p.setState(Plant.PlantState.ACTIVE);
        p.setAlive(true);
        return p;
    }

    public int getTargetMatches() { return targetMatches; }
    public int getCurrentMatches() { return currentMatches; }
    public Map<String, String> getActivePlantTypes() { return activePlantTypes; }
}