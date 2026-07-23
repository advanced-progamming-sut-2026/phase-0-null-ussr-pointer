package com.ussr.pvztest.integration;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.AccountState;
import com.ussr.pvz.model.account.Gender;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.Row;
import com.ussr.pvz.model.board.terrain.Tile;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.dto.PlantPlantRequest;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.PlantArmor;
import com.ussr.pvz.model.entities.plants.PlantFactory;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.service.game.GameService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Wide-coverage integration suite exercising the plant lifecycle end to end:
 * factory construction & upgrades, in-session planting via GameService,
 * combat damage resolution (with and without armor), and death/removal.
 * Deliberately favors many small @Test cases over many small classes so
 * related scenarios stay grouped and easy to scan.
 */
class PlantLifecycleIntegrationTest {

    private GameSession session;
    private Account account;

    @BeforeAll
    static void initPlantData() throws FileNotFoundException {
        InputStream stream = new FileInputStream("src/resources/plants.json");
        //PlantFactory.init(stream);
    }

    @BeforeEach
    void setUp() {
        App.getAccounts().clear();
        App.login(null);

        Map<String, Integer> plantLevels = new HashMap<>();
        plantLevels.put("PEASHOOTER", 1);
        plantLevels.put("SUNFLOWER", 1);
        plantLevels.put("WALL-NUT", 1);

        AccountState state = new AccountState(
                "lifecycle-user", "Lifecycle", "pass", "life@example.com", Gender.MALE, 3,
                null, null, 1, 1, 0, 0, 0, 0, 0,
                plantLevels, new ArrayList<>(), new ArrayList<>(),
                null, null, 0, new HashMap<>(), new ArrayList<>(), new HashMap<>(),System.currentTimeMillis(),System.currentTimeMillis(),new ArrayList<>()
        );
        account = new Account(state, null);
        App.addAccount(account);
        App.login(account);

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
        session.addSun(1000);
        App.setGameSession(session);
    }

    // ====== FACTORY CONSTRUCTION ======

    @Test
    @DisplayName("✅ Level 1 plant uses unmodified base stats")
    void factory_levelOne_usesBaseStats() {
        Plant p = PlantFactory.createPlant(6, 1); // Peashooter
        assertEquals(100, p.getCost());
        assertEquals(300, p.getHp());
        assertEquals(20, p.getDamage());
    }

    @Test
    @DisplayName("✅ Level 4 plant has all upgrades cumulatively applied")
    void factory_levelFour_appliesAllUpgrades() {
        Plant p = PlantFactory.createPlant(6, 4); // Peashooter
        assertEquals(75, p.getCost());   // 100 - 25
        assertEquals(450, p.getHp());    // 300 + 150
        assertEquals(30, p.getDamage()); // 20 + 10
    }

    @Test
    @DisplayName("❌ Factory throws for unknown plant id")
    void factory_unknownId_throws() {
        assertThrows(IllegalArgumentException.class, () -> PlantFactory.createPlant(-1, 1));
    }

    @Test
    @DisplayName("✅ createPlantByName resolves regardless of exact casing/spacing in data")
    void factory_createByName_resolvesCorrectPlant() {
        Plant p = PlantFactory.createPlantByName("Peashooter", 2);
        assertEquals("Peashooter", p.getName());
        assertEquals(30, p.getDamage()); // level 2 buff applied
    }

    // ====== PLANTING VIA GAMESERVICE ======

    @Test
    @DisplayName("✅ Planting an unlocked plant succeeds, deducts sun, and occupies the cell")
    void plantPlant_succeeds_deductsSunAndOccupiesCell() {
        GameService gameService = new GameService();
        String result = gameService.plantPlant(new PlantPlantRequest("PEASHOOTER", "2", "3"));

        assertEquals("plant Peashooter placed at (2, 3)", result);
        assertEquals(900, session.getSunCount()); // 1000 - 100
        assertNotNull(session.getLawn().getCell(3, 2).getPlant());
    }

    @Test
    @DisplayName("❌ Planting fails cleanly when sun is insufficient")
    void plantPlant_fails_whenInsufficientSun() {
        session.spendSun(950); // leaves 50, Peashooter costs 100
        GameService gameService = new GameService();

        String result = gameService.plantPlant(new PlantPlantRequest("PEASHOOTER", "2", "3"));

        assertTrue(result.contains("not enough sun"));
        assertNull(session.getLawn().getCell(3, 2).getPlant());
    }

