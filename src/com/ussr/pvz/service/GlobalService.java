package com.ussr.pvz.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.AccountState;
import com.ussr.pvz.model.dto.AdvanceTimeRequest;
import com.ussr.pvz.model.dto.MenuEnterRequest;
import com.ussr.pvz.model.util.SessionManager;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class GlobalService {

    public String menuEnter(MenuEnterRequest request) {
        Optional<MenuState> target = Arrays.stream(MenuState.values())
                .filter(s -> s.getName().equalsIgnoreCase(request.menuName()))
                .findFirst();

        if (target.isEmpty()) {
            return "invalid menu name";
        }

        MenuState current = App.getMenuState();
        MenuState to = target.get();

        boolean allowed = switch (current) {
            case REGISTER -> to == MenuState.LOGIN;
            case LOGIN -> to == MenuState.MAIN && App.getAccount() != null;
            case MAIN -> to == MenuState.GAME || to == MenuState.SETTING || to == MenuState.NETWORK ||
                    to == MenuState.NEWS || to == MenuState.PROFILE;
            case GAME -> to == MenuState.COLLECTION;
            default -> false;
        };

        if (!allowed) {
            if (current == MenuState.LOGIN && to == MenuState.MAIN && App.getAccount() == null)
                return "you are not logged in";
            return "you can't enter " + to.getName() + " from " + current.getName();
        }

        App.setMenuState(to);
        return "menu changed to: " + to.getName();
    }

    public String menuShowCurrentMenu() {
        return "current menu: " + App.getMenuState().getName();
    }

    public String menuExit() {
        MenuState current = App.getMenuState();

        if (current == MenuState.MAIN) {
            return "please use logout command to exit main menu";
        }

        MenuState parent = parentOf(current);

        if (parent == null) {
            App.setMenuState(null);
            return "bye bye";
        }

        App.setMenuState(parent);
        return "menu changed to " + parent.getName();
    }

    public String handleLogout() {
        if (App.getAccount() == null) {
            return "you are not logged in";
        }

        String savedUsername = SessionManager.getAutoLoginUsername();
        if (savedUsername != null && savedUsername.equalsIgnoreCase(App.getAccount().getName())) {
            SessionManager.clearSession();
        }

        List<AccountState> updatedStates = App.getAccounts().stream()
                .map(Account::toState)
                .toList();
        SaveService.saveAccounts(updatedStates);

        App.login(null);
        App.setMenuState(MenuState.LOGIN);
        return "logged out successfully";
    }

    private MenuState parentOf(MenuState current) {
        return switch (current) {
            case REGISTER -> null;
            case LOGIN -> MenuState.REGISTER;
            case GAME, SETTING, NETWORK, NEWS, PROFILE -> MenuState.MAIN;
            case COLLECTION, GREENHOUSE, LEADERBOARD, TRAVEL_LOG, CHOOSE_PLANT, LEVEL_SELECTION -> MenuState.GAME;
            case SHOP -> MenuState.GREENHOUSE;
            default -> null;
        };
    }

    public String advanceTime(AdvanceTimeRequest request) {
        int count;
        try {
            count = Integer.parseInt(request.count());
        } catch (NumberFormatException e) {
            return "invalid count";
        }

        if (count <= 0) return "count must be positive";

        if (App.getGameSession() == null) return "no active game session";

        for (int i = 0; i < count; i++) {
            App.getGameSession().tick();
            if (App.getGameSession().isGameOver()) {
                return "advanced " + (i + 1) + " tick(s) — game ended.";
            }
        }

        return "advanced " + count + " tick(s) | elapsed: "
                + String.format("%.1f", App.getGameSession().getElapsedSeconds()) + "s"
                + " | zombies: " + App.getGameSession().getZombies().size();
    }

    public String handleQuit() {
        if (App.getAccount() != null) {
            List<AccountState> updatedStates = App.getAccounts().stream()
                    .map(Account::toState)
                    .toList();
            SaveService.saveAccounts(updatedStates);
        }
        System.exit(0);
        return "";
    }
}