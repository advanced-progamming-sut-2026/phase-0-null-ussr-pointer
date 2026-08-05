package com.ussr.pvz.model.entities.items;

public enum ItemType {
    SUN("768/INITIAL/EFFECTS/SUN/SUN.PAM"),
    COIN("768/INITIAL/EFFECTS/COIN_GOLD/COIN_GOLD.PAM"),
    DIAMOND("768/INITIAL/EFFECTS/COIN_DIAMOND/COIN_DIAMOND.PAM"),
    PLANT_FOOD("768/INITIAL/EFFECTS/PLANTFOOD_PICKUP/PLANTFOOD_PICKUP.PAM"),
    SEED_PACK("768/DEV/UI/CHOOSER/RENT_A_SEED_PACKET_OVERLAYS/RENT_A_SEED_PACKET_OVERLAYS.PAM");
    private final String pamLocation;

    ItemType(String pamLocation) {
        this.pamLocation = pamLocation;
    }
    public String getPamLocation() {
        return pamLocation;
    }
}
