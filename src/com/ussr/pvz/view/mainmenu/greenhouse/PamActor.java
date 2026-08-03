package com.ussr.pvz.view.mainmenu.greenhouse;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import pvz.libpvz.pam.ClipRef;
import pvz.libpvz.pam.PamPlayer;

public class PamActor extends Actor {

    protected final PamPlayer player;
    protected final String pamPath;
    protected final String preferredClip;
    protected ClipRef clipRef;
    protected float stateTime = 0f;
    protected boolean playing = true;
    protected float pamScale = 0.4f;
    protected float offsetY = 0f;

    public PamActor(PamPlayer player, String pamPath, String preferredClip) {
        this.player = player;
        this.pamPath = pamPath;
        this.preferredClip = preferredClip;
        setSize(80f, 80f);
        this.clipRef = resolveClip(player, pamPath, preferredClip);
    }

    public static ClipRef resolveClip(PamPlayer player, String pamPath, String preferredClip) {
        if (player == null || pamPath == null) return null;

        try {
            player.loadSync(pamPath);
        } catch (Exception ignored) {
        }

        String[] candidates = {preferredClip, "idle", "animation", "main", "sprout", "grow", "boost", "default", ""};
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

    public void setClip(String clipName) {
        ClipRef ref = resolveClip(player, pamPath, clipName);
        if (ref != null) {
            this.clipRef = ref;
        }
    }

    public void setPamScale(float pamScale) {
        this.pamScale = pamScale;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (playing) {
            stateTime += delta;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (player == null) return;

        if (clipRef == null) {
            clipRef = resolveClip(player, pamPath, preferredClip);
            if (clipRef == null) return;
        }

        float centerX = getX() + getWidth() / 2f;
        float centerY = getY() + getHeight() / 2f + offsetY;

        Matrix4 oldTransform = batch.getTransformMatrix().cpy();
        Matrix4 transform = batch.getTransformMatrix().cpy();

        transform.translate(centerX, centerY, 0);
        transform.scale(pamScale, pamScale, 1f);
        batch.setTransformMatrix(transform);

        try {
            player.draw(batch, clipRef, stateTime, 0, 0, true);
        } catch (Exception ignored) {
        }

        batch.setTransformMatrix(oldTransform);
    }

    public void resetAnimation() {
        this.stateTime = 0f;
        this.playing = true;
    }
}