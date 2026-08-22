package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.service.ChoosePlantService;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

public final class PauseMenuOverlay extends WidgetGroup {
    private static final float SIDEBAR_WIDTH = 440f;
    private static final float OPEN_DURATION = 0.35f;
    private static final float CLOSE_DURATION = 0.28f;

    private final GameplayController controller;
    private final PauseMenuAssets assets;

    private final Image dimLayer;
    private final Table sidebar;

    private boolean menuOpen;
    private boolean stateInitialized;
    private boolean positionInitialized;

    public PauseMenuOverlay(
            Skin skin,
            PauseMenuAssets assets,
            GameplayController controller
    ) {
        this.controller = controller;
        this.assets = assets;

        setFillParent(true);
        setTouchable(Touchable.disabled);

        dimLayer = createDimLayer(skin);
        sidebar = createSidebar(skin);

        addActor(dimLayer);
        addActor(sidebar);

        setVisible(false);
    }

    private Image createDimLayer(Skin skin) {
        Image dim = new Image(skin.newDrawable(
                "white-pixel",
                new Color(0f, 0f, 0f, 0.62f)
        ));

        dim.setTouchable(Touchable.enabled);

        dim.addListener(new ClickListener() {
            @Override
            public void clicked(
                    InputEvent event,
                    float x,
                    float y
            ) {
                controller.togglePauseMenu();
            }
        });

        return dim;
    }

    private Table createSidebar(Skin skin) {
        Table panel = new Table();
        panel.top();
        panel.pad(24f, 30f, 30f, 30f);
        panel.setTouchable(Touchable.childrenOnly);
        panel.setBackground(assets.cardDrawable());

        Image topper = new Image(
                assets.windowTopperDrawable()
        );
        topper.setScaling(Scaling.fit);

        Image sunflower = new Image(
                assets.sunflowerTopperDrawable()
        );
        sunflower.setScaling(Scaling.fit);

        Label title = new Label(
                "PAUSED",
                skin,
                "big_outline"
        );

        Stack header = new Stack();
        header.add(topper);

        Table headerContent = new Table();
        headerContent.add(sunflower)
                .size(100f, 84f)
                .left();
        headerContent.add(title)
                .expandX()
                .center()
                .padRight(90f);

        header.add(headerContent);

        panel.add(header)
                .width(SIDEBAR_WIDTH - 30f)
                .height(105f)
                .padBottom(20f)
                .row();

        panel.add(createLevelCard(skin))
                .width(SIDEBAR_WIDTH - 50f)
                .height(125f)
                .padBottom(25f)
                .row();

        addPauseButtons(panel, skin);

        return panel;
    }

    private Actor createLevelCard(Skin skin) {
        Stack stack = new Stack();

        Table background = new Table();

        Image left = new Image(
                assets.objectiveLeftDrawable()
        );
        left.setScaling(Scaling.stretch);

        Image middle = new Image(
                assets.objectiveMiddleDrawable()
        );
        middle.setScaling(Scaling.stretch);

        Image right = new Image(
                assets.objectiveRightDrawable()
        );
        right.setScaling(Scaling.stretch);

        background.add(left).width(110f).growY();
        background.add(middle).grow();
        background.add(right).width(22f).growY();

        Table information = new Table();

        String levelName = App.getGameSession() == null
                ? "Current Level"
                : App.getGameSession().getLevel().getId();

        information.add(new Label(
                levelName,
                skin,
                "medium"
        )).center().row();

        information.add(new Label(
                "Game paused",
                skin,
                "default"
        )).center().padTop(6f);

        stack.add(background);
        stack.add(information);

        return stack;
    }

    private void addPauseButtons(
            Table panel,
            Skin skin
    ) {
        TextButton.TextButtonStyle style =
                assets.createButtonStyle(skin);

        TextButton resumeButton =
                new TextButton("RESUME", style);

        TextButton settingsButton =
                new TextButton("SETTINGS", style);

        TextButton restartButton =
                new TextButton("RESTART LEVEL", style);

        TextButton exitButton =
                new TextButton("SAVE & EXIT", style);

        resumeButton.addListener(new ChangeListener() {
            @Override
            public void changed(
                    ChangeEvent event,
                    Actor actor
            ) {
                controller.togglePauseMenu();
            }
        });

        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(
                    ChangeEvent event,
                    Actor actor
            ) {
                App.setResumeToPauseMenu(true);
                App.setMenuState(MenuState.SETTING);
            }
        });

        restartButton.addListener(new ChangeListener() {
            @Override
            public void changed(
                    ChangeEvent event,
                    Actor actor
            ) {
                App.getLevelManager().startLevel(
                        App.getGameSession()
                                .getLevel()
                                .getId()
                );

                App.setMenuState(MenuState.CHOOSE_PLANT);
            }
        });

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(
                    ChangeEvent event,
                    Actor actor
            ) {
                App.setMenuState(MenuState.LEVEL_SELECTION);
                App.setGameSession(null);
            }
        });

        panel.add(resumeButton)
                .width(300f)
                .height(62f)
                .padBottom(14f)
                .row();

        panel.add(settingsButton)
                .width(300f)
                .height(62f)
                .padBottom(14f)
                .row();

        panel.add(restartButton)
                .width(300f)
                .height(62f)
                .padBottom(14f)
                .row();

        panel.add(exitButton)
                .width(300f)
                .height(62f)
                .row();
    }

    @Override
    public void layout() {
        dimLayer.setBounds(
                0f,
                0f,
                getWidth(),
                getHeight()
        );

        sidebar.setSize(
                SIDEBAR_WIDTH,
                getHeight()
        );

        if (!positionInitialized) {
            sidebar.setPosition(
                    menuOpen ? 0f : -SIDEBAR_WIDTH,
                    0f
            );

            positionInitialized = true;
        }
    }

    private void openSidebar() {
        sidebar.clearActions();
        dimLayer.clearActions();

        setVisible(true);
        setTouchable(Touchable.enabled);

        sidebar.setPosition(
                -SIDEBAR_WIDTH,
                0f
        );

        dimLayer.getColor().a = 0f;

        sidebar.addAction(moveTo(
                0f,
                0f,
                OPEN_DURATION,
                Interpolation.swingOut
        ));

        dimLayer.addAction(fadeIn(0.22f));
    }

    private void closeSidebar() {
        sidebar.clearActions();
        dimLayer.clearActions();

        sidebar.addAction(sequence(
                moveTo(
                        -SIDEBAR_WIDTH,
                        0f,
                        CLOSE_DURATION,
                        Interpolation.fade
                ),
                run(() -> {
                    setVisible(false);
                    setTouchable(Touchable.disabled);
                })
        ));

        dimLayer.addAction(fadeOut(0.2f));
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        boolean shouldOpen =
                App.getGameSession() != null
                        && controller.isPauseMenuOpen();

        if (!stateInitialized) {
            stateInitialized = true;
            menuOpen = shouldOpen;

            if (shouldOpen) {
                openSidebar();
            }

            return;
        }

        if (shouldOpen == menuOpen) {
            return;
        }

        menuOpen = shouldOpen;

        if (shouldOpen) {
            openSidebar();
        } else {
            closeSidebar();
        }
    }
}