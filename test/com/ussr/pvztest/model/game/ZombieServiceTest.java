package com.ussr.pvztest.model.game;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.Row;
import com.ussr.pvz.model.board.terrain.Tile;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;
import com.ussr.pvz.service.game.ZombieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZombieServiceTest {

    private ZombieService zombieService;
    private GameSession session;

    @BeforeEach
    void setUp() {
        zombieService = new ZombieService();
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
    }

    @Test
    @DisplayName("✅ Should detect collision and apply damage when plant is directly in front")
    void processEating_shouldDamagePlantAndReturnTrue_whenColliding() {
        // Arrange
        Plant plant = new Plant();
        plant.setHp(300);
        plant.setAlive(true);
        session.getLawn().getCell(2, 4).setPlant(plant);

        Zombie zombie = new Zombie("Basic", null, false);
        zombie.setPosition(Vec2.of(4.1, 2.0)); // Col 4.1, Row 2 (Hitbox overlaps Col 4)
        zombie.setEatDps(100);

        // Act
        boolean isEating = zombieService.processEating(zombie, session);

        // Assert
        assertTrue(isEating);
        assertTrue(plant.getHp() < 300); // Plant took damage (100 * 0.1 = 10 damage per tick)
    }

    @Test
    @DisplayName("✅ Should bypass eating logic if tile is empty")
    void processEating_shouldReturnFalse_whenTileEmpty() {
        // Arrange
        Zombie zombie = new Zombie("Basic", null, false);
        zombie.setPosition(Vec2.of(4.1, 2.0)); // Empty tile at col 4

        // Act
        boolean isEating = zombieService.processEating(zombie, session);

        // Assert
        assertFalse(isEating); // Path is clear
    }
}