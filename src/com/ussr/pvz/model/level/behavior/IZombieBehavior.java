package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.Brain;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.entities.items.sun.ProducedSun;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayList;
import java.util.List;

public class IZombieBehavior implements LevelBehavior {
    //todo this is not complete at all
    private final int redLineColumn;
    private final int startingSun;
    private final List<Brain> brains = new ArrayList<>();

    // In a real scenario, this would be populated by your JSON LevelFactory
    private final List<PrePlacedPlant> plantLayout;

    public IZombieBehavior(int redLineColumn, int startingSun, List<PrePlacedPlant> plantLayout) {
        this.redLineColumn = redLineColumn;
        this.startingSun = startingSun;
        this.plantLayout = plantLayout != null ? plantLayout : new ArrayList<>();
    }

    @Override
    public void onStart(Level level) {
        GameSession session = App.getGameSession();
        if (session == null || session.getLawn() == null) return;

        level.setSunFalling(false);

        // Give starting sun
        int currentSun = session.getSunCount();
        session.addSun(startingSun - currentSun);

        int rows = session.getLawn().getRows();

        // 1. Place Brains at column 0 for every row
        for (int r = 0; r < rows; r++) {
            Cell cell = session.getLawn().getCell(r, 0);
            if (cell != null) {
                Brain brain = new Brain();
                brain.setPosition(Vec2.of(0, r));
                cell.setStructure(brain);
                brains.add(brain);
                session.registerStructure(brain);
            }
        }

        // 2. Pre-place cardboard plants
        for (PrePlacedPlant pp : plantLayout) {
            // TODO: Use PlantFactory when ready
            // Plant plant = PlantFactory.createPlant(pp.plantName, 1);
            // plant.setLocation(new Plant.Location(pp.col, pp.row));
            // session.getLawn().getCell(pp.row, pp.col).setPlant(plant);
            // session.getPlants().add(plant);
        }

        // 3. Listen for Sunflower deaths to drop sun
        session.getEventBus().subscribe(GameEvent.PlantDied.class, event -> {
            if (event.plantName().equalsIgnoreCase("Sunflower")) {
                // Spawn a sun worth 200 at the exact location the plant died
                ProducedSun bigSun = new ProducedSun(event.row(), event.col(), 200);
                session.getItems().add(bigSun);
            }
        });
    }

    @Override
    public void onWaveComplete(Level level, int waveNumber) {
    }

    @Override
    public void onComplete(Level level) {
        brains.clear();
    }

    @Override
    public boolean isFailed(Level level) {
        GameSession session = App.getGameSession();
        if (session == null) return false;

        boolean outOfSun = session.getSunCount() < 50; // Cheapest zombie cost
        boolean noZombiesAlive = session.getZombies().stream().noneMatch(GameEntity::isAlive);
        boolean brainsRemaining = brains.stream().anyMatch(Brain::isAlive);

        // Fail if we can't buy zombies, have no zombies acting, and haven't eaten all brains
        return outOfSun && noZombiesAlive && brainsRemaining;
    }

    public boolean isWon() {
        // Win if all brains are dead
        return brains.stream().noneMatch(Brain::isAlive);
    }

    public int getRedLineColumn() {
        return redLineColumn;
    }

    public record PrePlacedPlant(String plantName, int row, int col) {
    }
}