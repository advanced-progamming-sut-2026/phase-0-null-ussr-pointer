package com.ussr.pvz.model.engine.event;

public sealed interface GameEvent permits
        GameEvent.ZombieSpawned,
        GameEvent.ZombieDied,
        GameEvent.ZombieReachedHouse,
        GameEvent.ZombieBreachedLane,
        GameEvent.LawnMowerTriggered,
        GameEvent.PlantDamaged,
        GameEvent.PlantDied,
        GameEvent.PlantPlanted,
        GameEvent.PlantPlucked,
        GameEvent.SunProduced,
        GameEvent.SunCollected,
        GameEvent.PlantFoodDropped,
        GameEvent.PlantFoodUsed,
        GameEvent.ProjectileFired,
        GameEvent.ProjectileHit,
        GameEvent.WaveStarted,
        GameEvent.WavesCompleted,
        GameEvent.GameOver,
        GameEvent.GameWon,
        GameEvent.StructureDestroyed,
        GameEvent.SunStartedFalling,
        GameEvent.SunGrounded,
        GameEvent.SunExpired,
        GameEvent.GraveDestroyed {


    record ZombieSpawned(String alias, int lane, int col) implements GameEvent {
    }

    record SunStartedFalling(String type, int x , int y) implements GameEvent {
    }

    record SunGrounded(int x , int y) implements GameEvent {
    }

    record SunExpired(int x, int y) implements GameEvent {
    }

    record ZombieDied(String alias, double x, double y, String killerPlantName) implements GameEvent {
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

    record PlantDied(String plantName, int row, int col) implements GameEvent {
    }

    record PlantPlanted(String plantName, int row, int col) implements GameEvent {
    }

    record PlantPlucked(String plantName, int row, int col) implements GameEvent {
    }

    record SunProduced(String plantName, int value, double x, double y) implements GameEvent {
    }

    record SunCollected(int value, int totalSun) implements GameEvent {
    }

    record PlantFoodDropped(double x, double y) implements GameEvent {
    }

    record PlantFoodUsed(String plantName, int row, int col) implements GameEvent {
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
}