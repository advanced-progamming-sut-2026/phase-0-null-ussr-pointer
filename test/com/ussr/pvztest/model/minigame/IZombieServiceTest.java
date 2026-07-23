package com.ussr.pvztest.model.minigame;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.level.behavior.IZombieBehavior;
import com.ussr.pvz.service.minigame.IZombieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IZombieServiceTest {

    private IZombieService iZombieService;
    private GameSession session;

    @BeforeEach
    void setUp() {
        iZombieService = new IZombieService();
        session = new GameSession();

        Lawn lawn = new Lawn(5, 9);
        session.setLawn(lawn);
        session.setZombies(new ArrayList<>());

        Level level = new Level();
        IZombieBehavior behavior = new IZombieBehavior(4, 150); // Red line at col 4
        level.setBehavior(behavior);
        session.setLevel(level);

        App.setGameSession(session);
        session.addSun(500);
    }

    @Test
    @DisplayName("❌ Should prevent placing the special Sun-Producing Zombie")
    void placeZombie_shouldFail_whenSelectingSunProducer() {
        String result = iZombieService.placeZombie("SunProducerZombie", 5, 2);
        assertEquals("You cannot manually place the Sun-Producing Zombie!", result);
    }

    @Test
    @DisplayName("❌ Should prevent placing a zombie behind the red line (Plant territory)")
    void placeZombie_shouldFail_whenBehindRedLine() {
        String result = iZombieService.placeZombie("ZombieDefault", 1, 1); // col 2 < red line 4
        assertTrue(result.contains("You can only spawn zombies to the right of the red line"));
    }

    @Test
    @DisplayName("✅ Should successfully spawn zombie in valid territory and deduct sun")
    void placeZombie_shouldSucceed_whenValidTerritoryAndSun() {
        String result = iZombieService.placeZombie("ZombieDefault", 6, 2); // ZombieDefault costs 100

        assertEquals("Spawned ZombieDefault at (6, 2).", result);
        assertEquals(400, session.getSunCount());
        assertEquals(1, session.getZombies().size());
    }
}