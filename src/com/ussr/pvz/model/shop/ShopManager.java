package com.ussr.pvz.model.shop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopManager {

    private static final String FILE_PATH = "shop_state.json";

    private static final Gson gson =
            new GsonBuilder().setPrettyPrinting().create();

    private final List<ShopItem> shopItems;

    public ShopManager() {
        this.shopItems = new ArrayList<>();
        initShopItems();
        restoreFromDisk();
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

    public synchronized void saveToDisk() {
        List<Map<String, Object>> data = new ArrayList<>();
        for (ShopItem item : shopItems) {
            data.add(item.toMap());
        }

        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            System.err.println("Could not save shop state to disk: " + e.getMessage());
        }
    }

    private void restoreFromDisk() {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
            List<Map<String, Object>> data = gson.fromJson(reader, listType);
            if (data == null) return;

            Map<String, ShopItem> savedById = new HashMap<>();
            for (Map<String, Object> map : data) {
                ShopItem saved = ShopItem.fromMap(map);
                if (saved != null) {
                    savedById.put(saved.getId(), saved);
                }
            }

            for (int i = 0; i < shopItems.size(); i++) {
                ShopItem current = shopItems.get(i);
                ShopItem saved = savedById.get(current.getId());
                if (saved != null) {
                    shopItems.set(i, saved);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load shop state from disk: " + e.getMessage());
        }
    }
}