package com.ussr.pvz.model.shop;

public enum ShopItemType {
    POT("1", 2000, "coin", 1, 20, false, "Unlock a greenhouse slot (Maximum of 20)"),
    PLANT_FOOD("2", 3, "gem", 1, 3, false, "Plant Food used at the start of the next stage. Maximum storage cap of 3"),
    SEED_PACK_RANDOM("3", 1000, "coin", 5, Integer.MAX_VALUE, false, "Contains 5 seed packets for a random unlocked plant"),
    SEED_PACK_SELECTIVE("4", 5, "gem", 10, Integer.MAX_VALUE, true, "Contains 10 seed packets for a chosen unlocked plant"),
    CURRENCY_CONVERT("5", 5, "gem", 500, Integer.MAX_VALUE, false, "Convert 5 Gems into 500 Coins"),
    DAILY_OFFER("6", 1600, "coin", 10, Integer.MAX_VALUE, false, "Special daily offer: 10 seed packets for a random unlocked plant with a 20% discount");

    private final String defaultId;
    private final int cost;
    private final String costType;
    private final int unit;
    private final int maxStack;
    private final boolean requiresPlantType;
    private final String description;

    ShopItemType(String defaultId, int cost, String costType, int unit, int maxStack, boolean requiresPlantType, String description) {
        this.defaultId = defaultId;
        this.cost = cost;
        this.costType = costType;
        this.unit = unit;
        this.maxStack = maxStack;
        this.requiresPlantType = requiresPlantType;
        this.description = description;
    }

    public String getDefaultId() {
        return defaultId;
    }

    public int getCost() {
        return cost;
    }

    public String getCostType() {
        return costType;
    }

    public int getUnit() {
        return unit;
    }

    public int getMaxStack() {
        return maxStack;
    }

    public boolean isRequiresPlantType() {
        return requiresPlantType;
    }

    public String getDescription() {
        return description;
    }

    public static ShopItemType fromString(String v) {
        if (v == null) {
            return null;
        }
        return switch (v.toLowerCase()) {
            case "pot" -> POT;
            case "plant_food" -> PLANT_FOOD;
            case "seed_pack_random" -> SEED_PACK_RANDOM;
            case "seed_pack_selective" -> SEED_PACK_SELECTIVE;
            case "currency_convert" -> CURRENCY_CONVERT;
            case "daily_offer", "dailed_offer" -> DAILY_OFFER;
            default -> null;
        };
    }

    public String getName() {
        return this.toString().toLowerCase();
    }

}