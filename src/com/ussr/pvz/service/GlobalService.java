package com.ussr.pvz.service;

import com.badlogic.gdx.Gdx;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.dto.MenuEnterRequest;

import java.util.Arrays;
import java.util.Optional;

public class GlobalService {

    private final LoginService loginService;

    public GlobalService() {

        this.loginService =
                new LoginService();
    }


    public String menuEnter(MenuEnterRequest request) {
        Optional<MenuState> target = Arrays.stream(MenuState.values())
                .filter(state -> state.getName().equalsIgnoreCase(request.menuName())).findFirst();
        if (target.isEmpty()) return "invalid menu name";
        MenuState current = App.getMenuState();
        MenuState to = target.get();
        boolean allowed =
                switch (current) {
                    case REGISTER -> to == MenuState.LOGIN;
                    case LOGIN -> to == MenuState.MAIN && App.getAccount() != null;
                    case MAIN -> to == MenuState.GAME || to == MenuState.SETTING || to == MenuState.NETWORK ||
                            to == MenuState.NEWS ||
                            to == MenuState.PROFILE;
                    case GAME -> to == MenuState.COLLECTION;
                    default -> false;
                };

        if (!allowed) {
            if (current == MenuState.LOGIN && to == MenuState.MAIN) return "you are not logged in";
            return "you can't enter " + to.getName() + " from " + current.getName();
        }
        App.setMenuState(to);
        return "menu changed to: " + to.getName();
    }

    public String menuExit() {

        MenuState current =
                App.getMenuState();

        if (current == MenuState.MAIN) {

            return "please use logout command "
                    + "to exit main menu";
        }

        MenuState previous =
                App.goBackMenuState();

        if (previous == null) {

            return "bye bye";
        }

        return "menu changed to "
                + previous.getName();
    }


    public String handleLogout() {

        if (App.getAccount() == null ||
                !loginService.isLoggedIn()) {

            return "you are not logged in";
        }

        AccountSyncService.sync();

        boolean loggedOut =
                loginService.logout();

        if (!loggedOut) {

            return "Could not log out from server.";
        }
        App.setMenuState(
                MenuState.LOGIN
        );

        return "logged out successfully";
    }


    public void handleQuit() {

        AccountSyncService.sync();

        Gdx.app.exit();

    }
}