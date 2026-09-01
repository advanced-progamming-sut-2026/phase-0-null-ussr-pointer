package com.ussr.pvz.view.gameplay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.ussr.pvz.view.animation.PamActor;

public final class SunProducerPlaceholderActor extends PamActor {
    private static final float RADIUS   = 28f;
    private static final float PULSE    = 1.8f; // cycles per second
    private float time = 0f;

    SunProducerPlaceholderActor() {
        super(null, null, null); // PamActor with null player — no PAM drawn
        setSize(RADIUS * 2f, RADIUS * 2f);
        setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
    }

    @Override
    public void act(float delta) {
        // Don't call super — no PamPlayer to tick
        time += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float pulse = 0.75f + 0.25f * (float) Math.sin(time * PULSE * 2 * Math.PI);

        float cx = getX() + RADIUS;
        float cy = getY() + RADIUS;
        float r  = RADIUS * pulse;

        // Outer glow (semi-transparent amber)
        Color old = batch.getColor().cpy();
        batch.setColor(1f, 0.80f, 0.10f, 0.40f * parentAlpha);
        batch.draw(com.ussr.pvz.view.util.WhitePixel.get(),
                cx - r - 6f, cy - r - 6f, (r + 6f) * 2f, (r + 6f) * 2f);

        // Core (opaque yellow)
        batch.setColor(1f, 0.92f, 0.15f, 0.95f * parentAlpha);
        batch.draw(com.ussr.pvz.view.util.WhitePixel.get(),
                cx - r, cy - r, r * 2f, r * 2f);

        // Sun symbol "☀" label — drawn as a tiny text actor if available,
        // but since we have no font here, just draw a small dark cross.
        batch.setColor(0.6f, 0.45f, 0f, 0.85f * parentAlpha);
        float arm = r * 0.35f;
        batch.draw(com.ussr.pvz.view.util.WhitePixel.get(),
                cx - arm * 0.2f, cy - arm, arm * 0.4f, arm * 2f);
        batch.draw(com.ussr.pvz.view.util.WhitePixel.get(),
                cx - arm, cy - arm * 0.2f, arm * 2f, arm * 0.4f);

        batch.setColor(old);
    }

    // PamActor stubs — nothing to do
    @Override public void setClip(String clip) {}
    @Override public void resetAnimation() {}
    @Override public boolean isPlaying() { return true; }
}
