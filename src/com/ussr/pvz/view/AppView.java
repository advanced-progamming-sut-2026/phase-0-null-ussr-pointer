package com.ussr.pvz.view;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.util.SessionManager;
import com.ussr.pvz.notification.NotificationCenter;
import com.ussr.pvz.notification.NotificationType;
import com.ussr.pvz.view.mainmenu.*;
import com.ussr.pvz.view.mainmenu.gamemenu.ChoosePlantMenu;
import com.ussr.pvz.view.mainmenu.gamemenu.CollectionMenu;
import com.ussr.pvz.view.mainmenu.gamemenu.GameMenu;
import com.ussr.pvz.view.notification.NotificationOverlay;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.Disposable;
import pvz.skin.PvzSkin;

import java.util.EnumMap;
import java.util.Map;
import java.util.Scanner;

public class AppView implements ApplicationListener {
    private AppMenu currentMenu;
    private final Map<MenuState, AppMenu> menus = new EnumMap<>(MenuState.class);

    private Stage stage;
    private Skin skin;
    private NotificationOverlay notificationOverlay;

    public AppView() {
        menus.put(MenuState.MAIN, new MainMenu());
        menus.put(MenuState.REGISTER, new RegisterMenu());
        menus.put(MenuState.LOGIN, new LoginMenu());
        menus.put(MenuState.GAME, new GameMenu());
        menus.put(MenuState.NEWS, new NewsMenu());
        menus.put(MenuState.NETWORK, new NetworkMenu());
        menus.put(MenuState.PROFILE, new ProfileMenu());
        menus.put(MenuState.SETTING, new SettingMenu());
        menus.put(MenuState.COLLECTION, new CollectionMenu());
        menus.put(MenuState.GREENHOUSE, new GreenHouseMenu());
        menus.put(MenuState.TRAVEL_LOG, new TravelLogMenu());
        menus.put(MenuState.LEADERBOARD, new LeaderBoardMenu());
        menus.put(MenuState.CHOOSE_PLANT, new ChoosePlantMenu());
        menus.put(MenuState.SHOP, new ShopMenu());
        menus.put(MenuState.LEVEL_SELECTION, new LevelSelectionMenu());

        App.initShop();
        App.getLevelManager().loadFromJson();

        tryAutoLogin();
    }

    private void tryAutoLogin() {
        String savedUsername = SessionManager.getAutoLoginUsername();
        if (savedUsername == null) {
            return;
        }

        Account autoLoginAccount = App.getAccounts().stream()
                .filter(a -> a.getName().equalsIgnoreCase(savedUsername))
                .findFirst()
                .orElse(null);

        if (autoLoginAccount != null) {
            // Update login time and check for daily resets
            autoLoginAccount.updateLoginTime();

            App.login(autoLoginAccount);

            App.setMenuState(MenuState.MAIN);
            System.out.println("[Session] Welcome back, " + autoLoginAccount.getName() + "! Auto-login successful.");
        } else {
            SessionManager.clearSession();
        }
    }

    public void run(Scanner scanner) {
        while (true) {
            setCurrentMenu(App.getMenuState());
            if (currentMenu != null) {
                currentMenu.run(scanner);
            } else {
                break;
            }
        }
    }

    public void setCurrentMenu(MenuState menuState) {
        currentMenu = menuState != null ? menus.get(menuState) : null;
    }

    @Override
    public void create() {
        Viewport viewport = new ExtendViewport(1280f, 720f);

        stage = new Stage(viewport);
        skin = PvzSkin.get();

        notificationOverlay = new NotificationOverlay(skin);
        stage.addActor(notificationOverlay);

        Gdx.input.setInputProcessor(stage);

        NotificationCenter.publish(
                "This is an information notification.",
                NotificationType.INFO,
                5f
        );

        NotificationCenter.publish(
                "This is a success notification.",
                NotificationType.SUCCESS,
                5f
        );

        NotificationCenter.publish(
                "This is a warning notification.",
                NotificationType.WARNING,
                7f
        );

        NotificationCenter.publish(
                "This is an error notification.",
                NotificationType.ERROR,
                9f
        );
    }


    @Override
    public void resize(int width, int height) {
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }

    @Override
    public void render() {
        float delta = Math.min(
                Gdx.graphics.getDeltaTime(),
                1f / 30f
        );

        Gdx.gl.glClearColor(0.08f, 0.1f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        NotificationCenter.clear();

        if (stage != null) {
            stage.dispose();
        }

        if (skin instanceof Disposable disposable) {
            disposable.dispose();
        }
    }
}