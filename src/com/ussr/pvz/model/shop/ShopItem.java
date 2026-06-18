package com.ussr.pvz.model.shop;

import java.util.Map;

public class ShopItem {

    private final String id;
    private final String name;
    private final ShopItemType type;
    private final int coinCost;
    private final int gemCost;
    private final int quantityPerPurchase;
    private final int maxStack;
    private final boolean requiresPlantType;
    private final String description;

    private final Float discountPercent;
    private boolean isExpired;

    public ShopItem(String id, String name, ShopItemType type, int coinCost, int gemCost, int quantityPerPurchase, int maxStack, boolean requiresPlantType, String description, Float discountPercent) {

        this.id = id;
        this.name = name;
        this.type = type;
        this.coinCost = coinCost;
        this.gemCost = gemCost;
        this.quantityPerPurchase = quantityPerPurchase;
        this.maxStack = maxStack;
        this.requiresPlantType = requiresPlantType;
        this.description = description;
        this.discountPercent = discountPercent;
        this.isExpired = false;
    }

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

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        this.isExpired = expired;
    }
}
