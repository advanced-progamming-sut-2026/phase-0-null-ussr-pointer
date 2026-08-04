package com.ussr.pvz.view.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.ussr.pvz.service.ChoosePlantService;
import com.ussr.pvz.service.CollectionService.PlantData;
import pvz.libpvz.textures.TextureBank;

import java.util.HashMap;
import java.util.Map;

public class PlantCard extends Table {

    private static final Map<String, String> PACKET_KEY_OVERRIDES = new HashMap<>();

    static {
        PACKET_KEY_OVERRIDES.put("ROTOBAGA", "ROTORUTABAGA");
        PACKET_KEY_OVERRIDES.put("GOO PEASHOOTER", "POISONPEASHOOTER");
        PACKET_KEY_OVERRIDES.put("MEGA GATLING PEA", "MEGAGATLING");
        PACKET_KEY_OVERRIDES.put("CHERRY BOMB", "CHERRY_BOMB");
        PACKET_KEY_OVERRIDES.put("ICEBERG LETTUCE", "ICEBURG");
        PACKET_KEY_OVERRIDES.put("CAT-TAIL", "ELECTRICPEEL");
        PACKET_KEY_OVERRIDES.put("PIERCE-MINT", "FILAMINT");
        PACKET_KEY_OVERRIDES.put("CATTAIL-MINT", "CONCEALMINT");
    }

    private static String resolvePacketKey(String plantName) {
        String upper = plantName.toUpperCase().trim();
        return PACKET_KEY_OVERRIDES.getOrDefault(upper,
                ChoosePlantService.getCleanedUppercaseName(plantName));
    }

    private final PlantData plant;
    private final Image selectionHighlight;
    private boolean isSelected = false;

    public PlantCard(PlantData plant, Skin skin, TextureBank textures, Runnable onClick) {
        this.plant = plant;
        setTouchable(Touchable.enabled);

        String plantKey = resolvePacketKey(plant.name); // ← uses override map

        Stack stack = new Stack();

        // ── 1. Background ────────────────────────────────────────────────
        String bgName = plant.isBoosted ? "IMAGE_UI_PACKETS_BOOST" : "IMAGE_UI_PACKETS_EGYPT";
        TextureRegion bgReg = textures.region(bgName);
        if (bgReg != null) {
            Image bg = new Image(new TextureRegionDrawable(bgReg));
            bg.setScaling(Scaling.stretch);
            stack.add(bg);
        }

        if (plant.level == 0) this.setColor(Color.DARK_GRAY);

        // ── 2. Plant packet image ─────────────────────────────────────────
        TextureRegion packetReg = textures.region("IMAGE_UI_PACKETS_" + plantKey);
        if (packetReg != null) {
            Image plantImg = new Image(new TextureRegionDrawable(packetReg));
            plantImg.setScaling(Scaling.fit);
            Container<Image> plantContainer = new Container<>(plantImg);
            plantContainer.center().pad(6);
            stack.add(plantContainer);
        }

        // ── 3. Overlay ───────────────────────────────────────────────────
        stack.add(buildOverlay(skin, textures));

        // ── 4. Selection highlight ───────────────────────────────────────
        selectionHighlight = buildSelectionHighlight(textures);
        stack.add(selectionHighlight);

        this.add(stack).expand().fill();

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onClick != null) onClick.run();
            }
        });
    }

    private Table buildOverlay(Skin skin, TextureBank textures) {
        Table overlay = new Table();

        Table topRow = new Table();
        topRow.add().expandX();
        Label levelLbl = new Label("LVL " + plant.level, skin, "default");
        levelLbl.setFontScale(0.65f);
        levelLbl.setColor(Color.WHITE);
        topRow.add(levelLbl).padTop(5).padRight(5);
        overlay.add(topRow).expandX().fillX().top().row();

        overlay.add().expand().fill().row();

        Table bottomRow = new Table();
        TextureRegion sunReg = textures.region("IMAGE_UI_ALMANAC_STAT_ICON_SUNCOST_LAYER_1");
        if (sunReg != null)
            bottomRow.add(new Image(new TextureRegionDrawable(sunReg))).size(26, 26).padLeft(5).padBottom(6);
        Label costLbl = new Label(String.valueOf(plant.cost), skin, "default");
        costLbl.setFontScale(1.1f);
        costLbl.setColor(Color.WHITE);
        bottomRow.add(costLbl).padLeft(3).padBottom(6);
        bottomRow.add().expandX();
        overlay.add(bottomRow).expandX().fillX().bottom().row();

        return overlay;
    }

    private Image buildSelectionHighlight(TextureBank textures) {
        Image highlight = new Image();
        TextureRegion reg = textures.region("IMAGE_UI_CARDS_CARD_TABLE_FRAME");
        if (reg != null) {
            highlight.setDrawable(new TextureRegionDrawable(reg));
            highlight.setScaling(Scaling.stretch);
            highlight.setColor(new Color(0.5f, 1f, 0.5f, 1f));
        }
        highlight.setVisible(false);
        return highlight;
    }

    public void toggleSelection() {
        isSelected = !isSelected;
        selectionHighlight.setVisible(isSelected);
    }

    public void setSelectionVisible(boolean v) {
        isSelected = v;
        selectionHighlight.setVisible(v);
    }

    public boolean isSelected() {
        return isSelected;
    }

    public PlantData getPlant() {
        return plant;
    }
}