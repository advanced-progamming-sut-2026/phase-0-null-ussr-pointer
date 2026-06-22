package com.ussr.pvz.model.shop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShopManager {

    private final List<ShopItem> shopItems;

    public ShopManager() {
        this.shopItems = new ArrayList<>();
        initShopItems();
    }

    private void initShopItems() {
        for (ShopItemType type : ShopItemType.values()) {
            Float discount = (type == ShopItemType.DAILY_OFFER) ? 20.0f : 0.0f;

            ShopItem item = new ShopItem(
                    type.getDefaultId(),
                    type,
                    discount
            );
            shopItems.add(item);
        }
    }

    public List<ShopItem> getShopItems() {
        return Collections.unmodifiableList(shopItems);
    }
}