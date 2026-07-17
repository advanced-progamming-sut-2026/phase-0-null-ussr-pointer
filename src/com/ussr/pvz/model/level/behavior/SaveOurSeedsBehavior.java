package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.level.Level;

import java.util.ArrayList;
import java.util.List;

public class SaveOurSeedsBehavior extends LevelBehavior {

    private final List<Plant> endangeredPlants = new ArrayList<>();
    private final List<TargetSeed> seedsToSpawn;
    private boolean missionFailed = false;

    public SaveOurSeedsBehavior(List<TargetSeed> seedsToSpawn) {
        this.seedsToSpawn = seedsToSpawn != null ? seedsToSpawn : new ArrayList<>();
    }

    public void addTargetSeed(String plantName, int row, int col) {
        seedsToSpawn.add(new TargetSeed(plantName, row, col));
    }

    @Override
    public void onStart(Level level) {
        super.onStart(level);

        GameSession session = App.getGameSession();
        if (session == null || session.getLawn() == null) return;

        Lawn lawn = session.getLawn();

        for (TargetSeed target : seedsToSpawn) {
            Plant specialPlant = new Plant();
            specialPlant.setName(target.plantName());
            specialPlant.setHp(300);
            specialPlant.setAlive(true);
            specialPlant.setState(Plant.PlantState.ACTIVE);
            specialPlant.setLocation(new Plant.Location(target.col(), target.row()));

            if (session.getPlants() != null) {
                session.getPlants().add(specialPlant);
            }
            Cell cell = lawn.getCell(target.row(), target.col());
            if (cell != null) {
                cell.setPlant(specialPlant);
            }

            endangeredPlants.add(specialPlant);
        }
    }

    @Override
    public void onPlantDied(GameSession session, Plant plant) {
        if (endangeredPlants.contains(plant)) {
            this.missionFailed = true;
            session.getEventBus().publish(new GameEvent.GameOver());
        }
    }

    @Override
    public boolean isFailed(Level level) {
        return missionFailed;
    }

    public List<Plant> getEndangeredPlants() {
        return endangeredPlants;
    }

    public record TargetSeed(String plantName, int row, int col) {}
}