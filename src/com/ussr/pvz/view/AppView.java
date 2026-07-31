package com.ussr.pvz.view;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.util.SessionManager;
import com.ussr.pvz.notification.NotificationCenter;
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

public class AppView implements ApplicationListener {
    private Stage stage;
    private Skin skin;
    private NotificationOverlay notificationOverlay;

    private Table screenRoot;
    private MenuState displayedMenu;

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

        showMenu(App.getMenuState());

        notificationOverlay = new NotificationOverlay(skin);
        stage.addActor(notificationOverlay);

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

        if (App.getMenuState() != displayedMenu) {
            showMenu(App.getMenuState());
        }

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
        screenRoot.clearChildren();

        switch (state) {
            case REGISTER ->
                    screenRoot.add(new RegisterMenu(skin)).grow();

            case LOGIN ->
                    screenRoot.add(
                            new Label("Login", skin, "big_outline")
                    );

            default ->
                    screenRoot.add(
                            new Label(
                                    state.getName(),
                                    skin,
                                    "big_outline"
                            )
                    );
        }

        displayedMenu = state;
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
