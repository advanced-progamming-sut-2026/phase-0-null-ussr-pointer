package com.ussr.pvz.audio;

/**
 * Sound effects available to the game.
 *
 * <p>This enum is intentionally empty until real SFX assets are added.</p>
 */
public enum SoundEffect {
    ;

    private final String path;

    SoundEffect(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
