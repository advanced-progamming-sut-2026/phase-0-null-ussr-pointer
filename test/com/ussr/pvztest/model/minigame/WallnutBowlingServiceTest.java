package com.ussr.pvztest.model.minigame;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.level.behavior.WallnutBowlingBehavior;
import com.ussr.pvz.service.minigame.WallnutBowlingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WallnutBowlingServiceTest {

    private WallnutBowlingService bowlingService;
    private GameSession session;

    @BeforeEach
    void setUp() {
        bowlingService = new WallnutBowlingService();
        session = new GameSession();
        session.setLawn(new Lawn(5, 9));

        Level level = new Level();
        level.setBehavior(new WallnutBowlingBehavior(2)); // Red line at col 2
        session.setLevel(level);

        App.setGameSession(session);
    }

    @Test
    @DisplayName("✅ Should spawn a bowling projectile when dropped before the red line")
    void rollWallnut_shouldSucceed_whenBehindRedLine() {
        // Act
        String result = bowlingService.rollWallnut("NORMAL", 1, 2);

        // Assert
        assertEquals("Rolled a NORMAL!", result);
        assertEquals(1, session.getProjectiles().size());
    }

    @Test
    @DisplayName("❌ Should reject rolling a nut past the red line")
    void rollWallnut_shouldFail_whenPastRedLine() {
        // Act
        String result = bowlingService.rollWallnut("GIANT-WALLNUT", 4, 2);

        // Assert
        assertEquals("You cannot place a Wall-nut past the red line!", result);
        assertTrue(session.getProjectiles().isEmpty());
    }
}