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
        int currentLvl,
        int coin,
        int gem,
        int score,
        Map<String, Integer> plantLvl,
        List<String> seenZombies,
        List<NewsItem> personalNews,
        Map<String , Object> greenhouse,
        int plantFoodCount,
        Map<String, Integer> seedPackets
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
                this.currentLvl,
                this.coin,
                this.gem,
                this.score,
                this.plantLvl,
                this.seenZombies,
                this.personalNews,
                this.greenhouse,
                this.plantFoodCount,
                this.seedPackets

        );
    }
}