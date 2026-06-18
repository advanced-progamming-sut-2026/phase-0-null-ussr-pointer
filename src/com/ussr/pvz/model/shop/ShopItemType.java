package com.ussr.pvz.model.shop;

public enum ShopItemType {
    POT(2000, "coin", 1),
    PLANT_FOOD(3,"gem",1),
    SEED_PACK_RANDOM(1000,"coin",5),
    SEED_PACK_SELECTIVE(5,"gem",10),
    CURRENCY_CONVERT(5,"gem",500),
    DAILY_OFFER(2000,"coin",10);

    private final int cost;
    private final String costType;
    private final int unit;

    ShopItemType(int cost, String costType, int unit) {
        this.cost = cost;
        this.costType = costType;
        this.unit = unit;
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

    public static ShopItemType fromString(String v) {
        return switch (v) {
            case "pot" -> POT;
            case "plant_food" -> PLANT_FOOD;
            case "seed_pack_random" -> SEED_PACK_RANDOM;
            case "seed_pack_selective" -> SEED_PACK_SELECTIVE;
            case "currency_convert" -> CURRENCY_CONVERT;
            case "dailed_offer" -> DAILY_OFFER;
            default -> null;
        };
    }
}
