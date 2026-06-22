package com.ussr.pvz.model.quest;

public record QuestReward(String rewardType, int amount, String formula) {

    public boolean hasFormula() {
        return formula != null && !formula.isBlank();
    }
}