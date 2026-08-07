package com.ussr.pvz.view.animation;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.ussr.pvz.model.entities.items.GroundItem;
import pvz.libpvz.pam.ClipRef;
import pvz.libpvz.pam.PamPlayer;

/**
 * PAM-based actor for collectable ground items: coins, diamonds, plant food, seed packs.
 *
 * Lifecycle:
 *   IDLE  →  (player hovers/presses A)  →  COLLECTED
 *
 * The item renders its PAM idle loop until collected, then the actor signals
 * isDone() so the render layer can remove it.
 *
 * There is no explicit collect animation phase here — the PAM files for these
 * items are simple idle loops. If you later want a "fly to HUD" tween, add a
 * COLLECTING phase and drive it from here.
 */
public class GroundItemActor extends Actor {

    public enum Phase { IDLE, COLLECTED }

    private static final float PAM_SCALE = 0.45f;

    private final PamPlayer player;
    private final GroundItem item;
    private final String pamPath;

    private ClipRef clipRef;
    private float stateTime = 0f;
    private Phase phase;

    public GroundItemActor(PamPlayer player, GroundItem item) {
        this.player  = player;
        this.item    = item;
        this.pamPath = item.getItemType().getPamLocation();

        try {
            player.loadSync(pamPath);
        } catch (Exception ignored) {}

        this.clipRef = resolveClip();
        this.phase   = Phase.IDLE;

        setSize(60f, 60f);
    }

    // ── public API ───────────────────────────────────────────────────────────

    /** Called by ItemRenderLayer when the player collects this item. */
    public void onCollected() {
        if (phase == Phase.COLLECTED) return;
        phase = Phase.COLLECTED;
        item.collect();
    }

    /** True once the actor has finished — remove it from the stage. */
    public boolean isDone() {
        return phase == Phase.COLLECTED;
    }

    public GroundItem getItem() {
        return item;
    }

    // ── Actor overrides ──────────────────────────────────────────────────────

    @Override
    public void act(float delta) {
        super.act(delta);

        if (phase == Phase.IDLE) {
            stateTime += delta;

            // If the model killed/collected the item externally, sync up
            if (!item.isAlive() || item.isCollected()) {
                phase = Phase.COLLECTED;
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (phase == Phase.COLLECTED) return;

        if (clipRef == null) {
            clipRef = resolveClip();
            if (clipRef == null) return;
        }

        float cx = getX() + getWidth()  / 2f;
        float cy = getY() + getHeight() / 2f;

        Matrix4 old = batch.getTransformMatrix().cpy();
        Matrix4 t   = batch.getTransformMatrix().cpy();
        t.translate(cx, cy, 0f);
        t.scale(PAM_SCALE, PAM_SCALE, 1f);
        batch.setTransformMatrix(t);

        try {
            player.draw(batch, clipRef, stateTime, 0f, 0f, true);
        } catch (Exception ignored) {}

        batch.setTransformMatrix(old);
    }

    // ── internals ────────────────────────────────────────────────────────────

    private ClipRef resolveClip() {
        String[] candidates = { "idle", "animation", "main", "loop", "" };
        for (String c : candidates) {
            try {
                ClipRef ref = player.getClip(pamPath, c);
                if (ref != null) return ref;
            } catch (Exception ignored) {}
        }
        // absolute last resort — let the player pick any clip
        try {
            return player.getClip(pamPath, null);
        } catch (Exception ignored) {}
        return null;
    }
}