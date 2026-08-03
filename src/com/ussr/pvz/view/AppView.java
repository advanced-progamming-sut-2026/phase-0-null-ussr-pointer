package com.ussr.pvz.view;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.ussr.pvz.controller.GlobalController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.util.SessionManager;
import com.ussr.pvz.notification.NotificationCenter;
import com.ussr.pvz.view.hud.GlobalMenuHud;
import com.ussr.pvz.view.loading.LoadingCenter;
import com.ussr.pvz.view.loading.LoadingOverlay;
import com.ussr.pvz.view.mainmenu.*;
import com.ussr.pvz.view.mainmenu.gamemenu.collection.CollectionMenu;
import com.ussr.pvz.view.mainmenu.gamemenu.GameMenu;
import com.ussr.pvz.view.mainmenu.gamemenu.GraphicalLevelSelectionMenu;
import com.ussr.pvz.view.mainmenu.greenhouse.GreenHouseMenu;
import com.ussr.pvz.view.notification.NotificationOverlay;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.Disposable;
import com.ussr.pvz.view.mainmenu.profile.ProfileMenu;
import pvz.skin.PvzSkin;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.ussr.pvz.view.mainmenu.news.NewsMenu;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

public class AppView implements ApplicationListener {
    private final GlobalController globalController = new GlobalController();
    private Stage stage;
    private Skin skin;
    private NotificationOverlay notificationOverlay;
    private LoadingOverlay loadingOverlay;

    private Table screenRoot;
    private GlobalMenuHud globalMenuHud;
    private MenuState displayedMenu;

    private static final float HALF_TRANSITION_DURATION = 0.25f;
    private static final float MINIMUM_LOADING_TIME = 2.5f;
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

        loadingOverlay = new LoadingOverlay(skin);
        stage.addActor(loadingOverlay);

        notificationOverlay = new NotificationOverlay(skin);
        stage.addActor(notificationOverlay);

        showMenu(App.getMenuState());

        Gdx.input.setInputProcessor(stage);
    }

    private void installMissingSkinStyles() {
        if (!skin.has("default", SelectBoxStyle.class)) {
            SelectBoxStyle style = createSelectBoxStyle();
            style.listStyle = createPopupListStyle();
            style.scrollStyle = createPopupScrollStyle();

            skin.add("default", style, SelectBoxStyle.class);
        }

        if (!skin.has("default", CheckBoxStyle.class)) {
            skin.add(
                    "default",
                    createCheckBoxStyle(),
                    CheckBoxStyle.class
            );
        }
    }

    private CheckBoxStyle createCheckBoxStyle() {
        LabelStyle labelStyle =
                skin.get("default", LabelStyle.class);

        CheckBoxStyle style = new CheckBoxStyle();
        style.checkboxOff = skin.getDrawable("checkbox_off");
        style.checkboxOn = skin.getDrawable("checkbox_on");
        style.checkboxOver = style.checkboxOff;
        style.checkboxOnOver = style.checkboxOn;
        style.font = labelStyle.font;
        style.fontColor = Color.WHITE;
        style.overFontColor = Color.WHITE;
        style.downFontColor = Color.WHITE;

        return style;
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

            case NEWS ->
                    screenRoot.add(createNewsScreen()).grow();

            case SETTING ->
                    screenRoot.add(new SettingMenu(skin)).grow();

            case GAME ->
                    screenRoot.add(new GameMenu(skin)).grow();

            case LEVEL_SELECTION ->
                    screenRoot.add(
                            new GraphicalLevelSelectionMenu(skin)
                    ).grow();

            case GREENHOUSE ->
                screenRoot.add(new GreenHouseMenu(skin)).grow();

            case SHOP ->
                screenRoot.add(new ShopMenu(skin)).grow();

            case TRAVEL_LOG ->
                screenRoot.add(new TravelLogMenu(skin)).grow();

            case LEADERBOARD ->
                    screenRoot.add(new LeaderBoardMenu(skin)).grow();

            case COLLECTION ->
                    screenRoot.add(new CollectionMenu(
                            skin
                    )).grow();

            default ->
                    screenRoot.add(
                            new Label(
                                    state.getName(),
                                    skin,
                                    "big_outline"
                            )
                    );
        }

        configureGlobalHud(state);
    }

    private Actor createNewsScreen() {
        Stack screen = new Stack();

        screen.add(new MainMenu(skin));
        screen.add(createNewsDimLayer());

        Table overlay = createNewsOverlay();
        screen.add(overlay);

        return screen;
    }

    private Image createNewsDimLayer() {
        Image dimLayer = new Image(skin.newDrawable(
                "white-pixel",
                new Color(0f, 0f, 0f, 0.48f)
        ));
        dimLayer.setTouchable(Touchable.disabled);
        dimLayer.getColor().a = 0f;
        dimLayer.addAction(fadeIn(0.2f));
        return dimLayer;
    }

    private Table createNewsOverlay() {
        Table overlay = new Table();

        overlay.setTouchable(Touchable.enabled);

        NewsMenu newsMenu = new NewsMenu(
                skin,
                this::closeNews
        );

        overlay.add(newsMenu)
                .width(760f)
                .height(590f);

        return overlay;
    }

    private void closeNews() {
        App.setMenuState(MenuState.MAIN);
    }

    private void transitionTo(MenuState targetMenu) {
        transitioning = true;
        displayedMenu = targetMenu;

        boolean showLoading =
                LoadingCenter.consumeFor(targetMenu);

        screenRoot.clearActions();
        screenRoot.setTouchable(Touchable.disabled);

        screenRoot.addAction(sequence(
                fadeOut(HALF_TRANSITION_DURATION),
                run(() -> completeMenuChange(
                        targetMenu,
                        showLoading
                ))
        ));
    }

    private void completeMenuChange(
            MenuState targetMenu,
            boolean showLoading
    ) {
        if (showLoading) {
            showLoadingAndRebuild(targetMenu);
            return;
        }

        rebuildAndFadeIn(targetMenu);
    }

    private void rebuildAndFadeIn(MenuState targetMenu) {
        rebuildMenu(targetMenu);
        screenRoot.getColor().a = 0f;

        screenRoot.addAction(sequence(
                fadeIn(HALF_TRANSITION_DURATION),
                run(this::finishTransition)
        ));
    }

    private void showLoadingAndRebuild(MenuState targetMenu) {
        loadingOverlay.show();

        loadingOverlay.addAction(sequence(
                delay(MINIMUM_LOADING_TIME),
                run(() -> {
                    rebuildMenu(targetMenu);
                    screenRoot.getColor().a = 0f;
                    loadingOverlay.hide();

                    screenRoot.addAction(sequence(
                            fadeIn(HALF_TRANSITION_DURATION),
                            run(this::finishTransition)
                    ));
                })
        ));
    }

    private void finishTransition() {
        transitioning = false;
        screenRoot.setTouchable(Touchable.childrenOnly);
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
        boolean activeGameplay = state == MenuState.GAME
                && App.getGameSession() != null;

        if (!state.showsGlobalHud() || activeGameplay || account == null) {
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
