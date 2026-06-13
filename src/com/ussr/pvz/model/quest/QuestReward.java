package com.ussr.pvz.model.quest;

public class QuestReward {
    private final String rewardType;
    private final int amount;
    private final String formula;

    public QuestReward(String rewardType, int amount, String formula) {
        this.rewardType = rewardType;
        this.amount = amount;
        this.formula = formula;
    }

    public boolean hasFormula() {
        return formula != null && !formula.isBlank();
    }

    public String getRewardType() {
        return rewardType;
    }

    public int getAmount() {
        return amount;
    }

    public String getFormula() {
        return formula;
    }
}
