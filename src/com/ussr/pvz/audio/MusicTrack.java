package com.ussr.pvz.audio;

/** Every music asset currently available to the game. */
public enum MusicTrack {
    STARTUP("assets/music/StartupMusic.ogg"),
    WORLD_MAP("assets/music/PvZ2WorldMap.ogg"),
    REWARD_SCREEN("assets/music/PvZ2RewardScreen.ogg"),

    FRONT_LAWN_THEME("assets/music/FrontLawnTheme.ogg"),
    FRONT_LAWN_REWARD("assets/music/RewardFrontLawn.ogg"),
    FRONT_LAWN_DEFEAT("assets/music/DefeatFrontLawn.ogg"),

    FROSTBITE_CAVES_THEME("assets/music/FC.ogg"),
    FROSTBITE_CAVES_CHOOSE_SEED("assets/music/FC_Choose_your_seed.ogg"),
    FROSTBITE_CAVES_INTRO("assets/music/FC_Intro.ogg"),
    FROSTBITE_CAVES_WAVE_1_START("assets/music/FC_Wave1(start).ogg"),
    FROSTBITE_CAVES_WAVE_1("assets/music/FC_Wave1.ogg"),
    FROSTBITE_CAVES_WINNING("assets/music/FC_Winning.ogg"),
    FROSTBITE_CAVES_LOSING("assets/music/FC_Losing.ogg"),
    FROSTBITE_CAVES_VICTORY("assets/music/FC_Victory.ogg"),

    ANCIENT_EGYPT_CHOOSE("assets/music/AncientEgyptChoose.ogg"),
    ANCIENT_EGYPT_INTRO("assets/music/AncientEgyptIntro.ogg"),
    ANCIENT_EGYPT_WAVE_1("assets/music/AncientEgyptWave1.ogg"),
    ANCIENT_EGYPT_MID_WAVE_A("assets/music/AncientEgyptMidWaveA.ogg"),
    ANCIENT_EGYPT_MID_WAVE_B("assets/music/AncientEgyptMidWaveB.ogg"),
    ANCIENT_EGYPT_FINAL_WAVE("assets/music/AncientEgyptFinalWave.ogg"),
    ANCIENT_EGYPT_REWARD("assets/music/AncientEgyptReward.ogg"),
    ANCIENT_EGYPT_VICTORY("assets/music/AncientEgyptVictory.ogg"),
    ANCIENT_EGYPT_DEFEAT("assets/music/AncientEgyptDefeat.ogg"),

    DARK_AGES_CHOOSE("assets/music/DarkAgesChoose.ogg"),
    DARK_AGES_INTRO("assets/music/DarkAgesIntro.ogg"),
    DARK_AGES_WAVE_1("assets/music/DarkAgesWave1.ogg"),
    DARK_AGES_MID_WAVE_A_INTRO("assets/music/DarkAgesMidwaveAIntro.ogg"),
    DARK_AGES_MID_WAVE_A("assets/music/DarkAgesMidwaveA.ogg"),
    DARK_AGES_MID_WAVE_B_INTRO("assets/music/DarkAgesMidwaveBIntro.ogg"),
    DARK_AGES_MID_WAVE_B("assets/music/DarkAgesMidwaveB.ogg"),
    DARK_AGES_ZOMBOSS_INTRO("assets/music/DarkAgesZombossIntro.ogg"),
    DARK_AGES_REWARD("assets/music/DarkAgesReward.ogg"),
    DARK_AGES_VICTORY("assets/music/DarkAgesVictory.ogg"),
    DARK_AGES_DEFEAT("assets/music/DarkAgesDefeat.ogg"),

    BIG_WAVE_BEACH_CHOOSE_INTRO("assets/music/BWB_ChooseIntro.ogg"),
    BIG_WAVE_BEACH_CHOOSE_LOOP("assets/music/BWB_ChooseLoop.ogg"),
    BIG_WAVE_BEACH_FIRST_WAVE("assets/music/BWB_FirstWave.ogg"),
    BIG_WAVE_BEACH_2A_INTRO("assets/music/BWB_2Aintro.ogg"),
    BIG_WAVE_BEACH_2A_LOOP("assets/music/BWB_2Aloop.ogg"),
    BIG_WAVE_BEACH_2B_INTRO("assets/music/BWB_2Bintro.ogg"),
    BIG_WAVE_BEACH_2B_LOOP("assets/music/BWB_2Bloop.ogg"),
    BIG_WAVE_BEACH_BOSS_INTRO("assets/music/BWB_Boss_Intro.mp3"),
    BIG_WAVE_BEACH_REWARD("assets/music/BWB_Reward.ogg"),
    BIG_WAVE_BEACH_VICTORY("assets/music/BWB_VictoryNew.ogg"),
    BIG_WAVE_BEACH_DEFEAT("assets/music/BWB_Lose.ogg"),

    ALMANAC_1("assets/music/PvZ2C_Almanac_Music_1.mp3"),
    ALMANAC_2("assets/music/PvZ2C_Almanac_Music_2.mp3"),
    ALMANAC_3("assets/music/PvZ2C_Almanac_Music_3.mp3"),
    ALMANAC_4("assets/music/PvZ2C_Almanac_Music_4.mp3"),
    ALMANAC_5("assets/music/PvZ2C_Almanac_Music_5.mp3"),
    ALMANAC_6("assets/music/PvZ2C_Almanac_Music_6.mp3"),

    STORE_2("assets/music/PvZ2C_Store_Music_2.mp3"),
    STORE_3("assets/music/PvZ2C_Store_Music_3.mp3"),
    STORE_4("assets/music/PvZ2C_Store_Music_4.mp3");

    private final String path;

    MusicTrack(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
