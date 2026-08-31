package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.level.delivery.ConveyorDeliveryStrategy;
import com.ussr.pvz.model.level.behavior.IZombieBehavior;
import com.ussr.pvz.model.level.behavior.LevelBehavior;
import com.ussr.pvz.model.level.behavior.MultiplayerIZombieBehavior;
import com.ussr.pvz.service.ChoosePlantService;
import pvz.libpvz.textures.TextureBank;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SeedBankHud extends Table {
    // Increased width and decreased height for seed packets
    private static final int SLOT_W = 100;
    private static final int SLOT_H = 95;

    private final Skin skin;
    private final TextureBank textures;

    private final Label sunLabel;
    private final Label plantFoodLabel;
    private final Table seedColumn;

    private final Map<String, SeedPacketWidget> packets = new LinkedHashMap<>();
    private String selectedKey = null;
    private Consumer<String> onPlantSelected;

    private GameSession lastSession;

    public SeedBankHud(Skin skin, TextureBank textures) {
        this.skin = skin;
        this.textures = textures;
        top().left();

        sunLabel = new Label("0", skin, "default");
        plantFoodLabel = new Label("0", skin, "default");

        Table sunCounter = buildCounter(
                sunLabel,
                "IMAGE_UI_ALMANAC_STAT_ICON_SUNCOST_LAYER_1"
        );

        Table plantFoodCounter = buildCounter(
                plantFoodLabel,
                "IMAGE_UI_HUD_INGAME_PLANTFOOD_ICON"
        );

        seedColumn = new Table();
        seedColumn.top().left();

        // Stack counters and seed packets vertically on the left side
        add(sunCounter).width(100f).height(38f).padBottom(4f).left().row();
        add(plantFoodCounter).width(100f).height(38f).padBottom(6f).left().row();
        add(seedColumn).top().left().row();

        setTouchable(Touchable.childrenOnly);
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
            counter.add(new Image(iconRegion)).size(24f).padLeft(6f).padRight(4f);
        }
        counter.add(valueLabel).minWidth(40f).padRight(6f);
        return counter;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        GameSession session = App.getGameSession();

        if (session == null || session.getLevel() == null) {
            setVisible(false);
            setTouchable(Touchable.disabled);
            lastSession = null;
            return;
        }

        boolean conveyorLevel =
                session.getLevel().getDeliveryStrategy()
                        instanceof ConveyorDeliveryStrategy;

        LevelBehavior behavior = session.getLevel().getBehavior();

        boolean offlineIZombie = behavior instanceof IZombieBehavior;

        boolean multiplayerZombie =
                behavior instanceof MultiplayerIZombieBehavior multiplayer
                        && multiplayer.isZombiesPlayer();

        if (conveyorLevel || offlineIZombie || multiplayerZombie) {
            setVisible(false);
            setTouchable(Touchable.disabled);
            clearSelection();
            lastSession = null;
            return;
        }

        setVisible(true);
        setTouchable(Touchable.childrenOnly);

        if (session != lastSession) {
            rebuildSeedRow(session);
            lastSession = session;
        }

        sunLabel.setText(String.valueOf(session.getSunCount()));
        plantFoodLabel.setText(
                String.valueOf(session.getPlantFoodCount())
        );

        int sun = session.getSunCount();

        for (SeedPacketWidget widget : packets.values()) {
            widget.refresh(sun);
        }
    }

    private void rebuildSeedRow(GameSession session) {
        seedColumn.clearChildren();
        packets.clear();
        selectedKey = null;

        if (App.getAccount() == null) return;

        for (String key : session.getSelectedPlants()) {
            Plant blueprint = App.getAccount().getAdventureProgress()
                    .getAccountPlants().stream()
                    .filter(p -> ChoosePlantService.normalizePlantKey(p.getName())
                            .equals(ChoosePlantService.normalizePlantKey(key)))
                    .findFirst()
                    .orElse(null);
            if (blueprint == null) continue;

            // Ensure buffed background triggers if plant was boosted in session
            if (session.isPlantBoosted(key)) {
                blueprint.setBuffed(true);
            }

            SeedPacketWidget widget = new SeedPacketWidget(
                    blueprint,
                    skin,
                    textures,
                    () -> selectPlant(key)
            );
            packets.put(key, widget);
            seedColumn.add(widget).size(SLOT_W, SLOT_H).padBottom(3f).left().row();
        }
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