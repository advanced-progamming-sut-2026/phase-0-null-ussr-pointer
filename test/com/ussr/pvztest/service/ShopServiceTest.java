package com.ussr.pvztest.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.AccountState;
import com.ussr.pvz.model.account.Gender;
import com.ussr.pvz.model.dto.ShopBuyRequest;
import com.ussr.pvz.service.ShopService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopServiceTest {

    private ShopService shopService;
    private Account activeAccount;

    @BeforeEach
    void setUp() {
        App.getAccounts().clear();
        App.login(null);
        App.initShop(); // Initializes ShopManager and items
        shopService = new ShopService();

        AccountState state = new AccountState(
                "shop-user", "Shopper", "pass", "shop@example.com", Gender.MALE, 3,
                null, null, 1, 1, 0, 0,
                5000, // 5000 Coins
                50,   // 50 Gems
                0,
                new HashMap<>(), new ArrayList<>(), new ArrayList<>(),
                null, null, 0, new HashMap<>(), new ArrayList<>(), new HashMap<>(),System.currentTimeMillis(),System.currentTimeMillis(),new ArrayList<>()
        );
        activeAccount = new Account(state, null);

        // Give user an unlocked plant so random seed packs don't fail
        activeAccount.getAdventureProgress().getPlantLvls().put("PEASHOOTER", 1);

        App.addAccount(activeAccount);
        App.login(activeAccount);
    }

    @Test
    @DisplayName("✅ Should successfully buy a Pot and deduct 2000 coins")
    void buy_shouldSucceed_forPot() {
        // Arrange
        ShopBuyRequest request = new ShopBuyRequest("1", "1", null); // ID 1 is POT
        int initialUnlocked = activeAccount.getGreenhouse().getUnlockedPots();

        // Act
        String result = shopService.buy(request);

        // Assert
        assertEquals("1 pot(s) unlocked", result);
        assertEquals(3000, activeAccount.getAdventureProgress().getCoin()); // 5000 - 2000
        assertEquals(initialUnlocked + 1, activeAccount.getGreenhouse().getUnlockedPots());
    }

    @Test
    @DisplayName("❌ Should fail to buy a Pot if insufficient coins")
    void buy_shouldFail_whenInsufficientCoins() {
        // Arrange
        activeAccount.getAdventureProgress().addCoin(-4000); // Leaves 1000 coins
        ShopBuyRequest request = new ShopBuyRequest("1", "1", null);

        // Act
        String result = shopService.buy(request);

        // Assert
        assertTrue(result.contains("insufficient coins"));
        assertEquals(1000, activeAccount.getAdventureProgress().getCoin()); // Unchanged
    }

    @Test
    @DisplayName("✅ Should successfully buy Plant Food and deduct gems")
    void buy_shouldSucceed_forPlantFood() {
        // Arrange
        ShopBuyRequest request = new ShopBuyRequest("2", "2", null); // ID 2 is PLANT_FOOD, costs 3 gems each

        // Act
        String result = shopService.buy(request);

        // Assert
        assertEquals("2 plant food added", result);
        assertEquals(44, activeAccount.getAdventureProgress().getGem()); // 50 - (2 * 3)
        assertEquals(2, activeAccount.getAdventureProgress().getPlantFoodCount());
    }

    @Test
    @DisplayName("✅ Should successfully buy specific seed packets")
    void buy_shouldSucceed_forSelectiveSeedPack() {
        // Arrange
        ShopBuyRequest request = new ShopBuyRequest("4", "1", "PEASHOOTER"); // ID 4 is SEED_PACK_SELECTIVE

        // Act
        String result = shopService.buy(request);

        // Assert
        assertEquals("10 seed packets added for PEASHOOTER", result);
        assertEquals(45, activeAccount.getAdventureProgress().getGem()); // 50 - 5
        assertEquals(10, activeAccount.getAdventureProgress().getSeedPackets().get("PEASHOOTER"));
    }

    @Test
    @DisplayName("❌ Should fail to buy specific seed packets if plant is locked")
    void buy_shouldFail_whenPlantIsLocked() {
        // Arrange
        ShopBuyRequest request = new ShopBuyRequest("4", "1", "WINTERMELON"); // Locked plant

        // Act
        String result = shopService.buy(request);

        // Assert
        assertEquals("plant not found: WINTERMELON", result);
    }

    @Test
    @DisplayName("✅ Should successfully convert currency (Gems to Coins)")
    void buy_shouldSucceed_forCurrencyConversion() {
        // Arrange
        ShopBuyRequest request = new ShopBuyRequest("5", "2", null); // 2 conversions (10 gems for 1000 coins)

        // Act
        String result = shopService.buy(request);

        // Assert
        assertEquals("converted to 1000 coins", result);
        assertEquals(40, activeAccount.getAdventureProgress().getGem()); // 50 - 10
        assertEquals(6000, activeAccount.getAdventureProgress().getCoin()); // 5000 + 1000
    }
}