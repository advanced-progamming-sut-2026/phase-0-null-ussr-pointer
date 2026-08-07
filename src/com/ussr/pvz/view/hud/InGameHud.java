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
    private final ConveyorBeltWidget conveyorBeltWidget;
    private final ShovelWidget shovelWidget;
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
        waveProgressBar = new WaveProgressBar(skin, textures);

        GameEventAnnouncer eventAnnouncer = new GameEventAnnouncer();
        DebugToolsWidget debugTools = new DebugToolsWidget(skin);

        seedBankHud.setOnPlantSelected(controller::setSelectedSeed);
        controller.setOnPlantingCompleted(seedBankHud::clearSelection);

        ObjectiveWidgetFactory.ObjectiveWidgets objectives = ObjectiveWidgetFactory.create(skin, textures);

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

        Table topRow = new Table();
        topRow.add(seedBankHud).left().expandX();
        topRow.add(objectives.topBarWidget()).center().expandX();
        topRow.add(waveProgressBar).right().top().padTop(12f).padRight(15f);
        topRow.add(debugTools).right().padRight(15f);
        topRow.add(pauseButton).right().top().pad(15f);

        Table bottomRow = new Table();
        bottomRow.add().expandX();
        bottomRow.add(shovelWidget).bottom().right().padRight(25f).padBottom(20f);

        Stack lawnStack = new Stack();
        lawnStack.add(objectives.lawnOverlayWidget());

        Table middleRow = new Table();
        middleRow.add(conveyorBeltWidget).top().padTop(10f).padLeft(10f);
        middleRow.add(lawnStack).grow();

        Table mainGameLayer = new Table();
        mainGameLayer.add(topRow).growX().top().row();
        mainGameLayer.add(middleRow).grow().row();
        mainGameLayer.add(bottomRow).growX().bottom();

        // High priority overlays
        PauseMenuOverlay pauseOverlay =
                new PauseMenuOverlay(
                        skin,
                        pauseMenuAssets,
                        controller
                );
        GameOverOverlay gameOverOverlay = new GameOverOverlay(skin);

        Stack rootStack = new Stack();
        rootStack.add(mainGameLayer);
        rootStack.add(pauseOverlay);
        rootStack.add(gameOverOverlay);
        rootStack.add(eventAnnouncer);

        add(rootStack).grow();
    }

    private void registerLawnDropTarget(DragAndDrop dragAndDrop, GameplayController controller) {
        dragAndDrop.addTarget(new DragAndDrop.Target(lawnWidget) {
            @Override
            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                lawnWidget.setDragPreviewKey((String) payload.getObject());
                return lawnWidget.gridCellAt(x, y) != null;
            }

            @Override
            public void reset(DragAndDrop.Source source, DragAndDrop.Payload payload) {
                lawnWidget.setDragPreviewKey(null);
            }

            @Override
            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                lawnWidget.setDragPreviewKey(null);
                int[] cell = lawnWidget.gridCellAt(x, y);
                if (cell != null) {
                    controller.plantAt((String) payload.getObject(), cell[0], cell[1]);
                }
            }
        });
    }

    public SeedBankHud getSeedBankHud() {
        return seedBankHud;
    }

    @Override
    public void dispose() {
        pauseMenuAssets.dispose();
    }
}
