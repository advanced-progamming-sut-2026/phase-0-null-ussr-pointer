package com.ussr.pvz.model.account;

import java.util.List;
import java.util.Map;

public record AccountState(
        String username,
        String nickname,
        String password,
        String email,
        Gender gender,
        SecurityQuestion securityQuestion,
        String securityAnswer,
        int currentLvl,
        int coin,
        int gem,
        int score,
        Map<String, Integer> plantLvl,
        List<NewsItem> personalNews
) {
}