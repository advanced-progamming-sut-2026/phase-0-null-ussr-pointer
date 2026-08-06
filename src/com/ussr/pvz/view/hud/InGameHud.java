package com.ussr.pvz.view.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import pvz.libpvz.textures.TextureBank;

public class InGameHud extends Table {
    private final SeedBankHud seedBankHud;
    private final ConveyorBeltWidget conveyorBeltWidget;
    private final ShovelWidget shovelWidget;
    private final WaveProgressBar waveProgressBar;
    private final LawnWidget lawnWidget;

    public InGameHud(Skin skin, TextureBank textures, GameplayController controller) {
        setFillParent(true);
        setTouchable(Touchable.childrenOnly);

        DragAndDrop dragAndDrop = new DragAndDrop();
        dragAndDrop.setTapSquareSize(24f);

        // Initialize components
        seedBankHud = new SeedBankHud(skin, textures, dragAndDrop, controller);
        conveyorBeltWidget = new ConveyorBeltWidget(skin, textures, controller);
        shovelWidget = new ShovelWidget(skin, textures, controller);
        waveProgressBar = new WaveProgressBar(skin, textures);
        lawnWidget = new LawnWidget(controller);

        registerLawnDropTarget(dragAndDrop, controller);

        HoverCursorWidget hoverCursor = new HoverCursorWidget(lawnWidget, seedBankHud, textures);
        GameEventAnnouncer eventAnnouncer = new GameEventAnnouncer();
        DebugToolsWidget debugTools = new DebugToolsWidget(skin);

        seedBankHud.setOnPlantSelected(controller::setSelectedSeed);

        ObjectiveWidgetFactory.ObjectiveWidgets objectives = ObjectiveWidgetFactory.create(skin, textures);

        TextButton pauseButton = new TextButton("Pause", skin, "brown");
        pauseButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.togglePauseMenu();
            }
        });

        Table topRow = new Table();
        topRow.add(seedBankHud).left().expandX();
        topRow.add(objectives.topBarWidget()).center().expandX();
        topRow.add(debugTools).right().padRight(15f);
        topRow.add(pauseButton).right().top().pad(15f);

        Table bottomRow = new Table();
        bottomRow.add(waveProgressBar).bottom().right().expandX().padRight(20f).padBottom(15f);
        bottomRow.add(shovelWidget).bottom().right().padRight(25f).padBottom(20f);

        Stack lawnStack = new Stack();
        lawnStack.add(objectives.lawnOverlayWidget());
        lawnStack.add(lawnWidget);
        lawnStack.add(hoverCursor);

        Table middleRow = new Table();
        middleRow.add(conveyorBeltWidget).top().padTop(10f).padLeft(10f);
        middleRow.add(lawnStack).grow();

        Table mainGameLayer = new Table();
        mainGameLayer.add(topRow).growX().top().row();
        mainGameLayer.add(middleRow).grow().row();
        mainGameLayer.add(bottomRow).growX().bottom();

        PauseMenuOverlay pauseOverlay = new PauseMenuOverlay(skin, controller);
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
}