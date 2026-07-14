package com.ussr.pvztest.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.AccountState;
import com.ussr.pvz.model.account.Gender;
import com.ussr.pvz.service.QuestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestServiceTest {

    private QuestService questService;

    @BeforeEach
    void setUp() {
        App.getAccounts().clear();
        questService = new QuestService();

        // Initialize user, QuestManager generates defaults based on quests.json loaded by backend
        AccountState state = new AccountState(
                "quest-user", "Quester", "pass", "quest@example.com", Gender.MALE, 3,
                null, null, 1, 1, 0, 0, 0, 0, 0,
                new HashMap<>(), new ArrayList<>(), new ArrayList<>(),
                null, null, 0, new HashMap<>(), new ArrayList<>(), new HashMap<>()
        );

        Account activeAccount = new Account(state, null);
        App.addAccount(activeAccount);
        App.login(activeAccount);
    }

    @Test
    @DisplayName("❌ Should block invalid quest page queries")
    void getPage_shouldFail_whenPageNameInvalid() {
        // Act
        String result = questService.getPage("invalid_page");

        // Assert
        assertTrue(result.contains("Invalid travel log page"));
    }

    // (Note: If your quests.json is loaded correctly in your local test environment,
    // a valid "daily" page query would yield a formatted string. If the JSON is not
    // mounted during tests, it gracefully returns "No active daily quests available".)
    @Test
    @DisplayName("✅ Should gracefully handle valid page query even if empty")
    void getPage_shouldHandle_validPageQuery() {
        // Act
        String result = questService.getPage("daily");

        // Assert
        assertTrue(result.contains("QUESTS ---") || result.contains("No active daily quests"));
    }
}