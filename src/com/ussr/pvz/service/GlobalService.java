package com.ussr.pvz.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.dto.MenuEnterRequest;

import java.util.Arrays;
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
        MenuState parent = switch (current) {
            case REGISTER -> null;
            case LOGIN -> MenuState.REGISTER;
            case GAME, SETTING, NETWORK, NEWS, PROFILE -> MenuState.MAIN;
            case COLLECTION -> MenuState.GAME;
            default -> null;
        };
        App.setMenuState(parent);
        return parent != null ? "menu changed to " + parent.getName() : "bye bye";
    }
}
