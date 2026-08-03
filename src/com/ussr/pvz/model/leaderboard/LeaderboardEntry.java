package com.ussr.pvz.model.leaderboard;

public record LeaderboardEntry(
        String username,
        int chapter,
        int level,
        int minigames,
        int dailyQuests,
        int otherQuests,
        int score
) {
}