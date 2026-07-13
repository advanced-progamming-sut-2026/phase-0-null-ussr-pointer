package com.ussr.pvz.model.level.chaptereffect;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.structures.Grave;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DarkAgesEffect implements ChapterEffect {

    @Override
    public void onWaveStart(GameSession session, Level level, int waveNumber, boolean isFinalWave) {
        int perWave = level.getZombiesPerNecromancyWave();
        String zombieAlias = level.getNecromancyZombieAlias();
        if (perWave <= 0 || zombieAlias == null || zombieAlias.isBlank()) return;

        Lawn lawn = session.getLawn();
        if (lawn == null) return;

        List<Grave> activeGraves = new ArrayList<>();
        int rows = lawn.getRows();
        int cols = lawn.getCols();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = lawn.getCell(r, c);
                if (cell != null && cell.getInteractableStructure() instanceof Grave grave && grave.isAlive()) {
                    activeGraves.add(grave);
                }
            }
        }

        Collections.shuffle(activeGraves);
        int spawned = 0;
        for (Grave grave : activeGraves) {
            if (spawned >= perWave) break;
            int gRow = (int) grave.getPosition().y();
            int gCol = (int) grave.getPosition().x();
            try {
                session.spawnZombie(ZombieFactory.create(zombieAlias, gRow, gCol));
                System.out.println("A zombie claws its way out of the grave at (" + gCol + ", " + gRow + ")!");
                spawned++;
            } catch (IllegalArgumentException e) {
                System.err.println("[DarkAgesEffect] Could not raise zombie: " + e.getMessage());
            }
        }
    }
}