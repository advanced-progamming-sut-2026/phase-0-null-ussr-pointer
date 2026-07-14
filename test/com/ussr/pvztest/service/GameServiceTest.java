package com.ussr.pvztest.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.AccountState;
import com.ussr.pvz.model.account.Gender;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.Row;
import com.ussr.pvz.model.board.terrain.Tile;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.dto.*;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.items.sun.ProducedSun;
import com.ussr.pvz.service.game.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {

    private GameService gameService;
    private GameSession session;
    private Account activeAccount;

    @BeforeEach
    void setUp() {
        App.getAccounts().clear();
        App.login(null);
        gameService = new GameService();

        // Initialize user with some plants
        AccountState state = new AccountState(
                "game-user", "Gamer", "pass", "gamer@example.com", Gender.MALE, 3,
                null, null, 1, 1, 0, 0, 1000, 50, 0,
                new HashMap<>(), new ArrayList<>(), new ArrayList<>(),
                null, null, 0, new HashMap<>(), new ArrayList<>(), new HashMap<>()
        );
        activeAccount = new Account(state, null);
        activeAccount.getAdventureProgress().getPlantLvls().put("PEASHOOTER", 1);
        activeAccount.getAdventureProgress().getPlantLvls().put("SUNFLOWER", 1);
        App.addAccount(activeAccount);
        App.login(activeAccount);

        // Initialize active GameSession
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
        session.addSun(500); // Pre-fund sun for planting

        App.setGameSession(session);
    }

    @Test
    @DisplayName("✅ Should collect sun exactly at target coordinates")
    void collectSun_shouldSucceed_whenSunExistsAtLocation() {
        // Arrange
        session.addItem(new ProducedSun(2, 3, 50));
        LocationRequest request = new LocationRequest("2", "3");

        // Act
        String result = gameService.collectSun(request);

        // Assert
        assertEquals("sun collected at (2, 3)", result);
        assertEquals(550, session.getSunCount());
    }

    @Test
    @DisplayName("❌ Should fail to collect sun if coordinates are empty")
    void collectSun_shouldFail_whenLocationEmpty() {
        // Arrange
        LocationRequest request = new LocationRequest("1", "1");

        // Act
        String result = gameService.collectSun(request);

        // Assert
        assertEquals("no sun found at (1, 1)", result);
        assertEquals(500, session.getSunCount());
    }

    @Test
    @DisplayName("✅ Should successfully plant a plant, deduct sun, and trigger event")
    void plantPlant_shouldSucceed_whenValid() {
        // Arrange
        PlantPlantRequest request = new PlantPlantRequest("PEASHOOTER", "2", "3");

        // Act
        String result = gameService.plantPlant(request);

        // Assert
        assertEquals("plant Peashooter placed at (2, 3)", result);
        assertEquals(400, session.getSunCount()); // 500 - 100 for peashooter
        assertNotNull(session.getLawn().getCell(3, 2).getPlant());
        assertEquals("Peashooter", session.getLawn().getCell(3, 2).getPlant().getName());
    }

    @Test
    @DisplayName("❌ Should fail to plant if player has insufficient sun")
    void plantPlant_shouldFail_whenInsufficientSun() {
        // Arrange
        session.spendSun(450); // Leaves 50 sun
        PlantPlantRequest request = new PlantPlantRequest("PEASHOOTER", "2", "3");

        // Act
        String result = gameService.plantPlant(request);

        // Assert
        assertTrue(result.contains("not enough sun to plant"));
        assertNull(session.getLawn().getCell(3, 2).getPlant());
    }

    @Test
    @DisplayName("❌ Should fail to plant on an already occupied tile")
    void plantPlant_shouldFail_whenTileOccupied() {
        // Arrange
        gameService.plantPlant(new PlantPlantRequest("PEASHOOTER", "2", "3"));
        PlantPlantRequest request2 = new PlantPlantRequest("SUNFLOWER", "2", "3");

        // Act
        String result = gameService.plantPlant(request2);

        // Assert
        assertTrue(result.contains("a plant is already at (2, 3)"));
    }

    @Test
    @DisplayName("✅ Should execute cheat to add suns correctly")
    void cheatAddSuns_shouldIncreaseSunCount() {
        // Arrange
        CheatAddSunsRequest request = new CheatAddSunsRequest("4");

        // Act
        String result = gameService.cheatAddSuns(request);

        // Assert
        assertEquals("added 4 suns", result);
        assertEquals(600, session.getSunCount()); // 500 + (4 * 25)
    }

    @Test
    @DisplayName("✅ Should execute nuke cheat killing all zombies")
    void releaseTheNuke_shouldClearAllZombies() {
        // Arrange
        session.getZombies().add(new com.ussr.pvz.model.entities.zombies.Zombie("Basic", null, false));

        // Act
        String result = gameService.releaseTheNuke();

        // Assert
        assertEquals("nuke released", result);
        assertTrue(session.getZombies().isEmpty());
    }
}