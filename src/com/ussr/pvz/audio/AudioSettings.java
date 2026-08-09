package com.ussr.pvz.audio;

public final class AudioSettings {
    private float masterVolume = 1f;
    private float musicVolume = 0.7f;
    private float sfxVolume = 1f;
    private boolean muted;

    public float getMasterVolume() {
        return masterVolume;
    }

    public void setMasterVolume(float volume) {
        masterVolume = clamp(volume);
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float volume) {
        musicVolume = clamp(volume);
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(float volume) {
        sfxVolume = clamp(volume);
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public float effectiveMusicVolume() {
        return muted ? 0f : masterVolume * musicVolume;
    }

    public float effectiveSfxVolume() {
        return muted ? 0f : masterVolume * sfxVolume;
    }

    private static float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
