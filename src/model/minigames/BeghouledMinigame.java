package model.minigames;

import model.minigames.beghouled.BeghouledTile;

import java.util.Map;

public class BeghouledMinigame extends BaseMinigame {
    private final BeghouledTile[][] grid;
    private int score;
    private int targetScore;
    private int movesLeft;
    private boolean complete;
    private boolean failed;
    private final Map<String, String> upgradeTable;

    public BeghouledMinigame(BeghouledTile[][] grid, Map<String, String> upgradeTable) {
        this.grid = grid;
        this.upgradeTable = upgradeTable;
    }

    public boolean swap(int r1, int c1, int r2, int c2) {
        return false;
    }

    private boolean hasMatch() {
        return false;
    }

    private void resolveMatches() {

    }

    private void refillGrid() {

    }

    public void tick() {

    }

    public boolean isComplete() {
        return false;
    }

    public boolean isFailed() {
        return false;
    }
}

