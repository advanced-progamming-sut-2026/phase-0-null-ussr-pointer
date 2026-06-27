package com.ussr.pvz.model.engine;

import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.structures.LawnMower;
import com.ussr.pvz.model.entities.items.GroundItem;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.state.ResourceState;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayList;
import java.util.List;

public class GameSession {
    private GameClock clock = new GameClock();
    private Level level;
    private ResourceState resourceState;
    private List<Zombie> zombies;
    private List<GroundItem> items;
    private List<Plant> plants;
    private int sunCount;
    private int plantFoodCount;
    private boolean wavesStarted;
    private Lawn lawn;
    private boolean gameOver = false;
    private static final int LAWN_COLS = 9;
    private List<LawnMower> lawnMowers = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();

    public void initClock() {
        clock.reset();
        plants.forEach(clock::addEntity);
        zombies.forEach(clock::addEntity);
        items.forEach(clock::addEntity);
        projectiles.forEach(clock::addEntity);
        initLawnMowers();
    }

    private void initLawnMowers() {
        if (lawn == null) return;
        lawnMowers.clear();

        int rows = lawn.getRows();
        for (int r = 0; r < rows; r++) {
            // Position them just to the left of the grid column 0 (e.g., x = -0.5)
            LawnMower mower = new LawnMower(r, new Vec2(-0.5, r));
            lawnMowers.add(mower);
            clock.addEntity(mower); // Ensure the clock ticks them!
        }
    }

    public void tick() {
        clock.tick();
        plants.removeIf(p -> !p.isAlive());
        zombies.removeIf(z -> !z.isAlive());
        items.removeIf(i -> !i.isAlive());
        projectiles.removeIf(p -> !p.isAlive());
        lawnMowers.removeIf(m -> !m.isAlive());
        cleanupDeadGridStructures();
        checkZombieBreaches();
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    private void cleanupDeadGridStructures() {
        if (lawn == null) return;

        int rows = lawn.getRows();
        int cols = lawn.getCols();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                var cell = lawn.getCell(r, c);
                if (cell != null && cell.getInteractableStructure() != null) {
                    var structure = cell.getInteractableStructure();

                    if (!structure.isAlive()) {
                        structure.onDestroy(this);
                        cell.setStructure(null);
                    }
                }
            }
        }
    }

    private void checkZombieBreaches() {
        for (Zombie zombie : zombies) {
            if (!zombie.isAlive()) continue;

            if (zombie.getPosition().x() < 0.0) {
                int row = (int) zombie.getPosition().y();
                LawnMower mower = getMowerForLane(row);

                if (mower != null && !mower.isActivated()) {
                    //todo remove the sout
                    System.out.println("[LAWNMOWER] Triggered on row " + row);
                    mower.activate();
                } else if (mower == null) {
                    onZombieReachedEnd();
                    break;
                }
            }
        }
    }

    private LawnMower getMowerForLane(int lane) {
        return lawnMowers.stream()
                .filter(m -> m.getLane() == lane)
                .findFirst()
                .orElse(null);
    }

    public void spawnZombie(Zombie zombie) {
        zombies.add(zombie);
        clock.addEntity(zombie);
    }

    public void onZombieReachedEnd() {
        gameOver = true;
        // TODO: show game over screen here
        System.out.println("[GAME OVER] A zombie reached the house!");
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int getSunCount() {
        return sunCount;
    }

    public void addSun(int amount) {
        sunCount += amount;
    }

    public boolean spendSun(int amount) {
        if (sunCount < amount) return false;
        sunCount -= amount;
        return true;
    }

    public void addPlantFood() {
        plantFoodCount++;
    }

    public boolean spendPlantFood() {
        if (plantFoodCount <= 0) return false;
        plantFoodCount--;
        return true;
    }

    public void killAllZombies() {
        if (zombies != null) {
            zombies.forEach(z -> z.isAlive = false);
            zombies.clear();
        }
    }

    public void removeAllCooldowns() {
        // TODO: implement after plants are handled
    }

    public void startWaves() {
        wavesStarted = true;
        // TODO: implement after zombies are handled
    }

    public boolean isWavesStarted() {
        return wavesStarted;
    }

    // temporary cli functions for testing
    public String renderMap() {
        if (lawn == null) return "map not initialized";
        StringBuilder sb = new StringBuilder();
        int rows = lawn.getRows();
        int cols = lawn.getCols();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                var cell = lawn.getCell(r, c);
                if (cell == null) {
                    sb.append(".");
                } else if (cell.getPlant() != null) {
                    sb.append("P");
                } else if (cell.getInteractableStructure() instanceof com.ussr.pvz.model.board.structures.Grave) {
                    sb.append("G"); // NEW: Render 'G' for Grave tiles
                } else {
                    sb.append(".");
                }
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    public String renderPlantsStatus() {
        if (plants == null || plants.isEmpty()) return "no plants on the field";
        StringBuilder sb = new StringBuilder();
        for (Plant plant : plants) {
            sb.append(plant.getName())
                    .append(" | hp: ").append(plant.getHp())
                    .append(" | level: ").append(plant.getLevel())
                    .append("\n");
        }
        return sb.toString().trim();
    }

    public String renderTileStatus(int row, int col) {
        if (lawn == null) return "map not initialized";
        var cell = lawn.getCell(row, col);
        if (cell == null) return "invalid tile (" + row + ", " + col + ")";
        StringBuilder sb = new StringBuilder();
        sb.append("tile (").append(row).append(", ").append(col).append("): ");

        if (cell.getPlant() != null) {
            sb.append("plant=").append(cell.getPlant().getName())
                    .append(" hp=").append(cell.getPlant().getHp());
        } else if (cell.getInteractableStructure() instanceof com.ussr.pvz.model.board.structures.Grave) {
            var grave = (com.ussr.pvz.model.board.structures.Grave) cell.getInteractableStructure();
            sb.append("structure=Grave hp=").append(grave.getHp()); // NEW: Show grave HP status
        } else {
            sb.append("empty");
        }
        return sb.toString();
    }

    public String renderZombiesInfo() {
        if (zombies == null || zombies.isEmpty()) return "no zombies on the field";
        StringBuilder sb = new StringBuilder();
        for (Zombie zombie : zombies) {
            sb.append(zombie.toString()).append("\n");
        }
        return sb.toString().trim();
    }

    public Lawn getLawn() {
        return lawn;
    }

    public void setLawn(Lawn lawn) {
        this.lawn = lawn;
    }

    public List<Plant> getPlants() {
        return plants;
    }

    public double getElapsedSeconds() {
        return clock.getElapsedSeconds();
    }

    public void setPlants(List<Plant> plants) {
        this.plants = plants;
    }

    public List<Zombie> getZombies() {
        return zombies;
    }

    public void setZombies(List<Zombie> zombies) {
        this.zombies = zombies;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public List<GroundItem> getItems() {
        return items;
    }
}