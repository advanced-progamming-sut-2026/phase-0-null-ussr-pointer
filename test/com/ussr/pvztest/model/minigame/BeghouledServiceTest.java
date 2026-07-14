package com.ussr.pvztest.model.minigame;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.Row;
import com.ussr.pvz.model.board.terrain.Tile;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.level.behavior.BeghouledBehavior;
import com.ussr.pvz.service.minigame.BeghouledService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BeghouledServiceTest {

    private BeghouledService beghouledService;
    private GameSession session;

    @BeforeEach
    void setUp() {
        beghouledService = new BeghouledService();
        session = new GameSession();

        Lawn lawn = new Lawn(5, 9);
        for (int r = 0; r < 5; r++) {
            Row row = new Row(r);
            for (int c = 0; c < 9; c++) {
                Cell cell = new Cell();
                cell.setRow(r);
                cell.setCol(c);
                cell.setTile(new Tile(TileType.Normal));
                row.addCell(cell);
            }
            lawn.addRow(row);
        }
        session.setLawn(lawn);
        session.setPlants(new ArrayList<>());
        session.setZombies(new ArrayList<>());
        session.setItems(new ArrayList<>());

        Level level = new Level();
        BeghouledBehavior behavior = new BeghouledBehavior(20, List.of("peashooter", "wall-nut", "sunflower"));
        level.setBehavior(behavior);
        session.setLevel(level);

        App.setGameSession(session);

        // Let behavior populate the board
        behavior.onStart(level);
    }

    @Test
    @DisplayName("❌ Should reject swap if tiles are not adjacent")
    void swapPlants_shouldFail_whenNotAdjacent() {
        // Act
        String result = beghouledService.swapPlants(0, 0, 2, 2);

        // Assert
        assertTrue(result.contains("Invalid swap"));
    }

    @Test
    @DisplayName("✅ Should successfully upgrade plants and deduct sun")
    void upgradePlant_shouldSucceed_whenSunIsSufficient() {
        // Arrange
        session.addSun(1000); // Repeater upgrade costs 500

        // Act
        String result = beghouledService.upgradePlant("peashooter");

        // Assert
        assertEquals("Successfully upgraded all peashooters to repeaters!", result);
        assertEquals(500, session.getSunCount());
    }

    @Test
    @DisplayName("❌ Should fail to upgrade plant when sun is insufficient")
    void upgradePlant_shouldFail_whenSunInsufficient() {
        // Arrange
        session.addSun(100); // Repeater upgrade costs 500

        // Act
        String result = beghouledService.upgradePlant("peashooter");

        // Assert
        assertEquals("Not enough sun! Upgrade to repeater costs 500 sun.", result);
    }
}