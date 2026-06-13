package com.ussr.pvz.model.shop;

import java.util.Map;

public class ShopItem {

    private String id;
    private String name;
    private ShopItemType type;
    private int coinCost;
    private int gemCost;
    private int quantityPerPurchase;
    private int maxStack;
    private boolean requiresPlantType;
    private String description;

    private Float discountPercent;
    private String dailyPlantKey;
    private String dailyDate;

    public String getId() {
        return id;
    }

    public ShopItemType getType() {
        return type;
    }

    public int getCoinCost() {
        return coinCost;
    }

    public int getGemCost() {
        return gemCost;
    }

    public int getQuantityPerPurchase() {
        return quantityPerPurchase;
    }

    public int getMaxStack() {
        return maxStack;
    }

    public boolean requiresPlantType() {
        return requiresPlantType;
    }

    public Float getDiscountPercent() {
        return discountPercent;
    }

    public boolean isDailyOffer() {
        return type == ShopItemType.DAILY_OFFER;
    }

    public Map<String, Object> toMap() {
        return null;
    }

    public static ShopItem fromMap(Map<String, Object> map) {
        return null;
    }
}
