package com.ussr.pvz.model.engine;

import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.entities.items.GroundItem;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.state.ResourceState;

import java.util.List;

public class GameSession {
    private GameClock clock;
    private Level level;
    private ResourceState resourceState;
    private List<Zombie> zombies;
    private List<GroundItem> items;
    private List<Plant> plants;
    private int sunCount;
    private int plantFoodCount;
    private boolean wavesStarted;
    private Lawn lawn;

    public void tick() {
        plants.forEach(Plant::tick);
        zombies.forEach(Zombie::tick);
        items.forEach(GroundItem::tick);
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
}