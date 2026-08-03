package com.ussr.pvz.view.mainmenu.greenhouse;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.ussr.pvz.view.animation.PamActor;
import pvz.libpvz.pam.ClipRef;
import pvz.libpvz.pam.PamPlayer;

public class WateringEffectActor extends Actor {

    public static final String WATER_PAM = "768/INITIAL/ZEN_GARDEN/ZENGARDEN_WATER_POURING/ZENGARDEN_WATER_POURING.PAM";

    private final PamPlayer player;
    private ClipRef clipRef;
    private float stateTime = 0f;
    private final float duration = 1.2f;
    private final Runnable onComplete;

    public WateringEffectActor(PamPlayer player, float x, float y, Runnable onComplete) {
        this.player = player;
        this.onComplete = onComplete;
        setPosition(x, y);

        if (player != null) {
            this.clipRef = PamActor.resolveClip(player, WATER_PAM, "idle");
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;
        if (stateTime >= duration) {
            remove();
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (player == null || clipRef == null) return;

        Vector2 stagePos = localToStageCoordinates(new Vector2(40f, 50f));

        Matrix4 oldTransform = batch.getTransformMatrix().cpy();
        Matrix4 transform = batch.getTransformMatrix().cpy();

        transform.translate(stagePos.x, stagePos.y, 0);
        transform.scale(0.4f, 0.4f, 1f);
        batch.setTransformMatrix(transform);

        try {
            player.draw(batch, clipRef, stateTime, 0, 0, true);
        } catch (Exception ignored) {
        }

        batch.setTransformMatrix(oldTransform);
    }
}