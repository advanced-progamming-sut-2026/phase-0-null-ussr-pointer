package com.ussr.pvz.audio;

import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.engine.session.GameSession;

public final class GameplayMusicDirector {
    private final AudioManager audio;
    private final GameSession session;
    private GameplayMusicCue currentCue;

    public GameplayMusicDirector(
            AudioManager audio,
            GameSession session
    ) {
        this.audio = audio;
        this.session = session;

        subscribeToEvents();
        play(GameplayMusicCue.INTRO, false);
    }

    private void subscribeToEvents() {
        session.getEventBus().subscribe(
                GameEvent.WaveStarted.class,
                this::onWaveStarted
        );

        session.getEventBus().subscribe(
                GameEvent.GameWon.class,
                event -> play(GameplayMusicCue.VICTORY, false)
        );

        session.getEventBus().subscribe(
                GameEvent.GameOver.class,
                event -> play(GameplayMusicCue.DEFEAT, false)
        );
    }

    private void onWaveStarted(GameEvent.WaveStarted event) {
        GameplayMusicCue cue;

        if (event.isFinalWave()) {
            cue = GameplayMusicCue.FINAL_WAVE;
        } else if (event.waveNumber() == 1) {
            cue = GameplayMusicCue.FIRST_WAVE;
        } else {
            int totalWaves = session.getLevel().getWaves().size();
            float progress = event.waveNumber() / (float) totalWaves;

            if (progress >= 0.66f) {
                cue = GameplayMusicCue.MID_WAVE_B;
            } else if (progress >= 0.33f) {
                cue = GameplayMusicCue.MID_WAVE_A;
            } else {
                return;
            }
        }

        play(cue, true);
    }

    private void play(GameplayMusicCue cue, boolean loop) {
        if (cue == currentCue) {
            return;
        }

        GameplayMusicResolver.Selection selection = GameplayMusicResolver.resolve(
                session.getLevel().getChapter(),
                cue
        );

        if (selection != null) {
            currentCue = cue;
            if (selection.hasLoop()) {
                audio.playMusicSequence(
                        selection.intro(),
                        selection.loop(),
                        0.6f
                );
            } else {
                audio.playMusic(selection.intro(), loop, 0.6f);
            }
        }
    }
}
