package com.ussr.pvz.model.account;

import java.util.Map;

public class AdventureProgress {
    private int coin;
    private int gem;
    private int currentLvl;
    private final Map<String, Integer> plantLvls;

    public AdventureProgress(int currentLvl, int coin, int gem, Map<String, Integer> plantLvls) {
        this.currentLvl = currentLvl;
        this.coin = coin;
        this.gem = gem;
        this.plantLvls = plantLvls;
    }

    //getters
    public int getCurrentLvl() {
        return this.currentLvl;
    }

    public int getCoin() {
        return this.coin;
    }

    public int getGem() {
        return this.gem;
    }

    public Map<String, Integer> getPlantLvls() {
        return this.plantLvls;
    }
}

