package com.ussr.pvz.service;

import com.badlogic.gdx.Gdx;
import com.ussr.pvz.controller.command.GlobalCommand;
import com.ussr.pvz.controller.command.LoginCommand;
import com.ussr.pvz.controller.command.RegisterCommand;
import com.ussr.pvz.controller.command.maincommand.*;
import com.ussr.pvz.controller.command.maincommand.gamecommand.ChoosePlantCommand;
import com.ussr.pvz.controller.command.maincommand.gamecommand.CollectionCommand;
import com.ussr.pvz.controller.command.maincommand.gamecommand.GameCommand;
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


    public String menuEnter(
            MenuEnterRequest request
    ) {

        Optional<MenuState> target =
                Arrays.stream(
                                MenuState.values()
                        )
                        .filter(state ->
                                state.getName()
                                        .equalsIgnoreCase(
                                                request.menuName()
                                        )
                        )
                        .findFirst();

        if (target.isEmpty()) {

            return "invalid menu name";
        }

        MenuState current =
                App.getMenuState();

        MenuState to =
                target.get();

        boolean allowed =
                switch (current) {

                    case REGISTER ->
                            to == MenuState.LOGIN;

                    case LOGIN ->
                            to == MenuState.MAIN &&
                                    App.getAccount() != null;

                    case MAIN ->
                            to == MenuState.GAME ||
                                    to == MenuState.SETTING ||
                                    to == MenuState.NETWORK ||
                                    to == MenuState.NEWS ||
                                    to == MenuState.PROFILE;

                    case GAME ->
                            to == MenuState.COLLECTION;

                    default ->
                            false;
                };

        if (!allowed) {

            if (current == MenuState.LOGIN &&
                    to == MenuState.MAIN &&
                    App.getAccount() == null) {

                return "you are not logged in";
            }

            return "you can't enter "
                    + to.getName()
                    + " from "
                    + current.getName();
        }

        App.setMenuState(to);

        return "menu changed to: "
                + to.getName();
    }


    public String menuShowCurrentMenu() {

        return "current menu: "
                + App.getMenuState()
                .getName();
    }


    public String menuExit() {

        MenuState current =
                App.getMenuState();

        if (current == MenuState.MAIN) {

            return "please use logout command "
                    + "to exit main menu";
        }

        MenuState parent =
                current.getParent();

        if (parent == null) {

            App.setMenuState(null);

            return "bye bye";
        }

        if (current == MenuState.GAME &&
                App.getGameSession() != null) {

            App.setGameSession(null);
        }

        App.setMenuState(parent);

        return "menu changed to "
                + parent.getName();
    }


    public String handleLogout() {

        if (App.getAccount() == null ||
                !loginService.isLoggedIn()) {

            return "you are not logged in";
        }

        boolean loggedOut =
                loginService.logout();

        if (!loggedOut) {

            return "Could not log out from server.";
        }

        /*
         * LoginService.logout() already:
         *
         * 1. sends LOGOUT + token to server
         * 2. clears SessionManager
         * 3. clears App.account
         */

        App.setMenuState(
                MenuState.LOGIN
        );

        return "logged out successfully";
    }


    public String handleQuit() {

        Gdx.app.exit();

        return "";
    }


    public String showHelp() {

        MenuState current =
                App.getMenuState();

        StringBuilder sb =
                new StringBuilder();

        sb.append(
                "--- Global Commands ---\n"
        );

        for (GlobalCommand cmd :
                GlobalCommand.values()) {

            sb.append("- ")
                    .append(
                            cmd.name()
                                    .replace(
                                            '_',
                                            ' '
                                    )
                                    .toLowerCase()
                    )
                    .append("\n");
        }

        sb.append("\n--- ")
                .append(
                        current.getName()
                )
                .append(
                        " Commands ---\n"
                );

        Enum<?>[] specificCommands =
                switch (current) {

                    case REGISTER ->
                            RegisterCommand.values();

                    case LOGIN ->
                            LoginCommand.values();

                    case GAME ->
                            GameCommand.values();

                    case COLLECTION ->
                            CollectionCommand.values();

                    case CHOOSE_PLANT ->
                            ChoosePlantCommand.values();

                    case LEADERBOARD ->
                            LeaderBoardCommand.values();

                    case PROFILE ->
                            ProfileCommand.values();

                    case SETTING ->
                            SettingCommand.values();

                    case GREENHOUSE ->
                            GreenHouseCommand.values();

                    case SHOP ->
                            ShopCommand.values();

                    case LEVEL_SELECTION ->
                            LevelSelectionCommand.values();

                    case TRAVEL_LOG ->
                            TravelLogCommand.values();

                    default ->
                            new Enum<?>[0];
                };

        for (Enum<?> cmd :
                specificCommands) {

            sb.append("- ")
                    .append(
                            cmd.name()
                                    .replace(
                                            '_',
                                            ' '
                                    )
                                    .toLowerCase()
                    )
                    .append("\n");
        }

        return sb.toString()
                .trim();
    }


    public String handleMenuShowAll() {

        StringBuilder result =
                new StringBuilder();

        Arrays.stream(
                        MenuState.values()
                )
                .forEach(state -> {

                    result.append(
                                    state.getName()
                            )
                            .append("\n");
                });

        return result.toString();
    }
}