package com.ussr.pvz.shared.dto;

public record LeaderboardEntryDto(
        String username,
        int currentChapter,
        int currentLevel,
        int minigamesWon,
        int dailyQuests,
        int otherQuests,
        int score
) {
}