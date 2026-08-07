package com.ussr.pvz.view.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Disposable;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import pvz.libpvz.textures.TextureBank;

/**
 * Master layout for the in-game UI layer.
 * Enforces MVC by delegating all interactions to the GameplayController.
 */
public class InGameHud extends Table implements Disposable {

    private final SeedBankHud seedBankHud;
    private final ShovelWidget shovelWidget;
    private final PlantFoodWidget plantFoodWidget;
    private final WaveProgressBar waveProgressBar;
    private final PauseMenuAssets pauseMenuAssets;

    public InGameHud(Skin skin, TextureBank textures, GameplayController controller) {
        setFillParent(true);
        setTouchable(Touchable.childrenOnly);

        pauseMenuAssets = new PauseMenuAssets();

        // Initialize components
        seedBankHud = new SeedBankHud(skin, textures);
        shovelWidget = new ShovelWidget(
                skin,
                textures,
                controller,
                seedBankHud::clearSelection
        );

        // Plant food button — wires itself to controller.setOnPlantFoodDeactivated
        plantFoodWidget = new PlantFoodWidget(skin, textures, controller);

        waveProgressBar = new WaveProgressBar(skin, textures);

        GameEventAnnouncer eventAnnouncer = new GameEventAnnouncer();
        DebugToolsWidget debugTools = new DebugToolsWidget(skin);

        seedBankHud.setOnPlantSelected(controller::setSelectedSeed);
        controller.setOnPlantingCompleted(seedBankHud::clearSelection);

        ObjectiveWidgetFactory.ObjectiveWidgets objectives =
                ObjectiveWidgetFactory.create(skin, textures);

        // Pause Menu Trigger
        ImageButton pauseButton = new ImageButton(
                pauseMenuAssets.sliderBoltDrawable()
        );
        pauseButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.togglePauseMenu();
            }
        });

        // Top Row: seed bank | objectives | wave bar | debug | pause
        Table topRow = new Table();
        topRow.add(seedBankHud).left().expandX();
        topRow.add(objectives.topBarWidget()).center().expandX();
        topRow.add(waveProgressBar).right().top().padTop(12f).padRight(15f);
        topRow.add(debugTools).right().padRight(15f);
        topRow.add(pauseButton).right().top().pad(15f);

        // Bottom Row: [plant food] ... [shovel]
        // Plant food sits to the left of the shovel so both are thumb-reachable.
        Table bottomRow = new Table();
        bottomRow.add().expandX();
        bottomRow.add(plantFoodWidget).bottom().right().padRight(10f).padBottom(20f);
        bottomRow.add(shovelWidget).bottom().right().padRight(25f).padBottom(20f);

        // Center overlay (objectives lawn widget, etc.)
        Stack lawnStack = new Stack();
        lawnStack.add(objectives.lawnOverlayWidget());

        // Standard game layer
        Table mainGameLayer = new Table();
        mainGameLayer.add(topRow).growX().top().row();
        mainGameLayer.add(lawnStack).grow().row();
        mainGameLayer.add(bottomRow).growX().bottom();

        // High priority overlays
        PauseMenuOverlay pauseOverlay =
                new PauseMenuOverlay(skin, pauseMenuAssets, controller);
        GameOverOverlay gameOverOverlay = new GameOverOverlay(skin);

        // Root stack
        Stack rootStack = new Stack();
        rootStack.add(mainGameLayer);
        rootStack.add(pauseOverlay);
        rootStack.add(gameOverOverlay);
        rootStack.add(eventAnnouncer);

        add(rootStack).grow();
    }

    public SeedBankHud getSeedBankHud() {
        return seedBankHud;
    }

    /** Exposed so HoverCursorWidget can read plant-food mode state. */
    public PlantFoodWidget getPlantFoodWidget() {
        return plantFoodWidget;
    }

    @Override
    public void dispose() {
        pauseMenuAssets.dispose();
    }
}