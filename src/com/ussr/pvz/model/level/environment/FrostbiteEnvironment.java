package com.ussr.pvz.model.level.environment;

import com.ussr.pvz.model.board.structures.IceBlock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.util.Vec2;

public class FrostbiteEnvironment implements Environment {
    private final double windIntervalSeconds;
    private final int freezeStacksPerWind;

    private double windTimer = 0.0;
    private double thawTimer = 0.0;

    public FrostbiteEnvironment(double windIntervalSeconds, int freezeStacksPerWind) {
        this.windIntervalSeconds = windIntervalSeconds;
        this.freezeStacksPerWind = freezeStacksPerWind;
    }

    @Override
    public void onStart(GameSession session) {
        // Any initial setup
    }

    @Override
    public void tick(GameSession session, double deltaTime) {
        windTimer += deltaTime;
        thawTimer += deltaTime;

        // Thaw 1 level per second around fire plants
        if (thawTimer >= 1.0) {
            thawTimer = 0.0;
            processFireThawing(session);
        }

        // Apply Freezing Wind
        if (windIntervalSeconds > 0 && windTimer >= windIntervalSeconds) {
            windTimer = 0.0;
            applyFreezingWind(session);
        }
    }

    private void processFireThawing(GameSession session) {
        if (session.getPlants() == null) return;
        for (Plant firePlant : session.getPlants()) {
            if (firePlant.isAlive() && firePlant.getTags().contains(Tag.FIRE)) {
                int fx = firePlant.getLocation().x();
                int fy = firePlant.getLocation().y();

                for (Plant target : session.getPlants()) {
                    if (!target.isAlive() || target == firePlant) continue;
                    int tx = target.getLocation().x();
                    int ty = target.getLocation().y();

                    if (Math.abs(fx - tx) <= 1 && Math.abs(fy - ty) <= 1 && target.getChillLevel() > 0) {
                        target.setChillLevel(target.getChillLevel() - 1);
                        if (target.getChillLevel() < 3 && target.getState() == Plant.PlantState.INCAPACITATED) {
                            target.setState(Plant.PlantState.ACTIVE);
                        }
                    }
                }
            }
        }
    }

    private void applyFreezingWind(GameSession session) {
        if (session.getPlants() == null) return;
        for (Plant plant : session.getPlants()) {
            if (plant.isAlive() && !plant.getTags().contains(Tag.FIRE)) {
                plant.setChillLevel(Math.min(3, plant.getChillLevel() + freezeStacksPerWind));
                if (plant.getChillLevel() == 3 && plant.getState() != Plant.PlantState.INCAPACITATED) {
                    IceBlock iceBlock = new IceBlock(plant, 600);
                    iceBlock.setPosition(Vec2.of(plant.getLocation().x(), plant.getLocation().y()));
                    session.getLawn().getCell(plant.getLocation().y(), plant.getLocation().x()).setStructure(iceBlock);
                    session.registerStructure(iceBlock);
                }
            }
        }
    }
}