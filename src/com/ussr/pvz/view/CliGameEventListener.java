package com.ussr.pvz.view;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.engine.event.GameEventBus;

public class CliGameEventListener {

    private final GameSession session;

    public CliGameEventListener(GameSession session) {
        this.session = session;
        GameEventBus bus = session.getEventBus();
        registerZombieEvents(bus);
        registerPlantEvents(bus);
        registerSystemEvents(bus);
        registerSunEvents(bus);
    }

    private String tick() {
        return "[T" + session.getTicks() + "] ";
    }

    private void registerZombieEvents(GameEventBus bus) {
        bus.subscribe(GameEvent.ZombieSpawned.class,
                e -> {
                    String glowLabel = e.isGlowing() ? " [GLOWING]" : "";
                    System.out.println(tick() + "[SPAWN] " + e.alias() + glowLabel + " entered lane " + e.lane() + " at col " + e.col());
                });

        bus.subscribe(GameEvent.ZombieDied.class,
                e -> System.out.println(tick() + "[DEAD] " + e.alias() + " was defeated at (" + String.format("%.1f", e.x()) + ", " + String.format("%.1f", e.y()) + ")"));

        bus.subscribe(GameEvent.ZombieBreachedLane.class,
                e -> System.out.println(tick() + "[BREACH] Zombie reached the end of lane " + e.lane() + " — lawn mower activated!"));

        bus.subscribe(GameEvent.ZombieReachedHouse.class,
                e -> System.out.println(tick() + "[BREACH] A zombie reached the house!"));

        bus.subscribe(GameEvent.GlowingZombieDroppedPlantFood.class,
                e -> System.out.println(tick() + "The glowing zombie dropeed a plant food; you have " + e.currentCount() + " plant foods now."));
    }

    private void registerPlantEvents(GameEventBus bus) {
        bus.subscribe(GameEvent.PlantDied.class,
                e -> System.out.println(tick() + "[PLANT DIED] " + e.plantName() + " at (" + e.col() + ", " + e.row() + ")"));

        bus.subscribe(GameEvent.PlantPlanted.class,
                e -> System.out.println(tick() + "[PLANTED] " + e.plantName() + " at (" + e.col() + ", " + e.row() + ")"));

        bus.subscribe(GameEvent.PlantPlucked.class,
                e -> System.out.println(tick() + "[PLUCKED] " + e.plantName() + " removed from (" + e.col() + ", " + e.row() + ")"));

        bus.subscribe(GameEvent.PlantIncinerated.class,
                e -> System.out.println(tick() + "[INCINERATED] " + e.plantName() + " was burned to death by " + e.sourceZombieAlias() + "'s torch at (" + e.col() + ", " + e.row() + ")"));
    }

    private void registerSystemEvents(GameEventBus bus) {
        bus.subscribe(GameEvent.LawnMowerTriggered.class,
                e -> System.out.println(tick() + "[LAWNMOWER] Triggered on row " + e.lane()));

        bus.subscribe(GameEvent.GraveDestroyed.class,
                e -> System.out.println(tick() + "[GRAVE] A tombstone at (" + e.col() + ", " + e.row() + ") has been destroyed!"));

        bus.subscribe(GameEvent.StructureDestroyed.class,
                e -> System.out.println(tick() + "[STRUCTURE] " + e.structureType() + " at (" + e.col() + ", " + e.row() + ") destroyed"));

        bus.subscribe(GameEvent.WaveStarted.class, e -> {
            if (e.isFinalWave()) System.out.println(tick() + "The final wave has come.");
            else System.out.println(tick() + "Wave " + e.waveNumber() + " started.");
        });

        bus.subscribe(GameEvent.WavesCompleted.class,
                e -> System.out.println(tick() + "[WAVES] All waves cleared!"));

        bus.subscribe(GameEvent.GameOver.class,
                e -> System.out.println(tick() + "[GAME OVER] A zombie reached the house!"));

        bus.subscribe(GameEvent.GameWon.class,
                e -> System.out.println(tick() + "[YOU WIN] All zombies defeated!"));
    }

    private void registerSunEvents(GameEventBus bus) {
        bus.subscribe(GameEvent.SunStartedFalling.class,
                e -> System.out.println(tick() + "New " + e.type() + " sun is dropping at position (" + e.x() + ", " + e.y() + ")"));

        bus.subscribe(GameEvent.SunGrounded.class,
                e -> System.out.println(tick() + "Sun reached the ground at position (" + e.x() + ", " + e.y() + ")"));

        bus.subscribe(GameEvent.SunExpired.class,
                e -> System.out.println(tick() + "Sun at position (" + e.x() + ", " + e.y() + ") expired and disappeared"));

        bus.subscribe(GameEvent.SunProduced.class,
                e -> System.out.println(tick() + e.plantName() + " produced a sun at (" + (int) e.x() + ", " + (int) e.y() + ")"));

        bus.subscribe(GameEvent.SunCollected.class,
                e -> System.out.println(tick() + "Collected a sun! (+" + e.value() + ") Total sun: " + e.totalSun()));

        bus.subscribe(GameEvent.SunAbsorbedByZombie.class,
                e -> System.out.println(tick() + "[SUN STOLEN] " + e.zombieAlias() + " absorbed a sun (+" + e.value() + ") at (" + (int) e.x() + ", " + (int) e.y() + ")"));
    }
}