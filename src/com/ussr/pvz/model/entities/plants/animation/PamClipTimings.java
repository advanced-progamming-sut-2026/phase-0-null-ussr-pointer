package com.ussr.pvz.model.entities.plants.animation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PamClipTimings {
    private static final Map<String, Float> DURATIONS = new ConcurrentHashMap<>();

    private PamClipTimings() {}

    public static void put(String pamPath, String clipName, float durationSeconds) {
        if (pamPath == null || clipName == null) return;
        DURATIONS.put(key(pamPath, clipName), durationSeconds);
    }

    public static Float get(String pamPath, String clipName) {
        if (pamPath == null || clipName == null) return null;
        return DURATIONS.get(key(pamPath, clipName));
    }

    private static String key(String pamPath, String clipName) {
        return pamPath + "#" + clipName;
    }
}