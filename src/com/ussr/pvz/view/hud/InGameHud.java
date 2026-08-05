package com.ussr.pvz.view.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import pvz.libpvz.textures.TextureBank;

/**
 * Master layout for the in-game UI layer.
 * Enforces MVC by delegating all interactions to the GameplayController.
 */
public class InGameHud extends Table {
    private final SeedBankHud seedBankHud;
    private final ShovelWidget shovelWidget;
    private final WaveProgressBar waveProgressBar;

    public InGameHud(Skin skin, TextureBank textures, GameplayController controller) {
        setFillParent(true);
        setTouchable(Touchable.childrenOnly);

        // Initialize components
        seedBankHud = new SeedBankHud(skin, textures);
        shovelWidget = new ShovelWidget(skin, textures, controller);
        waveProgressBar = new WaveProgressBar(skin, textures);

        GameEventAnnouncer eventAnnouncer = new GameEventAnnouncer();
        DebugToolsWidget debugTools = new DebugToolsWidget(skin);

        seedBankHud.setOnPlantSelected(controller::setSelectedSeed);

        ObjectiveWidgetFactory.ObjectiveWidgets objectives = ObjectiveWidgetFactory.create(skin, textures);

        // Pause Menu Trigger
        TextButton pauseButton = new TextButton("Pause", skin, "brown");
        pauseButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.togglePauseMenu();
            }
        });

        // Top Row Layout
        Table topRow = new Table();
        topRow.add(seedBankHud).left().expandX();
        topRow.add(objectives.topBarWidget()).center().expandX();
        topRow.add(debugTools).right().padRight(15f);
        topRow.add(pauseButton).right().top().pad(15f);

        // Bottom Row Layout
        Table bottomRow = new Table();
        bottomRow.add(waveProgressBar).bottom().right().expandX().padRight(20f).padBottom(15f);
        bottomRow.add(shovelWidget).bottom().right().padRight(25f).padBottom(20f);

        // Center Grid Layout
        Stack lawnStack = new Stack();
        lawnStack.add(objectives.lawnOverlayWidget());

        // Assemble standard game layer
        Table mainGameLayer = new Table();
        mainGameLayer.add(topRow).growX().top().row();
        mainGameLayer.add(lawnStack).grow().row();
        mainGameLayer.add(bottomRow).growX().bottom();

        // High priority overlays
        PauseMenuOverlay pauseOverlay = new PauseMenuOverlay(skin, controller);
        GameOverOverlay gameOverOverlay = new GameOverOverlay(skin);

        // Root construction
        Stack rootStack = new Stack();
        rootStack.add(mainGameLayer);
        rootStack.add(pauseOverlay);
        rootStack.add(gameOverOverlay);
        rootStack.add(eventAnnouncer); // Logical UI event dispatcher (invisible)

        add(rootStack).grow();
    }

    public SeedBankHud getSeedBankHud() {
        return seedBankHud;
    }
}