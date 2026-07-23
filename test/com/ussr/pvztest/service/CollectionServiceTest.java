package com.ussr.pvztest.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.AccountState;
import com.ussr.pvz.model.account.Gender;
import com.ussr.pvz.model.dto.PlantTypeRequest;
import com.ussr.pvz.service.CollectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollectionServiceTest {

    private CollectionService collectionService;
    private Account activeAccount;

    @BeforeEach
    void setUp() {
        App.getAccounts().clear();
        App.login(null);
        collectionService = new CollectionService();

        AccountState state = new AccountState(
                "coll-user", "Collector", "pass", "coll@example.com", Gender.MALE, 3,
                null, null, 1, 1, 0, 0, 5000, 50, 0,
                new HashMap<>(), new ArrayList<>(), new ArrayList<>(),
                null, null, 0, new HashMap<>(), new ArrayList<>(), new HashMap<>(),System.currentTimeMillis(),System.currentTimeMillis(),new ArrayList<>()
        );
        activeAccount = new Account(state, null);
        App.addAccount(activeAccount);
        App.login(activeAccount);
    }

    @Test
    @DisplayName("✅ Should successfully purchase a locked plant")
    void purchasePlant_shouldUnlockAndDeductCoins() {
        // Arrange
        PlantTypeRequest request = new PlantTypeRequest("PEASHOOTER"); // Currently locked (lvl 0)

        // Act
        String result = collectionService.purchasePlant(request);

        // Assert
        assertEquals("Success! PEASHOOTER purchased and added to your collection.", result);
        assertEquals(3000, activeAccount.getAdventureProgress().getCoin()); // 5000 - 2000
        assertEquals(1, activeAccount.getAdventureProgress().getPlantLvls().get("PEASHOOTER"));
    }

    @Test
    @DisplayName("❌ Should fail to purchase if not enough coins")
    void purchasePlant_shouldFail_whenInsufficientCoins() {
        // Arrange
        activeAccount.getAdventureProgress().addCoin(-4000); // Drop to 1000 coins
        PlantTypeRequest request = new PlantTypeRequest("PEASHOOTER");

        // Act
        String result = collectionService.purchasePlant(request);

        // Assert
        assertEquals("Error: Not enough coins to purchase. Cost is 2,000 coins.", result);
    }

    @Test
    @DisplayName("❌ Should fail to upgrade a plant if missing seed packets")
    void upgradePlant_shouldFail_whenMissingSeedPackets() {
        // Arrange
        activeAccount.getAdventureProgress().getPlantLvls().put("PEASHOOTER", 1);
        // User has coins, but 0 seed packets
        PlantTypeRequest request = new PlantTypeRequest("PEASHOOTER");

        // Act
        String result = collectionService.upgradePlant(request);

        // Assert
        assertEquals("Error: Not enough seed packets. Need 10", result);
        assertEquals(1, activeAccount.getAdventureProgress().getPlantLvls().get("PEASHOOTER"));
    }

    @Test
    @DisplayName("✅ Should successfully upgrade a plant when requirements met")
    void upgradePlant_shouldSucceed_whenCurrencyAndSeedsAvailable() {
        // Arrange
        activeAccount.getAdventureProgress().getPlantLvls().put("PEASHOOTER", 1);
        activeAccount.getAdventureProgress().getSeedPackets().put("PEASHOOTER", 15);
        PlantTypeRequest request = new PlantTypeRequest("PEASHOOTER");

        // Act
        String result = collectionService.upgradePlant(request);

        // Assert
        assertEquals("Success! PEASHOOTER upgraded to level 2.", result);
        assertEquals(4000, activeAccount.getAdventureProgress().getCoin()); // 5000 - (1 * 1000)
        assertEquals(5, activeAccount.getAdventureProgress().getSeedPackets().get("PEASHOOTER")); // 15 - (1 * 10)
    }
}