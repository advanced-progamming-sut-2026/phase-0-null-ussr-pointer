package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.service.ChoosePlantService;
import pvz.libpvz.textures.TextureBank;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SeedBankHud extends Table {
    private static final int SLOT_W = 64;
    private static final int SLOT_H = 82;

    private final Skin skin;
    private final TextureBank textures;
    private final DragAndDrop dragAndDrop;
    private final GameplayController controller;

    private final Label sunLabel;
    private final Label plantFoodLabel;
    private final Table seedRow;

    private final Map<String, SeedPacketWidget> packets = new LinkedHashMap<>();
    private String selectedKey = null;
    private Consumer<String> onPlantSelected;

    private GameSession lastSession;

    public SeedBankHud(Skin skin, TextureBank textures, DragAndDrop dragAndDrop, GameplayController controller) {
        this.skin = skin;
        this.textures = textures;
        this.dragAndDrop = dragAndDrop;
        this.controller = controller;
        top().left();

        sunLabel = new Label("0", skin, "default");
        plantFoodLabel = new Label("0", skin, "default");

        Table sunCounter = buildCounter(
                sunLabel,
                "IMAGE_UI_ALMANAC_STAT_ICON_SUNCOST_LAYER_1"
        );

        // NOTE: no plant-food icon exists yet in the asset pack as far as I
        // could tell — this key is a placeholder. Swap it for the real one;
        // buildCounter() degrades gracefully (icon just omitted) if missing.
        Table plantFoodCounter = buildCounter(
                plantFoodLabel,
                "IMAGE_UI_HUD_INGAME_PLANTFOOD_ICON"
        );

        seedRow = new Table();
        seedRow.left();

        add(sunCounter).height(48f).pad(6f);
        add(plantFoodCounter).height(48f).pad(6f);
        add(seedRow).height(SLOT_H).padLeft(10f).left();

        setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.childrenOnly);
    }

    public void setOnPlantSelected(Consumer<String> callback) {
        this.onPlantSelected = callback;
    }

    public String getSelectedPlantKey() {
        return selectedKey;
    }

    private Table buildCounter(Label valueLabel, String iconKey) {
        Table counter = new Table();
        TextureRegion bg = textures.region("IMAGE_UI_HUD_INGAME_BACKGROUND_3SLICE");
        if (bg != null) counter.setBackground(new TextureRegionDrawable(bg));

        TextureRegion iconRegion = textures.region(iconKey);
        if (iconRegion != null) {
            counter.add(new Image(iconRegion)).size(28f).padLeft(8f).padRight(5f);
        }
        counter.add(valueLabel).minWidth(50f).padRight(10f);
        return counter;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        GameSession session = App.getGameSession();
        if (session == null) {
            setVisible(false);
            return;
        }
        setVisible(true);

        if (session != lastSession) {
            rebuildSeedRow(session);
            lastSession = session;
        }

        sunLabel.setText(String.valueOf(session.getSunCount()));
        plantFoodLabel.setText(String.valueOf(session.getPlantFoodCount()));

        int sun = session.getSunCount();
        for (SeedPacketWidget widget : packets.values()) {
            widget.refresh(sun);
        }
    }

    private void rebuildSeedRow(GameSession session) {
        seedRow.clearChildren();
        packets.clear();
        selectedKey = null;

        if (App.getAccount() == null) return;

        for (String key : session.getSelectedPlants()) {
            Plant blueprint = App.getAccount().getAdventureProgress()
                    .getAccountPlants().stream()
                    .filter(p -> ChoosePlantService.normalizePlantKey(p.getName()).equals(key))
                    .findFirst()
                    .orElse(null);
            if (blueprint == null) continue;

            SeedPacketWidget widget = new SeedPacketWidget(
                    blueprint,
                    skin,
                    textures,
                    () -> selectPlant(key)
            );
            packets.put(key, widget);
            seedRow.add(widget).size(SLOT_W, SLOT_H).pad(2f);
            registerDragSource(widget, key);
        }
    }

    private void registerDragSource(SeedPacketWidget widget, String key) {
        dragAndDrop.addSource(new DragAndDrop.Source(widget) {
            @Override
            public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                if (!widget.isUsable()) return null;
                if (controller.isPaused() || controller.isShovelModeActive() || controller.isPlantFoodModeActive()) {
                    return null;
                }

                DragAndDrop.Payload payload = new DragAndDrop.Payload();
                payload.setObject(key);

                Image dragIcon = widget.createDragIcon(textures);
                payload.setDragActor(dragIcon);
                dragAndDrop.setDragActorPosition(dragIcon.getWidth() / 2f, -dragIcon.getHeight() / 2f);

                return payload;
            }
        });
    }

    private void selectPlant(String key) {
        if (key.equals(selectedKey)) {
            selectedKey = null;
        } else {
            selectedKey = key;
        }
        for (Map.Entry<String, SeedPacketWidget> entry : packets.entrySet()) {
            entry.getValue().setSelected(entry.getKey().equals(selectedKey));
        }
        if (onPlantSelected != null) {
            onPlantSelected.accept(selectedKey);
        }
    }

    public void clearSelection() {
        if (selectedKey == null) {
            return;
        }

        selectedKey = null;

        for (SeedPacketWidget widget : packets.values()) {
            widget.setSelected(false);
        }

        if (onPlantSelected != null) {
            onPlantSelected.accept(null);
        }
    }
}