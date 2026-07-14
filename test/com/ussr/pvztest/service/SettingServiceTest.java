package com.ussr.pvztest.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.AccountState;
import com.ussr.pvz.model.account.Gender;
import com.ussr.pvz.model.dto.ChangeDifficultyRequest;
import com.ussr.pvz.service.SettingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SettingServiceTest {

    private SettingService settingService;
    private Account activeAccount;

    @BeforeEach
    void setUp() {
        App.getAccounts().clear();
        App.login(null);
        settingService = new SettingService();

        AccountState state = new AccountState(
                "main-user", "MainNick", "ValidP&ss1", "user@example.com", Gender.MALE, 3, // Default difficulty 3
                null, null, 1, 1, 0, 0, 0, 0, 0,
                new HashMap<>(), new ArrayList<>(), new ArrayList<>(),
                null, null, 0, new HashMap<>(), new ArrayList<>(), new HashMap<>()
        );
        activeAccount = new Account(state, null);
        App.addAccount(activeAccount);
        App.login(activeAccount);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "2", "3", "4", "5"})
    @DisplayName("✅ Should successfully change difficulty when in valid range 1-5")
    void changeDifficulty_shouldSucceed_whenInRange(String validLevel) {
        ChangeDifficultyRequest request = new ChangeDifficultyRequest(validLevel);
        String result = settingService.changeDifficulty(request);

        assertEquals("new difficulty lvl applied successfully.", result);
        assertEquals(Integer.parseInt(validLevel), activeAccount.getDifficultyLvl());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "6", "-1", "100"})
    @DisplayName("❌ Should fail when difficulty integer is out of bounds")
    void changeDifficulty_shouldFail_whenOutOfBounds(String invalidLevel) {
        ChangeDifficultyRequest request = new ChangeDifficultyRequest(invalidLevel);
        String result = settingService.changeDifficulty(request);

        assertEquals("invalid difficulty level", result);
        assertEquals(3, activeAccount.getDifficultyLvl()); // Should remain unchanged
    }

    @ParameterizedTest
    @ValueSource(strings = {"easy", "hard", "III", "3.5", ""})
    @DisplayName("❌ Should fail when difficulty format is not an integer")
    void changeDifficulty_shouldFail_whenNotAnInteger(String invalidFormat) {
        ChangeDifficultyRequest request = new ChangeDifficultyRequest(invalidFormat);
        String result = settingService.changeDifficulty(request);

        assertEquals("invalid difficulty level format", result);
        assertEquals(3, activeAccount.getDifficultyLvl()); // Should remain unchanged
    }
}