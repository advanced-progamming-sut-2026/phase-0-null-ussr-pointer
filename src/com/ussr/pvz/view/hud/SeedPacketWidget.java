package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
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
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.view.components.PlantCard;
import pvz.libpvz.textures.TextureBank;

public class SeedPacketWidget extends Stack {
    private static final Color AFFORDABLE = Color.WHITE;
    private static final Color UNAFFORDABLE = new Color(0.5f, 0.5f, 0.5f, 1f);
    private static final Color IMITATED = new Color(0.45f, 0.45f, 0.45f, 1f);

    private final Plant blueprint;
    private final Image portraitBackground;
    private final Image portraitIcon;
    private final Label costLabel;
    private final CooldownOverlay cooldownOverlay;
    private final Actor selectionFrame;
    private final boolean availabilityRestricted;

    private boolean affordable = true;
    private boolean selected = false;

    public SeedPacketWidget(Plant blueprint, Skin skin, TextureBank textures, Runnable onClick) {
        this(blueprint, skin, textures, onClick, true);
    }

    public SeedPacketWidget(Plant blueprint, Skin skin, TextureBank textures,
                            Runnable onClick, boolean availabilityRestricted) {
        this.blueprint = blueprint;
        this.availabilityRestricted = availabilityRestricted;
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

        Drawable goldPixel = skin.has("white-pixel", Drawable.class)
                ? skin.newDrawable("white-pixel", new Color(1f, 0.72f, 0.08f, 1f))
                : null;
        selectionFrame = new SelectionFrame(goldPixel);
        selectionFrame.setTouchable(Touchable.disabled);
        selectionFrame.setVisible(false);
        add(selectionFrame);

        Table costLayer = new Table();
        costLayer.setTouchable(Touchable.disabled);
        costLayer.bottom().left();
        costLabel = new Label(String.valueOf(blueprint.getCost()), skin, "default");
        costLabel.setFontScale(0.62f);
        costLabel.setAlignment(Align.left);
        costLabel.setTouchable(Touchable.disabled);
        costLayer.add(costLabel).pad(2f);
        costLayer.setVisible(availabilityRestricted);
        add(costLayer);
        cooldownOverlay.setVisible(availabilityRestricted);

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (availabilityRestricted && !isUsable()) return;
                if (onClick != null) onClick.run();
            }
        });
    }

    public void refresh(int currentSun) {
        double maxRecharge = blueprint.getMaxRecharge();
        float fraction = maxRecharge > 0 ? (float) (blueprint.getRecharge() / maxRecharge) : 0f;
        cooldownOverlay.setProgress(fraction);

        affordable = currentSun >= blueprint.getCost();
        boolean imitated = App.getGameSession() != null
                && App.getGameSession().isPlantTypeImitated(blueprint.getName());
        Color tint = imitated ? IMITATED : (affordable ? AFFORDABLE : UNAFFORDABLE);
        portraitBackground.setColor(tint);
        portraitIcon.setColor(tint);
        costLabel.setColor(affordable ? Color.WHITE : new Color(0.85f, 0.35f, 0.3f, 1f));
    }

    public Image createDragIcon(TextureBank textures) {
        String packetKey = PlantCard.resolvePacketKey(blueprint.getName());
        TextureRegion iconRegion = textures.region("IMAGE_UI_PACKETS_" + packetKey);
        Image icon = iconRegion != null ? new Image(iconRegion) : new Image();
        icon.setScaling(Scaling.fit);
        icon.setSize(64f, 82f);
        icon.getColor().a = 0.85f;
        return icon;
    }

    public boolean isUsable() {
        return affordable && blueprint.getRecharge() <= 0;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        // This widget is positioned by SeedBankHud's Table. Changing Y here
        // overrides the table layout and makes vertically stacked packets
        // collapse onto one another after the first selection.
        setOrigin(Align.center);
        setScale(selected ? 1.05f : 1f);
        selectionFrame.setVisible(selected);
    }

    public boolean isSelected() {
        return selected;
    }

    public Plant getBlueprint() {
        return blueprint;
    }

    private static final class SelectionFrame extends Actor {
        private static final float BORDER_WIDTH = 5f;
        private final Drawable pixel;

        private SelectionFrame(Drawable pixel) {
            this.pixel = pixel;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (pixel == null) return;

            float x = getX();
            float y = getY();
            float width = getWidth();
            float height = getHeight();

            pixel.draw(batch, x, y, width, BORDER_WIDTH);
            pixel.draw(batch, x, y + height - BORDER_WIDTH, width, BORDER_WIDTH);
            pixel.draw(batch, x, y, BORDER_WIDTH, height);
            pixel.draw(batch, x + width - BORDER_WIDTH, y, BORDER_WIDTH, height);
        }
    }
}