package com.ussr.pvz.view;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.ussr.pvz.controller.GlobalController;
import com.ussr.pvz.audio.AudioManager;
import com.ussr.pvz.audio.AudioSettings;
import com.ussr.pvz.audio.GdxAudioManager;
import com.ussr.pvz.audio.GameplayMusicCue;
import com.ussr.pvz.audio.GameplayMusicResolver;
import com.ussr.pvz.audio.MenuMusicResolver;
import com.ussr.pvz.audio.MusicTrack;
import com.ussr.pvz.controller.LoginController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.util.SessionManager;
import com.ussr.pvz.network.NetworkClient;
import com.ussr.pvz.notification.NotificationCenter;
import com.ussr.pvz.shared.dto.LoginResult;
import com.ussr.pvz.shared.dto.enums.LoginStatus;
import com.ussr.pvz.view.hud.DebugOverlay;
import com.ussr.pvz.view.hud.GlobalInviteOverlay;
import com.ussr.pvz.view.hud.GlobalMenuHud;
import com.ussr.pvz.view.hud.MenuDebugHud;
import com.ussr.pvz.view.loading.LoadingOverlay;
import com.ussr.pvz.view.mainmenu.*;
import com.ussr.pvz.view.mainmenu.gamemenu.chooseplant.ChoosePlantMenu;
import com.ussr.pvz.view.mainmenu.gamemenu.collection.CollectionMenu;
import com.ussr.pvz.view.mainmenu.gamemenu.GameMenu;
import com.ussr.pvz.view.mainmenu.gamemenu.GraphicalLevelSelectionMenu;
import com.ussr.pvz.view.mainmenu.greenhouse.GreenHouseMenu;
import com.ussr.pvz.view.notification.NotificationOverlay;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.Disposable;
import com.ussr.pvz.view.mainmenu.profile.ProfileMenu;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;
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
import com.badlogic.gdx.utils.TimeUtils;

import java.io.IOException;

import static com.badlogic.gdx.Gdx.files;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

public class AppView implements ApplicationListener {
    private static final float MENU_WORLD_WIDTH = 1280f;
    private static final float MENU_WORLD_HEIGHT = 720f;
    private static final float GAME_WORLD_WIDTH = 1920f;
    private static final float GAME_WORLD_HEIGHT = 1080f;
    private final GlobalController globalController = new GlobalController();
    private GlobalInviteOverlay globalInviteOverlay;
    private Stage stage;
    private Skin skin;
    private NotificationOverlay notificationOverlay;
    private LoadingOverlay loadingOverlay;
    private AudioManager audioManager;
    private MenuDebugHud menuDebugHud;
    private Table screenRoot;
    private GlobalMenuHud globalMenuHud;
    private MenuState displayedMenu;
    private boolean displayedActiveGameplay;

    private static final float HALF_TRANSITION_DURATION = 0.25f;
    private static final long LOADING_FADE_IN_TIME_MS = 180L;
    private static final long MINIMUM_LOADING_TIME_MS = 1_500L;

    private boolean transitioning;
    private boolean menuLoaded;
    private MenuState loadingTarget;
    private long loadingStartedAt;

    private boolean menuLoadPending;
    private int loadingFramesDrawn;

    public AppView() {
        App.initShop();
        App.getLevelManager().loadFromJson();
    }

    @Override
    public void create() {
        try {
            NetworkClient.getInstance()
                    .connect(
                            "localhost",
                            8080
                    );

            if (SessionManager.isLoggedIn()) {
                LoginResult restore = new LoginController().restoreSession();
                if (restore.status() == LoginStatus.LOGIN_SUCCESS) {
                    App.setMenuState(MenuState.MAIN);
                }
            }
        } catch (IOException e) {
            System.err.println(
                    "Could not connect to server: "
                            + e.getMessage()
            );
        }
        // The lawn background, entities, hitboxes and mouse input all share
        // this fixed logical canvas. Different monitor sizes/aspect ratios only
        // scale the complete canvas; they never change its world dimensions.
        Viewport viewport = new FitViewport(MENU_WORLD_WIDTH, MENU_WORLD_HEIGHT);

        stage = new Stage(viewport);
        skin = PvzSkin.get();
        configureFontRendering();
        audioManager = new GdxAudioManager(new AudioSettings());
        installMissingSkinStyles();

        screenRoot = new Table();
        screenRoot.setFillParent(true);
        stage.addActor(screenRoot);

        globalMenuHud = new GlobalMenuHud(skin);
        stage.addActor(globalMenuHud);

        loadingOverlay = new LoadingOverlay(skin);
        stage.addActor(loadingOverlay);
        globalInviteOverlay = new GlobalInviteOverlay(skin);
        stage.addActor(globalInviteOverlay);

        notificationOverlay = new NotificationOverlay(skin);
        stage.addActor(notificationOverlay);
        menuDebugHud = new MenuDebugHud(skin);
        stage.addActor(menuDebugHud);
        showMenu(App.getMenuState());

        Gdx.input.setInputProcessor(stage);
    }

