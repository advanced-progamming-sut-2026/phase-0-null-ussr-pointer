package com.ussr.pvz.model.level.environment;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class AncientEgyptEnvironment implements Environment {

    public record SandstormEvent(double triggerTimeSeconds, String zombieAlias) {}

    private final List<SandstormEvent> sandstormSchedule;
    private int nextEventIndex = 0;
    private final Random rand = new Random();

    public AncientEgyptEnvironment(List<SandstormEvent> sandstormSchedule) {
        this.sandstormSchedule = sandstormSchedule != null ? new ArrayList<>(sandstormSchedule) : new ArrayList<>();
        this.sandstormSchedule.sort(Comparator.comparingDouble(SandstormEvent::triggerTimeSeconds));
    }

    @Override
    public void onStart(GameSession session) {}

    @Override
    public void tick(GameSession session, double deltaTime) {
        if (nextEventIndex < sandstormSchedule.size()) {
            SandstormEvent nextEvent = sandstormSchedule.get(nextEventIndex);

            if (session.getElapsedSeconds() >= nextEvent.triggerTimeSeconds()) {
                triggerSandstorm(session, nextEvent.zombieAlias());
                nextEventIndex++;
            }
        }
    }

    private void triggerSandstorm(GameSession session, String zombieAlias) {
        if (session.getLawn() == null) return;

        int targetCol = rand.nextInt(4) + 1;
        int targetRow = rand.nextInt(session.getLawn().getRows());

        Zombie zombie = ZombieFactory.create(zombieAlias, targetRow, targetCol);
        zombie.setPosition(Vec2.of(targetCol, targetRow));
        session.spawnZombie(zombie);
    }
}