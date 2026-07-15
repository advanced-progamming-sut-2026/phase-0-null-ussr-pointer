package com.ussr.pvztest.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.AccountState;
import com.ussr.pvz.model.account.Gender;
import com.ussr.pvz.model.dto.PlantTypeRequest;
import com.ussr.pvz.service.ChoosePlantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChoosePlantServiceTest {

    private ChoosePlantService choosePlantService;
    private Account activeAccount;

    @BeforeEach
    void setUp() {
        App.getAccounts().clear();
        App.login(null);
        choosePlantService = new ChoosePlantService();

        // Create player with some unlocked plants
        Map<String, Integer> plantLevels = new HashMap<>();
        plantLevels.put("PEASHOOTER", 1);
        plantLevels.put("SUNFLOWER", 1);
        plantLevels.put("WALL-NUT", 1);

        AccountState state = new AccountState(
                "plant-chooser", "Chooser", "pass", "chooser@example.com", Gender.FEMALE, 3,
                null, null, 1, 1, 0, 0, 0, 0, 0,
                plantLevels, new ArrayList<>(), new ArrayList<>(),
                null, null, 0, new HashMap<>(), new ArrayList<>(), new HashMap<>()
        );
        activeAccount = new Account(state, null);
        App.addAccount(activeAccount);
        App.login(activeAccount);
    }

    @Test
    @DisplayName("✅ Should successfully add an unlocked plant to the selection array")
    void addPlant_shouldSucceed_whenPlantIsUnlocked() {
        // Arrange
        PlantTypeRequest request = new PlantTypeRequest("PEASHOOTER");

        // Act
        String result = choosePlantService.addPlant(request);

        // Assert
        assertEquals("PEASHOOTER added (1/8)", result);
    }

    @Test
    @DisplayName("❌ Should fail to add a plant the player has not unlocked")
    void addPlant_shouldFail_whenPlantIsLocked() {
        // Arrange
        PlantTypeRequest request = new PlantTypeRequest("WINTERMELON");

        // Act
        String result = choosePlantService.addPlant(request);

        // Assert
        assertEquals("you don't have WINTER MELON unlocked.", result);
    }

    @Test
    @DisplayName("❌ Should fail to add a duplicate plant")
    void addPlant_shouldFail_whenPlantAlreadySelected() {
        // Arrange
        choosePlantService.addPlant(new PlantTypeRequest("SUNFLOWER"));
        PlantTypeRequest duplicateRequest = new PlantTypeRequest("SUNFLOWER");

        // Act
        String result = choosePlantService.addPlant(duplicateRequest);

        // Assert
        assertEquals("SUNFLOWER is already selected", result);
    }

    @Test
    @DisplayName("✅ Should successfully remove a selected plant")
    void removePlant_shouldSucceed_whenPlantInSelection() {
        // Arrange
        choosePlantService.addPlant(new PlantTypeRequest("WALLNUT"));
        PlantTypeRequest removeRequest = new PlantTypeRequest("WALLNUT");

        // Act
        String result = choosePlantService.removePlant(removeRequest);

        assertEquals("WALL-NUT removed (0/8)", result);
    }

    @Test
    @DisplayName("❌ Should fail to remove a plant that is not in the selection")
    void removePlant_shouldFail_whenPlantNotInSelection() {
        // Arrange
        PlantTypeRequest removeRequest = new PlantTypeRequest("PEASHOOTER");

        // Act
        String result = choosePlantService.removePlant(removeRequest);

        // Assert
        assertEquals("PEASHOOTER is not in your selection", result);
    }

    @Test
    @DisplayName("❌ Should fail to boost a plant without seed packets")
    void boostPlant_shouldFail_whenNoSeedPackets() {
        // Arrange
        choosePlantService.addPlant(new PlantTypeRequest("PEASHOOTER"));
        PlantTypeRequest boostRequest = new PlantTypeRequest("PEASHOOTER");

        // Act
        String result = choosePlantService.boostPlant(boostRequest);

        // Assert
        assertEquals("no seed packets available for PEASHOOTER", result);
    }
}