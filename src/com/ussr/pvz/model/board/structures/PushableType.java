package com.ussr.pvz.model.board.structures;

// TODO(verify-troglobite-ice-block-spawn): ICE_BLOCK's spawnAlias is set to "ZombieImp", so
//  PushableStructure.onDestroy() will spawn a ZombieImp whenever a Troglobite's pushed ice block
//  is destroyed. Per spec, only the "roller barrel" zombie's container spawns 2 imps on
//  destruction — Troglobite's ice blocks are just supposed to vanish when they hit a plant or a
//  hypnotized zombie, no imp mentioned. Double-check this against the spec and set spawnAlias to
//  null for ICE_BLOCK if confirmed, so it behaves like ARCADE_CABINET below instead of BARREL.
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
