package com.ussr.pvz.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.AdventureProgress;
import com.ussr.pvz.model.dto.ShopBuyRequest;
import com.ussr.pvz.model.shop.ShopItem;
import com.ussr.pvz.model.shop.ShopItemType;

import java.util.Random;

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
        if (count <= 0) return "count must be greater than 0";


        ShopItem item = App.getShopManager().getShopItems().stream()
                .filter(i -> i.getId().equals(request.itemId()))
                .findFirst()
                .orElse(null);

        if (item == null) {
            throw new IllegalArgumentException("Error: Invalid Item ID!");
        }
        if (item.isExpired()) return "this offer has expired";


        if (item.requiresPlantType()) {
            if (request.plantType().isEmpty()) {
                throw new IllegalArgumentException("plant type is empty");
            }
            String plantType = request.plantType().trim().toUpperCase();
            boolean plantExists = App.getAccount().getAdventureProgress()
                    .getPlantLvls().containsKey(plantType);
            if (!plantExists) return "plant not found: " + request.plantType();
        }

        AdventureProgress adv = App.getAccount().getAdventureProgress();
        int totalCost = discountedCost(item) * count;

        switch (item.getType().getCostType()) {
            case "coin" -> {
                if (adv.getCoin() < totalCost)
                    return "insufficient coins (need " + totalCost + ", have " + adv.getCoin() + ")";
                adv.addCoin(-totalCost);
            }
            case "gem" -> {
                if (adv.getGem() < totalCost)
                    return "insufficient gems (need " + totalCost + ", have " + adv.getGem() + ")";
                adv.addGem(-totalCost);
            }
            default -> { return "unknown currency type"; }
        }

        //todo cancel the shop and return the fund if the limit is exceeded
        //todo if the type is chosen plant type the logic should implemented
        //todo if the type is daily the randomness logic should implemented
        //todo add exceptions and throw them as above

        return applyItem(item, count, request.plantType());
    }
    private int discountedCost(ShopItem item) {
        if (item.getDiscountPercent() == null || item.getDiscountPercent() == 0f)
            return item.getCost();
        return (int) (item.getCost() * (1f - item.getDiscountPercent() / 100f));
    }
    private String applyItem(ShopItem item, int count, String plantType) {
        AdventureProgress adv = App.getAccount().getAdventureProgress();

        return switch (item.getType()) {
            case POT -> {
                int currentUnlocked = App.getAccount().getGreenhouse().getUnlockedPots();
                int canUnlock = Math.min(count, ShopItemType.POT.getMaxStack() - currentUnlocked);
                if (canUnlock <= 0) yield "all pots are already unlocked";

                int unlocked = 0;
                outer:
                for (int x = 0; x < 5; x++) {
                    for (int y = 0; y < 4; y++) {
                        if (!App.getAccount().getGreenhouse().isPotUnlocked(x, y)) {
                            App.getAccount().getGreenhouse().unlockPot(x, y);
                            unlocked++;
                            if (unlocked >= canUnlock) break outer;
                        }
                    }
                }
                yield unlocked + " pot(s) unlocked";
            }
            case PLANT_FOOD -> {
                // plant food is capped at 3; enforce that
                // stored on AdventureProgress — you may want a dedicated field later
                // for now just report success; wire to actual storage when ready
                yield count + " plant food added";  // TODO: add plantFoodCount field to AdventureProgress
            }
            case SEED_PACK_RANDOM -> {
                // give count * 5 seed packets for a random unlocked plant
                String randomPlant = randomUnlockedPlant(adv);
                if (randomPlant == null) yield "no unlocked plants to give seeds for";
                int seeds = count * item.getQuantityPerPurchase();
                // TODO: add seed packet inventory to AdventureProgress; for now just report
                yield seeds + " seed packets added for " + randomPlant;
            }
            case SEED_PACK_SELECTIVE -> {
                String target = plantType.trim().toUpperCase();
                int seeds = count * item.getQuantityPerPurchase();
                // TODO: same as above — wire to seed inventory when ready
                yield seeds + " seed packets added for " + target;
            }
            case CURRENCY_CONVERT -> {
                // each purchase already cost 5 gems; give 500 coins per unit
                int coinsGained = count * item.getQuantityPerPurchase();
                adv.addCoin(coinsGained);
                yield "converted to " + coinsGained + " coins";
            }
            case DAILY_OFFER -> {
                String randomPlant = randomUnlockedPlant(adv);
                if (randomPlant == null) yield "no unlocked plants to give seeds for";
                int seeds = count * item.getQuantityPerPurchase();
                yield seeds + " seed packets added for " + randomPlant + " (daily offer)";
            }
        };
    }

    private String randomUnlockedPlant(AdventureProgress adv) {
        java.util.List<String> unlocked = adv.getPlantLvls().entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(java.util.Map.Entry::getKey)
                .toList();
        if (unlocked.isEmpty()) return null;
        return unlocked.get(new Random().nextInt(unlocked.size()));
    }
}
