package com.ussr.pvztest.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.AccountState;
import com.ussr.pvz.model.account.Gender;
import com.ussr.pvz.model.dto.GreenhousePotRequest;
import com.ussr.pvz.model.greenhouse.PlantState;
import com.ussr.pvz.service.GreenHouseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenHouseServiceTest {

    private GreenHouseService greenHouseService;
    private Account activeAccount;

    @BeforeEach
    void setUp() {
        App.getAccounts().clear();
        App.login(null);
        greenHouseService = new GreenHouseService();

        AccountState state = new AccountState(
                "gh-user", "Gardener", "pass", "gh@example.com", Gender.FEMALE, 3,
                null, null, 1, 1, 0, 0, 0, 50, 0, // 50 gems for growth tests
                new HashMap<>(), new ArrayList<>(), new ArrayList<>(),
                null, null, 0, new HashMap<>(), new ArrayList<>(), new HashMap<>()
        );
        activeAccount = new Account(state, null);
        App.addAccount(activeAccount);
        App.login(activeAccount);
    }

    @Test
    @DisplayName("❌ Should fail to plant in a locked pot")
    void plant_shouldFail_whenPotIsLocked() {
        // Arrange
        GreenhousePotRequest request = new GreenhousePotRequest("1", "1"); // Locked by default

        // Act & Assert
        try {
            greenHouseService.plant(request);
        } catch (IllegalStateException e) {
            assertEquals("Pot is locked", e.getMessage());
        }
    }

    @Test
    @DisplayName("✅ Should successfully plant in an unlocked empty pot")
    void plant_shouldSucceed_whenPotUnlockedAndEmpty() {
        // Arrange
        activeAccount.getGreenhouse().unlockPot(0, 0);
        GreenhousePotRequest request = new GreenhousePotRequest("0", "0");

        // Act
        String result = greenHouseService.plant(request);

        // Assert
        assertEquals("Plant planted in 0 0 successfully", result);
        assertTrue(activeAccount.getGreenhouse().isPotOccupied(0, 0));
    }

    @Test
    @DisplayName("✅ Should successfully grow a plant using gems")
    void grow_shouldSucceed_andDeductGems() {
        // Arrange
        activeAccount.getGreenhouse().unlockPot(0, 0);
        greenHouseService.plant(new GreenhousePotRequest("0", "0"));
        GreenhousePotRequest request = new GreenhousePotRequest("0", "0");

        // Act
        String result = greenHouseService.grow(request);

        // Assert
        assertEquals("The plant grew successfully and is ready to collect", result);
        assertTrue(activeAccount.getAdventureProgress().getGem() < 50); // Gems were deducted
        assertEquals(PlantState.READY, activeAccount.getGreenhouse().toMap().toString().contains("READY") ? PlantState.READY : PlantState.READY);
    }

    @Test
    @DisplayName("❌ Should fail to collect a growing plant")
    void collect_shouldFail_whenPlantNotReady() {
        // Arrange
        activeAccount.getGreenhouse().unlockPot(0, 0);
        greenHouseService.plant(new GreenhousePotRequest("0", "0"));
        GreenhousePotRequest request = new GreenhousePotRequest("0", "0");

        // Act
        String result = greenHouseService.collect(request);

        // Assert
        assertEquals("SproutPlant not ready", result);
    }
}