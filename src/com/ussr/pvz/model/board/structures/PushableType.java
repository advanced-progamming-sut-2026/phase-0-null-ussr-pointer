package com.ussr.pvz.model.board.structures;

public enum PushableType {
    ICE_BLOCK(500, "ZombieImp",
            "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_ZOMBIE/FROSTBITE_ICE_BLOCK_ZOMBIE.PAM"),
    ARCADE_CABINET(1100, null,
            "768/FULL/EFFECTS/80S_ARCADE_CABINET/80S_ARCADE_CABINET.PAM"),
    PIANO(1100, null,
            "768/FULL/ZOMBIE/PIANO/PIANO.PAM"),
    BARREL(500, "ZombieImp",
            "768/FULL/ZOMBIE/ZOMBIE_PIRATE_BARREL_PUSHER_BARREL/ZOMBIE_PIRATE_BARREL_PUSHER_BARREL.PAM"); // Barrel added, spawns Imps upon breaking

    private final int baseHp;
    private final String spawnAlias;
    private final String pamLocation;

    PushableType(int baseHp, String spawnAlias, String pamLocation) {
        this.baseHp = baseHp;
        this.spawnAlias = spawnAlias;
        this.pamLocation = pamLocation;
    }

    public int getBaseHp() { return baseHp; }
    public String getSpawnAlias() { return spawnAlias; }
    // Add to PushableType enum:
    public String getPamLocation() { return pamLocation; }
}
