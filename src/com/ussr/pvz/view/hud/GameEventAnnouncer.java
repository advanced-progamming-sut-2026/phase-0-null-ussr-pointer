package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.engine.session.GameSession;

import java.util.ArrayDeque;
import java.util.Queue;

public class GameEventAnnouncer extends Table {

    private static final float DEFAULT_DURATION  = 2.0f;
    private static final float RESULT_DURATION   = 3.0f;
    private static final float MILESTONE_DURATION = 2.5f;
    private static final float BONUS_DURATION = 1.2f;

    private static final Color MEOW_BONUS_COLOUR = new Color(1f, 0.6f, 0.1f, 1f);

    // Milestone label colours – must mirror MeowScoreWidget.MILESTONE_COLOURS
    private static final Color[] MILESTONE_COLOURS = {
            new Color(1f, 0.85f, 0.2f,  1f),   //  500
            new Color(0.3f, 1f,  0.4f,  1f),   // 1000
            new Color(0.2f, 0.8f, 1f,  1f),    // 2000
            new Color(0.9f, 0.3f, 1f,  1f),    // 4000
            new Color(1f,  0.4f, 0.1f, 1f),    // 8000
            new Color(1f,  0.2f, 0.2f, 1f),    // 15000
            new Color(1f,  1f,   1f,   1f),    // 30000
    };

    private static final String[] MILESTONE_LABELS = {
            "NICE!",
            "GREAT!",
            "AMAZING!",
            "AWESOME!!",
            "UNSTOPPABLE!!!",
            "LEGENDARY!!!!",
            "∞ GODLIKE ∞",
    };

    private record Announcement(
            String message,
            float  duration,
            Color  colour
    ) {}

    private final Queue<Announcement> queue = new ArrayDeque<>();

    private final Label     messageLabel;
    private final Container<Label> messageContainer;
    private boolean showing;

    public GameEventAnnouncer(
            Skin        skin,
            GameSession session
    ) {
        setFillParent(true);
        setTouchable(Touchable.disabled);
        center();

        messageLabel = new Label("", skin, "big_outline");
        messageLabel.setFontScale(1.6f);
        messageLabel.setAlignment(Align.center);
        messageLabel.setWrap(false);

        messageContainer = new Container<>(messageLabel);
        messageContainer.setTransform(true);
        messageContainer.setTouchable(Touchable.disabled);
        messageContainer.setVisible(false);

        add(messageContainer)
                .width(900f)
                .center();

        if (session != null) {
            subscribe(session);
        }
    }

    private void subscribe(GameSession session) {
        // ── Standard game events ──────────────────────────────────────────────
        session.getEventBus().subscribe(
                GameEvent.WaveStarted.class,
                this::onWaveStarted
        );

        session.getEventBus().subscribe(
                GameEvent.GameWon.class,
                event -> announce("LEVEL COMPLETE!", RESULT_DURATION, Color.RED)
        );

        session.getEventBus().subscribe(
                GameEvent.GameOver.class,
                event -> announce("GAME OVER!", RESULT_DURATION, Color.RED)
        );

        session.getEventBus().subscribe(
                GameEvent.SandstormTriggered.class,
                event -> announce("SANDSTORM!", DEFAULT_DURATION, Color.RED)
        );

        session.getEventBus().subscribe(
                GameEvent.FreezingWindTriggered.class,
                event -> announce("FREEZING WINDS!", DEFAULT_DURATION, Color.RED)
        );

        session.getEventBus().subscribe(
                GameEvent.SpecialLevelAnnouncement.class,
                event -> announce(event.message(), DEFAULT_DURATION, Color.RED)
        );

        // ── Meow score milestones ─────────────────────────────────────────────
        session.getEventBus().subscribe(
                GameEvent.MeowScoreMilestone.class,
                this::onMeowMilestone
        );

        session.getEventBus().subscribe(
                GameEvent.MeowBonusEarned.class,
                this::onMeowBonus
        );
    }

    // ── Event handlers ────────────────────────────────────────────────────────

    private void onWaveStarted(GameEvent.WaveStarted event) {
        if (event.isFinalWave()) {
            announce("FINAL WAVE!", RESULT_DURATION, Color.RED);
        } else {
            announce("WAVE " + event.waveNumber(), DEFAULT_DURATION, Color.RED);
        }
    }

    private void onMeowMilestone(GameEvent.MeowScoreMilestone event) {
        int idx = event.milestoneIndex();

        String label = (idx >= 0 && idx < MILESTONE_LABELS.length)
                ? MILESTONE_LABELS[idx]
                : "SCORE MILESTONE!";

        Color colour = (idx >= 0 && idx < MILESTONE_COLOURS.length)
                ? MILESTONE_COLOURS[idx]
                : new Color(1f, 0.85f, 0.2f, 1f);

        String scoreText = formatScore(event.threshold());
        announce(label + " " + scoreText + " pts", MILESTONE_DURATION, colour);
    }

    private void onMeowBonus(GameEvent.MeowBonusEarned event) {
        announce(event.label() + " +" + event.points() + " pts", BONUS_DURATION, MEOW_BONUS_COLOUR);
    }

    // ── Announce queue ────────────────────────────────────────────────────────

    private void announce(String message, float duration, Color colour) {
        if (message == null || message.isBlank()) return;
        queue.add(new Announcement(message, duration, colour));
        if (!showing) showNext();
    }

    private void showNext() {
        Announcement next = queue.poll();

        if (next == null) {
            showing = false;
            messageContainer.setVisible(false);
            return;
        }

        showing = true;
        messageLabel.setText(next.message());
        messageLabel.setColor(next.colour());

        messageContainer.clearActions();
        messageContainer.setVisible(true);
        messageContainer.setOrigin(Align.center);
        messageContainer.setScale(0.12f);
        messageContainer.getColor().a = 0f;

        messageContainer.addAction(
                Actions.sequence(
                        Actions.parallel(
                                Actions.fadeIn(0.3f, Interpolation.fade),
                                Actions.scaleTo(1.12f, 1.12f, 0.65f, Interpolation.exp5Out)
                        ),
                        Actions.scaleTo(1f, 1f, 0.12f, Interpolation.sineOut),
                        Actions.delay(next.duration()),
                        Actions.fadeOut(0.35f),
                        Actions.run(() -> {
                            showing = false;
                            showNext();
                        })
                )
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String formatScore(int score) {
        if (score >= 1_000_000) return String.format("%.1fM", score / 1_000_000.0);
        if (score >= 1_000)     return String.format("%.1fK", score / 1_000.0);
        return String.valueOf(score);
    }
}