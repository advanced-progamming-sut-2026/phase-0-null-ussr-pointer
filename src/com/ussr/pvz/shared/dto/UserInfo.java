package com.ussr.pvz.shared.dto;

public record UserInfo(
        String username,
        String nickname,
        String email,
        String gender,
        int coin,
        int gem,
        int score,
        int currentChapter,
        int currentLevel
) {
}