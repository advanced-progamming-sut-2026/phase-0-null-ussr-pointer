package com.ussr.pvz.model.shop;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ShopItem {

    private final String id;
    private final ShopItemType type;
    private final Float discountPercent;
    private boolean expired;

    public ShopItem(String id, ShopItemType type, Float discountPercent) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.type = Objects.requireNonNull(type, "ShopItemType cannot be null");
        this.discountPercent = discountPercent != null ? discountPercent : 0.0f;
        this.expired = false;
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

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("type", type.getName());
        map.put("discountPercent", discountPercent);
        map.put("expired", expired);
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
        return item;
    }
}