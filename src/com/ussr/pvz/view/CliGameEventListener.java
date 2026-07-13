package com.ussr.pvz.view;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.engine.event.GameEventBus;

public class CliGameEventListener {

    public CliGameEventListener(GameSession session) {
        GameEventBus bus = session.getEventBus();
        registerZombieEvents(bus);
        registerPlantEvents(bus);
        registerSystemEvents(bus);
        registerSunEvents(bus);
    }

    private void registerZombieEvents(GameEventBus bus) {
        bus.subscribe(GameEvent.ZombieSpawned.class,
                e -> System.out.println("[SPAWN] " + e.alias() + " entered lane " + e.lane() + " at col " + e.col()));

        bus.subscribe(GameEvent.ZombieDied.class,
                e -> System.out.println("[DEAD] " + e.alias() + " was defeated at (" + String.format("%.1f", e.x()) + ", " + String.format("%.1f", e.y()) + ")"));

        bus.subscribe(GameEvent.ZombieBreachedLane.class,
                e -> System.out.println("[BREACH] Zombie reached the end of lane " + e.lane() + " — lawn mower activated!"));

        bus.subscribe(GameEvent.ZombieReachedHouse.class,
                e -> System.out.println("[BREACH] A zombie reached the house!"));
    }

    private void registerPlantEvents(GameEventBus bus) {
        bus.subscribe(GameEvent.PlantDied.class,
                e -> System.out.println("[PLANT DIED] " + e.plantName() + " at (" + e.row() + ", " + e.col() + ")"));

        bus.subscribe(GameEvent.PlantPlanted.class,
                e -> System.out.println("[PLANTED] " + e.plantName() + " at (" + e.row() + ", " + e.col() + ")"));

        bus.subscribe(GameEvent.PlantPlucked.class,
                e -> System.out.println("[PLUCKED] " + e.plantName() + " removed from (" + e.row() + ", " + e.col() + ")"));
    }

    private void registerSystemEvents(GameEventBus bus) {
        bus.subscribe(GameEvent.LawnMowerTriggered.class,
                e -> System.out.println("[LAWNMOWER] Triggered on row " + e.lane()));

        bus.subscribe(GameEvent.GraveDestroyed.class,
                e -> System.out.println("[GRAVE] A tombstone at (" + e.row() + ", " + e.col() + ") has been destroyed!"));

        bus.subscribe(GameEvent.StructureDestroyed.class,
                e -> System.out.println("[STRUCTURE] " + e.structureType() + " at (" + e.row() + ", " + e.col() + ") destroyed"));

        bus.subscribe(GameEvent.WaveStarted.class, e -> {
            if (e.isFinalWave()) System.out.println("The final wave has come.");
            else System.out.println("Wave " + e.waveNumber() + " started.");
        });

        bus.subscribe(GameEvent.WavesCompleted.class,
                e -> System.out.println("[WAVES] All waves cleared!"));

        bus.subscribe(GameEvent.GameOver.class,
                e -> System.out.println("[GAME OVER] A zombie reached the house!"));

        bus.subscribe(GameEvent.GameWon.class,
                e -> System.out.println("[YOU WIN] All zombies defeated!"));
    }

    private void registerSunEvents(GameEventBus bus) {
        bus.subscribe(GameEvent.SunStartedFalling.class,
                e -> System.out.println("New " + e.type() + " sun is dropping at position (" + e.x() + "," + e.y() + ")"));

        bus.subscribe(GameEvent.SunGrounded.class,
                e -> System.out.println("Sun reached the ground at position (" + e.x() + "," + e.y() + ")"));

        bus.subscribe(GameEvent.SunExpired.class,
                e -> System.out.println("Sun at position (" + e.x() + "," + e.y() + ") expired and disappeared"));
    }
}