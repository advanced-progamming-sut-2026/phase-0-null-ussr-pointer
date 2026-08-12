package com.ussr.pvz.view.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import com.ussr.pvz.model.App;
import pvz.libpvz.textures.TextureBank;

/**
 * Master layout for the in-game UI layer.
 * Enforces MVC by delegating all interactions to the GameplayController.
 */
public class InGameHud extends Table implements Disposable {

    private final IZombieHud iZombieHud;

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

        seedBankHud = new SeedBankHud(skin, textures);
        this.iZombieHud = new IZombieHud(skin, textures, controller);
        shovelWidget = new ShovelWidget(skin, textures, controller, this::clearPlantSelection);

        plantFoodWidget = new PlantFoodWidget(skin, textures, controller);
        waveProgressBar = new WaveProgressBar(skin, textures);

        nukeMinionWidget   = new NukeMinionWidget(skin, textures);
        resetTerrainWidget = new ResetTerrainWidget(skin, textures);

        GameEventAnnouncer eventAnnouncer = new GameEventAnnouncer(skin, App.getGameSession());
        DebugToolsWidget debugTools = new DebugToolsWidget(skin);
        LawnGridDebugOverlay lawnGridDebugOverlay = new LawnGridDebugOverlay(skin);

        seedBankHud.setOnPlantSelected(controller::setSelectedSeed);
        controller.setOnPlantingCompleted(this::clearPlantSelection);

        ObjectiveWidgetFactory.ObjectiveWidgets objectives =
                ObjectiveWidgetFactory.create(skin, textures);

        BeghouledUpgradePanel upgradePanel = new BeghouledUpgradePanel(skin, controller);

        // Pause button
        ImageButton.ImageButtonStyle pauseStyle = new ImageButton.ImageButtonStyle();
        pauseStyle.imageUp   = new TextureRegionDrawable(textures.region("IMAGE_UI_HUD_INGAME_PAUSE_BUTTON"));
        pauseStyle.imageDown = new TextureRegionDrawable(textures.region("IMAGE_UI_HUD_INGAME_PAUSE_BUTTON_DOWN"));
        ImageButton pauseButton = new ImageButton(pauseStyle);
        pauseButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.togglePauseMenu();
            }
        });

        // Top Row: seed bank (left) | objectives (center, expands) | upgrade panel | controls (right)
        Table topRow = new Table();
        topRow.setFillParent(true);
        topRow.setTouchable(Touchable.childrenOnly);
        topRow.top().left();

        // SeedBankHud and IZombieHud share the same top-left slot.
        // Each self-hides when its level type is not active, so only one shows at a time.
        Stack leftHudStack = new Stack();
        leftHudStack.add(seedBankHud);
        leftHudStack.add(this.iZombieHud);

        topRow.add(leftHudStack).top().left().pad(0f, 4f, 0f, 0f);
        topRow.add(objectives.topBarWidget()).top().center().expandX().padTop(0f);
        topRow.add(upgradePanel).top().right().height(82f).padRight(6f); // same height as seed packets

        Table topRightControls = new Table();
        topRightControls.setTouchable(Touchable.childrenOnly);
        topRightControls.top().right();
        topRightControls.add(waveProgressBar).right().top().padTop(2f).padRight(12f).row();
        topRightControls.add(debugTools).right().padTop(4f).padRight(12f).row();
        topRightControls.add(pauseButton).size(64f).right().top().padTop(4f).padRight(12f);

        topRow.add(topRightControls).top().right();

        // Conveyor layer — padTop reduced so it doesn't overlap the seed bank awkwardly
        Table conveyorLayer = new Table();
        conveyorLayer.setFillParent(true);
        conveyorLayer.setTouchable(Touchable.childrenOnly);
        conveyorLayer.top().left();
        conveyorLayer.add(conveyorBeltWidget)
                .top()
                .left()
                .padTop(65f)
                .padLeft(12f);

        // Bottom Row
        Table bottomRow = new Table();
        bottomRow.setFillParent(true);
        bottomRow.bottom().left();
        bottomRow.setTouchable(Touchable.childrenOnly);
        bottomRow.add(nukeMinionWidget).bottom().left().padLeft(15f).padBottom(20f);
        bottomRow.add(resetTerrainWidget).bottom().left().padLeft(8f).padBottom(20f);
        bottomRow.add().expandX();
        bottomRow.add(plantFoodWidget).bottom().right().padRight(10f).padBottom(20f);
        bottomRow.add(shovelWidget).bottom().right().padRight(25f).padBottom(20f);

        // Center overlay
        Stack lawnStack = new Stack();
        lawnStack.setFillParent(true);
        lawnStack.setTouchable(Touchable.childrenOnly);
        lawnStack.add(objectives.lawnOverlayWidget());

        // High priority overlays
        PauseMenuOverlay pauseOverlay    = new PauseMenuOverlay(skin, pauseMenuAssets, controller);
        GameOverOverlay  gameOverOverlay = new GameOverOverlay(skin, textures);

        // Root stack
        Stack rootStack = new Stack();
        rootStack.setTouchable(Touchable.childrenOnly);
        // Keep HUD zones as independent full-screen overlays. Their positions
        // must not depend on seed-bank height, dialogue text, or chapter art.
        rootStack.add(lawnStack);
        rootStack.add(topRow);
        rootStack.add(bottomRow);
        rootStack.add(conveyorLayer);
        rootStack.add(lawnGridDebugOverlay);
        rootStack.add(pauseOverlay);
        rootStack.add(gameOverOverlay);
        rootStack.add(eventAnnouncer);

        add(rootStack).grow().minSize(0f);
    }

    public SeedBankHud getSeedBankHud() { return seedBankHud; }

    private void clearPlantSelection() {
        seedBankHud.clearSelection();
        conveyorBeltWidget.clearSelection();
        iZombieHud.clearSelection();
    }

    public PlantFoodWidget getPlantFoodWidget() { return plantFoodWidget; }

    @Override
    public void dispose() { pauseMenuAssets.dispose(); }
}
