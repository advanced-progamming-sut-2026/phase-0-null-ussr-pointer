package com.ussr.pvz.view.animation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.ussr.pvz.model.entities.plants.animation.PamClipTimings;
import pvz.libpvz.pam.ClipRef;
import pvz.libpvz.pam.PamPlayer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PamActor extends Actor {

    protected final PamPlayer player;
    protected final String pamPath;
    protected final String preferredClip;
    protected ClipRef clipRef;
    protected float stateTime = 0f;
    protected boolean playing = true;
    protected float pamScale = 0.4f;
    protected float offsetY = 0f;
    protected float offsetX = 0f;
    protected boolean looping = true;
    protected String currentClipName;
    protected Map<String, Boolean> partVisibility;
    protected float rotationDegrees = 0f;
    private boolean greyTint = false;
    private boolean paused = false;
    private static final Color IMITATED_TINT = new Color(0.55f, 0.55f, 0.55f, 1f);

    public void setGreyTint(boolean greyTint) {
        this.greyTint = greyTint;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public PamActor(PamPlayer player, String pamPath, String preferredClip) {
        this.player = player;
        this.pamPath = pamPath;
        this.preferredClip = preferredClip;
        setSize(80f, 80f);
        this.clipRef = resolveClip(player, pamPath, preferredClip);
        this.currentClipName = preferredClip;
    }

    private static final Set<String> WARMED_PAM_PATHS = ConcurrentHashMap.newKeySet();

    public static ClipRef resolveClip(PamPlayer player, String pamPath, String preferredClip) {
        if (player == null || pamPath == null) return null;

        try {
            player.loadSync(pamPath);
        } catch (Exception ignored) {
        }

        warmClipTimings(player, pamPath);

        String[] candidates = {preferredClip, "idle", "almanac_idle", "animation", "main", "sprout", "grow", "boost", "default", ""};
        for (String candidate : candidates) {
            if (candidate == null) continue;
            try {
                ClipRef ref = player.getClip(pamPath, candidate);
                if (ref != null) return ref;
            } catch (Exception ignored) {
            }
        }

        try {
            ClipRef ref = player.getClip(pamPath, null);
            if (ref != null) return ref;
        } catch (Exception ignored) {
        }

        return null;
    }

    private static void warmClipTimings(PamPlayer player, String pamPath) {
        if (WARMED_PAM_PATHS.contains(pamPath)) return;
        try {
            List<String> clips = player.clips(pamPath);
            if (clips == null) return;
            for (String clip : clips) {
                float duration = player.clipDurationSeconds(pamPath, clip);
                PamClipTimings.put(pamPath, clip, duration);
            }
            WARMED_PAM_PATHS.add(pamPath);
        } catch (Exception ignored) {
        }
    }

    public void setClip(String clipName) {
        if (java.util.Objects.equals(
                currentClipName,
                clipName
        )) {
            return;
        }

        ClipRef ref = resolveClip(
                player,
                pamPath,
                clipName
        );

        if (ref == null) {
            return;
        }

        currentClipName = clipName;
        clipRef = ref;
        stateTime = 0f;
        playing = true;
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    public void switchPam(String newPamPath, String clip, boolean oneShot) {
        ClipRef ref = resolveClip(player, newPamPath, clip);
        if (ref == null) return;
        this.clipRef = ref;
        this.currentClipName = clip;
        this.stateTime = 0f;
        this.looping = !oneShot;
        this.playing = true;
    }

    public void setPamScale(float pamScale) {
        this.pamScale = pamScale;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    public void setRotationDegrees(float rotationDegrees) {
        this.rotationDegrees = rotationDegrees;
    }

    public float getRotationDegrees() {
        return rotationDegrees;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (playing && !paused) {
            stateTime += delta;
            if (!looping && clipRef != null) {
                float duration = clipRef.duration; // whatever the actual method is on ClipRef
                if (stateTime >= duration) {
                    stateTime = duration; // clamp, don't reset
                    playing = false;
                }
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (player == null) return;

        if (clipRef == null) {
            clipRef = resolveClip(player, pamPath, preferredClip);
            if (clipRef == null) return;
        }

        float centerX = getX() + getWidth() / 2f + offsetX;
        float centerY = getY() + getHeight() / 2f + offsetY;

        Matrix4 oldTransform = batch.getTransformMatrix().cpy();
        Matrix4 transform = batch.getTransformMatrix().cpy();

        transform.translate(centerX, centerY, 0);
        if (rotationDegrees != 0f) {
            transform.rotate(0f, 0f, 1f, rotationDegrees);
        }
        transform.scale(pamScale, pamScale, 1f);
        batch.setTransformMatrix(transform);

        Color oldColor = greyTint ? batch.getColor().cpy() : null;
        if (greyTint) {
            batch.setColor(IMITATED_TINT.r, IMITATED_TINT.g, IMITATED_TINT.b, oldColor.a);
        }

        try {
            if (partVisibility == null || partVisibility.isEmpty()) {
                player.draw(batch, clipRef, stateTime, 0, 0, looping);
            } else {
                player.draw(batch, clipRef, stateTime, 0, 0, looping, partVisibility);
            }
        } catch (Exception ignored) {
        }

        if (greyTint) {
            batch.setColor(oldColor);
        }

        batch.setTransformMatrix(oldTransform);
    }

    public void resetAnimation() {
        this.stateTime = 0f;
        this.playing = true;
    }
    // In PamActor or ZombiePamActor:
    public boolean isPlaying() {
        return playing;
    }

    public float getAnimationTime() {
        return stateTime;
    }
}