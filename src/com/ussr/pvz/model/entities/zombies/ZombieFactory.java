package com.ussr.pvz.model.entities.zombies;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ussr.pvz.model.entities.zombies.armor.Armor;
import com.ussr.pvz.model.entities.zombies.armor.ArmorType;
import com.ussr.pvz.model.entities.zombies.attack.ChompAttack;
import com.ussr.pvz.model.entities.zombies.defense.NormalDefense;
import com.ussr.pvz.model.entities.zombies.move.NormalWalk;
import com.ussr.pvz.model.util.Vec2;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZombieFactory {

    private static final String ZOMBIES_PATH = "src/resources/zombies.json";
    private static final String ARMOR_PATH = "src/resources/ArmorTypeData.json";

    private static final Map<String, Map<String, Object>> blueprints = new HashMap<>();
    private static final Map<String, Integer> armorBaseHp = new HashMap<>();

    private static boolean loaded = false;

    public static void init() {
        if (loaded) return;
        loadZombies();
        loadArmorData();
        loaded = true;
    }

    @SuppressWarnings("unchecked")
    private static void loadZombies() {
        File file = new File(ZOMBIES_PATH);
        if (!file.exists()) {
            System.err.println("Critical Error: zombies.json not found at " + ZOMBIES_PATH);
            return;
        }
        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<Map<String, Object>>>() {
            }.getType();
            List<Map<String, Object>> entries = new Gson().fromJson(reader, listType);
            if (entries == null) return;

            for (Map<String, Object> entry : entries) {
                List<String> aliases = (List<String>) entry.get("aliases");
                Map<String, Object> objdata = (Map<String, Object>) entry.get("objdata");
                if (aliases == null || objdata == null) continue;
                for (String alias : aliases) {
                    blueprints.put(alias, objdata);
                }
            }
        } catch (IOException e) {
            System.err.println("Critical Error: could not read zombies.json: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void loadArmorData() {
        File file = new File(ARMOR_PATH);
        if (!file.exists()) {
            System.err.println("Critical Error: ArmorTypeData.json not found at " + ARMOR_PATH);
            return;
        }
        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<Map<String, Object>>>() {
            }.getType();
            List<Map<String, Object>> entries = new Gson().fromJson(reader, listType);
            if (entries == null) return;

            for (Map<String, Object> entry : entries) {
                List<String> aliases = (List<String>) entry.get("aliases");
                Map<String, Object> objdata = (Map<String, Object>) entry.get("objdata");
                if (aliases == null || objdata == null) continue;
                int baseHp = ((Number) objdata.get("BaseHealth")).intValue();
                for (String alias : aliases) {
                    armorBaseHp.put(alias, baseHp);
                }
            }
        } catch (IOException e) {
            System.err.println("Critical Error: could not read ArmorTypeData.json: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static Zombie create(String alias, int row, int cols) {
        init();

        Map<String, Object> data = blueprints.get(alias);
        if (data == null) {
            throw new IllegalArgumentException("Unknown zombie alias: " + alias);
        }

        int hp = ((Number) data.get("Hitpoints")).intValue();
        double speed = ((Number) data.get("Speed")).doubleValue();
        double eatDps = ((Number) data.get("EatDPS")).doubleValue();

        String sizeStr = data.containsKey("Size") ? (String) data.get("Size") : "default";
        ZombieSize size = switch (sizeStr.toLowerCase()) {
            case "imp" -> ZombieSize.IMP;
            case "large" -> ZombieSize.GARGANTUAR;
            default -> ZombieSize.DEFAULT;
        };

        Armor armor = null;
        List<String> armorProps = (List<String>) data.get("ZombieArmorProps");
        if (armorProps != null && !armorProps.isEmpty()) {
            String rtid = armorProps.get(0);
            String armorAlias = parseRtidAlias(rtid);
            ArmorType type = resolveArmorType(armorAlias);
            if (type != null) {
                armor = new Armor(type);
            }
        }

        Zombie zombie = new Zombie(alias, armor);
        zombie.setHp(hp);
        zombie.setEatDps(eatDps);
        zombie.setSize(size);

        zombie.setPosition(Vec2.of(cols, row));

        zombie.setSpeed(Vec2.of(-speed, 0));

        zombie.setMoveBehavior(new NormalWalk());
        zombie.setAttackBehavior(new ChompAttack());
        zombie.setDefenseBehavior(new NormalDefense());

        return zombie;
    }

    private static String parseRtidAlias(String rtid) {
        int start = rtid.indexOf('(');
        int at = rtid.indexOf('@');
        if (start < 0 || at < 0) return rtid;
        return rtid.substring(start + 1, at);
    }

    private static ArmorType resolveArmorType(String alias) {
        return switch (alias) {
            case "ConeDefault" -> ArmorType.CONE;
            case "BucketDefault" -> ArmorType.BUCKET;
            case "BrickDefault" -> ArmorType.BRICK;
            case "CrownDefault" -> ArmorType.HELMET;
            case "ShoulderArmorDefault" -> ArmorType.HELMET;
            case "NewspaperDefault" -> null;
            default -> null;
        };
    }
}