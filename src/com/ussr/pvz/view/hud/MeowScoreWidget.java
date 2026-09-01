package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.level.GameMode;
import com.ussr.pvz.model.level.behavior.MeowBehavior;
import pvz.libpvz.textures.TextureBank;

/**
 * In-game HUD panel that displays the player's current Meow-mode score
 * and their all-time personal best, pulled live from {@link MeowBehavior}.
 *
 * <p>The widget is self-hiding: it calls {@link #setVisible(boolean) setVisible(false)}
 * whenever the current session is not a Meow session, so it is safe to add it
 * to the HUD unconditionally.
 *
 * <p>Score milestones are visualised by a brief pulse/colour-flash animation
 * triggered through {@link GameEvent.MeowScoreMilestone}.
 */
public class MeowScoreWidget extends Table {

    // ── Thresholds that trigger the coloured flash on the score label ─────────
    private static final int[]   MILESTONE_THRESHOLDS = {500, 1000, 2000, 4000, 8000, 15000, 30000};
    private static final Color[] MILESTONE_COLOURS    = {
            new Color(1f, 0.85f, 0.2f,  1f),   //  500  – golden
            new Color(0.3f, 1f,  0.4f,  1f),   // 1000  – green
            new Color(0.2f, 0.8f, 1f,  1f),    // 2000  – cyan
            new Color(0.9f, 0.3f, 1f,  1f),    // 4000  – purple
            new Color(1f,  0.4f, 0.1f, 1f),    // 8000  – orange
            new Color(1f,  0.2f, 0.2f, 1f),    // 15000 – red
            new Color(1f,  1f,   1f,   1f),    // 30000 – white (legendary)
    };

    // ── UI ────────────────────────────────────────────────────────────────────
    private final Label scoreValueLabel;
    private final Label bestValueLabel;
    private final Stack panelStack;

    // ── State ─────────────────────────────────────────────────────────────────
    private int  lastDisplayedScore = -1;
    private boolean subscribed      = false;

    // =========================================================================
    public MeowScoreWidget(Skin skin, TextureBank textures) {
        setTouchable(Touchable.disabled);
        Label scoreTitleLabel = new Label("SCORE", skin, "default");
        scoreTitleLabel.setFontScale(0.6f);
        scoreTitleLabel.setColor(new Color(1f, 0.9f, 0.5f, 1f));
        scoreTitleLabel.setAlignment(Align.center);

        scoreValueLabel = new Label("0", skin, "big_outline");
        scoreValueLabel.setFontScale(0.9f);
        scoreValueLabel.setColor(Color.WHITE);
        scoreValueLabel.setAlignment(Align.center);

        Label bestTitleLabel = new Label("BEST", skin, "default");
        bestTitleLabel.setFontScale(0.55f);
        bestTitleLabel.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
        bestTitleLabel.setAlignment(Align.center);

        bestValueLabel = new Label("0", skin, "default");
        bestValueLabel.setFontScale(0.65f);
        bestValueLabel.setColor(new Color(1f, 0.85f, 0.2f, 1f));
        bestValueLabel.setAlignment(Align.center);

        Table inner = new Table();
        inner.pad(6f, 10f, 6f, 10f);
        inner.add(scoreTitleLabel).expandX().center().row();
        inner.add(scoreValueLabel).expandX().center().padBottom(2f).row();
        inner.add(bestTitleLabel).expandX().center().row();
        inner.add(bestValueLabel).expandX().center();

        panelStack = new Stack();
        if (textures != null) {
            var bg = textures.region("image_ui_hud_ingame_background_3slice");
            if (bg == null) bg = textures.region("IMAGE_UI_HUD_INGAME_BACKGROUND_3SLICE");
            if (bg != null) {
                Image bgImg = new Image(new TextureRegionDrawable(bg));
                bgImg.setTouchable(Touchable.disabled);
                panelStack.add(bgImg);
            }
        }
        panelStack.add(inner);

        add(panelStack).width(130f).top().padTop(4f);
        setVisible(false);
    }

    // =========================================================================
    // act() — called every frame by the scene2d render loop
    // =========================================================================
    @Override
    public void act(float delta) {
        super.act(delta);

        GameSession session = App.getGameSession();

        // Hide if there's no Meow session
        if (!isMeowSession(session)) {
            setVisible(false);
            subscribed   = false;
            lastDisplayedScore = -1;
            return;
        }

        setVisible(true);

        // Subscribe to milestone events exactly once per session
        if (!subscribed) {
            subscribed = true;
            session.getEventBus().subscribe(
                    GameEvent.MeowScoreMilestone.class,
                    this::onMilestone
            );
        }

        // Pull live score from the behavior
        MeowBehavior behavior = getMeowBehavior(session);
        int current = behavior != null ? behavior.getCurrentScore() : 0;
        int best    = bestScore();

        if (current != lastDisplayedScore) {
            lastDisplayedScore = current;
            scoreValueLabel.setText(formatScore(current));
            bestValueLabel.setText(formatScore(Math.max(current, best)));
        }
    }

    // =========================================================================
    // Milestone animation
    // =========================================================================
    private void onMilestone(GameEvent.MeowScoreMilestone event) {
        int idx = event.milestoneIndex();
        Color flash = (idx >= 0 && idx < MILESTONE_COLOURS.length)
                ? MILESTONE_COLOURS[idx]
                : new Color(1f, 0.85f, 0.2f, 1f);

        scoreValueLabel.clearActions();
        scoreValueLabel.addAction(Actions.sequence(
                Actions.color(flash, 0.15f, Interpolation.fade),
                Actions.parallel(
                        Actions.scaleTo(1.25f, 1.25f, 0.18f, Interpolation.exp5Out),
                        Actions.color(flash, 0.18f)
                ),
                Actions.scaleTo(1f, 1f, 0.25f, Interpolation.sineOut),
                Actions.color(Color.WHITE, 0.5f, Interpolation.fade)
        ));

        panelStack.clearActions();
        panelStack.addAction(Actions.sequence(
                Actions.color(flash, 0.12f, Interpolation.fade),
                Actions.color(Color.WHITE, 0.5f, Interpolation.fade)
        ));
    }

    // =========================================================================
    // Helpers
    // =========================================================================
    private static boolean isMeowSession(GameSession session) {
        if (session == null || session.getLevel() == null) return false;
        var chapter = App.getLevelManager().findChapter(session.getLevel().getChapter());
        return chapter != null && chapter.getGameMode() == GameMode.MEOW;
    }

    private static MeowBehavior getMeowBehavior(GameSession session) {
        if (session.getLevel() == null || session.getLevel().getBehavior() == null) return null;
        var b = session.getLevel().getBehavior();
        return b instanceof MeowBehavior mb ? mb : null;
    }

    private static int bestScore() {
        var account = App.getAccount();
        if (account == null || account.getScoreRecord() == null) return 0;
        return account.getScoreRecord().getScore();
    }

    private static String formatScore(int score) {
        if (score >= 1_000_000) return String.format("%.1fM", score / 1_000_000.0);
        if (score >= 1_000)     return String.format("%.1fK", score / 1_000.0);
        return String.valueOf(score);
    }

    // ── Public threshold table (used by MeowBehavior to know when to fire) ───
    public static int[] getMilestoneThresholds() { return MILESTONE_THRESHOLDS; }
}