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
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import pvz.libpvz.textures.TextureBank;

public class PlantFoodWidget extends Stack {

    private static final String REGION_PLANTFOOD =
            "IMAGE_BACKGROUNDS_TILE_PLANTFOOD_TILE_PLANTFOOD_45X46";

    private final Image iconImage;
    private final Label countLabel;
    private boolean isActive = false;

    public PlantFoodWidget(Skin skin, TextureBank textures, GameplayController controller) {
        setSize(70f, 70f);
        setTouchable(Touchable.enabled);

        // Background button
        Image bankImage = new Image(textures.region("IMAGE_UI_GENERIC_BROWNBUTTON"));
        bankImage.setScaling(Scaling.fit);
        bankImage.setTouchable(Touchable.disabled);
        add(bankImage);

        // Plant food icon
        TextureRegion pfRegion = textures != null ? textures.region(REGION_PLANTFOOD) : null;
        if (pfRegion == null && textures != null) {
            pfRegion = textures.region("IMAGE_UI_HUD_INGAME_PLANTFOOD_BUTTON");
        }

        iconImage = pfRegion != null
                ? new Image(new TextureRegionDrawable(pfRegion))
                : new Image(skin.getDrawable("IMAGE_UI_GENERIC_GEM_ICON_SMALL"));
        iconImage.setScaling(Scaling.fit);
        iconImage.setTouchable(Touchable.disabled);
        add(iconImage);

        // Count badge
        Table labelTable = new Table();
        labelTable.setTouchable(Touchable.disabled);
        labelTable.bottom().right();
        countLabel = new Label("0", skin, "default");
        countLabel.setFontScale(0.75f);
        countLabel.setColor(Color.GREEN);
        countLabel.setAlignment(Align.right);
        labelTable.add(countLabel).pad(2f, 0f, 2f, 6f);
        add(labelTable);

        // Click: toggle plant-food mode
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameSession session = App.getGameSession();
                if (session == null || session.getPlantFoodCount() <= 0) return;

                isActive = !isActive;
                controller.togglePlantFoodMode(isActive);
                updateVisuals();
            }
        });

        // Controller calls this when a feed completes (or mode is cancelled)
        // so the widget always reflects reality.
        controller.setOnPlantFoodDeactivated(this::deactivate);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        GameSession session = App.getGameSession();
        if (session != null) {
            int count = session.getPlantFoodCount();
            countLabel.setText(String.valueOf(count));
            // Auto-deactivate if player runs out
            if (count <= 0 && isActive) {
                deactivate();
            }
        }
    }

    // ── called by controller callback or internally ───────────────────────

    public void deactivate() {
        isActive = false;
        updateVisuals();
    }

    // ── visuals ───────────────────────────────────────────────────────────

    private void updateVisuals() {
        if (isActive) {
            // Green tint + slight lift = "selected" look
            iconImage.setColor(0.4f, 1.0f, 0.4f, 1.0f);
            iconImage.setY(8f);
        } else {
            iconImage.setColor(Color.WHITE);
            iconImage.setY(0f);
        }
    }
}