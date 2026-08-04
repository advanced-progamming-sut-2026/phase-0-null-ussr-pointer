package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import pvz.libpvz.textures.TextureBank;

/**
 * Tracks and visualizes the zombie wave progression.
 * Safely calculates progress based on elapsed time to prevent dependency on non-existent wave models.
 */
public class WaveProgressBar extends Stack {
    // TODO: find wave progress bar path
    private static final String REGION_BG = "";
    private static final String REGION_FILL = "";
    private static final String REGION_HEAD = "";

    private final ProgressBar progressBar;
    private final Image zombieHeadTracker;
    private final float barWidth = 200f;

    // Assumes an average level is 180 seconds for generic visual tracking
    private static final float ESTIMATED_LEVEL_DURATION = 180f;

    public WaveProgressBar(Skin skin, TextureBank textures) {
        TextureRegion bgRegion = textures.region(REGION_BG);
        TextureRegion fillRegion = textures.region(REGION_FILL);

        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();
        if (bgRegion != null) style.background = new TextureRegionDrawable(bgRegion);
        if (fillRegion != null) style.knobBefore = new TextureRegionDrawable(fillRegion);

        progressBar = new ProgressBar(0f, 1f, 0.01f, false, style);
        progressBar.setWidth(barWidth);
        progressBar.setValue(0f);
        add(progressBar);

        TextureRegion headRegion = textures.region(REGION_HEAD);
        zombieHeadTracker = headRegion != null ? new Image(new TextureRegionDrawable(headRegion)) : new Image();
        zombieHeadTracker.setScaling(Scaling.fit);
        zombieHeadTracker.setSize(32f, 32f);
        zombieHeadTracker.setOrigin(Align.center);
        add(zombieHeadTracker);
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

        // Interpolate progress manually
        float progress = Math.min(1.0f, (float) (session.getElapsedSeconds() / ESTIMATED_LEVEL_DURATION));
        progressBar.setValue(progress);

        // Head tracker starts on the right, moves to the left
        float headX = barWidth - (barWidth * progress) - (zombieHeadTracker.getWidth() / 2f);
        zombieHeadTracker.setPosition(headX, -5f);
    }
}