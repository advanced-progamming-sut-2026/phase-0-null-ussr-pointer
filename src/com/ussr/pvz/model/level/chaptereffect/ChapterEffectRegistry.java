package com.ussr.pvz.model.level.chaptereffect;

import java.util.HashMap;
import java.util.Map;

public final class ChapterEffectRegistry {

    private static final Map<String, ChapterEffect> EFFECTS = new HashMap<>();

    static {
        EFFECTS.put("frostbite-caves-1", new FrostbiteCavesEffect());
        EFFECTS.put("big-wave-beach-1", new BigWaveBeachEffect());
        EFFECTS.put("dark-ages-1", new DarkAgesEffect());
    }

    private ChapterEffectRegistry() {
    }

    public static ChapterEffect get(String chapterId) {
        return chapterId == null ? null : EFFECTS.get(chapterId);
    }
}