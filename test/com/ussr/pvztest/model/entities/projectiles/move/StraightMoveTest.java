package com.ussr.pvztest.model.entities.projectiles.move;

import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.move.StraightMove;
import com.ussr.pvz.model.util.Vec2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StraightMoveTest {

    @Test
    @DisplayName("✅ Should move precisely according to GameClock tick delta")
    void move_shouldApplyVelocityBasedOnTickRate() {
        // Arrange
        StraightMove moveStrategy = new StraightMove();
        Projectile projectile = new Projectile(
                null,
                Vec2.of(0, 2),
                Vec2.of(4.0, 0), // Speed 4.0 in X direction
                20,
                moveStrategy,
                null
        );

        // Act - Simulate 1 tick (0.1 seconds)
        moveStrategy.move(projectile);

        // Assert
        // New X = 0 + (4.0 * 0.1) = 0.4
        assertEquals(0.4, projectile.getPosition().x(), 0.001);
        assertEquals(2.0, projectile.getPosition().y(), 0.001); // Y remains unchanged
    }
}