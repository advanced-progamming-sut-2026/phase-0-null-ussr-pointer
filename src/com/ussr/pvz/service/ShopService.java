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
        rotateDailyOffersIfNeeded();
        StringBuffer sb = new StringBuffer();
        //todo organize this so it would be beautiful
        sb.append("id    |name       |cost         |description     |discount\n");
        App.getShopManager().getShopItems().forEach(item -> {
            sb.append(makeItem(item));
        });
        return sb.toString();
    }

    public String shopDaily() {
        rotateDailyOffersIfNeeded();
        StringBuilder sb = new StringBuilder();
        App.getShopManager().getShopItems().forEach(item -> {
            if (item.getType().equals(ShopItemType.DAILY_OFFER)) {
                sb.append(makeItem(item));
                if (item.getFeaturedPlant() != null) {
                    sb.append("  featuring: ").append(item.getFeaturedPlant())
                            .append(item.isExpired() ? " (already claimed today)\n" : "\n");
                }
            }
        });
        return sb.toString();
    }

    private void rotateDailyOffersIfNeeded() {
        AdventureProgress adv = App.getAccount() != null ? App.getAccount().getAdventureProgress() : null;
        if (adv == null) return;

        App.getShopManager().getShopItems().forEach(item -> {
            if (item.isDailyRotationDue()) {
                item.rotateDaily(randomUnlockedPlant(adv));
            }
        });
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
        rotateDailyOffersIfNeeded();

        int count;
        try {
            count = Integer.parseInt(request.count());
        } catch (NumberFormatException e) {
            throw new NumberFormatException("count must be an integer");
        }
        if (count <= 0) return "count must be greater than 0";

        ShopItem item = App.getShopManager().getShopItems().stream()
                .filter(i -> i.getId().equals(request.itemId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Error: Invalid Item ID!"));

        String validationError = validatePurchaseConditions(item, count, request.plantType());
        if (validationError != null) return validationError;

        String paymentError = processPayment(item, count);
        if (paymentError != null) return paymentError;

        String result = applyItem(item, count, request.plantType());

        if (item.isDailyOffer()) {
            item.setExpired(true);
        }

        return result;
    }

    private String validatePurchaseConditions(ShopItem item, int count, String plantType) {
        if (item.isExpired()) return "this offer has expired";
        if (item.isDailyOffer() && count > 1) {
            return "the daily offer can only be bought once per rotation";
        }
        if (item.requiresPlantType()) {
            if (plantType == null || plantType.isEmpty()) {
                throw new IllegalArgumentException("plant type is empty");
            }
            String pType = plantType.trim().toUpperCase();
            boolean plantExists = App.getAccount().getAdventureProgress().getPlantLvls().containsKey(pType);
            if (!plantExists) return "plant not found: " + plantType;
        }
        return null;
    }

    private String processPayment(ShopItem item, int count) {
        AdventureProgress adv = App.getAccount().getAdventureProgress();
        int totalCost = discountedCost(item) * count;

        switch (item.getType().getCostType()) {
            case "coin" -> {
                if (adv.getCoin() < totalCost) return "insufficient coins (need " + totalCost + ", have " + adv.getCoin() + ")";
                adv.addCoin(-totalCost);
            }
            case "gem" -> {
                if (adv.getGem() < totalCost) return "insufficient gems (need " + totalCost + ", have " + adv.getGem() + ")";
                adv.addGem(-totalCost);
            }
            default -> { return "unknown currency type"; }
        }
        return null;
    }

    private int discountedCost(ShopItem item) {
        if (item.getDiscountPercent() == null || item.getDiscountPercent() == 0f)
            return item.getCost();
        return (int) (item.getCost() * (1f - item.getDiscountPercent() / 100f));
    }
    private String applyItem(ShopItem item, int count, String plantType) {
        AdventureProgress adv = App.getAccount().getAdventureProgress();

        return switch (item.getType()) {
            case POT -> applyPot(count);
            case PLANT_FOOD -> applyPlantFood(count);
            case SEED_PACK_RANDOM -> applySeedPackRandom(item, count, adv);
            case SEED_PACK_SELECTIVE -> applySeedPackSelective(item, count, plantType);
            case CURRENCY_CONVERT -> applyCurrencyConvert(item, count, adv);
            case DAILY_OFFER -> applyDailyOffer(item, count, adv);
        };
    }

    private String applyPot(int count) {
        int currentUnlocked = App.getAccount().getGreenhouse().getUnlockedPots();
        int canUnlock = Math.min(count, ShopItemType.POT.getMaxStack() - currentUnlocked);
        if (canUnlock <= 0) return "all pots are already unlocked";

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
        return unlocked + " pot(s) unlocked";
    }

    private String applyPlantFood(int count) {
        // TODO: add plantFoodCount field to AdventureProgress
        return count + " plant food added";
    }

    private String applySeedPackRandom(ShopItem item, int count, AdventureProgress adv) {
        String randomPlant = randomUnlockedPlant(adv);
        if (randomPlant == null) return "no unlocked plants to give seeds for";
        int seeds = count * item.getQuantityPerPurchase();
        // TODO: add seed packet inventory logic
        return seeds + " seed packets added for " + randomPlant;
    }

    private String applySeedPackSelective(ShopItem item, int count, String plantType) {
        String target = plantType.trim().toUpperCase();
        int seeds = count * item.getQuantityPerPurchase();
        // TODO: add seed packet inventory logic
        return seeds + " seed packets added for " + target;
    }

    private String applyCurrencyConvert(ShopItem item, int count, AdventureProgress adv) {
        int coinsGained = count * item.getQuantityPerPurchase();
        adv.addCoin(coinsGained);
        return "converted to " + coinsGained + " coins";
    }

    private String applyDailyOffer(ShopItem item, int count, AdventureProgress adv) {
        String plant = item.getFeaturedPlant();
        if (plant == null) plant = randomUnlockedPlant(adv);
        if (plant == null) return "no unlocked plants to give seeds for";
        int seeds = count * item.getQuantityPerPurchase();
        return seeds + " seed packets added for " + plant + " (daily offer)";
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