package com.ussr.pvz.view.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
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
    private final ConveyorBeltWidget conveyorBeltWidget;
    private final NukeMinionWidget nukeMinionWidget;
    private final ResetTerrainWidget resetTerrainWidget;

    public InGameHud(Skin skin, TextureBank textures, GameplayController controller) {
        setFillParent(true);
        setTouchable(Touchable.childrenOnly);

        pauseMenuAssets = new PauseMenuAssets();
        conveyorBeltWidget = new ConveyorBeltWidget(skin, textures, controller);

        // Initialize components
        seedBankHud = new SeedBankHud(skin, textures);
        shovelWidget = new ShovelWidget(
                skin,
                textures,
                controller,
                this::clearPlantSelection
        );

        // Plant food button — wires itself to controller.setOnPlantFoodDeactivated
        plantFoodWidget = new PlantFoodWidget(skin, textures, controller);

        waveProgressBar = new WaveProgressBar(skin, textures);

        nukeMinionWidget   = new NukeMinionWidget(skin, textures);
        resetTerrainWidget = new ResetTerrainWidget(skin, textures);

        GameEventAnnouncer eventAnnouncer = new GameEventAnnouncer();
        DebugToolsWidget debugTools = new DebugToolsWidget(skin);
        LawnGridDebugOverlay lawnGridDebugOverlay = new LawnGridDebugOverlay(skin);

        seedBankHud.setOnPlantSelected(controller::setSelectedSeed);
        controller.setOnPlantingCompleted(this::clearPlantSelection);

        ObjectiveWidgetFactory.ObjectiveWidgets objectives =
                ObjectiveWidgetFactory.create(skin, textures);

        // Pause Menu Trigger
        ImageButton.ImageButtonStyle pauseStyle = new ImageButton.ImageButtonStyle();
        pauseStyle.imageUp = new TextureRegionDrawable(textures.region(
                "IMAGE_UI_HUD_INGAME_PAUSE_BUTTON"
        ));
        pauseStyle.imageDown = new TextureRegionDrawable(textures.region(
                "IMAGE_UI_HUD_INGAME_PAUSE_BUTTON_DOWN"
        ));
        ImageButton pauseButton = new ImageButton(pauseStyle);
        pauseButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.togglePauseMenu();
            }
        });

        // Top Row: Vertical seed bank (left) | objectives (center) | controls (right)
        Table topRow = new Table();
        topRow.setTouchable(Touchable.childrenOnly);
        topRow.top().left();

        topRow.add(seedBankHud).top().left().pad(0f, 4f, 0f, 0f);
        topRow.add(objectives.topBarWidget()).top().center().expandX().padTop(0f);

        Table topRightControls = new Table();
        topRightControls.setTouchable(Touchable.childrenOnly);
        topRightControls.top().right();
        topRightControls.add(waveProgressBar).right().top().padTop(2f).padRight(12f).row();
        topRightControls.add(debugTools).right().padTop(4f).padRight(12f).row();
        topRightControls.add(pauseButton).size(64f).right().top().padTop(4f).padRight(12f);

        topRow.add(topRightControls).top().right();

        // Conveyor table
        Table conveyorLayer = new Table();
        conveyorLayer.setFillParent(true);
        conveyorLayer.setTouchable(Touchable.childrenOnly);
        conveyorLayer.top().left();

        conveyorLayer.add(conveyorBeltWidget)
                .top()
                .left()
                .padTop(2f)
                .padLeft(4f);

        // Bottom Row: [nuke | reset terrain] ... [plant food] [shovel]
        Table bottomRow = new Table();
        bottomRow.setTouchable(Touchable.childrenOnly);
        bottomRow.add(nukeMinionWidget).bottom().left().padLeft(12f).padBottom(12f);
        bottomRow.add(resetTerrainWidget).bottom().left().padLeft(8f).padBottom(12f);
        bottomRow.add().expandX();
        bottomRow.add(plantFoodWidget).bottom().right().padRight(10f).padBottom(12f);
        bottomRow.add(shovelWidget).bottom().right().padRight(20f).padBottom(12f);

        // Center overlay
        Stack lawnStack = new Stack();
        lawnStack.setTouchable(Touchable.childrenOnly);
        lawnStack.add(objectives.lawnOverlayWidget());

        // Standard game layer
        Table mainGameLayer = new Table();
        mainGameLayer.setTouchable(Touchable.childrenOnly);
        mainGameLayer.add(topRow).growX().top().row();
        mainGameLayer.add(lawnStack).grow().row();
        mainGameLayer.add(bottomRow).growX().bottom();

        // High priority overlays
        PauseMenuOverlay pauseOverlay =
                new PauseMenuOverlay(skin, pauseMenuAssets, controller);
        GameOverOverlay gameOverOverlay = new GameOverOverlay(skin, textures);

        // Root stack
        Stack rootStack = new Stack();
        rootStack.setTouchable(Touchable.childrenOnly);
        rootStack.add(mainGameLayer);
        rootStack.add(conveyorLayer);
        rootStack.add(lawnGridDebugOverlay);
        rootStack.add(pauseOverlay);
        rootStack.add(gameOverOverlay);
        rootStack.add(eventAnnouncer);

        add(rootStack).grow();
    }

    public SeedBankHud getSeedBankHud() {
        return seedBankHud;
    }

    private void clearPlantSelection() {
        seedBankHud.clearSelection();
        conveyorBeltWidget.clearSelection();
    }

    public PlantFoodWidget getPlantFoodWidget() {
        return plantFoodWidget;
    }

    @Override
    public void dispose() {
        pauseMenuAssets.dispose();
    }
}