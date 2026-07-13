package com.ussr.pvz.model.level.chaptereffect;

import java.util.HashMap;
import java.util.Map;

public final class ChapterEffectRegistry {

    private static final Map<String, ChapterEffect> EFFECTS = new HashMap<>();

    static {
        EFFECTS.put("ancient_egypt", new AncientEgyptEffect());
        EFFECTS.put("big_wave_beach", new BigWaveBeachEffect());
        EFFECTS.put("dark_ages", new DarkAgesEffect());
        EFFECTS.put("frostbite_caves", new FrostbiteCavesEffect());
    }

    private ChapterEffectRegistry() {
    }

    public static ChapterEffect get(String chapterId) {
        return chapterId == null ? null : EFFECTS.get(chapterId);
    }
}