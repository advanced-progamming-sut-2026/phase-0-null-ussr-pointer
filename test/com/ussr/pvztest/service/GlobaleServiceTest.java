package com.ussr.pvztest.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.AccountState;
import com.ussr.pvz.model.dto.MenuEnterRequest;
import com.ussr.pvz.service.GlobalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalServiceTest {

    private GlobalService globalService;

    @BeforeEach
    void setUp() {
        App.getAccounts().clear();
        App.login(null);
        App.setMenuState(MenuState.MAIN);
        globalService = new GlobalService();
    }

    // ====== MENU TRANSITION TESTS ======

    @Test
    @DisplayName("✅ Should allow REGISTER -> LOGIN transition")
    void menuEnter_shouldAllow_RegisterToLogin() {
        App.setMenuState(MenuState.REGISTER);

        MenuEnterRequest request = new MenuEnterRequest("login");
        String result = globalService.menuEnter(request);

        assertEquals("menu changed to: login", result);
        assertEquals(MenuState.LOGIN, App.getMenuState());
    }

    @Test
    @DisplayName("❌ Should block REGISTER -> MAIN transition")
    void menuEnter_shouldBlock_RegisterToMain() {
        App.setMenuState(MenuState.REGISTER);

        MenuEnterRequest request = new MenuEnterRequest("main");
        String result = globalService.menuEnter(request);

        assertEquals("you can't enter main from register", result);
        assertEquals(MenuState.REGISTER, App.getMenuState());
    }

    @Test
    @DisplayName("✅ Should allow LOGIN -> MAIN transition when account is logged in")
    void menuEnter_shouldAllow_LoginToMain_whenLoggedIn() {
        App.setMenuState(MenuState.LOGIN);
        AccountState dummyState = new AccountState("user", "nick", "pass", "e@mail.com", null, 3, null, null, 1, 1, 0, 0, 0, 0, 0, new HashMap<>(), new ArrayList<>(), new ArrayList<>(), null, null, 0, new HashMap<>(), new ArrayList<>(), new HashMap<>());
        App.login(new Account(dummyState, null));

        MenuEnterRequest request = new MenuEnterRequest("main");
        String result = globalService.menuEnter(request);

        assertEquals("menu changed to: main", result);
        assertEquals(MenuState.MAIN, App.getMenuState());
    }

    @Test
    @DisplayName("❌ Should block LOGIN -> MAIN transition when account is null")
    void menuEnter_shouldBlock_LoginToMain_whenNotLoggedIn() {
        App.setMenuState(MenuState.LOGIN);
        App.login(null);

        MenuEnterRequest request = new MenuEnterRequest("main");
        String result = globalService.menuEnter(request);

        assertEquals("you are not logged in", result);
        assertEquals(MenuState.LOGIN, App.getMenuState());
    }

    @Test
    @DisplayName("❌ Should fail when target menu name is invalid")
    void menuEnter_shouldFail_whenMenuNameInvalid() {
        App.setMenuState(MenuState.MAIN);

        MenuEnterRequest request = new MenuEnterRequest("non_existent_menu");
        String result = globalService.menuEnter(request);

        assertEquals("invalid menu name", result);
    }

    // ====== MENU EXIT TESTS ======

    @Test
    @DisplayName("✅ Should properly pop menu stack (GAME -> MAIN)")
    void menuExit_shouldReturnToMain_fromGame() {
        App.setMenuState(MenuState.GAME);

        String result = globalService.menuExit();

        assertEquals("menu changed to main", result);
        assertEquals(MenuState.MAIN, App.getMenuState());
    }

    @Test
    @DisplayName("✅ Should properly pop menu stack (LOGIN -> REGISTER)")
    void menuExit_shouldReturnToRegister_fromLogin() {
        App.setMenuState(MenuState.LOGIN);

        String result = globalService.menuExit();

        assertEquals("menu changed to register", result);
        assertEquals(MenuState.REGISTER, App.getMenuState());
    }

    @Test
    @DisplayName("❌ Should block menu exit from MAIN without logout command")
    void menuExit_shouldFail_whenAtMainMenu() {
        App.setMenuState(MenuState.MAIN);

        String result = globalService.menuExit();

        assertEquals("please use logout command to exit main menu", result);
        assertEquals(MenuState.MAIN, App.getMenuState());
    }
}