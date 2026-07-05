package com.ussr.pvz.model.board.structures;

public enum PushableType {
    ICE_BLOCK(500, "ZombieImp"),
    ARCADE_CABINET(1100, null),
    BARREL(500, "ZombieImp"); // Barrel added, spawns Imps upon breaking

    private final int baseHp;
    private final String spawnAlias;

    PushableType(int baseHp, String spawnAlias) {
        this.baseHp = baseHp;
        this.spawnAlias = spawnAlias;
    }

    public int getBaseHp() { return baseHp; }
    public String getSpawnAlias() { return spawnAlias; }
}