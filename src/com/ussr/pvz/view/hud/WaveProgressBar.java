package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import pvz.libpvz.textures.TextureBank;

/** Displays how much of the level's total zombie budget has been spawned. */
public class WaveProgressBar extends WidgetGroup {
    private static final String REGION_BG =
            "IMAGE_UI_GENERIC_XP_PROGRESS_BAR";
    private static final String REGION_FILL =
            "IMAGE_UI_GENERIC_XP_PROGRESS_BAR_FILL_GREEN";
    private static final String REGION_HEAD =
            "IMAGE_UI_HUD_INGAME_PROGRESS_METER_ZOMBIEHEAD";

    private static final float BAR_X = 16f;
    private static final float BAR_Y = 11f;
    private static final float BAR_WIDTH = 200f;
    private static final float BAR_HEIGHT = 18f;
    private static final float HEAD_SIZE = 32f;
    private static final float PREF_WIDTH = BAR_WIDTH + HEAD_SIZE;
    private static final float PREF_HEIGHT = 40f;

    private final ProgressBar progressBar;
    private final Image zombieHeadTracker;

    public WaveProgressBar(Skin skin, TextureBank textures) {
        TextureRegion bgRegion = textures.region(REGION_BG);
        TextureRegion fillRegion = textures.region(REGION_FILL);

        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();
        style.background = bgRegion != null
                ? new TextureRegionDrawable(bgRegion)
                : skin.newDrawable("white-pixel", new Color(0.12f, 0.16f, 0.10f, 0.9f));
        style.knobBefore = fillRegion != null
                ? new TextureRegionDrawable(fillRegion)
                : skin.newDrawable("white-pixel", new Color(0.42f, 0.78f, 0.18f, 1f));

        progressBar = new ProgressBar(0f, 1f, 0.001f, false, style);
        progressBar.setAnimateDuration(0.12f);
        // This is a remaining-wave meter: full on the right at the start,
        // then it drains toward the left together with the zombie head.
        progressBar.setValue(1f);
        addActor(progressBar);

        TextureRegion headRegion = textures.region(REGION_HEAD);
        zombieHeadTracker = new Image(headRegion != null
                ? new TextureRegionDrawable(headRegion)
                : skin.newDrawable("white-pixel", new Color(0.45f, 0.68f, 0.38f, 1f)));
        zombieHeadTracker.setScaling(Scaling.fit);
        addActor(zombieHeadTracker);

        setSize(PREF_WIDTH, PREF_HEIGHT);
    }

    @Override
    public void layout() {
        progressBar.setBounds(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT);
        zombieHeadTracker.setSize(HEAD_SIZE, HEAD_SIZE);
        updateHeadPosition(1f - progressBar.getValue());
    }

    @Override
    public float getPrefWidth() {
        return PREF_WIDTH;
    }

    @Override
    public float getPrefHeight() {
        return PREF_HEIGHT;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        GameSession session = App.getGameSession();
        if (session == null || session.getLevel() == null) {
            setVisible(false);
            return;
        }
        setVisible(true);

        var behavior = session.getLevel().getBehavior();
        float progress = behavior == null || behavior.getAiManager() == null
                ? 0f
                : behavior.getAiManager().getCostProgress(session.getLevel().getWaves());

        progressBar.setValue(1f - progress);
        updateHeadPosition(1f - progressBar.getVisualValue());
    }

    private void updateHeadPosition(float progress) {
        // PvZ's head travels from the right end toward the left as waves are spent.
        float headX = BAR_X + BAR_WIDTH * (1f - progress) - HEAD_SIZE / 2f;
        float headY = BAR_Y + (BAR_HEIGHT - HEAD_SIZE) / 2f;
        zombieHeadTracker.setPosition(headX, headY);
    }
}
