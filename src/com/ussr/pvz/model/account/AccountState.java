package com.ussr.pvz.model.account;

import com.ussr.pvz.model.util.SecurityUtil;

import java.util.List;
import java.util.Map;

public record AccountState(
        String username,
        String nickname,
        String password,
        String email,
        Gender gender,
        int difficultyLvl,
        SecurityQuestion securityQuestion,
        String securityAnswer,
        int currentChapter,
        int currentLvl,
        int minigamesWon,
        int questsCompleted,
        int coin,
        int gem,
        int score,
        Map<String, Integer> plantLvl,
        List<String> seenZombies,
        List<NewsItem> personalNews,
        Map<String , Object> greenhouse,
        List<String> savedBoosts,
        int plantFoodCount,
        Map<String, Integer> seedPackets,
        List<String> completedQuests,
        Map<String, List<Integer>> activeQuestProgress,
        long lastLoginTime,
        long lastDailyResetTime,
        List<String> completedLevels
) {
    public AccountState finalizeRegistration(SecurityQuestion question, String answer) {
        return new AccountState(
                this.username,
                this.nickname,
                SecurityUtil.hashPassword(this.password),
                this.email,
                this.gender,
                this.difficultyLvl,
                question,
                answer,
                this.currentChapter,
                this.currentLvl,
                this.minigamesWon,
                this.questsCompleted,
                this.coin,
                this.gem,
                this.score,
                this.plantLvl,
                this.seenZombies,
                this.personalNews,
                this.greenhouse,
                this.savedBoosts,
                this.plantFoodCount,
                this.seedPackets,
                this.completedQuests,
                this.activeQuestProgress,
                this.lastLoginTime,
                this.lastDailyResetTime,
                this.completedLevels
        );
    }
}