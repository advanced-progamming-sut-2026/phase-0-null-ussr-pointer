package com.ussr.pvz.model.shop;

import java.util.ArrayList;
import java.util.List;

public class ShopManager {

    private List<ShopItem> shopItems;

    public ShopManager() {
        shopItems = new ArrayList<>();
        for (int i = 0; i < ShopItemType.values().length; i++) {
            //TODO:initialize the shop item
        }
    }
}