    /**
     * PvzSkin uses bitmap fonts. The stage is designed at 1280x720 and is
     * enlarged on higher-resolution displays, so nearest filtering exposes
     * the individual font texels. Linear filtering keeps scaled glyph edges
     * smooth, while non-integer placement avoids fullscreen rounding jitter.
     */
    private void configureFontRendering() {
        for (BitmapFont font
                : skin.getAll(BitmapFont.class).values()) {
            font.setUseIntegerPositions(false);
            font.getRegions().forEach(region ->
                    region.getTexture().setFilter(
                            Texture.TextureFilter.Linear,
                            Texture.TextureFilter.Linear
                    )
            );
        }
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

        startPendingMenuLoad();
        updateLoadingTransition();

        Gdx.gl.glClearColor(0.08f, 0.1f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        boolean gameplayModeChanged =
                App.getMenuState() == MenuState.GAME
                        && displayedMenu == MenuState.GAME
                        && displayedActiveGameplay
                        != (App.getGameSession() != null);

        if (!transitioning
                && (App.getMenuState() != displayedMenu
                || gameplayModeChanged)) {
            transitionTo(App.getMenuState());
        }
        refreshGlobalHudCurrencies();

        stage.act(delta);
        audioManager.update(delta);
        stage.setDebugAll(DebugOverlay.isHitboxEnabled());
        stage.draw();

        countRenderedLoadingFrame();
    }

    private void startPendingMenuLoad() {
        if (!menuLoadPending
                || loadingFramesDrawn < 1
                || TimeUtils.timeSinceMillis(loadingStartedAt)
                < LOADING_FADE_IN_TIME_MS) {
            return;
        }

        MenuState targetMenu = loadingTarget;
        menuLoadPending = false;
        loadTargetMenu(targetMenu);
    }

    private void countRenderedLoadingFrame() {
        if (menuLoadPending && loadingOverlay.isVisible()) {
            loadingFramesDrawn++;
        }
    }

    private void updateLoadingTransition() {
        if (loadingTarget == null || !menuLoaded) {
            return;
        }

        long elapsed = TimeUtils.timeSinceMillis(loadingStartedAt);

        if (elapsed >= MINIMUM_LOADING_TIME_MS) {
            finishLoadingTransition();
        }
    }

    @Override
    public void pause() {
        if (audioManager != null) {
            audioManager.pause();
        }
    }

    @Override
    public void resume() {
        if (audioManager != null) {
            audioManager.resume();
        }
    }

    private void showMenu(MenuState state) {
        rebuildMenu(state);
        screenRoot.getColor().a = 1f;
        displayedMenu = state;
    }

    private void rebuildMenu(MenuState state) {
        disposeCurrentScreen();
        screenRoot.clearChildren();

        configureViewportFor(state);

        switch (state) {
            case REGISTER -> screenRoot.add(new RegisterMenu(skin)).grow();

            case LOGIN -> screenRoot.add(new LoginMenu(skin)).grow();

            case PROFILE -> screenRoot.add(new ProfileMenu(skin)).grow();

            case MAIN -> screenRoot.add(new MainMenu(skin)).grow();

            case NEWS -> screenRoot.add(createNewsScreen()).grow();

            case SETTING -> screenRoot.add(new SettingMenu(skin)).grow();

            case GAME -> {
                if (App.getGameSession() != null) {
                    FileHandle assetsFolder =
                            files.local("pvz-assets");
                    TextureBank gameTextures =
                            new TextureBank("ATLASES", assetsFolder);
                    PamPlayer gamePamPlayer =
                            new PamPlayer(gameTextures, assetsFolder);

                    screenRoot.add(new com.ussr.pvz.view.gameplay.ActiveGameplayView(
                            skin,
                            gameTextures,
                            gamePamPlayer,
                            audioManager
                    )).grow();
                } else {
                    screenRoot.add(new GameMenu(skin)).grow();
                }
            }

            case LEVEL_SELECTION -> screenRoot.add(
                    new GraphicalLevelSelectionMenu(skin)
            ).grow();

            case GREENHOUSE -> screenRoot.add(new GreenHouseMenu(skin)).grow();

            case SHOP -> screenRoot.add(new ShopMenu(skin)).grow();

            case TRAVEL_LOG -> screenRoot.add(new TravelLogMenu(skin)).grow();

            case LEADERBOARD -> screenRoot.add(new LeaderBoardMenu(skin)).grow();

            case COLLECTION -> screenRoot.add(new CollectionMenu(
                    skin
            )).grow();
            case CHOOSE_PLANT -> screenRoot.add(new ChoosePlantMenu(skin)).grow();
            case LOBBY -> screenRoot.add(
                    new com.ussr.pvz.view.mainmenu.lobby.LobbyMenu(
                            skin,
                            globalInviteOverlay   // pass the overlay reference
                    )
            ).grow();
            default -> screenRoot.add(
                    new Label(
                            state.getName(),
                            skin,
                            "big_outline"
                    )
            );
        }

        configureGlobalHud(state);
        updateMenuMusic(state);
        displayedActiveGameplay = state == MenuState.GAME
                && App.getGameSession() != null;
    }

    private void configureViewportFor(MenuState state) {
        boolean activeGameplay = state == MenuState.GAME
                && App.getGameSession() != null;
        float worldWidth = activeGameplay ? GAME_WORLD_WIDTH : MENU_WORLD_WIDTH;
        float worldHeight = activeGameplay ? GAME_WORLD_HEIGHT : MENU_WORLD_HEIGHT;

        Viewport current = stage.getViewport();
        if (current.getWorldWidth() == worldWidth
                && current.getWorldHeight() == worldHeight) {
            return;
        }

        Viewport viewport = new FitViewport(worldWidth, worldHeight);
        stage.setViewport(viewport);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    private void updateMenuMusic(MenuState state) {
        if (state == MenuState.GAME && App.getGameSession() != null) {
            return;
        }

        if (state == MenuState.CHOOSE_PLANT) {
            var chapter = App.getLevelManager().getCurrentChapter();
            GameplayMusicResolver.Selection selection = chapter == null
                    ? null
                    : GameplayMusicResolver.resolve(
                    chapter.getId(),
                    GameplayMusicCue.CHOOSE
            );

            if (selection != null && selection.hasLoop()) {
                audioManager.playMusicSequence(
                        selection.intro(),
                        selection.loop(),
                        0.6f
                );
                return;
            }
        }

        MusicTrack track = MenuMusicResolver.resolve(state);
        if (track == null) {
            audioManager.stopMusic(0.6f);
            return;
        }
        audioManager.playMusic(track, true, 0.6f);
    }

    private void disposeCurrentScreen() {
        for (Actor actor : screenRoot.getChildren()) {
            if (actor instanceof Disposable disposable) {
                disposable.dispose();
            }
        }
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
        if (targetMenu == null) {
            return;
        }

        transitioning = true;
        displayedMenu = targetMenu;

        screenRoot.clearActions();
        screenRoot.setTouchable(Touchable.disabled);

        /*
         * Crossfade the old menu and loading overlay. Menu construction
         * starts only after the loading overlay has finished fading in.
         */
        showLoadingAndRebuild(targetMenu);
        screenRoot.addAction(fadeOut(HALF_TRANSITION_DURATION));
    }

    private void showLoadingAndRebuild(MenuState targetMenu) {
        loadingTarget = targetMenu;
        loadingStartedAt = TimeUtils.millis();
        menuLoaded = false;

        menuLoadPending = true;
        loadingFramesDrawn = 0;

        loadingOverlay.show();
    }

    private void loadTargetMenu(MenuState targetMenu) {
        if (loadingTarget != targetMenu) {
            return;
        }

        try {
            rebuildMenu(targetMenu);
            menuLoaded = true;
        } catch (RuntimeException exception) {
            handleMenuLoadingFailure(targetMenu, exception);
        }
    }

    private void finishLoadingTransition() {
        if (loadingTarget == null) {
            return;
        }

        loadingTarget = null;
        menuLoaded = false;
        loadingStartedAt = 0L;
        menuLoadPending = false;
        loadingFramesDrawn = 0;

        screenRoot.getColor().a = 0f;
        loadingOverlay.hide();

        screenRoot.clearActions();
        screenRoot.addAction(sequence(
                fadeIn(HALF_TRANSITION_DURATION),
                run(this::finishTransition)
        ));
    }

    private void handleMenuLoadingFailure(
            MenuState targetMenu,
            RuntimeException exception
    ) {
        Gdx.app.error(
                "AppView",
                "Failed to load menu: " + targetMenu,
                exception
        );

        loadingTarget = null;
        menuLoaded = false;
        loadingStartedAt = 0L;
        menuLoadPending = false;
        loadingFramesDrawn = 0;

        loadingOverlay.hide();

        transitioning = false;
        screenRoot.setTouchable(Touchable.childrenOnly);

        NotificationCenter.error(
                "Could not load " + targetMenu.getName() + "."
        );
    }

    private void finishTransition() {
        transitioning = false;
        screenRoot.setTouchable(Touchable.childrenOnly);
    }

    @Override
    public void dispose() {
        NotificationCenter.clear();

        if (audioManager != null) {
            audioManager.dispose();
        }

        if (stage != null) {
            disposeCurrentScreen();
            stage.dispose();
        }

        if (skin instanceof Disposable disposable) {
            disposable.dispose();
        }

        NetworkClient.getInstance()
                .disconnect();
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
