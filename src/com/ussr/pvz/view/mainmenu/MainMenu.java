package com.ussr.pvz.view.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.Align;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.view.loading.LoadingCenter;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

public class MainMenu extends Table {
    private static final String BACKGROUND_DRAWABLE =
            "exported-main-menu-background";
    private static final String BACKGROUND_PATH =
            "pvz-assets/Exports/MainMenu_Background/"
                    + "mainmenu_background.png";
    private static final String LOGO_DRAWABLE =
            "exported-main-menu-logo";

    private static final String LOGO_PATH =
            "pvz-assets/Exports/UI_MainMenuLogo/"
                    + "pvz2_logo_horizontal.png";

    private final Skin skin;
    private final Table drawer;
    private boolean drawerOpen;

    public MainMenu(Skin skin) {
        this.skin = skin;
        this.drawer = new Table();
        this.drawerOpen = false;

        installExportedDrawable(
                BACKGROUND_DRAWABLE,
                BACKGROUND_PATH
        );

        installExportedDrawable(
                LOGO_DRAWABLE,
                LOGO_PATH
        );
        setFillParent(true);
        top().right();

        buildUi();
    }

    private void buildUi() {
        Stack layers = new Stack();

        layers.add(createBackground());
        layers.add(createLogoLayer());
        layers.add(createCenterActions());
        layers.add(createBottomActions());
        layers.add(createNavigation());

        add(layers).grow();
    }

    private Table createLogoLayer() {
        Table layer = new Table();
        layer.top();

        Image logo = new Image(
                skin.getDrawable(LOGO_DRAWABLE)
        );

        logo.setScaling(Scaling.fit);
        logo.setTouchable(Touchable.disabled);

        layer.add(logo)
                .size(390f, 145f)
                .padTop(45f);

        return layer;
    }

    private Image createBackground() {
        Image background = new Image(
                skin.getDrawable(BACKGROUND_DRAWABLE)
        );
        background.setScaling(Scaling.fill);
        background.setTouchable(Touchable.disabled);
        return background;
    }

    private void installExportedDrawable(
            String drawableName,
            String filePath
    ) {
        if (skin.has(drawableName, Drawable.class)) {
            return;
        }

        Texture texture = new Texture(
                Gdx.files.local(filePath)
        );

        texture.setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear
        );

        skin.add(drawableName, texture, Texture.class);
        skin.add(
                drawableName,
                new TextureRegionDrawable(texture),
                Drawable.class
        );
    }

    private Table createNavigation() {
        Table navigation = new Table();
        navigation.top().right();

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

        navigation.padTop(15f).padRight(15f);
        return navigation;
    }

    private Table createCenterActions() {
        Table layer = new Table();
        ImageButton gameButton = createBannerButton(
                "image_ui_mainmenu_mainmenu_content_offline"
        );
        gameButton.addListener(listener(this::openGame));
        layer.add(createLabeledBanner(gameButton, "Game"))
                .size(270f, 108f);
        return layer;
    }

    private Table createBottomActions() {
        Table layer = new Table();
        layer.bottom();

        ImageButton travelLogButton = createBannerButton(
                "image_ui_mainmenu_mainmenu_content_downloading"
        );
        travelLogButton.addListener(listener(this::openTravelLog));

        layer.add(createLabeledBanner(travelLogButton, "Travel Log"))
                .size(270f, 108f)
                .padBottom(24f);
        return layer;
    }

    private Stack createLabeledBanner(
            ImageButton button,
            String caption
    ) {
        Stack banner = new Stack();
        banner.add(button);

        Label label = new Label(caption, skin, "medium_outline");
        label.setTouchable(Touchable.disabled);

        Table captionLayer = new Table();
        captionLayer.bottom();
        captionLayer.add(label).padBottom(7f);
        captionLayer.setTouchable(Touchable.disabled);
        banner.add(captionLayer);
        return banner;
    }

    private ImageButton createBannerButton(String drawableName) {
        ImageButtonStyle style = new ImageButtonStyle();
        style.imageUp = skin.getDrawable(drawableName);
        style.imageDown = skin.newDrawable(
                drawableName,
                new Color(0.82f, 0.82f, 0.82f, 1f)
        );
        ImageButton button = new ImageButton(style);
        addHoverEffect(button);
        return button;
    }

    private void addHoverEffect(ImageButton button) {
        button.addListener(new ClickListener() {
            @Override
            public void enter(
                    InputEvent event,
                    float x,
                    float y,
                    int pointer,
                    Actor fromActor
            ) {
                button.clearActions();
                button.setTransform(true);
                button.setOrigin(Align.center);
                button.addAction(parallel(
                        color(
                                new Color(1f, 1f, 0.78f, 1f),
                                0.16f,
                                Interpolation.fade
                        ),
                        scaleTo(
                                1.06f,
                                1.06f,
                                0.16f,
                                Interpolation.smooth
                        )
                ));
            }

            @Override
            public void exit(
                    InputEvent event,
                    float x,
                    float y,
                    int pointer,
                    Actor toActor
            ) {
                button.clearActions();
                button.addAction(parallel(
                        color(
                                Color.WHITE,
                                0.16f,
                                Interpolation.fade
                        ),
                        scaleTo(
                                1f,
                                1f,
                                0.16f,
                                Interpolation.smooth
                        )
                ));
            }
        });
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
                createDrawerButton(
                        "image_ui_mainmenu_mm_playericon"
                );

        TextButton newsButton =
                createDrawerButton(
                        "image_ui_mainmenu_mm_newsicon"
                );

        TextButton settingsButton =
                createDrawerButton(
                        "image_ui_mainmenu_mm_settings"
                );

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

    private TextButton createDrawerButton(String drawableName) {
        TextButton button = new TextButton(
                "",
                skin,
                "green"
        );

        button.clearChildren();

        Image icon = new Image(
                skin.getDrawable(drawableName)
        );

        icon.setScaling(Scaling.fit);
        icon.setTouchable(Touchable.disabled);

        button.add(icon).size(44f, 40f);
        return button;
    }

    private void openNews() {
        App.setMenuState(MenuState.NEWS);
    }

    private void openSettings() {
        App.setMenuState(MenuState.SETTING);
    }

    private void openGame() {
        LoadingCenter.requestFor(MenuState.GAME);
        App.setMenuState(MenuState.GAME);
    }

    private void openTravelLog() {
        App.setMenuState(MenuState.TRAVEL_LOG);
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
