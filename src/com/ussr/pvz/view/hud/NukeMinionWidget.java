package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.notification.NotificationCenter;
import pvz.libpvz.textures.TextureBank;

import java.util.List;

/**
 * Cheat button — kills every zombie on the lawn except Zomboss variants.
 * Sits at the bottom-left of the HUD alongside ResetTerrainWidget.
 */
public class NukeMinionWidget extends Stack {

    private static final String REGION_ICON = "IMAGE_UI_THUMBS_PINATA_TINY_PRIZE_PINATA_FIRE_TINY";
    private static final Color FLASH_COLOR  = new Color(1f, 0.4f, 0.1f, 1f);
    private static final float FLASH_DURATION = 0.18f;

    private final Image iconImage;
    private float flashTimer = 0f;

    public NukeMinionWidget(Skin skin, TextureBank textures) {
        setSize(64f, 64f);
        setTouchable(Touchable.enabled);

        // Background — same brown button used by Shovel / PlantFood
        Image bg = new Image(textures.region("IMAGE_UI_GENERIC_BROWNBUTTON"));
        bg.setScaling(Scaling.fit);
        bg.setTouchable(Touchable.disabled);
        add(bg);

        // Icon
        var region = textures.region(REGION_ICON);
        iconImage = region != null
                ? new Image(new TextureRegionDrawable(region))
                : new Image(skin.getDrawable("image_ui_generic_brownbutton_10"));
        iconImage.setScaling(Scaling.fit);
        iconImage.setTouchable(Touchable.disabled);
        add(iconImage);

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                executeNuke();
            }
        });
    }

    private void executeNuke() {
        GameSession session = App.getGameSession();
        if (session == null || session.isGameOver()) return;

        List<Zombie> killed = session.getZombies().stream()
                .filter(GameEntity::isAlive)
                .filter(z -> !z.getAlias().toLowerCase().contains("zomboss")).toList();
        killed.forEach(z -> {
            z.takeDamage(z.getHp());
        });

        if (!killed.isEmpty()) {
            NotificationCenter.success("Nuke released — " + killed.size() + " zombie(s) eliminated!");
            flashTimer = FLASH_DURATION;
        } else {
            NotificationCenter.info("No minions to nuke.");
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (flashTimer > 0f) {
            flashTimer -= delta;
            iconImage.setColor(flashTimer > 0f ? FLASH_COLOR : Color.WHITE);
        }
    }
}