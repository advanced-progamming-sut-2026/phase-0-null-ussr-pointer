package com.ussr.pvz.model.level.chaptereffect;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.Grave;
import com.ussr.pvz.model.board.terrain.Tile;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.util.Vec2;

import java.util.List;
import java.util.Random;

public class AncientEgyptEffect implements ChapterEffect {

    private static final Random RAND = new Random();


    @Override
    public void onWaveStart(GameSession session, Level level, int waveNumber, boolean isFinalWave) {
        if (!isFinalWave) return;

        List<Level.SandstormEvent> schedule = level.getSandstormSchedule();
        if (schedule != null) {
            for (Level.SandstormEvent event : schedule) {
                triggerSandstorm(session, event.zombieAlias());
            }
        }
    }

    private void triggerSandstorm(GameSession session, String zombieAlias) {
        if (session.getLawn() == null) return;

        int targetCol = RAND.nextInt(4) + 1;
        int targetRow = RAND.nextInt(session.getLawn().getRows());

        try {
            Zombie zombie = ZombieFactory.create(zombieAlias, targetRow, targetCol);
            zombie.setPosition(Vec2.of(targetCol, targetRow));
            session.spawnZombie(zombie);
            System.out.println("A sandstorm blows in a zombie at row " + (targetRow + 1) + "!");
        } catch (IllegalArgumentException e) {
            System.err.println("[AncientEgyptEffect] Could not spawn sandstorm zombie: " + e.getMessage());
        }
    }
}