    @Test
    @DisplayName("❌ Planting fails on an already-occupied tile without double-charging sun")
    void plantPlant_fails_onOccupiedTile_noDoubleCharge() {
        GameService gameService = new GameService();
        gameService.plantPlant(new PlantPlantRequest("PEASHOOTER", "2", "3"));
        int sunAfterFirst = session.getSunCount();

        String result = gameService.plantPlant(new PlantPlantRequest("SUNFLOWER", "2", "3"));

        assertTrue(result.contains("already at"));
        assertEquals(sunAfterFirst, session.getSunCount()); // no second charge
    }

    @Test
    @DisplayName("❌ Planting an unowned plant fails with a helpful message")
    void plantPlant_fails_whenPlantNotUnlocked() {
        GameService gameService = new GameService();

        String result = gameService.plantPlant(new PlantPlantRequest("WINTERMELON", "2", "3"));

        assertTrue(result.contains("haven't unlocked"));
    }

    @Test
    @DisplayName("❌ Planting rejects out-of-bounds coordinates")
    void plantPlant_fails_onOutOfBoundsCoordinates() {
        GameService gameService = new GameService();

        String result = gameService.plantPlant(new PlantPlantRequest("PEASHOOTER", "50", "50"));

        assertEquals("invalid location", result);
    }

    // ====== COMBAT DAMAGE RESOLUTION ======

    @Test
    @DisplayName("✅ Plant without armor takes damage directly on HP")
    void takeDamage_noArmor_reducesHpDirectly() {
        Plant plant = new Plant();
        plant.setHp(300);
        plant.setAlive(true);

        plant.takeDamage(100, (Zombie) null);

        assertEquals(200, plant.getHp());
        assertTrue(plant.isAlive());
    }

    @Test
    @DisplayName("✅ Plant with armor absorbs damage before HP is touched")
    void takeDamage_withArmor_absorbsBeforeHp() {
        Plant plant = new Plant();
        plant.setHp(300);
        plant.setAlive(true);
        plant.setArmor(new PlantArmor(150, 0, false));

        plant.takeDamage(100, (Zombie) null);

        assertEquals(300, plant.getHp()); // untouched, absorbed by armor
        assertNotNull(plant.getArmor());
    }

    @Test
    @DisplayName("✅ Overflow damage passes through to HP once armor is depleted")
    void takeDamage_armorOverflow_passesRemainderToHp() {
        Plant plant = new Plant();
        plant.setHp(300);
        plant.setAlive(true);
        plant.setArmor(new PlantArmor(50, 0, false));

        plant.takeDamage(120, (Zombie) null); // 50 absorbed, 70 overflow

        assertEquals(230, plant.getHp()); // 300 - 70
    }

    @Test
    @DisplayName("✅ Plant dies exactly when HP reaches zero, not before")
    void takeDamage_killsPlant_atExactlyZeroHp() {
        Plant plant = PlantFactory.createPlantByName("peashooter",1);
        plant.setHp(50);
        plant.setAlive(true);

        plant.takeDamage(49, (Zombie) null);
        assertTrue(plant.isAlive());

        plant.takeDamage(1, (Zombie) null);
        assertFalse(plant.isAlive());
    }

    @Test
    @DisplayName("✅ Overkill damage does not go negative or resurrect the plant")
    void takeDamage_overkill_doesNotGoNegative() {
        Plant plant = PlantFactory.createPlantByName("peashooter",1);
        plant.setHp(50);
        plant.setAlive(true);

        plant.takeDamage(999, ZombieFactory.create("ZombieDefault",1,1));

        assertEquals(0, plant.getHp());
        assertFalse(plant.isAlive());
    }

    @Test
    @DisplayName("❌ Dead plant does not take further damage or change state")
    void takeDamage_onAlreadyDeadPlant_isNoOp() {
        Plant plant = new Plant();
        plant.setHp(0);
        plant.setAlive(false);

        plant.takeDamage(50, (Zombie) null);

        assertEquals(0, plant.getHp());
        assertFalse(plant.isAlive());
    }

    // ====== PLUCKING / REMOVAL ======

    @Test
    @DisplayName("✅ Plucking an existing plant removes it from both session list and lawn cell")
    void removePlantAt_succeeds_forExistingPlant() {
        GameService gameService = new GameService();
        gameService.plantPlant(new PlantPlantRequest("PEASHOOTER", "2", "3"));

        boolean removed = session.removePlantAt(2, 3);

        assertTrue(removed);
        assertNull(session.getLawn().getCell(3, 2).getPlant());
    }

    @Test
    @DisplayName("❌ Plucking an empty tile returns false without side effects")
    void removePlantAt_fails_whenTileEmpty() {
        boolean removed = session.removePlantAt(0, 0);
        assertFalse(removed);
    }
}