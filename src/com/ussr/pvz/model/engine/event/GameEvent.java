package com.ussr.pvz.model.engine.event;

public sealed interface GameEvent permits GameEvent.GameOver, GameEvent.GameWon, GameEvent.GraveDestroyed, GameEvent.LawnMowerTriggered, GameEvent.PlantDamaged, GameEvent.PlantDied, GameEvent.PlantFoodDropped, GameEvent.PlantFoodUsed, GameEvent.PlantIncinerated, GameEvent.PlantPlanted, GameEvent.PlantPlucked, GameEvent.ProjectileFired, GameEvent.ProjectileHit, GameEvent.StructureDestroyed, GameEvent.SunAbsorbedByZombie, GameEvent.SunCollected, GameEvent.SunExpired, GameEvent.SunGrounded, GameEvent.SunProduced, GameEvent.SunStartedFalling, GameEvent.WaveStarted, GameEvent.WavesCompleted, GameEvent.ZombieBreachedLane, GameEvent.ZombieDied, GameEvent.ZombieReachedHouse, GameEvent.ZombieSpawned, GameEvent.GlowingZombieDroppedPlantFood {


    record ZombieSpawned(String alias, int lane, int col, boolean isGlowing) implements GameEvent {
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

    record SunAbsorbedByZombie(String zombieAlias, int value, double x, double y) implements GameEvent {
    }

    record PlantIncinerated(String plantName, String sourceZombieAlias, int row, int col) implements GameEvent {
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

    record GlowingZombieDroppedPlantFood(int currentCount) implements GameEvent {
    }
}