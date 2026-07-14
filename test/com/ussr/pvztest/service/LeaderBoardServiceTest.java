package com.ussr.pvztest.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.AccountState;
import com.ussr.pvz.model.account.Gender;
import com.ussr.pvz.model.dto.LeaderBoardSortRequest;
import com.ussr.pvz.service.LeaderBoardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LeaderBoardServiceTest {

    private LeaderBoardService leaderBoardService;

    @BeforeEach
    void setUp() {
        App.getAccounts().clear();
        leaderBoardService = new LeaderBoardService();

        // Account A: Score 1500, Minigames 5, Chapter 2
        AccountState stateA = createMockState("Alice", 1500, 5, 2);
        // Account B: Score 3000, Minigames 2, Chapter 1
        AccountState stateB = createMockState("Bob", 3000, 2, 1);
        // Account C: Score 500, Minigames 10, Chapter 4
        AccountState stateC = createMockState("Charlie", 500, 10, 4);

        App.addAccount(new Account(stateA, null));
        App.addAccount(new Account(stateB, null));
        App.addAccount(new Account(stateC, null));
    }

    private AccountState createMockState(String name, int score, int minigames, int chapter) {
        return new AccountState(
                name, name, "pass", name + "@ex.com", Gender.FEMALE, 3,
                null, null, chapter, 1, minigames, 0, 0, 0, score,
                new HashMap<>(), new ArrayList<>(), new ArrayList<>(),
                null, null, 0, new HashMap<>(), new ArrayList<>(), new HashMap<>()
        );
    }

    @Test
    @DisplayName("✅ Should correctly sort leaderboard by SCORE in DESCENDING order")
    void sort_shouldOrderDescByScore() {
        // Act
        LeaderBoardSortRequest request = new LeaderBoardSortRequest("score", "desc");
        leaderBoardService.sort(request);

        // Assert
        assertEquals("Bob", App.getAccounts().get(0).getName());     // 3000
        assertEquals("Alice", App.getAccounts().get(1).getName());   // 1500
        assertEquals("Charlie", App.getAccounts().get(2).getName()); // 500
    }

    @Test
    @DisplayName("✅ Should correctly sort leaderboard by MINIGAMES in ASCENDING order")
    void sort_shouldOrderAscByMinigames() {
        // Act
        LeaderBoardSortRequest request = new LeaderBoardSortRequest("minigames", "asc");
        leaderBoardService.sort(request);

        // Assert
        assertEquals("Bob", App.getAccounts().get(0).getName());     // 2
        assertEquals("Alice", App.getAccounts().get(1).getName());   // 5
        assertEquals("Charlie", App.getAccounts().get(2).getName()); // 10
    }

    @Test
    @DisplayName("✅ Should correctly tie-break with alphabetical username when values are identical")
    void sort_shouldTieBreakAlphabetically() {
        // Arrange - Give David same score as Alice
        App.addAccount(new Account(createMockState("David", 1500, 1, 1), null));

        // Act
        LeaderBoardSortRequest request = new LeaderBoardSortRequest("score", "desc");
        leaderBoardService.sort(request);

        // Assert - Bob (3000) -> Alice (1500) -> David (1500) -> Charlie (500)
        assertEquals("Alice", App.getAccounts().get(1).getName());
        assertEquals("David", App.getAccounts().get(2).getName());
    }
}