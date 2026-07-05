package com.ussr.pvz.model.entities.zombies;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.PushableStructure;
import com.ussr.pvz.model.board.structures.PushableType;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.armor.Armor;
import com.ussr.pvz.model.entities.zombies.armor.ArmorType;
import com.ussr.pvz.model.entities.zombies.factory.AttackBehaviorRegistry;
import com.ussr.pvz.model.entities.zombies.factory.DefenseBehaviorRegistry;
import com.ussr.pvz.model.entities.zombies.factory.EffectStatusRegistry;
import com.ussr.pvz.model.entities.zombies.factory.MoveBehaviorRegistry;
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
            Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
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
            Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
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

    public static int getZombieCost(String alias) {
        init();
        Map<String, Object> data = blueprints.get(alias);
        if (data == null) return Integer.MAX_VALUE; // Prevent buying unknown zombies
        return ((Number) data.getOrDefault("WavePointCost", 100)).intValue();
    }

    @SuppressWarnings("unchecked")
    public static Zombie create(String alias, int row, int col) {
        init();

        Map<String, Object> data = blueprints.get(alias);
        if (data == null) {
            throw new IllegalArgumentException("Unknown zombie alias: " + alias);
        }

        Zombie zombie = buildBaseZombie(alias, data, row, col);

        Object moveSpec = data.getOrDefault("move", "NormalWalk");
        Object attackSpec = data.getOrDefault("attack", "ChompAttack");
        Object defenseSpec = data.getOrDefault("defense", "NormalDefense");
        Object effectSpec = data.get("effect");

        zombie.setMoveBehavior(MoveBehaviorRegistry.create(moveSpec));
        zombie.setAttackBehavior(AttackBehaviorRegistry.create(attackSpec, data));
        zombie.setDefenseBehavior(DefenseBehaviorRegistry.create(defenseSpec));
        zombie.setEffectStatus(EffectStatusRegistry.createOrNull(effectSpec, data));

        attachPushedStructureIfNeeded(zombie);

        return zombie;
    }

    private static Zombie buildBaseZombie(String alias, Map<String, Object> data, int row, int col) {
        int hp = ((Number) data.get("Hitpoints")).intValue();
        double speed = ((Number) data.get("Speed")).doubleValue();
        double eatDps = ((Number) data.get("EatDPS")).doubleValue();

        String sizeStr = data.containsKey("Size") ? (String) data.get("Size") : "default";
        ZombieSize size = switch (sizeStr.toLowerCase()) {
            case "imp" -> ZombieSize.IMP;
            case "large" -> ZombieSize.GARGANTUAR;
            default -> ZombieSize.DEFAULT;
        };

        boolean canSpawnPlantFood = data.containsKey("CanSpawnPlantFood") ? (Boolean) data.get("CanSpawnPlantFood") : true;

        Armor armor = resolveArmor(data);

        Zombie zombie = new Zombie(alias, armor, canSpawnPlantFood);
        zombie.setMaxHp(hp);
        zombie.setHp(hp);
        zombie.setEatDps(eatDps);
        zombie.setSize(size);

        Vec2 spawnPos = Vec2.of(col, row);
        zombie.setPosition(spawnPos);
        zombie.setSpeed(Vec2.of(-speed, 0));

        applyDifficultyScaling(zombie, data);

        return zombie;
    }

    @SuppressWarnings("unchecked")
    private static void applyDifficultyScaling(Zombie zombie, Map<String, Object> data) {
        if (App.getAccount() == null) return;
        int diff = App.getAccount().getDifficultyLvl();

        List<Map<String, Object>> scaledProps = (List<Map<String, Object>>) data.get("ScaledProps");
        if (scaledProps != null) {
            for (Map<String, Object> prop : scaledProps) {
                if ("standard".equals(prop.get("Formula"))) {
                    double arg1 = ((Number) prop.get("Arg1")).doubleValue();
                    double arg2 = ((Number) prop.get("Arg2")).doubleValue();
                    double scale = arg1 + ((diff - 1) * arg2);

                    if ("Hitpoints".equals(prop.get("Key"))) {
                        zombie.setMaxHp((int)(zombie.getMaxHp() * scale));
                        zombie.setHp(zombie.getMaxHp());
                    } else if ("EatDPS".equals(prop.get("Key"))) {
                        zombie.setEatDps(zombie.getEatDps() * scale);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Armor resolveArmor(Map<String, Object> data) {
        List<String> armorProps = (List<String>) data.get("ZombieArmorProps");
        if (armorProps == null || armorProps.isEmpty()) return null;

        int accumulatedArmorHp = 0;
        ArmorType primaryType = null;

        for (String rtid : armorProps) {
            String armorAlias = parseRtidAlias(rtid);
            ArmorType resolvedType = resolveArmorType(armorAlias);

            if (resolvedType != null) {
                primaryType = resolvedType;
                int hpValue = armorBaseHp.getOrDefault(armorAlias, resolvedType.getArmorHp());
                accumulatedArmorHp += hpValue;
            }
        }

        if (primaryType == null || accumulatedArmorHp <= 0) return null;
        return new Armor(primaryType, accumulatedArmorHp);
    }

    public static Armor createKnightArmor() {
        int crownHp = armorBaseHp.getOrDefault("CrownDefault", ArmorType.CROWN.getArmorHp());
        int shoulderHp = armorBaseHp.getOrDefault("ShoulderArmorDefault", ArmorType.SHOULDER_ARMOR.getArmorHp());
        return new Armor(ArmorType.CROWN, crownHp + shoulderHp);
    }

    private static void attachPushedStructureIfNeeded(Zombie zombie) {
        PushableType type = switch (zombie.getAlias()) {
            case "ZombieArcade" -> PushableType.ARCADE_CABINET;
            case "ZombieIceAgeTroglobite" -> PushableType.ICE_BLOCK;
            case "ZombieBarrelRoller", "ZombieBarrel" -> PushableType.BARREL;
            default -> null;
        };

        if (type == null) return;

        PushableStructure structure = new PushableStructure(type, zombie.getPosition());
        zombie.setPushedStructure(structure);
        placeOnLawnIfPossible(zombie, structure);
    }

    private static void placeOnLawnIfPossible(Zombie zombie, PushableStructure structure) {
        GameSession session = App.getGameSession();
        if (session == null || session.getLawn() == null) return;

        int row = (int) zombie.getPosition().y();
        int lastCol = session.getLawn().getCols() - 1;
        Cell cell = session.getLawn().getCell(row, lastCol);

        if (cell != null && cell.getInteractableStructure() == null) {
            cell.setStructure(structure);
        }

        session.registerStructure(structure);
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
            case "CrownDefault" -> ArmorType.CROWN;
            case "ShoulderArmorDefault" -> ArmorType.SHOULDER_ARMOR;
            case "NewspaperDefault" -> ArmorType.NEWSPAPER;
            default -> null;
        };
    }
}