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

    private static final float DEFAULT_DURATION = 2.0f;
    private static final float RESULT_DURATION = 3.0f;

    private record Announcement(
            String message,
            float duration
    ) {
    }

    private final Queue<Announcement> queue =
            new ArrayDeque<>();

    private final Label messageLabel;
    private final Container<Label> messageContainer;
    private boolean showing;

    public GameEventAnnouncer(
            Skin skin,
            GameSession session
    ) {
        setFillParent(true);
        setTouchable(Touchable.disabled);
        center();

        messageLabel = new Label("", skin, "big_outline");
        messageLabel.setColor(Color.RED);
        messageLabel.setFontScale(1.6f);
        messageLabel.setAlignment(Align.center);
        messageLabel.setWrap(true);

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
        session.getEventBus().subscribe(
                GameEvent.WaveStarted.class,
                this::onWaveStarted
        );

        session.getEventBus().subscribe(
                GameEvent.GameWon.class,
                event -> announce(
                        "LEVEL COMPLETE!",
                        RESULT_DURATION
                )
        );

        session.getEventBus().subscribe(
                GameEvent.GameOver.class,
                event -> announce(
                        "GAME OVER!",
                        RESULT_DURATION
                )
        );

        session.getEventBus().subscribe(
                GameEvent.SandstormTriggered.class,
                event -> announce(
                        "SANDSTORM!",
                        DEFAULT_DURATION
                )
        );

        session.getEventBus().subscribe(
                GameEvent.FreezingWindTriggered.class,
                event -> announce(
                        "FREEZING WINDS!",
                        DEFAULT_DURATION
                )
        );

        session.getEventBus().subscribe(
                GameEvent.SpecialLevelAnnouncement.class,
                event -> announce(
                        event.message(),
                        DEFAULT_DURATION
                )
        );
    }

    private void onWaveStarted(GameEvent.WaveStarted event) {
        if (event.isFinalWave()) {
            announce(
                    "FINAL WAVE!",
                    RESULT_DURATION
            );
            return;
        }

        announce(
                "WAVE " + event.waveNumber(),
                DEFAULT_DURATION
        );
    }

    private void announce(
            String message,
            float duration
    ) {
        if (message == null || message.isBlank()) {
            return;
        }

        queue.add(new Announcement(message, duration));

        if (!showing) {
            showNext();
        }
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
        messageContainer.clearActions();
        messageContainer.setVisible(true);
        messageContainer.setOrigin(Align.center);
        messageContainer.setScale(0.12f);
        messageContainer.getColor().a = 0f;

        messageContainer.addAction(
                Actions.sequence(
                        Actions.parallel(
                                Actions.fadeIn(
                                        0.3f,
                                        Interpolation.fade
                                ),
                                Actions.scaleTo(
                                        1.12f,
                                        1.12f,
                                        0.65f,
                                        Interpolation.exp5Out
                                )
                        ),
                        Actions.scaleTo(
                                1f,
                                1f,
                                0.12f,
                                Interpolation.sineOut
                        ),
                        Actions.delay(next.duration()),
                        Actions.fadeOut(0.35f),
                        Actions.run(() -> {
                            showing = false;
                            showNext();
                        })
                )
        );
    }
}
