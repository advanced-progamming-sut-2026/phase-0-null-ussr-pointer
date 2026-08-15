package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.level.behavior.TimedWarBehavior;
import pvz.libpvz.textures.TextureBank;

/**
 * HUD widget for TimedWar levels.
 * Shows remaining time (MM:SS) and progress toward the kill/sun target.
 * Hides itself automatically in every other level type.
 */
public class TimedWarHudWidget extends Table {

    private static final Color COLOR_NORMAL  = Color.WHITE;
    private static final Color COLOR_WARNING = new Color(1f, 0.55f, 0.1f, 1f); // orange
    private static final Color COLOR_DANGER  = new Color(1f, 0.18f, 0.18f, 1f); // red
    /** Seconds left at which the timer turns orange. */
    private static final float WARN_THRESHOLD   = 30f;
    /** Seconds left at which the timer turns red. */
    private static final float DANGER_THRESHOLD = 10f;

    private final Label timeLabel;
    private final Label progressLabel;
    private final Label headerLabel;

    public TimedWarHudWidget(Skin skin, TextureBank textures) {
        setTouchable(Touchable.disabled);
        setVisible(false);

        // Optional background — reuse the same panel texture the wave bar uses.
        var bg = textures.region("image_ui_hud_ingame_background_3slice");
        if (bg != null) setBackground(new TextureRegionDrawable(bg));

        pad(6f, 10f, 6f, 10f);
        defaults().center();

        headerLabel   = new Label("TIME WAR", skin, "default");
        timeLabel     = new Label("--:--",    skin, "default");
        progressLabel = new Label("0 / 0",    skin, "default");

        headerLabel.setFontScale(0.65f);
        headerLabel.setColor(new Color(0.85f, 0.85f, 0.85f, 1f));

        timeLabel.setFontScale(1.1f);
        timeLabel.setColor(COLOR_NORMAL);

        progressLabel.setFontScale(0.8f);
        progressLabel.setColor(Color.YELLOW);

        add(headerLabel).row();
        add(timeLabel).padTop(1f).row();
        add(progressLabel).padTop(2f);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        GameSession session = App.getGameSession();
        if (session == null || session.isGameOver()) {
            setVisible(false);
            return;
        }

        Level level = session.getLevel();
        if (level == null || !(level.getBehavior() instanceof TimedWarBehavior behavior)) {
            setVisible(false);
            return;
        }

        setVisible(true);

        // ── Time ─────────────────────────────────────────────────────────────
        double elapsed    = session.getElapsedSeconds();
        int    limitSecs  = level.getTimeLimitSeconds();
        float  remaining  = Math.max(0f, limitSecs - (float) elapsed);

        int minutes = (int) remaining / 60;
        int seconds = (int) remaining % 60;
        timeLabel.setText(String.format("%02d:%02d", minutes, seconds));

        if (remaining <= DANGER_THRESHOLD) {
            timeLabel.setColor(COLOR_DANGER);
        } else if (remaining <= WARN_THRESHOLD) {
            timeLabel.setColor(COLOR_WARNING);
        } else {
            timeLabel.setColor(COLOR_NORMAL);
        }

        // ── Progress ─────────────────────────────────────────────────────────
        int counter = behavior.getCounter();
        int target  = behavior.getTargetValue();

        String unit = switch (behavior.getLimitationType()) {
            case ZOMBIE -> "☠";   // skull for kills
            case SUN    -> "☀";   // sun for sun collected
        };

        progressLabel.setText(unit + " " + counter + " / " + target);

        // Turn green when target is reached
        progressLabel.setColor(counter >= target ? Color.GREEN : Color.YELLOW);
    }

    @Override
    public float getPrefHeight() {
        return isVisible() ? super.getPrefHeight() : 0f;
    }

    @Override
    public float getPrefWidth() {
        return isVisible() ? super.getPrefWidth() : 0f;
    }
}