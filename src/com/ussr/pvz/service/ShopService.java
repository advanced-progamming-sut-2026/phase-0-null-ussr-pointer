package com.ussr.pvz.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.AdventureProgress;
import com.ussr.pvz.model.dto.ShopBuyRequest;
import com.ussr.pvz.model.shop.ShopItem;
import com.ussr.pvz.model.shop.ShopItemType;

public class ShopService {
    public ShopService() {
    }

    public String shopList() {
        StringBuffer sb = new StringBuffer();
        //todo organize this so it would be beautiful
        sb.append("id    |name       |cost         |description     |discount\n");
        App.getShopManager().getShopItems().forEach(item -> {
            sb.append(makeItem(item));
        });
        return sb.toString();
    }

    public String shopDaily() {
        StringBuilder sb = new StringBuilder();
        App.getShopManager().getShopItems().forEach(item -> {
            if (item.getType().equals(ShopItemType.DAILY_OFFER)) {
                sb.append(makeItem(item));
            }
        });
        return sb.toString();
    }

    private StringBuilder makeItem(ShopItem item) {
        StringBuilder sb = new StringBuilder();
        sb.append(item.getId()).append(",");
        sb.append(item.getName()).append(",");
        sb.append(item.getCost()).append(" ").append(item.getType().getCost()).append(",");
        sb.append(item.getDescription()).append(",");
        sb.append(item.getDiscountPercent()).append("\n");
        return sb;
    }

    public String buy(ShopBuyRequest request) {
        int count = 0;
        try {
            count = Integer.parseInt(request.count());
        } catch (NumberFormatException e) {
            throw new NumberFormatException("count must be an integer");
        }

        ShopItem item = App.getShopManager().getShopItems().stream()
                .filter(i -> i.getId().equals(request.itemId()))
                .findFirst()
                .orElse(null);

        if (item == null) {
            throw new IllegalArgumentException("Error: Invalid Item ID!");
        }

        if (item.requiresPlantType()) {
            if (request.plantType().isEmpty()) {
                throw new IllegalArgumentException("plant type is empty");
            }
        }

        AdventureProgress adv = App.getAccount().getAdventureProgress();
        switch (item.getType().getCostType()) {
            case "coin":
                if (adv.getCoin() < item.getCost() * count) {
                    throw new IllegalArgumentException("Error: Insufficient funds!");
                }
                adv.addCoin(-item.getCost());
            case "gem":
                if (adv.getGem() < item.getCost() * count) {
                    throw new IllegalArgumentException("Error: Insufficient funds!");
                }
                adv.addGem(-item.getCost());
        }
        //todo cancel the shop and return the fund if the limit is exceeded
        //todo if the type is chosen plant type the logic should implemented
        //todo if the type is daily the randomness logic should implemented
        //todo add exceptions and throw them as above

        return "item bought successfully!";
    }
}
