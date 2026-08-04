package com.ussr.pvz.view.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.ussr.pvz.service.ChoosePlantService;
import com.ussr.pvz.service.CollectionService.PlantData;
import pvz.libpvz.textures.TextureBank;

import java.util.HashMap;
import java.util.Map;

/** A responsive horizontal plant card shared by every plant-card screen. */
public class PlantCard extends Table {
    private static final Color TEXT_DARK =
            new Color(0.16f, 0.12f, 0.08f, 1f);
    private static final Color TEXT_MUTED =
            new Color(0.30f, 0.25f, 0.19f, 1f);
    private static final Color TEXT_SUCCESS =
            new Color(0.18f, 0.42f, 0.12f, 1f);
    private static final Color TEXT_ERROR =
            new Color(0.55f, 0.12f, 0.10f, 1f);

    private static final Map<String, String> PACKET_KEY_OVERRIDES =
            new HashMap<>();

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

    private final PlantData plant;
    private final Image selectionHighlight;
    private boolean selected;

    public PlantCard(
            PlantData plant,
            Skin skin,
            TextureBank textures,
            Runnable onClick
    ) {
        this.plant = plant;
        setTouchable(Touchable.enabled);
        pad(5f);

        if (skin.has(
                "image_ui_dialog_asset_inner_bkgd_10",
                Drawable.class
        )) {
            setBackground(skin.getDrawable(
                    "image_ui_dialog_asset_inner_bkgd_10"
            ));
        }

        Stack layers = new Stack();
        Table body = new Table();
        body.pad(4f);

        body.add(buildPreview(textures))
                .width(Value.percentWidth(0.40f, body))
                .growY()
                .padRight(6f);

        body.add(buildInformation(skin, textures))
                .grow()
                .left();

        layers.add(body);

        selectionHighlight = buildSelectionHighlight(textures);
        layers.add(selectionHighlight);

        add(layers).grow();

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onClick != null) {
                    onClick.run();
                }
            }
        });
    }

    private Stack buildPreview(TextureBank textures) {
        String plantKey = resolvePacketKey(plant.name);
        Stack preview = new Stack();

        String backgroundName = plant.isBoosted
                ? "IMAGE_UI_PACKETS_BOOST"
                : "IMAGE_UI_PACKETS_EGYPT";

        TextureRegion backgroundRegion = textures.region(backgroundName);
        if (backgroundRegion != null) {
            Image background = new Image(backgroundRegion);
            background.setScaling(Scaling.fit);
            if (plant.level == 0) {
                background.setColor(Color.DARK_GRAY);
            }
            preview.add(background);
        }

        TextureRegion packetRegion = textures.region(
                "IMAGE_UI_PACKETS_" + plantKey
        );
        if (packetRegion != null) {
            Image plantImage = new Image(packetRegion);
            plantImage.setScaling(Scaling.fit);
            if (plant.level == 0) {
                plantImage.setColor(Color.GRAY);
            }

            Container<Image> imageContainer =
                    new Container<>(plantImage);
            imageContainer.center().pad(3f);
            preview.add(imageContainer);
        }

        return preview;
    }

    private Table buildInformation(Skin skin, TextureBank textures) {
        Table information = new Table();
        information.left().top();

        Label name = new Label(plant.name, skin, "default");
        name.setFontScale(0.68f);
        name.setAlignment(Align.left);
        name.setEllipsis("...");
        name.setColor(TEXT_DARK);
        information.add(name).growX().left().row();

        Label status = new Label(
                plant.level > 0 ? "LEVEL " + plant.level : "LOCKED",
                skin,
                "default"
        );
        status.setFontScale(0.58f);
        status.setColor(
                plant.level > 0
                        ? TEXT_SUCCESS
                        : TEXT_ERROR
        );
        information.add(status).growX().left().padTop(2f).row();

        information.add().growY().row();
        information.add(buildCostRow(skin, textures))
                .growX()
                .left()
                .row();

        if (plant.level > 0 && plant.level < 4) {
            int requiredPackets = plant.level * 10;
            Label packets = new Label(
                    plant.ownedPackets + "/" + requiredPackets
                            + " packets",
                    skin,
                    "default"
            );
            packets.setFontScale(0.50f);
            packets.setColor(
                    plant.ownedPackets >= requiredPackets
                            ? TEXT_SUCCESS
                            : TEXT_MUTED
            );
            information.add(packets)
                    .growX()
                    .left()
                    .padTop(1f)
                    .row();
        }

        return information;
    }

    private Table buildCostRow(Skin skin, TextureBank textures) {
        Table costRow = new Table();
        costRow.left();

        TextureRegion sunRegion = textures.region(
                "IMAGE_UI_ALMANAC_STAT_ICON_SUNCOST_LAYER_1"
        );
        if (sunRegion != null) {
            costRow.add(new Image(sunRegion))
                    .size(18f, 18f)
                    .padRight(3f);
        }

        Label cost = new Label(String.valueOf(plant.cost), skin, "default");
        cost.setFontScale(0.72f);
        cost.setColor(TEXT_DARK);
        costRow.add(cost);
        return costRow;
    }

    private Image buildSelectionHighlight(TextureBank textures) {
        Image highlight = new Image();
        TextureRegion region = textures.region(
                "IMAGE_UI_CARDS_CARD_TABLE_FRAME"
        );
        if (region != null) {
            highlight.setDrawable(new TextureRegionDrawable(region));
            highlight.setScaling(Scaling.stretch);
            highlight.setColor(new Color(0.5f, 1f, 0.5f, 1f));
        }
        highlight.setTouchable(Touchable.disabled);
        highlight.setVisible(false);
        return highlight;
    }

    private static String resolvePacketKey(String plantName) {
        String upper = plantName.toUpperCase().trim();
        return PACKET_KEY_OVERRIDES.getOrDefault(
                upper,
                ChoosePlantService.getCleanedUppercaseName(plantName)
        );
    }

    public void toggleSelection() {
        setSelectionVisible(!selected);
    }

    public void setSelectionVisible(boolean visible) {
        selected = visible;
        selectionHighlight.setVisible(visible);
    }

    public boolean isSelected() {
        return selected;
    }

    public PlantData getPlant() {
        return plant;
    }
}
