package com.ussr.pvz.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import java.util.EnumMap;
import java.util.Map;

/** LibGDX-backed audio service shared by the entire application. */
public final class GdxAudioManager implements AudioManager {
    private static final float MINIMUM_FADE_SECONDS = 0.01f;

    private final AudioSettings settings;
    private final Map<SoundEffect, Sound> sounds =
            new EnumMap<>(SoundEffect.class);

    private Music currentMusic;
    private Music outgoingMusic;
    private MusicTrack currentTrack;
    private float fadeDuration;
    private float fadeElapsed;
    private boolean stopping;
    private boolean paused;
    private boolean resumeCurrent;
    private boolean resumeOutgoing;

    public GdxAudioManager(AudioSettings settings) {
        this.settings = settings;
    }

    @Override
    public void playMusic(MusicTrack track, boolean loop, float fadeSeconds) {
        if (track == null) {
            return;
        }

        if (track == currentTrack && currentMusic != null) {
            currentMusic.setLooping(loop);
            stopping = false;
            return;
        }

        Music nextMusic = Gdx.audio.newMusic(
                Gdx.files.internal(track.getPath())
        );
        nextMusic.setLooping(loop);

        disposeOutgoingMusic();
        outgoingMusic = currentMusic;
        currentMusic = nextMusic;
        currentTrack = track;
        stopping = false;
        fadeDuration = Math.max(MINIMUM_FADE_SECONDS, fadeSeconds);
        fadeElapsed = fadeSeconds <= 0f ? fadeDuration : 0f;

        currentMusic.setVolume(
                fadeSeconds <= 0f ? settings.effectiveMusicVolume() : 0f
        );
        currentMusic.play();

        if (fadeSeconds <= 0f) {
            disposeOutgoingMusic();
        }
    }

    @Override
    public void playMusicSequence(
            MusicTrack intro,
            MusicTrack loop,
            float fadeSeconds
    ) {
        if (intro == null) {
            playMusic(loop, true, fadeSeconds);
            return;
        }
        if (loop == null) {
            playMusic(intro, false, fadeSeconds);
            return;
        }

        playMusic(intro, false, fadeSeconds);
        Music introMusic = currentMusic;
        introMusic.setOnCompletionListener(completed -> {
            if (completed == currentMusic && currentTrack == intro) {
                playMusic(loop, true, 0f);
            }
        });
    }

    @Override
    public void stopMusic(float fadeSeconds) {
        if (currentMusic == null) {
            return;
        }

        disposeOutgoingMusic();
        stopping = true;
        fadeDuration = Math.max(MINIMUM_FADE_SECONDS, fadeSeconds);
        fadeElapsed = fadeSeconds <= 0f ? fadeDuration : 0f;

        if (fadeSeconds <= 0f) {
            disposeCurrentMusic();
        }
    }

    @Override
    public long playSfx(SoundEffect effect) {
        return playSfx(effect, 1f, 1f, 0f);
    }

    @Override
    public long playSfx(
            SoundEffect effect,
            float volume,
            float pitch,
            float pan
    ) {
        if (effect == null) {
            return -1L;
        }

        Sound sound = sounds.computeIfAbsent(
                effect,
                key -> Gdx.audio.newSound(
                        Gdx.files.internal(key.getPath())
                )
        );
        float finalVolume = clamp(volume)
                * settings.effectiveSfxVolume();
        return sound.play(finalVolume, pitch, clampPan(pan));
    }

    @Override
    public void setMusicVolume(float volume) {
        settings.setMusicVolume(volume);
        applyMusicVolumes();
    }

    @Override
    public void setSfxVolume(float volume) {
        settings.setSfxVolume(volume);
    }

    @Override
    public void setMasterVolume(float volume) {
        settings.setMasterVolume(volume);
        applyMusicVolumes();
    }

    @Override
    public float getMasterVolume() {
        return settings.getMasterVolume();
    }

    @Override
    public void setMuted(boolean muted) {
        settings.setMuted(muted);
        applyMusicVolumes();
    }

    @Override
    public void update(float delta) {
        if (paused || currentMusic == null) {
            return;
        }

        fadeElapsed = Math.min(fadeDuration, fadeElapsed + delta);
        applyMusicVolumes();

        if (fadeElapsed < fadeDuration) {
            return;
        }

        disposeOutgoingMusic();
        if (stopping) {
            disposeCurrentMusic();
        }
    }

    @Override
    public void pause() {
        if (paused) {
            return;
        }
        paused = true;

        resumeCurrent = currentMusic != null && currentMusic.isPlaying();
        resumeOutgoing = outgoingMusic != null && outgoingMusic.isPlaying();

        if (currentMusic != null) {
            currentMusic.pause();
        }
        if (outgoingMusic != null) {
            outgoingMusic.pause();
        }
    }

    @Override
    public void resume() {
        if (!paused) {
            return;
        }
        paused = false;

        if (resumeCurrent && currentMusic != null) {
            currentMusic.play();
        }
        if (resumeOutgoing && outgoingMusic != null) {
            outgoingMusic.play();
        }
    }

    @Override
    public void dispose() {
        disposeOutgoingMusic();
        disposeCurrentMusic();
        sounds.values().forEach(Sound::dispose);
        sounds.clear();
    }

    private void applyMusicVolumes() {
        float progress = fadeDuration <= 0f
                ? 1f
                : Math.min(1f, fadeElapsed / fadeDuration);
        float volume = settings.effectiveMusicVolume();

        if (currentMusic != null) {
            currentMusic.setVolume(stopping
                    ? volume * (1f - progress)
                    : volume * progress);
        }
        if (outgoingMusic != null) {
            outgoingMusic.setVolume(volume * (1f - progress));
        }
    }

    private void disposeCurrentMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
        }
        currentTrack = null;
        stopping = false;
    }

    private void disposeOutgoingMusic() {
        if (outgoingMusic != null) {
            outgoingMusic.stop();
            outgoingMusic.dispose();
            outgoingMusic = null;
        }
    }

    private static float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    private static float clampPan(float value) {
        return Math.max(-1f, Math.min(1f, value));
    }
}