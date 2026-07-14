package com.ussr.pvztest.model.game;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;
import com.ussr.pvz.service.game.PlantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlantServiceTest {

    private PlantService plantService;
    private GameSession session;
    private Plant testPlant;

    @BeforeEach
    void setUp() {
        plantService = new PlantService();
        session = new GameSession();
        session.setZombies(new ArrayList<>());
        session.setItems(new ArrayList<>());
        App.setGameSession(session);

        testPlant = new Plant();
        testPlant.setLocation(new Plant.Location(2, 2)); // Col 2, Row 2
        testPlant.setHp(300);
        testPlant.setAlive(true);
    }

    @Test
    @DisplayName("✅ Should find the nearest zombie in the same lane to the right")
    void findTargetInLane_shouldReturnNearestZombieRightOfPlant() {
        // Arrange
        Zombie z1 = new Zombie("Basic", null, false);
        z1.setPosition(Vec2.of(5.0, 2.0)); // Row 2, Col 5 (Target)

        Zombie z2 = new Zombie("Basic", null, false);
        z2.setPosition(Vec2.of(8.0, 2.0)); // Row 2, Col 8 (Further away)

        Zombie z3 = new Zombie("Basic", null, false);
        z3.setPosition(Vec2.of(1.0, 2.0)); // Row 2, Col 1 (Behind plant, should be ignored)

        session.getZombies().addAll(List.of(z1, z2, z3));

        // Act
        Zombie target = plantService.findTargetInLane(testPlant, session);

        // Assert
        assertNotNull(target);
        assertEquals(z1, target);
    }

    @Test
    @DisplayName("✅ Should retrieve all zombies within an AoE radius")
    void findZombiesInRange_shouldReturnZombiesWithinRadius() {
        // Arrange
        Zombie z1 = new Zombie("Basic", null, false);
        z1.setPosition(Vec2.of(2.0, 2.0)); // Exact same spot (Dist 0)

        Zombie z2 = new Zombie("Basic", null, false);
        z2.setPosition(Vec2.of(3.0, 3.0)); // Diagonal (Dist ~1.41)

        Zombie z3 = new Zombie("Basic", null, false);
        z3.setPosition(Vec2.of(7.0, 2.0)); // Far away (Dist 5.0)

        session.getZombies().addAll(List.of(z1, z2, z3));

        // Act - Radius of 1.5
        List<Zombie> targets = plantService.findZombiesInRange(testPlant, 1.5, session);

        // Assert
        assertEquals(2, targets.size());
        assertTrue(targets.contains(z1));
        assertTrue(targets.contains(z2));
        assertFalse(targets.contains(z3));
    }

    @Test
    @DisplayName("✅ Should damage zombies in radius and kill plant upon explosion")
    void explode_shouldDamageZombiesAndKillPlant() {
        // Arrange
        Zombie z1 = new Zombie("Basic", null, false);
        z1.setPosition(Vec2.of(2.5, 2.0));
        z1.setHp(200);
        session.getZombies().add(z1);

        // Act
        plantService.explode(testPlant, session, 1800, 1.5);

        // Assert
        assertTrue(z1.getHp() < 200); // Zombie took damage
        assertFalse(testPlant.isAlive()); // Plant sacrificed itself
    }
}