package com.ussr.pvz.model.level.chaptereffect;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.util.Vec2;

import java.util.List;
import java.util.Random;

/**
 * Ancient Egypt: at scheduled times (level.getSandstormSchedule(), driven by
 * the "sandstorms" array in levels.json), a sandstorm drops an extra zombie
 * into a random lane/column.
 */
public class AncientEgyptEffect implements ChapterEffect {

    private static final Random RAND = new Random();

    @Override
    public void onTick(GameSession session, Level level, double deltaTime) {
        List<Level.SandstormEvent> schedule = level.getSandstormSchedule();
        int nextIndex = level.getNextSandstormIndex();

        if (nextIndex >= schedule.size()) return;

        Level.SandstormEvent nextEvent = schedule.get(nextIndex);
        if (session.getElapsedSeconds() >= nextEvent.triggerTimeSeconds()) {
            triggerSandstorm(session, nextEvent.zombieAlias());
            level.setNextSandstormIndex(nextIndex + 1);
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