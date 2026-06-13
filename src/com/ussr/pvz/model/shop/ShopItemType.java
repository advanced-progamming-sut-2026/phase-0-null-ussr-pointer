package com.ussr.pvz.model.shop;

public enum ShopItemType {
    POT,
    PLANT_FOOD,
    SEED_PACK_RANDOM,
    SEED_PACK_SELECTIVE,
    CURRENCY_CONVERT,
    DAILY_OFFER;

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
