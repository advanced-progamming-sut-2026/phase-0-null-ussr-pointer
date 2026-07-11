package com.ussr.pvz.model.level.chaptereffect;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.structures.IceBlock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.level.Level;

import java.util.Random;

public class FrostbiteCavesEffect implements ChapterEffect {

    private static final Random RAND = new Random();
    private static final int FREEZE_STACKS = 3;

    @Override
    public void onWaveStart(GameSession session, Level level, int waveNumber, boolean isFinalWave) {
        Lawn lawn = session.getLawn();
        if (lawn == null) return;

        int rows = lawn.getRows();
        int cols = lawn.getCols();
        int hitRows = 1 + RAND.nextInt(2);

        for (int i = 0; i < hitRows; i++) {
            int row = RAND.nextInt(rows);
            System.out.println("A freezing wind sweeps through row " + (row + 1) + "!");

            for (int col = 0; col < cols; col++) {
                Cell cell = lawn.getCell(row, col);
                if (cell == null) continue;

                Plant plant = cell.getPlant();
                if (plant == null || !plant.isAlive()) continue;
                if (plant.getState() == Plant.PlantState.INCAPACITATED) continue;
                if (plant.getTags().contains(Tag.FIRE)) continue;

                int newLevel = plant.getChillLevel() + 1;
                plant.setChillLevel(newLevel);

                if (newLevel >= FREEZE_STACKS) {
                    cell.setStructure(new IceBlock(plant, 500));
                    session.registerStructure(cell.getInteractableStructure());
                }
            }
        }
    }
}