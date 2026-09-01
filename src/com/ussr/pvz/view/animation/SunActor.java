package com.ussr.pvz.view.animation;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.ussr.pvz.model.entities.items.GroundItem;
import com.ussr.pvz.model.entities.items.ItemType;
import com.ussr.pvz.model.entities.items.sun.ProducedSun;
import com.ussr.pvz.model.entities.items.sun.SunDropType;
import com.ussr.pvz.model.entities.items.sun.SunToken;
import pvz.libpvz.pam.ClipRef;
import pvz.libpvz.pam.PamPlayer;

public class SunActor extends Actor {

    public enum SunPhase {
        FALLING,        // SunToken only — dropping from sky
        IDLE,           // on ground, waiting to be collected
        TRANSITIONING,  // radioactive → normal (transition clip then normal idle)
        EXPLODING,      // radioactive collected while falling → attack clip
        COLLECTED       // done, remove me
    }

    private static final float TRANSITION_DURATION = 1.2f; // tweak to match transition clip length
    private static final float EXPLODE_DURATION = 0.8f;// world-unit height sun falls from

    private static final float BASE_SCALE = 0.45f;
    private static final float MIN_SCALE_MULTIPLIER = 0.5f;
    private static final float MAX_SCALE_MULTIPLIER = 1.5f;
    private static final int SCALE_PIVOT_VALUE = 50;

    private final PamPlayer player;
    private final GroundItem item;
    private final float drawScale;

    private SunPhase phase;
    private ClipRef clipRef;
    private float stateTime = 0f;
    private float phaseTimer = 0f;  // counts time inside TRANSITIONING / EXPLODING

    // pam paths resolved once
    private final String pamPath;
    private final boolean isRadioactive;
    private final boolean isSunToken;

    public SunActor(PamPlayer player, GroundItem item) {
        this.player = player;
        this.item = item;

        this.isSunToken = item instanceof SunToken;
        this.isRadioactive = isSunToken
                && ((SunToken) item).getDropType() == SunDropType.RADIOACTIVE;

        // resolve pam path
        if (isSunToken) {
            this.pamPath = ((SunToken) item).getDropType().getPamLocation();
        } else {
            this.pamPath = item.getItemType().getPamLocation(); // ItemType.SUN pam
        }

        // load pam eagerly
        try {
            player.loadSync(pamPath);
        } catch (Exception ignored) {
        }

        this.phase = isSunToken ? SunPhase.FALLING : SunPhase.IDLE;
        this.clipRef = resolveClip(phase);
        this.drawScale = computeDrawScale(item);

        setSize(60f, 60f);
    }

    private static float computeDrawScale(GroundItem item) {
        if (!(item instanceof ProducedSun producedSun)) {
            return BASE_SCALE;
        }
        float ratio = producedSun.getValue() / (float) SCALE_PIVOT_VALUE;
        float multiplier = Math.max(MIN_SCALE_MULTIPLIER, Math.min(MAX_SCALE_MULTIPLIER, ratio));
        return BASE_SCALE * multiplier;
    }

    // ── public API ──────────────────────────────────────────────────────────

    /**
     * Call when the item is collected (from input layer)
     */
    public void onCollected(boolean explode) {
        if (phase == SunPhase.COLLECTED) return;
        if (explode) {
            enterPhase(SunPhase.EXPLODING);
        } else {
            enterPhase(SunPhase.COLLECTED); // normal collect — just vanish
        }
    }

    public boolean isDone() {
        return phase == SunPhase.COLLECTED;
    }

    public float getStateTime() {
        return stateTime;
    }

    // ── Actor overrides ──────────────────────────────────────────────────────

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;

        switch (phase) {
            case FALLING -> {
                if (isSunToken) {
                    SunToken token = (SunToken) item;
                    if (!token.isFalling()) {
                        // just landed
                        if (isRadioactive) {
                            enterPhase(SunPhase.TRANSITIONING);
                        } else {
                            enterPhase(SunPhase.IDLE);
                        }
                    }
                }
            }
            case TRANSITIONING -> {
                phaseTimer += delta;
                if (phaseTimer >= TRANSITION_DURATION) {
                    enterPhase(SunPhase.IDLE); // now shows normal sun idle
                }
            }
            case EXPLODING -> {
                phaseTimer += delta;
                if (phaseTimer >= EXPLODE_DURATION) {
                    enterPhase(SunPhase.COLLECTED);
                }
            }
            case IDLE -> {
                // check if the model says it died (expired or collected externally)
                if (!item.isAlive() || item.isCollected()) {
                    enterPhase(SunPhase.COLLECTED);
                }
            }
            case COLLECTED -> {
            } // nothing
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (phase == SunPhase.COLLECTED) return;
        if (clipRef == null) {
            clipRef = resolveClip(phase);
            if (clipRef == null) return;
        }

        float cx = getX() + getWidth() / 2f;
        float cy = getY() + getHeight() / 2f;

        Matrix4 old = batch.getTransformMatrix().cpy();
        Matrix4 t = batch.getTransformMatrix().cpy();
        t.translate(cx, cy, 0);
        t.scale(drawScale, drawScale, 1f);
        batch.setTransformMatrix(t);

        try {
            player.draw(batch, clipRef, stateTime, 0, 0, true);
        } catch (Exception ignored) {
        }

        batch.setTransformMatrix(old);
    }

    // ── internals ────────────────────────────────────────────────────────────

    private void enterPhase(SunPhase next) {
        this.phase = next;
        this.phaseTimer = 0f;
        this.stateTime = 0f;
        this.clipRef = resolveClip(next);
    }

    private ClipRef resolveClip(SunPhase forPhase) {
        // for TRANSITIONING we want the radioactive→normal transition clip
        // for EXPLODING we want the attack/explode clip
        // for IDLE after radioactive we want normal sun idle (regular pam)
        // for FALLING we want the falling/idle clip of whatever pam

        String path = pamPath;
        String[] candidates;

        switch (forPhase) {
            case FALLING -> candidates = new String[]{"idle", "animation", "main", ""};
            case IDLE -> {
                // after radioactive transition, use normal sun pam for idle
                if (isRadioactive) {
                    path = SunDropType.REGULAR.getPamLocation();
                    try {
                        player.loadSync(path);
                    } catch (Exception ignored) {
                    }
                }
                candidates = new String[]{"idle", "animation", "main", ""};
            }
            case TRANSITIONING -> candidates = new String[]{"transition", "morph", "idle", ""};
            case EXPLODING -> candidates = new String[]{"attack", "explode", "idle", ""};
            default -> candidates = new String[]{"idle", ""};
        }

        for (String c : candidates) {
            try {
                ClipRef ref = player.getClip(path, c);
                if (ref != null) return ref;
            } catch (Exception ignored) {
            }
        }

        // last resort — any clip
        try {
            return player.getClip(path, null);
        } catch (Exception ignored) {
        }
        return null;
    }
}