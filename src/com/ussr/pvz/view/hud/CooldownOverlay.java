package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;

public class CooldownOverlay extends Actor {
    private static Texture whitePixel;

    private float progress = 0f;

    public CooldownOverlay() {
        setTouchable(Touchable.disabled);
        if (whitePixel == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            whitePixel = new Texture(pixmap);
            pixmap.dispose();
        }
    }

    public void setProgress(float progress) {
        this.progress = Math.clamp(progress, 0f, 1f);
    }

    public float getProgress() {
        return progress;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (progress <= 0f) return;

        float overlayHeight = getHeight() * progress;
        Color prev = batch.getColor();
        batch.setColor(0.08f, 0.10f, 0.12f, 0.72f * parentAlpha);
        batch.draw(
                new TextureRegion(whitePixel),
                getX(),
                getY(),
                getWidth(),
                overlayHeight
        );

        // A bright moving edge makes the retracting cooldown curtain clear.
        float edgeHeight = Math.min(3f, overlayHeight);
        batch.setColor(0.78f, 0.88f, 0.96f, 0.8f * parentAlpha);
        batch.draw(
                new TextureRegion(whitePixel),
                getX(),
                getY() + overlayHeight - edgeHeight,
                getWidth(),
                edgeHeight
        );
        batch.setColor(prev);
    }
}
