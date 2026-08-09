package com.ussr.pvz.audio;

public interface AudioManager {
    void playMusic(MusicTrack track, boolean loop, float fadeSeconds);
    void playMusicSequence(MusicTrack intro, MusicTrack loop, float fadeSeconds);
    void stopMusic(float fadeSeconds);

    long playSfx(SoundEffect effect);
    long playSfx(SoundEffect effect, float volume, float pitch, float pan);

    void setMusicVolume(float volume);
    void setSfxVolume(float volume);
    void setMuted(boolean muted);

    void update(float delta);
    void pause();
    void resume();
    void dispose();
}
