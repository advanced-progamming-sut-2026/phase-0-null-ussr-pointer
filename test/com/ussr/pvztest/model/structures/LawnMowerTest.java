package com.ussr.pvztest.model.structures;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.structures.LawnMower;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LawnMowerTest {

    private LawnMower mower;
    private GameSession session;

    @BeforeEach
    void setUp() {
        session = new GameSession();
        session.setLawn(new Lawn(5, 9));
        session.setZombies(new ArrayList<>());
        App.setGameSession(session);

        mower = new LawnMower(2, Vec2.of(-0.5, 2)); // Lane 2
    }

    @Test
    @DisplayName("✅ Should not move if not activated")
    void tick_shouldNotMove_whenNotActivated() {
        mower.tick();
        assertEquals(-0.5, mower.getPosition().x(), 0.001);
    }

    @Test
    @DisplayName("✅ Should kill zombies in the same lane when rolling over them")
    void tick_shouldKillZombies_whenActivatedAndOverlapping() {
        // Arrange
        mower.activate();

        Zombie zombie = new Zombie("Basic", null, false);
        zombie.setPosition(Vec2.of(0.0, 2.0)); // In lane 2, X=0.0
        zombie.setHp(190);
        session.getZombies().add(zombie);

        // Act - Simulate multiple ticks to move mower past the zombie
        // Mower speed is 0.15 per tick
        for (int i = 0; i < 5; i++) {
            mower.tick();
        }

        // Assert
        // Mower is now at X = -0.5 + (0.15 * 5) = 0.25 (Past the zombie at 0.0)
        assertEquals(0.25, mower.getPosition().x(), 0.001);
        assertTrue(zombie.getHp() <= 0); // Zombie is dead
    }

    @Test
    @DisplayName("✅ Should despawn itself when passing the right edge of the lawn")
    void tick_shouldDespawn_whenPastLawnBoundary() {
        // Arrange
        mower.activate();
        mower.setPosition(Vec2.of(9.4, 2)); // Just before the edge

        // Act
        mower.tick(); // Moves to 9.55

        // Assert
        assertFalse(mower.isAlive());
    }
}