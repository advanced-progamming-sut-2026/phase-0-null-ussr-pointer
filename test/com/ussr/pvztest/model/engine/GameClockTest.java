package com.ussr.pvztest.model.engine;

import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.Tickable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameClockTest {

    private GameClock gameClock;

    @BeforeEach
    void setUp() {
        gameClock = new GameClock();
    }

    @Test
    @DisplayName("✅ Should increment ticks and calculate exact elapsed seconds (0.1s per tick)")
    void tick_shouldCalculateElapsedSecondsCorrectly() {
        // Arrange
        int targetTicks = 15;

        // Act
        for (int i = 0; i < targetTicks; i++) {
            gameClock.tick();
        }

        // Assert
        assertEquals(15, gameClock.getTicks());
        assertEquals(1.5, gameClock.getElapsedSeconds(), 0.001);
    }

    @Test
    @DisplayName("✅ Should broadcast tick to all registered entities")
    void tick_shouldNotifyAllEntities() {
        // Arrange
        final int[] tickCounter = {0};
        Tickable mockEntity = () -> tickCounter[0]++;
        gameClock.addEntity(mockEntity);

        // Act
        gameClock.tick();
        gameClock.tick();

        // Assert
        assertEquals(2, tickCounter[0]);
    }

    @Test
    @DisplayName("✅ Should clear entities and reset counters upon reset")
    void reset_shouldClearState() {
        // Arrange
        gameClock.addEntity(() -> {});
        gameClock.tick();

        // Act
        gameClock.reset();

        // Assert
        assertEquals(0, gameClock.getTicks());
        assertEquals(0.0, gameClock.getElapsedSeconds(), 0.001);
    }
}