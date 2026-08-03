package com.ussr.pvz.model.leaderboard;

public enum LeaderboardColumn {
    SCORE("MooPoints"),
    PROGRESS("Progress"),
    MINIGAMES("Minigames"),
    DAILY_QUESTS("Daily quests"),
    OTHER_QUESTS("Other quests"),
    USERNAME("Username");

    private final String title;

    LeaderboardColumn(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}