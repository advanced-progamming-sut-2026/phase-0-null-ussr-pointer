package com.ussr.pvz.model.quest;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.NewsItem;

public class QuestRewardApplier {

    public static void applyReward(QuestReward reward, String questTitle) {
        Account account = App.getAccount();
        if (account == null || reward == null) {
            return;
        }

        int rewardAmount = calculateRewardAmount(reward);

        // Apply reward based on reward type
        switch (reward.rewardType().toUpperCase()) {
            case "COIN":
                account.getAdventureProgress().addCoin(rewardAmount);
                break;
            case "GEM":
                account.getAdventureProgress().addGem(rewardAmount);
                break;
            case "SEED_PACK":
                // For seed packs, award as bonus coins
                account.getAdventureProgress().addCoin(rewardAmount * 10);
                break;
            default:
                break;
        }

        // Increment quests completed counter
        account.getAdventureProgress().incrementQuestsCompleted();

        // Create and add news notification
        addQuestCompletionNews(questTitle, reward, rewardAmount);
    }

    private static int calculateRewardAmount(QuestReward reward) {
        if (reward.hasFormula()) {
            // You could implement formula evaluation here if needed
            // For now, just return the base amount
            return reward.amount();
        }
        return reward.amount();
    }

    private static void addQuestCompletionNews(String questTitle, QuestReward reward, int rewardAmount) {
        Account account = App.getAccount();
        if (account == null) {
            return;
        }

        String title = "Quest Completed!";
        String content = String.format(
                "You have completed the quest \"%s\" and earned %d %s!",
                questTitle,
                rewardAmount,
                formatRewardType(reward.rewardType())
        );

        int currentTimestamp = (int) (System.currentTimeMillis() / 1000);
        NewsItem news = new NewsItem(title, content, currentTimestamp);
        account.getPersonalNews().add(news);
    }

    private static String formatRewardType(String rewardType) {
        return switch (rewardType.toUpperCase()) {
            case "COIN" -> "Coins";
            case "GEM" -> "Gems";
            case "SEED_PACK" -> "Seed Packets";
            default -> rewardType;
        };
    }
}


