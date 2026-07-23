package com.ussr.pvz.service.minigame;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.level.behavior.BeghouledBehavior;
import com.ussr.pvz.model.level.behavior.LevelBehavior;

import java.util.HashMap;
import java.util.Map;

public class BeghouledService {

    private static final Map<String, UpgradeData> UPGRADES = new HashMap<>();

    static {
        // Mapped to match the documentation's requirements and costs while
        // correcting the typo names so they successfully load from plants.json
        UPGRADES.put("peashooter", new UpgradeData("repeater", 500));
        UPGRADES.put("repeater", new UpgradeData("mega gatling pea", 1500));
        UPGRADES.put("wall-nut", new UpgradeData("tall-nut", 500));
        UPGRADES.put("puff-shroom", new UpgradeData("fume-shroom", 250));
        UPGRADES.put("cabbage-pult", new UpgradeData("melon-pult", 1000));
        UPGRADES.put("melon-pult", new UpgradeData("winter melon", 750));
    }

    public String swapPlants(int r1, int c1, int r2, int c2) {
        GameSession session = App.getGameSession();
        if (session == null || session.getLawn() == null) return "Game session not active.";

        LevelBehavior behavior = (LevelBehavior) session.getLevel().getBehavior();
        if (!(behavior instanceof BeghouledBehavior beghouledBehavior)) {
            return "Current level is not a Beghouled minigame.";
        }

        boolean success = beghouledBehavior.trySwap(r1, c1, r2, c2);

        if (success) {
            return "Swapped successfully! Current matches: " +
                    beghouledBehavior.getCurrentMatches() + "/" + beghouledBehavior.getTargetMatches();
        } else {
            return "Invalid swap. Swaps must be adjacent, not into craters, and MUST create a match of 3 or more.";
        }
    }

    public String upgradePlant(String currentPlantType) {
        GameSession session = App.getGameSession();
        if (session == null) return "Game session not active.";

        LevelBehavior behavior = (LevelBehavior) session.getLevel().getBehavior();
        if (!(behavior instanceof BeghouledBehavior beghouledBehavior)) {
            return "Current level is not a Beghouled minigame.";
        }

        String key = currentPlantType.toLowerCase();
        if (!UPGRADES.containsKey(key)) {
            return "No upgrade path found for " + currentPlantType;
        }

        UpgradeData upgrade = UPGRADES.get(key);

        if (session.getSunCount() < upgrade.cost) {
            return "Not enough sun! Upgrade to " + upgrade.nextForm + " costs " + upgrade.cost + " sun.";
        }

        // Verify the plant is actually on the board before allowing the upgrade
        if (!beghouledBehavior.getActivePlantTypes().containsValue(key) && !beghouledBehavior.getActivePlantTypes()
                .containsKey(key)) {
            return currentPlantType + " is not currently active in this level.";
        }

        session.spendSun(upgrade.cost);
        beghouledBehavior.upgradePlantType(key, upgrade.nextForm, session);

        return "Successfully upgraded all " + currentPlantType + "s to " + upgrade.nextForm + "s!";
    }

    private record UpgradeData(String nextForm, int cost) {}
}