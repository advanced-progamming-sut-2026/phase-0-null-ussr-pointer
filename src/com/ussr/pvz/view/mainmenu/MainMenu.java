package com.ussr.pvz.view.mainmenu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

public class MainMenu extends Table {
    private final Skin skin;
    private final Table drawer;
    private boolean drawerOpen;

    public MainMenu(Skin skin) {
        this.skin = skin;
        this.drawer = new Table();
        this.drawerOpen = false;

        setFillParent(true);
        top().right();

        buildUi();
    }

    private void buildUi() {
        Table navigation = new Table();

        TextButton menuButton = createMenuButton();

        configureDrawer();
        configureMenuButton(menuButton);

        navigation.add(menuButton)
                .size(58f)
                .right()
                .row();

        navigation.add(drawer)
                .width(200f)
                .right()
                .padTop(6f)
                .row();

        add(navigation)
                .top()
                .right()
                .padTop(15f)
                .padRight(15f);
    }

    private TextButton createMenuButton() {
        TextButton button = new TextButton("", skin, "brown");
        button.clearChildren();

        Table icon = new Table();
        addMenuLine(icon);
        addMenuLine(icon);
        addMenuLine(icon);

        button.add(icon).size(28f, 24f);
        return button;
    }

    private void addMenuLine(Table icon) {
        Image line = new Image(
                skin.newDrawable("white-pixel", Color.WHITE)
        );
        icon.add(line).width(28f).height(3f).padBottom(4f).row();
    }

    private void configureDrawer() {
        drawer.setTransform(true);
        drawer.top().right();

        drawer.setBackground(
                skin.getDrawable(
                        "image_ui_dialog_asset_inner_bkgd_10"
                )
        );

        drawer.pad(10f);

        TextButton profileButton =
                createDrawerButton("Profile");

        TextButton newsButton =
                createDrawerButton("News");

        TextButton settingsButton =
                createDrawerButton("Settings");

        drawer.add(profileButton)
                .growX()
                .height(52f)
                .row();

        drawer.add(newsButton)
                .growX()
                .height(52f)
                .padTop(6f)
                .row();

        drawer.add(settingsButton)
                .growX()
                .height(52f)
                .padTop(6f)
                .row();

        profileButton.addListener(
                listener(this::openProfile)
        );

        newsButton.addListener(
                listener(this::openNews)
        );

        settingsButton.addListener(
                listener(this::openSettings)
        );

        setDrawerVisible(false);
    }

    private TextButton createDrawerButton(String text) {
        return new TextButton(text, skin, "green");
    }

    private void openNews() {
        App.setMenuState(MenuState.NEWS);
    }

    private void openSettings() {
        App.setMenuState(MenuState.SETTING);
    }

    private void configureMenuButton(TextButton menuButton) {
        menuButton.addListener(
                listener(this::toggleDrawer)
        );
    }

    private void toggleDrawer() {
        setDrawerVisible(!drawerOpen);
    }

    private void setDrawerVisible(boolean visible) {
        drawerOpen = visible;
        drawer.clearActions();

        if (drawer.getStage() == null) {
            drawer.setVisible(visible);
            drawer.setTouchable(
                    visible
                            ? Touchable.childrenOnly
                            : Touchable.disabled
            );
            return;
        }

        if (visible) {
            animateDrawerOpen();
        } else {
            animateDrawerClosed();
        }
    }

    private void animateDrawerOpen() {
        drawer.setVisible(true);
        drawer.setTouchable(Touchable.childrenOnly);
        drawer.getColor().a = 0f;
        drawer.setScale(0.92f);

        drawer.addAction(parallel(
                fadeIn(0.2f, Interpolation.fade),
                scaleTo(
                        1f,
                        1f,
                        0.2f,
                        Interpolation.smooth
                )
        ));
    }

    private void animateDrawerClosed() {
        drawer.setTouchable(Touchable.disabled);

        drawer.addAction(sequence(
                parallel(
                        fadeOut(0.15f, Interpolation.fade),
                        scaleTo(
                                0.92f,
                                0.92f,
                                0.15f,
                                Interpolation.smooth
                        )
                ),
                run(() -> {
                    drawer.setVisible(false);
                    drawer.getColor().a = 1f;
                    drawer.setScale(1f);
                })
        ));
    }

    private ChangeListener listener(Runnable action) {
        return new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        };
    }

    private void openProfile() {
        App.setMenuState(MenuState.PROFILE);
    }
}
