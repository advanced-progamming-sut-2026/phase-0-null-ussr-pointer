package com.ussr.pvztest.model.minigame;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.Row;
import com.ussr.pvz.model.board.structures.Vase;
import com.ussr.pvz.model.board.structures.VaseType;
import com.ussr.pvz.model.board.terrain.Tile;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.items.ItemType;
import com.ussr.pvz.model.entities.items.SeedPackDrop;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.level.behavior.VaseBreakerBehavior;
import com.ussr.pvz.model.util.Vec2;
import com.ussr.pvz.service.minigame.VaseBreakerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class VaseBreakerServiceTest {

    private VaseBreakerService vaseBreakerService;
    private GameSession session;

    @BeforeEach
    void setUp() {
        vaseBreakerService = new VaseBreakerService();
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
        session.setItems(new ArrayList<>());
        session.setPlants(new ArrayList<>());

        Level level = new Level();
        level.setBehavior(new VaseBreakerBehavior());
        session.setLevel(level);

        App.setGameSession(session);
    }

    @Test
    @DisplayName("✅ Should instantly smash a vase at valid coordinates")
    void smashVase_shouldDestroyVase_whenPresent() {
        // Arrange
        Vase vase = new Vase();
        vase.setAlive(true);
        vase.setPosition(Vec2.of(5, 2));
        session.getLawn().getCell(2, 5).setStructure(vase);

        // Act
        String result = vaseBreakerService.smashVase(5, 2);

        // Assert
        assertEquals("Vase smashed at (5, 2)!", result);
        assertFalse(vase.isAlive());
    }

    @Test
    @DisplayName("❌ Should fail to smash vase if none exists at coordinates")
    void smashVase_shouldFail_whenNoVasePresent() {
        // Act
        String result = vaseBreakerService.smashVase(1, 1);

        // Assert
        assertEquals("No vase found at location (1, 1).", result);
    }

    @Test
    @DisplayName("✅ Should successfully plant from a dropped seed pack")
    void plantFromSeedPack_shouldSucceed_whenSeedPackExists() {
        // Arrange
        SeedPackDrop pack = new SeedPackDrop(ItemType.SEED_PACK, 40f, 20f, 1); // 1 = Peashooter
        pack.setPosition(Vec2.of(5, 2));
        session.addItem(pack);

        // Act
        String result = vaseBreakerService.plantFromSeedPack(5, 2, 3, 2);

        // Assert
        assertTrue(result.contains("Successfully planted"));
        assertFalse(pack.isAlive()); // Consumed
        assertNotNull(session.getLawn().getCell(2, 3).getPlant());
    }

    @Test
    @DisplayName("❌ Should fail to plant if no seed pack exists at source location")
    void plantFromSeedPack_shouldFail_whenNoSeedPack() {
        // Act
        String result = vaseBreakerService.plantFromSeedPack(0, 0, 3, 2);

        // Assert
        assertEquals("No seed pack found at that location.", result);
    }
}