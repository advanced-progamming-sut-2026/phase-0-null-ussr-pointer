package com.ussr.pvz.view.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.level.behavior.MultiplayerIZombieBehavior;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

public class InGameHud extends Table implements Disposable {

    private final IZombieHud iZombieHud;
    private final TimedWarHudWidget timedWarHudWidget;
    private final SeedBankHud seedBankHud;
    private final ShovelWidget shovelWidget;
    private final PlantFoodWidget plantFoodWidget;
    private final WaveProgressBar waveProgressBar;
    private final PauseMenuAssets pauseMenuAssets;
    private final ConveyorBeltWidget conveyorBeltWidget;
    private final NukeMinionWidget nukeMinionWidget;
    private final ResetTerrainWidget resetTerrainWidget;
    private final ReactionHudWidget reactionHud;
    private final ReactionOverlayWidget reactionOverlay;
    private final HoverCursorWidget hoverCursorWidget;
    private final Label waitingForOpponentLabel;
    private final PamPlayer pamPlayer;

    private GameSession wiredSession = null;

    public InGameHud(Skin skin, TextureBank textures, GameplayController controller, PamPlayer pamPlayer) {
        this.pamPlayer = pamPlayer;
        setFillParent(true);
        setTouchable(Touchable.childrenOnly);

        pauseMenuAssets    = new PauseMenuAssets();
        conveyorBeltWidget = new ConveyorBeltWidget(skin, textures, controller);
        seedBankHud        = new SeedBankHud(skin, textures);
        iZombieHud         = new IZombieHud(skin, textures, controller);
        shovelWidget       = new ShovelWidget(skin, textures, controller, this::clearPlantSelection);
        plantFoodWidget    = new PlantFoodWidget(skin, textures, controller);
        waveProgressBar    = new WaveProgressBar(skin, textures);
        nukeMinionWidget   = new NukeMinionWidget(skin, textures);
        resetTerrainWidget = new ResetTerrainWidget(skin, textures);
        timedWarHudWidget  = new TimedWarHudWidget(skin, textures);
        reactionHud        = new ReactionHudWidget(skin, textures, pamPlayer);
        reactionOverlay    = new ReactionOverlayWidget(skin, textures, pamPlayer);
        hoverCursorWidget   = new HoverCursorWidget(textures, controller, pamPlayer);

        waitingForOpponentLabel = new Label(
                "WAITING FOR OPPONENT...",
                skin,
                "medium_outline"
        );
        waitingForOpponentLabel.setTouchable(Touchable.disabled);
        waitingForOpponentLabel.setVisible(false);

        MeowScoreWidget      meowScoreWidget      = new MeowScoreWidget(skin, textures);
        GameEventAnnouncer   eventAnnouncer       = new GameEventAnnouncer(skin, App.getGameSession());
        DebugToolsWidget     debugTools           = new DebugToolsWidget(skin);
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
            @Override public void changed(ChangeEvent event, Actor actor) {
                controller.togglePauseMenu();
            }
        });

        // Top row
        Table topRow = new Table();
        topRow.setFillParent(true);
        topRow.setTouchable(Touchable.childrenOnly);
        topRow.top().left();
        topRow.add(seedBankHud).top().left().pad(0f, 4f, 0f, 0f);
        topRow.add(objectives.topBarWidget()).top().center().expandX().padTop(0f).align(Align.top);
        topRow.add(upgradePanel).top().right().height(82f).padRight(6f);
        topRow.add(iZombieHud).top().right().pad(0f, 0f, 0f, 4f);

        Table waveAndTimer = new Table();
        waveAndTimer.top().right();
        waveAndTimer.add(waveProgressBar).right().row();
        waveAndTimer.add(timedWarHudWidget).right().padTop(4f);

        Table topRightControls = new Table();
        topRightControls.setTouchable(Touchable.childrenOnly);
        topRightControls.top().right();
        topRightControls.add(waveAndTimer).right().top().padTop(2f).padRight(12f).row();
        topRightControls.add(debugTools).right().padTop(4f).padRight(12f).row();
        topRightControls.add(pauseButton).size(64f).right().top().padTop(4f).padRight(12f).row();
        topRightControls.add(meowScoreWidget).right().top().padTop(6f).padRight(12f);
        topRow.add(topRightControls).top().right();

        // Conveyor layer
        Table conveyorLayer = new Table();
        conveyorLayer.setFillParent(true);
        conveyorLayer.setTouchable(Touchable.childrenOnly);
        conveyorLayer.top().left();
        conveyorLayer.add(conveyorBeltWidget).top().left().padTop(65f).padLeft(12f);

        // Bottom row
        InGameCurrencyHud currencyHud    = new InGameCurrencyHud(skin);
        LetsRockWidget    letsRockWidget = new LetsRockWidget(skin);

        Table currencyRow = new Table();
        currencyRow.left();
        currencyRow.add(currencyHud).left();
        currencyRow.add(letsRockWidget).left().padLeft(10f).bottom();

        Table leftButtonsRow = new Table();
        leftButtonsRow.add(nukeMinionWidget).bottom().left().padLeft(15f).padBottom(20f);
        leftButtonsRow.add(resetTerrainWidget).bottom().left().padLeft(8f).padBottom(20f);

        Table leftBottomColumn = new Table();
        leftBottomColumn.bottom().left();
        leftBottomColumn.add(currencyRow).left().padLeft(15f).padBottom(6f).row();
        leftBottomColumn.add(leftButtonsRow).left().row();

        Table bottomRow = new Table();
        bottomRow.setFillParent(true);
        bottomRow.bottom().left();
        bottomRow.setTouchable(Touchable.childrenOnly);
        bottomRow.add(leftBottomColumn).bottom().left().expandY();
        bottomRow.add().expandX();
        bottomRow.add(plantFoodWidget).bottom().right().padRight(10f).padBottom(20f);
        bottomRow.add(shovelWidget).bottom().right().padRight(25f).padBottom(20f);

        // Reaction HUD
        Table reactionLayer = new Table();
        reactionLayer.setFillParent(true);
        reactionLayer.setTouchable(Touchable.childrenOnly);
        reactionLayer.bottom().left();
        reactionLayer.add(reactionHud).bottom().left().padLeft(18f).padBottom(145f);

        // Center overlay
        Stack lawnStack = new Stack();
        lawnStack.setFillParent(true);
        lawnStack.setTouchable(Touchable.childrenOnly);
        lawnStack.add(objectives.lawnOverlayWidget());

        // High-priority overlays
        PauseMenuOverlay pauseOverlay    = new PauseMenuOverlay(skin, pauseMenuAssets, controller);
        GameOverOverlay  gameOverOverlay = new GameOverOverlay(skin, textures);

        Table reactionOverlayWrapper = new Table();
        reactionOverlayWrapper.setFillParent(true);
        reactionOverlayWrapper.setTouchable(Touchable.disabled);
        reactionOverlayWrapper.add(reactionOverlay).grow();

        Table waitingLayer = new Table();
        waitingLayer.setFillParent(true);
        waitingLayer.setTouchable(Touchable.disabled);
        waitingLayer.add(waitingForOpponentLabel).center();

        // Root stack — hoverCursorWidget renders cursor highlights & preview animations
        Stack rootStack = new Stack();
        rootStack.setTouchable(Touchable.childrenOnly);
        rootStack.add(lawnStack);
        rootStack.add(topRow);
        rootStack.add(bottomRow);
        rootStack.add(conveyorLayer);
        rootStack.add(lawnGridDebugOverlay);
        rootStack.add(hoverCursorWidget);
        rootStack.add(waitingLayer);
        rootStack.add(pauseOverlay);
        rootStack.add(gameOverOverlay);
        rootStack.add(eventAnnouncer);
        rootStack.add(reactionLayer);
        rootStack.add(reactionOverlayWrapper);

        add(rootStack).grow().minSize(0f);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        GameSession session = App.getGameSession();
        boolean waitingForOpponent = session != null
                && session.getLevel() != null
                && session.getLevel().getBehavior()
                instanceof MultiplayerIZombieBehavior behavior
                && behavior.isWaitingForPlayers()
                && behavior.isLocalPlayerReady();
        waitingForOpponentLabel.setVisible(waitingForOpponent);

        if (session != null && session != wiredSession && session.getEventBus() != null) {
            session.getEventBus().subscribe(
                    GameEvent.ReactionReceivedEvent.class,
                    reactionOverlay::onReactionReceived
            );
            wiredSession = session;
        }
    }

    public SeedBankHud getSeedBankHud() { return seedBankHud; }
    public PlantFoodWidget getPlantFoodWidget() { return plantFoodWidget; }

    private void clearPlantSelection() {
        seedBankHud.clearSelection();
        conveyorBeltWidget.clearSelection();
    }

    @Override
    public void dispose() {
        pauseMenuAssets.dispose();
        wiredSession = null;
    }
}