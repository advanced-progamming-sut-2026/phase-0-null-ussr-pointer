package com.ussr.pvz.model.level.environment;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.Grave;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DarkAgesEnvironment implements Environment {
    private final String necromancyZombieAlias;
    private final int zombiesPerNecromancyWave;

    public DarkAgesEnvironment(String necromancyZombieAlias, int zombiesPerNecromancyWave) {
        this.necromancyZombieAlias = necromancyZombieAlias;
        this.zombiesPerNecromancyWave = zombiesPerNecromancyWave;
    }

    @Override
    public void onStart(GameSession session) {
        // Event-driven mechanic: Spawns zombies from graves when a wave starts
        if (zombiesPerNecromancyWave > 0) {
            session.getEventBus().subscribe(GameEvent.WaveStarted.class, event -> {
                triggerNecromancy(session);
            });
        }
    }

    @Override
    public void tick(GameSession session, double deltaTime) {
        // No continuous physics needed for Dark Ages
    }

    private void triggerNecromancy(GameSession session) {
        if (session.getLawn() == null) return;

        List<Grave> activeGraves = new ArrayList<>();
        int rows = session.getLawn().getRows();
        int cols = session.getLawn().getCols();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = session.getLawn().getCell(r, c);
                if (cell != null && cell.getInteractableStructure() instanceof Grave grave && grave.isAlive()) {
                    activeGraves.add(grave);
                }
            }
        }

        Collections.shuffle(activeGraves);
        int spawned = 0;
        for (Grave grave : activeGraves) {
            if (spawned >= zombiesPerNecromancyWave) break;
            int gRow = (int) grave.getPosition().y();
            int gCol = (int) grave.getPosition().x();
            session.spawnZombie(ZombieFactory.create(necromancyZombieAlias, gRow, gCol));
            spawned++;
        }
    }
}