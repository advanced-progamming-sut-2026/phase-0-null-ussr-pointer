package com.ussr.pvz.model.level.chaptereffect;

import com.ussr.pvz.model.board.structures.IceBlock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.util.Vec2;

public class FrostbiteCavesEffect implements ChapterEffect {

    @Override
    public void onTick(GameSession session, Level level, double deltaTime) {
        double windTimer = level.getWindTimerElapsed() + deltaTime;
        double thawTimer = level.getThawTimerElapsed() + deltaTime;

        if (thawTimer >= 1.0) {
            thawTimer = 0.0;
            processFireThawing(session);
        }
        level.setThawTimerElapsed(thawTimer);

        double windInterval = level.getWindIntervalSeconds();
        if (windInterval > 0 && windTimer >= windInterval) {
            windTimer = 0.0;
            applyFreezingWind(session, level);
        }
        level.setWindTimerElapsed(windTimer);
    }

    private void processFireThawing(GameSession session) {
        if (session.getPlants() == null) return;
        for (Plant firePlant : session.getPlants()) {
            if (!firePlant.isAlive() || !firePlant.getTags().contains(Tag.FIRE)) continue;

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

    private void applyFreezingWind(GameSession session, Level level) {
        if (session.getPlants() == null) return;

        int stacks = level.getFreezeStacksPerWind();
        System.out.println("A freezing wind sweeps through the lawn!");

        for (Plant plant : session.getPlants()) {
            if (!plant.isAlive() || plant.getTags().contains(Tag.FIRE)) continue;

            plant.setChillLevel(Math.min(3, plant.getChillLevel() + stacks));
            if (plant.getChillLevel() == 3 && plant.getState() != Plant.PlantState.INCAPACITATED) {
                IceBlock iceBlock = new IceBlock(plant, 600);
                iceBlock.setPosition(Vec2.of(plant.getLocation().x(), plant.getLocation().y()));
                session.getLawn().getCell(plant.getLocation().y(), plant.getLocation().x()).setStructure(iceBlock);
                session.registerStructure(iceBlock);
            }
        }
    }
}