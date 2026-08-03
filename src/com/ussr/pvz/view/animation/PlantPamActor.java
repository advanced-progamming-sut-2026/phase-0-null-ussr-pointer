package com.ussr.pvz.view.animation;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import pvz.libpvz.pam.PamPlayer;

public class PlantPamActor extends PamActor {
    private final TextureRegion bgRegion;

    public PlantPamActor(PamPlayer player, String pamPath, TextureRegion bgRegion) {
        super(player, pamPath, "idle");
        this.bgRegion = bgRegion;
        this.pamScale = 0.8f; // Preferred scale for overlay previews

        // Adjust the offset so the plant sits nicely on the deck
        this.offsetY = -20f;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // Draw the arcade pirate deck background first
        if (bgRegion != null) {
            batch.draw(bgRegion, getX(), getY(), getWidth(), getHeight());
        }

        // Let the parent PamActor handle the complex matrix scaling and PAM drawing on top
        super.draw(batch, parentAlpha);
    }
}