package com.ussr.pvz.model.shop;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ShopItem {

    public static final long DAILY_ROTATION_INTERVAL_MILLIS = 24L * 60 * 60 * 1000;

    private final String id;
    private final ShopItemType type;
    private final Float discountPercent;
    private boolean expired;

    private long lastRefreshedAt;
    private String featuredPlant;

    public ShopItem(String id, ShopItemType type, Float discountPercent) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.type = Objects.requireNonNull(type, "ShopItemType cannot be null");
        this.discountPercent = discountPercent != null ? discountPercent : 0.0f;
        this.expired = false;
        this.lastRefreshedAt = System.currentTimeMillis();
        this.featuredPlant = null;
    }

    public String getId() {
        return id;
    }

    public ShopItemType getType() {
        return type;
    }

    public int getCost(){
        return this.type.getCost();
    }

    public int getQuantityPerPurchase() {
        return type.getUnit();
    }

    public int getMaxStack() {
        return type.getMaxStack();
    }

    public boolean requiresPlantType() {
        return type.isRequiresPlantType();
    }

    public Float getDiscountPercent() {
        return discountPercent;
    }

    public boolean isDailyOffer() {
        return type == ShopItemType.DAILY_OFFER;
    }

    public String getName() {
        return type.getName();
    }

    public String getDescription() {
        return type.getDescription();
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public boolean isDailyRotationDue() {
        if (!isDailyOffer()) return false;
        return System.currentTimeMillis() - lastRefreshedAt >= DAILY_ROTATION_INTERVAL_MILLIS;
    }

    public void rotateDaily(String newFeaturedPlant) {
        this.featuredPlant = newFeaturedPlant;
        this.lastRefreshedAt = System.currentTimeMillis();
        this.expired = false;
    }

    public String getFeaturedPlant() {
        return featuredPlant;
    }

    public long getLastRefreshedAt() {
        return lastRefreshedAt;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("type", type.getName());
        map.put("discountPercent", discountPercent);
        map.put("expired", expired);
        map.put("lastRefreshedAt", lastRefreshedAt);
        map.put("featuredPlant", featuredPlant);
        return map;
    }

    public static ShopItem fromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        String id = (String) map.get("id");
        ShopItemType type = ShopItemType.fromString((String) map.get("type"));
        Float discountPercent = (Float) map.get("discountPercent");
        Boolean expired = (Boolean) map.get("expired");

        ShopItem item = new ShopItem(id, type, discountPercent);
        if (expired != null) {
            item.setExpired(expired);
        }
        Number lastRefreshedAt = (Number) map.get("lastRefreshedAt");
        if (lastRefreshedAt != null) {
            item.lastRefreshedAt = lastRefreshedAt.longValue();
        }
        Object featuredPlant = map.get("featuredPlant");
        if (featuredPlant != null) {
            item.featuredPlant = (String) featuredPlant;
        }
        return item;
    }
}