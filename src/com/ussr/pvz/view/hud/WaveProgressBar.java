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
import com.ussr.pvz.model.level.Level;
import pvz.libpvz.textures.TextureBank;

import java.util.List;

/** Displays how much of the level's total zombie budget has been spawned,
 *  with a flag icon at each wave boundary so the zombie-head tracker
 *  visibly "reaches" each flag as the corresponding wave begins. */
public class WaveProgressBar extends WidgetGroup {

    // ── Texture keys ─────────────────────────────────────────────────────────
    private static final String REGION_BG =
            "IMAGE_UI_GENERIC_XP_PROGRESS_BAR";
    private static final String REGION_FILL =
            "IMAGE_UI_GENERIC_XP_PROGRESS_BAR_FILL_GREEN";
    private static final String REGION_HEAD =
            "IMAGE_UI_HUD_INGAME_PROGRESS_METER_ZOMBIEHEAD";
    private static final String REGION_FLAG =
            "IMAGE_UI_HUD_LOD_PROGRESS_MEGAPINATA_SM";

    // ── Layout constants ─────────────────────────────────────────────────────
    private static final float BAR_X      = 16f;
    private static final float BAR_Y      = 11f;
    private static final float BAR_WIDTH  = 200f;
    private static final float BAR_HEIGHT = 18f;
    private static final float HEAD_SIZE  = 32f;
    private static final float PREF_WIDTH  = BAR_WIDTH + HEAD_SIZE;
    private static final float PREF_HEIGHT = 40f;

    // Flags are sized to match the bar height and centred vertically on it.
    private static final float FLAG_SIZE   = BAR_HEIGHT;
    private static final float FLAG_Y      = BAR_Y + (BAR_HEIGHT - FLAG_SIZE) / 2f;

    // ── Actors ────────────────────────────────────────────────────────────────
    private final ProgressBar progressBar;
    private final Image       zombieHeadTracker;
    private final TextureBank textures;
    private final Skin        skin;

    private Image[]          waveFlags      = new Image[0];
    private List<Level.Wave> lastKnownWaves = null;

    // ─────────────────────────────────────────────────────────────────────────

    public WaveProgressBar(Skin skin, TextureBank textures) {
        this.skin     = skin;
        this.textures = textures;

        // ── Progress bar ─────────────────────────────────────────────────────
        TextureRegion bgRegion   = textures.region(REGION_BG);
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
        // Remaining-wave meter: starts full on the right, drains toward left.
        progressBar.setValue(1f);
        addActor(progressBar);

        // ── Zombie-head tracker ───────────────────────────────────────────────
        TextureRegion headRegion = textures.region(REGION_HEAD);
        zombieHeadTracker = new Image(headRegion != null
                ? new TextureRegionDrawable(headRegion)
                : skin.newDrawable("white-pixel", new Color(0.45f, 0.68f, 0.38f, 1f)));
        zombieHeadTracker.setScaling(Scaling.fit);
        addActor(zombieHeadTracker);

        setSize(PREF_WIDTH, PREF_HEIGHT);
    }

    // ── WidgetGroup overrides ────────────────────────────────────────────────

    @Override
    public void layout() {
        progressBar.setBounds(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT);
        zombieHeadTracker.setSize(HEAD_SIZE, HEAD_SIZE);
        updateHeadPosition(1f - progressBar.getValue());
        layoutFlags();
    }

    @Override public float getPrefWidth()  { return PREF_WIDTH;  }
    @Override public float getPrefHeight() { return PREF_HEIGHT; }

    @Override
    public void act(float delta) {
        super.act(delta);

        GameSession session = App.getGameSession();
        if (session == null || session.getLevel() == null) {
            setVisible(false);
            return;
        }
        setVisible(true);

        List<Level.Wave> waves = session.getLevel().getWaves();
        if (waves != lastKnownWaves) {
            rebuildFlags(waves);
            lastKnownWaves = waves;
        }

        var behavior = session.getLevel().getBehavior();
        float progress = behavior == null || behavior.getAiManager() == null
                ? 0f
                : behavior.getAiManager().getCostProgress(waves);

        progressBar.setValue(1f - progress);
        updateHeadPosition(1f - progressBar.getVisualValue());
    }

    // ── Flag management ──────────────────────────────────────────────────────

    private void rebuildFlags(List<Level.Wave> waves) {
        for (Image flag : waveFlags) flag.remove();

        if (waves == null || waves.size() <= 1) {
            waveFlags = new Image[0];
            return;
        }

        long totalCost = 0L;
        for (Level.Wave wave : waves) totalCost += Math.max(0, wave.cost());

        if (totalCost <= 0L) {
            waveFlags = new Image[0];
            return;
        }

        // One flag per inter-wave boundary (skip the last wave — bar end covers it).
        int flagCount = waves.size() - 1;
        waveFlags = new Image[flagCount];

        TextureRegion flagRegion = textures.region(REGION_FLAG);

        long cumulative = 0L;
        for (int i = 0; i < flagCount; i++) {
            cumulative += Math.max(0, waves.get(i).cost());

            Image flag = new Image(flagRegion != null
                    ? new TextureRegionDrawable(flagRegion)
                    : skin.newDrawable("white-pixel", new Color(1f, 0.85f, 0.1f, 1f)));
            flag.setScaling(Scaling.fit);
            // Store normalised position in the name; layoutFlags() reads it back.
            flag.setName(Float.toString((float) cumulative / (float) totalCost));

            waveFlags[i] = flag;
            // Insert before the head so the head always renders on top.
            addActorBefore(zombieHeadTracker, flag);
        }

        layoutFlags();
    }

    private void layoutFlags() {
        for (Image flag : waveFlags) {
            float t;
            try { t = Float.parseFloat(flag.getName()); }
            catch (NumberFormatException ignored) { continue; }

            float centerX = BAR_X + BAR_WIDTH * t;
            flag.setBounds(centerX - FLAG_SIZE / 2f, FLAG_Y, FLAG_SIZE, FLAG_SIZE);
        }
    }

    // ── Head positioning ─────────────────────────────────────────────────────

    private void updateHeadPosition(float progress) {
        float headX = BAR_X + BAR_WIDTH * (1f - progress) - HEAD_SIZE / 2f;
        float headY = BAR_Y + (BAR_HEIGHT - HEAD_SIZE) / 2f;
        zombieHeadTracker.setPosition(headX, headY);
    }
}