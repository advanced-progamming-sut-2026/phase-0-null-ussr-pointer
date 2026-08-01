package com.ussr.pvz.view;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.ussr.pvz.controller.GlobalController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.util.SessionManager;
import com.ussr.pvz.notification.NotificationCenter;
import com.ussr.pvz.view.hud.GlobalMenuHud;
import com.ussr.pvz.view.mainmenu.*;
import com.ussr.pvz.view.notification.NotificationOverlay;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.Disposable;
import pvz.skin.PvzSkin;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

public class AppView implements ApplicationListener {
    private final GlobalController globalController = new GlobalController();
    private Stage stage;
    private Skin skin;
    private NotificationOverlay notificationOverlay;

    private Table screenRoot;
    private GlobalMenuHud globalMenuHud;
    private MenuState displayedMenu;

    private static final float HALF_TRANSITION_DURATION = 0.25f;
    private boolean transitioning;

    public AppView() {
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

    @Override
    public void create() {
        Viewport viewport = new ExtendViewport(1280f, 720f);

        stage = new Stage(viewport);
        skin = PvzSkin.get();
        installMissingSkinStyles();

        screenRoot = new Table();
        screenRoot.setFillParent(true);
        stage.addActor(screenRoot);

        globalMenuHud = new GlobalMenuHud(skin);
        stage.addActor(globalMenuHud);

        notificationOverlay = new NotificationOverlay(skin);
        stage.addActor(notificationOverlay);

        showMenu(App.getMenuState());

        Gdx.input.setInputProcessor(stage);
    }

    private void installMissingSkinStyles() {
        if (skin.has("default", SelectBoxStyle.class)) {
            return;
        }

        SelectBoxStyle style = createSelectBoxStyle();
        style.listStyle = createPopupListStyle();
        style.scrollStyle = createPopupScrollStyle();

        skin.add("default", style, SelectBoxStyle.class);
    }

    private SelectBoxStyle createSelectBoxStyle() {
        LabelStyle labelStyle =
                skin.get("default", LabelStyle.class);

        SelectBoxStyle style = new SelectBoxStyle();
        style.font = labelStyle.font;
        style.fontColor = Color.BLACK;

        style.background = skin.getDrawable(
                "image_ui_mainmenu_name_field_10"
        );

        style.backgroundOpen = skin.getDrawable(
                "image_ui_mainmenu_name_field_hover_10"
        );

        style.backgroundOver = style.backgroundOpen;

        return style;
    }

    private ListStyle createPopupListStyle() {
        ListStyle style = new ListStyle(
                skin.get("default", ListStyle.class)
        );

        style.background = skin.getDrawable(
                "image_ui_dialog_asset_inner_bkgd_10"
        );

        style.fontColorSelected = Color.WHITE;
        style.fontColorUnselected = Color.WHITE;

        return style;
    }

    private ScrollPaneStyle createPopupScrollStyle() {
        ScrollPaneStyle style = new ScrollPaneStyle(
                skin.get("default", ScrollPaneStyle.class)
        );

        style.background = skin.getDrawable(
                "image_ui_dialog_asset_inner_bkgd_10"
        );

        return style;
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

        if (!transitioning
                && App.getMenuState() != displayedMenu) {
            transitionTo(App.getMenuState());
        }
        refreshGlobalHudCurrencies();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    private void showMenu(MenuState state) {
        rebuildMenu(state);
        screenRoot.getColor().a = 1f;
        displayedMenu = state;
    }

    private void rebuildMenu(MenuState state) {
        screenRoot.clearChildren();

        switch (state) {
            case REGISTER ->
                    screenRoot.add(new RegisterMenu(skin)).grow();

            case LOGIN ->
                    screenRoot.add(new LoginMenu(skin)).grow();

            case PROFILE ->
                    screenRoot.add(new ProfileMenu(skin)).grow();

            case MAIN ->
                    screenRoot.add(new MainMenu(skin)).grow();

            default ->
                    screenRoot.add(
                            new Label(state.getName(), skin, "big_outline")
                    );
        }

        configureGlobalHud(state);
    }

    private void transitionTo(MenuState targetMenu) {
        transitioning = true;
        displayedMenu = targetMenu;

        screenRoot.clearActions();

        screenRoot.addAction(sequence(
                fadeOut(HALF_TRANSITION_DURATION),

                run(() -> {
                    rebuildMenu(targetMenu);
                    screenRoot.getColor().a = 0f;
                }),

                fadeIn(HALF_TRANSITION_DURATION),

                run(() -> transitioning = false)
        ));
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

    private void configureGlobalHud(MenuState state) {
        Account account = App.getAccount();

        if (!state.showsGlobalHud() || account == null) {
            globalMenuHud.configure(false, 0, 0, null);
            return;
        }

        int coins = account.getAdventureProgress().getCoin();
        int diamonds = account.getAdventureProgress().getGem();

        boolean mainMenu = state == MenuState.MAIN;

        Runnable navigationAction = mainMenu
                ? globalController::handMenuQuit
                : this::exitCurrentMenu;

        globalMenuHud.setExitMode(mainMenu);

        globalMenuHud.configure(
                true,
                coins,
                diamonds,
                navigationAction
        );
    }

    private void exitCurrentMenu() {
        String result = globalController.exitCurrentMenu();
        if (result != null && !result.isBlank()) {
            NotificationCenter.info(result);
        }
    }

    private void refreshGlobalHudCurrencies() {
        if (!globalMenuHud.isVisible()) {
            return;
        }

        Account account = App.getAccount();
        if (account == null) {
            globalMenuHud.configure(false, 0, 0, null);
            return;
        }

        globalMenuHud.updateCurrencies(
                account.getAdventureProgress().getCoin(),
                account.getAdventureProgress().getGem()
        );
    }
}
