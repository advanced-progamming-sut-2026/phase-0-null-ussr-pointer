package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.view.components.PlantCard;
import pvz.libpvz.textures.TextureBank;

public class SeedPacketWidget extends Stack {
    private static final Color AFFORDABLE = Color.WHITE;
    private static final Color UNAFFORDABLE = new Color(0.5f, 0.5f, 0.5f, 1f);

    private final Plant blueprint;
    private final Image portraitBackground;
    private final Image portraitIcon;
    private final Label costLabel;
    private final CooldownOverlay cooldownOverlay;

    private boolean affordable = true;
    private boolean selected = false;

    public SeedPacketWidget(Plant blueprint, Skin skin, TextureBank textures, Runnable onClick) {
        this.blueprint = blueprint;
        setTouchable(Touchable.enabled);

        String packetKey = PlantCard.resolvePacketKey(blueprint.getName());

        if (skin.has("image_ui_dialog_asset_inner_bkgd_10", Drawable.class)) {
            Image cardBackground = new Image(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));
            cardBackground.setTouchable(Touchable.disabled);
            add(cardBackground);
        }

        Table portraitLayer = new Table();
        portraitLayer.setTouchable(Touchable.disabled);
        TextureRegion bgRegion = textures.region(blueprint.isBuffed() ? "IMAGE_UI_PACKETS_BOOST" : "IMAGE_UI_PACKETS_EGYPT");
        portraitBackground = bgRegion != null ? new Image(bgRegion) : new Image();
        portraitBackground.setScaling(Scaling.fit);
        portraitBackground.setTouchable(Touchable.disabled);
        portraitLayer.add(portraitBackground).grow();
        add(portraitLayer);

        TextureRegion iconRegion = textures.region("IMAGE_UI_PACKETS_" + packetKey);
        portraitIcon = iconRegion != null ? new Image(iconRegion) : new Image();
        portraitIcon.setScaling(Scaling.fit);
        portraitIcon.setTouchable(Touchable.disabled);
        Table iconLayer = new Table();
        iconLayer.setTouchable(Touchable.disabled);
        iconLayer.add(portraitIcon).grow().pad(3f);
        add(iconLayer);

        cooldownOverlay = new CooldownOverlay();
        add(cooldownOverlay);

        Table costLayer = new Table();
        costLayer.setTouchable(Touchable.disabled);
        costLayer.bottom().left();
        costLabel = new Label(String.valueOf(blueprint.getCost()), skin, "default");
        costLabel.setFontScale(0.62f);
        costLabel.setAlignment(Align.left);
        costLabel.setTouchable(Touchable.disabled);
        costLayer.add(costLabel).pad(2f);
        add(costLayer);

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!isUsable()) return;
                if (onClick != null) onClick.run();
            }
        });
    }

    public void refresh(int currentSun) {
        double maxRecharge = blueprint.getMaxRecharge();
        float fraction = maxRecharge > 0 ? (float) (blueprint.getRecharge() / maxRecharge) : 0f;
        cooldownOverlay.setProgress(fraction);

        affordable = currentSun >= blueprint.getCost();
        Color tint = affordable ? AFFORDABLE : UNAFFORDABLE;
        portraitBackground.setColor(tint);
        portraitIcon.setColor(tint);
        costLabel.setColor(affordable ? Color.WHITE : new Color(0.85f, 0.35f, 0.3f, 1f));
    }

    public boolean isUsable() {
        return affordable && blueprint.getRecharge() <= 0;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        setY(selected ? 10f : 0f);
    }

    public boolean isSelected() {
        return selected;
    }

    public Plant getBlueprint() {
        return blueprint;
    }
}