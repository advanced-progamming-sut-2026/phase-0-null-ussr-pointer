package com.ussr.pvz.model.engine.event;

import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;

public sealed interface GameEvent permits GameEvent.FreezingWindTriggered,
        GameEvent.GameOver, GameEvent.GameWon, GameEvent.GlowingZombieDroppedPlantFood,
        GameEvent.GraveDestroyed, GameEvent.LawnMowerTriggered, GameEvent.PlantDamaged,
        GameEvent.PlantDied, GameEvent.PlantFoodDropped, GameEvent.PlantFoodUsed,
        GameEvent.PlantIncinerated, GameEvent.PlantPlanted, GameEvent.PlantPlucked,
        GameEvent.ProjectileFired, GameEvent.ProjectileHit, GameEvent.SandstormTriggered,
        GameEvent.StructureDestroyed, GameEvent.SunAbsorbedByZombie, GameEvent.SunCollected,
        GameEvent.SunExpired, GameEvent.SunGrounded, GameEvent.SunProduced, GameEvent.SunStartedFalling,
        GameEvent.WaveStarted, GameEvent.WavesCompleted, GameEvent.ZombieBreachedLane, GameEvent.ZombieDied,
        GameEvent.ZombieReachedHouse, GameEvent.SpecialLevelAnnouncement, GameEvent.ZombieSpawned
        ,GameEvent.MeowScoreMilestone{


    record ZombieSpawned(Zombie zombie) implements GameEvent {
        public String alias() { return zombie.getAlias(); }
        public int lane() { return (int) zombie.getPosition().y(); }
        public int col() { return (int) zombie.getPosition().x(); }
        public boolean isGlowing() { return zombie.isGlowing(); }
    }

    record SunStartedFalling(String type, int x , int y) implements GameEvent {
    }

    record SunGrounded(int x , int y) implements GameEvent {
    }

    record SunExpired(int x, int y) implements GameEvent {
    }

    record ZombieDied(Zombie zombie, String killerPlantName) implements GameEvent {
        public String alias() { return zombie.getAlias(); }
        public double x() { return zombie.getPosition().x(); }
        public double y() { return zombie.getPosition().y(); }
    }

    record ZombieReachedHouse(int lane) implements GameEvent {
    }

    record ZombieBreachedLane(int lane) implements GameEvent {
    }

    record LawnMowerTriggered(int lane) implements GameEvent {
    }

    record PlantDamaged(String plantName, int row, int col, int damageDealt, int hpRemaining)
            implements GameEvent {
    }

    record PlantDied(Plant plant) implements GameEvent {
        public String plantName() { return plant.getName(); }
        public int row() { return plant.getLocation().y(); }
        public int col() { return plant.getLocation().x(); }
    }

    record PlantPlanted(Plant plant) implements GameEvent {
        public String plantName() { return plant.getName(); }
        public int row() { return plant.getLocation().y(); }
        public int col() { return plant.getLocation().x(); }
    }

    record PlantPlucked(Plant plant) implements GameEvent {
        public String plantName() { return plant.getName(); }
        public int row() { return plant.getLocation().y(); }
        public int col() { return plant.getLocation().x(); }
    }

    record SunProduced(String plantName, int value, double x, double y) implements GameEvent {
    }

    record SunCollected(int value, int totalSun) implements GameEvent {
    }

    record SunAbsorbedByZombie(String zombieAlias, int value, double x, double y) implements GameEvent {
    }

    record PlantIncinerated(String plantName, String sourceZombieAlias, int row, int col) implements GameEvent {
    }

    record PlantFoodDropped(double x, double y) implements GameEvent {
    }

    record PlantFoodUsed(Plant plant) implements GameEvent {
        public String plantName() { return plant.getName(); }
        public int row() { return plant.getLocation().y(); }
        public int col() { return plant.getLocation().x(); }
    }

    record ProjectileFired(String plantName, double startX, double startY) implements GameEvent {
    }

    record ProjectileHit(String zombieAlias, int damageDealt) implements GameEvent {
    }

    record WaveStarted(int waveNumber, boolean isFinalWave) implements GameEvent {
    }

    record WavesCompleted() implements GameEvent {
    }

    record GameOver() implements GameEvent {
    }

    record GameWon() implements GameEvent {
    }

    record StructureDestroyed(String structureType, int row, int col) implements GameEvent {
    }

    record GraveDestroyed(int row, int col) implements GameEvent {
    }

    record GlowingZombieDroppedPlantFood(int currentCount) implements GameEvent {
    }

    record SandstormTriggered(int row, int column) implements GameEvent {
    }

    record FreezingWindTriggered() implements GameEvent {
    }

    record SpecialLevelAnnouncement(String message) implements GameEvent {
    }

    record MeowScoreMilestone(int threshold, int milestoneIndex) implements GameEvent {}
}
