package com.ussr.pvz.model.account;

import java.util.ArrayList;
import java.util.List;

public class SavedBoosts {
    private final List<String> boosts = new ArrayList<>();

    public List<String> getBoosts() {
        return boosts;
    }

    public boolean addBoost(String plantName) {
        if (plantName == null || boosts.contains(plantName.toUpperCase())) {
            return false;
        }
        boosts.add(plantName.toUpperCase());
        return true;
    }

    public boolean useBoost(String plantName) {
        if (plantName == null) return false;
        return boosts.remove(plantName.toUpperCase());
    }
}