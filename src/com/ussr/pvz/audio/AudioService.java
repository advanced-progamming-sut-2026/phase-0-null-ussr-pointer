package com.ussr.pvz.audio;

public final class AudioService {

    private static AudioManager instance;

    private AudioService() {
    }

    public static void set(AudioManager manager) {
        instance = manager;
    }

    public static AudioManager get() {
        return instance;
    }
}