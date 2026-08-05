package com.ussr.pvz.view.animation;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import pvz.libpvz.pam.PamPlayer;

public class PlantPamActor extends PamActor {
    private final TextureRegion bgRegion;

    // Collection Constructor (Defaults to "idle" and uses a background deck)
    public PlantPamActor(PamPlayer player, String pamPath, TextureRegion bgRegion) {
        super(player, pamPath, "idle");
        this.bgRegion = bgRegion;
        this.pamScale = 0.8f;
        this.offsetY = -20f;
    }

    // Gameplay Constructor (No background deck, takes dynamic clips like "plantfood")
    public PlantPamActor(PamPlayer player, String pamPath, String preferredClip) {
        super(player, pamPath, preferredClip);
        this.bgRegion = null;
        this.pamScale = 0.8f;
        this.offsetY = -20f;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (bgRegion != null) {
            batch.draw(bgRegion, getX(), getY(), getWidth(), getHeight());
        }
        super.draw(batch, parentAlpha);
    }
}