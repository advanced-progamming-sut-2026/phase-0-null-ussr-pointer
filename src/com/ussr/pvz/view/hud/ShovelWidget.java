package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import pvz.libpvz.textures.TextureBank;

public class ShovelWidget extends Stack {
    private static final String REGION_SHOVEL_BANK = "IMAGE_UI_HUD_INGAME_SHOVEL_BUTTON";
    // TODO: find proper shovel for this
    private static final String REGION_SHOVEL = "IMAGE_UI_HUD_INGAME_SHOVEL_BUTTON";

    private final Image shovelImage;
    private boolean isActive = false;

    public ShovelWidget(Skin skin, TextureBank textures, GameplayController controller) {
        setSize(70f, 70f);
        setTouchable(Touchable.enabled);

        TextureRegion bankRegion = textures != null ? textures.region(REGION_SHOVEL_BANK) : null;
        Image bankImage = bankRegion != null
                ? new Image(new TextureRegionDrawable(bankRegion))
                : new Image(skin.getDrawable("image_ui_generic_brownbutton_10"));
        bankImage.setScaling(Scaling.fit);
        bankImage.setTouchable(Touchable.disabled);
        add(bankImage);

        TextureRegion shovelRegion = textures != null ? textures.region(REGION_SHOVEL) : null;
        if (shovelRegion == null && textures != null) {
            shovelRegion = textures.region("IMAGE_SHOVEL");
        }

        if (shovelRegion != null) {
            shovelImage = new Image(new TextureRegionDrawable(shovelRegion));
        } else if (skin.has("image_ui_generic_brownbutton_10", com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)) {
            shovelImage = new Image(skin.getDrawable("image_ui_generic_brownbutton_10"));
        } else {
            shovelImage = new Image();
        }

        shovelImage.setScaling(Scaling.fit);
        shovelImage.setTouchable(Touchable.disabled);
        add(shovelImage);

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isActive = !isActive;
                controller.toggleShovelMode(isActive);
                updateVisuals();
            }
        });
    }

    private void updateVisuals() {
        if (isActive) {
            shovelImage.setColor(new Color(0.5f, 1.0f, 0.5f, 1.0f));
            shovelImage.setY(10f);
        } else {
            shovelImage.setColor(Color.WHITE);
            shovelImage.setY(0f);
        }
    }

    public void deactivate() {
        if (!isActive) return;
        isActive = false;
        updateVisuals();
    }
}