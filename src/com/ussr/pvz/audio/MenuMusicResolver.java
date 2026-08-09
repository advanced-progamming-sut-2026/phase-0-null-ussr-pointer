package com.ussr.pvz.audio;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;

/** Keeps menu-to-music decisions out of individual view classes. */
public final class MenuMusicResolver {
    private MenuMusicResolver() {
    }

    public static MusicTrack resolve(MenuState state) {
        return switch (state) {
            case LOGIN, REGISTER, MAIN, NEWS, PROFILE, SETTING ->
                    MusicTrack.STARTUP;
            case GAME -> App.getGameSession() == null
                    ? MusicTrack.WORLD_MAP
                    : null;
            case LEVEL_SELECTION, TRAVEL_LOG, LEADERBOARD ->
                    MusicTrack.WORLD_MAP;
            case COLLECTION -> MusicTrack.ALMANAC_2;
            case GREENHOUSE -> MusicTrack.FRONT_LAWN_THEME;
            case SHOP -> MusicTrack.STORE_2;
            case CHOOSE_PLANT -> resolveChoosePlantMusic();
            case NETWORK -> MusicTrack.STARTUP;
        };
    }

    private static MusicTrack resolveChoosePlantMusic() {
        var chapter = App.getLevelManager().getCurrentChapter();
        if (chapter == null) {
            return MusicTrack.WORLD_MAP;
        }

        GameplayMusicResolver.Selection selection =
                GameplayMusicResolver.resolve(
                        chapter.getId(),
                        GameplayMusicCue.CHOOSE
                );
        return selection == null ? MusicTrack.WORLD_MAP : selection.intro();
    }
}
