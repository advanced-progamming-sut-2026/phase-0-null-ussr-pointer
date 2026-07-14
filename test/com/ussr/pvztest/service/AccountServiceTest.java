package com.ussr.pvztest.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.AccountState;
import com.ussr.pvz.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceTest {

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        App.getAccounts().clear();
        App.login(null);
        accountService = new AccountService();
    }

    @Test
    @DisplayName("✅ Should successfully logout active user, trigger state save, and revert menu")
    void logoutAccount_shouldLogout_whenUserLoggedIn() {
        // Arrange
        AccountState state = new AccountState(
                "testuser", "TestUser", "hash", "test@test.com", null, 3, null, null,
                1, 1, 0, 0, 0, 0, 0,
                new java.util.HashMap<>(), new java.util.ArrayList<>(), new java.util.ArrayList<>(),
                null, null, 0, new java.util.HashMap<>(), new java.util.ArrayList<>(), new java.util.HashMap<>()
        );
        Account testAccount = new Account(state, null);
        App.addAccount(testAccount);
        App.login(testAccount);
        App.setMenuState(MenuState.MAIN);

        // Act
        String result = accountService.logoutAccount();

        // Assert
        assertEquals("logged out successfully", result);
        assertNull(App.getAccount());
        assertEquals(MenuState.LOGIN, App.getMenuState());
    }

    @Test
    @DisplayName("❌ Should fail to logout when no user is currently logged in")
    void logoutAccount_shouldFail_whenNoUserLoggedIn() {
        // Act
        String result = accountService.logoutAccount();

        // Assert
        assertEquals("you are not logged in", result);
        assertNull(App.getAccount());
    }
}