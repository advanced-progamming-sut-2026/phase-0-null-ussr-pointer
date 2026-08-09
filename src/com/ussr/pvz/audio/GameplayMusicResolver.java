package com.ussr.pvz.audio;

/** Resolves chapter-specific music without leaking chapter rules into views. */
public final class GameplayMusicResolver {
    private GameplayMusicResolver() {
    }

    public static Selection resolve(String chapterId, GameplayMusicCue cue) {
        if (chapterId == null || cue == null) {
            return null;
        }

        return switch (chapterId) {
            case "ancient_egypt" -> ancientEgypt(cue);
            case "frostbite_caves" -> frostbiteCaves(cue);
            case "dark_ages" -> darkAges(cue);
            case "big_wave_beach" -> bigWaveBeach(cue);
            default -> frontLawn(cue);
        };
    }

    private static Selection ancientEgypt(GameplayMusicCue cue) {
        return single(switch (cue) {
            case CHOOSE -> MusicTrack.ANCIENT_EGYPT_CHOOSE;
            case INTRO -> MusicTrack.ANCIENT_EGYPT_INTRO;
            case FIRST_WAVE -> MusicTrack.ANCIENT_EGYPT_WAVE_1;
            case MID_WAVE_A -> MusicTrack.ANCIENT_EGYPT_MID_WAVE_A;
            case MID_WAVE_B -> MusicTrack.ANCIENT_EGYPT_MID_WAVE_B;
            case FINAL_WAVE -> MusicTrack.ANCIENT_EGYPT_FINAL_WAVE;
            case VICTORY -> MusicTrack.ANCIENT_EGYPT_VICTORY;
            case DEFEAT -> MusicTrack.ANCIENT_EGYPT_DEFEAT;
            case REWARD -> MusicTrack.ANCIENT_EGYPT_REWARD;
        });
    }

    private static Selection frostbiteCaves(GameplayMusicCue cue) {
        return switch (cue) {
            case FIRST_WAVE -> sequence(
                    MusicTrack.FROSTBITE_CAVES_WAVE_1_START,
                    MusicTrack.FROSTBITE_CAVES_WAVE_1
            );
            case CHOOSE -> single(MusicTrack.FROSTBITE_CAVES_CHOOSE_SEED);
            case INTRO -> single(MusicTrack.FROSTBITE_CAVES_INTRO);
            case MID_WAVE_A -> single(MusicTrack.FROSTBITE_CAVES_THEME);
            case MID_WAVE_B, FINAL_WAVE ->
                    single(MusicTrack.FROSTBITE_CAVES_WINNING);
            case VICTORY -> single(MusicTrack.FROSTBITE_CAVES_VICTORY);
            case DEFEAT -> single(MusicTrack.FROSTBITE_CAVES_LOSING);
            case REWARD -> single(MusicTrack.REWARD_SCREEN);
        };
    }

    private static Selection darkAges(GameplayMusicCue cue) {
        return switch (cue) {
            case MID_WAVE_A -> sequence(
                    MusicTrack.DARK_AGES_MID_WAVE_A_INTRO,
                    MusicTrack.DARK_AGES_MID_WAVE_A
            );
            case MID_WAVE_B, FINAL_WAVE -> sequence(
                    MusicTrack.DARK_AGES_MID_WAVE_B_INTRO,
                    MusicTrack.DARK_AGES_MID_WAVE_B
            );
            case CHOOSE -> single(MusicTrack.DARK_AGES_CHOOSE);
            case INTRO -> single(MusicTrack.DARK_AGES_INTRO);
            case FIRST_WAVE -> single(MusicTrack.DARK_AGES_WAVE_1);
            case VICTORY -> single(MusicTrack.DARK_AGES_VICTORY);
            case DEFEAT -> single(MusicTrack.DARK_AGES_DEFEAT);
            case REWARD -> single(MusicTrack.DARK_AGES_REWARD);
        };
    }

    private static Selection bigWaveBeach(GameplayMusicCue cue) {
        return switch (cue) {
            case CHOOSE -> sequence(
                    MusicTrack.BIG_WAVE_BEACH_CHOOSE_INTRO,
                    MusicTrack.BIG_WAVE_BEACH_CHOOSE_LOOP
            );
            case MID_WAVE_A -> sequence(
                    MusicTrack.BIG_WAVE_BEACH_2A_INTRO,
                    MusicTrack.BIG_WAVE_BEACH_2A_LOOP
            );
            case MID_WAVE_B -> sequence(
                    MusicTrack.BIG_WAVE_BEACH_2B_INTRO,
                    MusicTrack.BIG_WAVE_BEACH_2B_LOOP
            );
            case FINAL_WAVE -> sequence(
                    MusicTrack.BIG_WAVE_BEACH_BOSS_INTRO,
                    MusicTrack.BIG_WAVE_BEACH_2B_LOOP
            );
            case INTRO, FIRST_WAVE ->
                    single(MusicTrack.BIG_WAVE_BEACH_FIRST_WAVE);
            case VICTORY -> single(MusicTrack.BIG_WAVE_BEACH_VICTORY);
            case DEFEAT -> single(MusicTrack.BIG_WAVE_BEACH_DEFEAT);
            case REWARD -> single(MusicTrack.BIG_WAVE_BEACH_REWARD);
        };
    }

    private static Selection frontLawn(GameplayMusicCue cue) {
        return single(switch (cue) {
            case DEFEAT -> MusicTrack.FRONT_LAWN_DEFEAT;
            case VICTORY -> MusicTrack.FROSTBITE_CAVES_VICTORY;
            case REWARD -> MusicTrack.FRONT_LAWN_REWARD;
            default -> MusicTrack.FRONT_LAWN_THEME;
        });
    }

    private static Selection single(MusicTrack track) {
        return new Selection(track, null);
    }

    private static Selection sequence(MusicTrack intro, MusicTrack loop) {
        return new Selection(intro, loop);
    }

    public record Selection(MusicTrack intro, MusicTrack loop) {
        public boolean hasLoop() {
            return loop != null;
        }
    }
}
