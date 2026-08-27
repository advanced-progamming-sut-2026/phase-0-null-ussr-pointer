package com.ussr.pvz.model.entities.zombies;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.PushableStructure;
import com.ussr.pvz.model.board.structures.PushableType;
import com.ussr.pvz.model.engine.session.GameSession;
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
    private static final Map<String, String> pamLocations = new HashMap<>();
    private static final Map<String, String> textureRegions = new HashMap<>();
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
                String pamLocation = (String) entry.get("pamLocation");
                String textureRegion = (String) entry.get("textureRegion");

                if (aliases == null || objdata == null) continue;

                for (String alias : aliases) {
                    blueprints.put(alias, objdata);
                    if (pamLocation != null) {
                        pamLocations.put(alias, pamLocation);
                    }
                    if (textureRegion != null && !textureRegion.isEmpty()) {
                        textureRegions.put(alias, textureRegion);
                    }
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

        Map<String, Object> blueprint = blueprints.get(alias);
        if (blueprint == null) {
            throw new IllegalArgumentException("Unknown zombie alias: " + alias);
        }

        Map<String, Object> data = new java.util.HashMap<>(blueprint);

        Zombie zombie = buildBaseZombie(alias, data, row, col);

        Object moveSpec = data.getOrDefault("move", "NormalWalk");
        Object attackSpec = data.getOrDefault("attack", "ChompAttack");
        Object defenseSpec = data.getOrDefault("defense", "NormalDefense");
        Object effectSpec = data.get("effect");

        zombie.setMoveBehavior(MoveBehaviorRegistry.create(moveSpec, data));
        zombie.setAttackBehavior(AttackBehaviorRegistry.create(attackSpec, data));
        zombie.setDefenseBehavior(DefenseBehaviorRegistry.create(defenseSpec));
        zombie.setEffectStatus(EffectStatusRegistry.createOrNull(effectSpec, data));

        attachPushedStructureIfNeeded(zombie, data);

        return zombie;
    }

    private static Zombie buildBaseZombie(String alias, Map<String, Object> data, int row, int col) {
        int hp = ((Number) data.get("Hitpoints")).intValue();
        double speed = ((Number) data.get("Speed")).doubleValue();
        double eatDps = ((Number) data.get("EatDPS")).doubleValue();

        if (data.containsKey("RunningSpeedScale")) {
            double runningSpeedScale = ((Number) data.get("RunningSpeedScale")).doubleValue();
            if (runningSpeedScale > 0) {
                speed = speed / runningSpeedScale;
            }
        }

        String sizeStr = data.containsKey("Size") ? (String) data.get("Size") : "default";
        ZombieSize size = switch (sizeStr.toLowerCase()) {
            case "imp" -> ZombieSize.IMP;
            case "large" -> ZombieSize.GARGANTUAR;
            default -> ZombieSize.DEFAULT;
        };

        boolean canSpawnPlantFood = data.containsKey("CanSpawnPlantFood") ? (Boolean) data.get("CanSpawnPlantFood")
                : true;

        Armor armor = resolveArmor(data);

        Zombie zombie = new Zombie(alias, armor, canSpawnPlantFood);
        zombie.setMaxHp(hp);
        zombie.setHp(hp);
        zombie.setEatDps(eatDps);
        zombie.setSize(size);
        zombie.setPamPath(pamLocations.get(alias));

        if (data.containsKey("DamageWhileSubmerged")) {
            Map<String, Object> map = (Map<String, Object>) data.get("DamageWhileSubmerged");
            zombie.setDamageWhileSubmerged((List<String>) map.get("List"));
        }
        if (data.containsKey("DamageWhileSubmergedPlantfoodOnly")) {
            Map<String, Object> map = (Map<String, Object>) data.get("DamageWhileSubmergedPlantfoodOnly");
            zombie.setDamageWhileSubmergedPlantfoodOnly((List<String>) map.get("List"));
        }
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
                    } else if ("SmashDamage".equals(prop.get("Key")) && data.get("SmashDamage")
                            instanceof Number smashBase) {
                        data.put("SmashDamage", (int) (smashBase.doubleValue() * scale));
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

    @SuppressWarnings("unchecked")
    private static void attachPushedStructureIfNeeded(Zombie zombie, Map<String, Object> data) {
        PushableType type = switch (zombie.getAlias()) {
            case "ZombieArcade" -> PushableType.ARCADE_CABINET;
            case "ZombiePiano" -> PushableType.PIANO;
            case "ZombieIceAgeTroglobite" -> PushableType.ICE_BLOCK;
            case "ZombieBarrelRoller", "ZombieBarrel" -> PushableType.BARREL;
            default -> null;
        };

        if (type == null) return;

        // Zombies now spawn a column off-screen to the right (see WaveDirector),
        // so a pushed structure can no longer just be placed at the zombie's own
        // spawn position (that cell would be off the lawn). Ice blocks in
        // particular belong on the lawn's last column, waiting for the
        // Troglobite to walk up to them.
        Vec2 spawnPosition = (type == PushableType.ICE_BLOCK)
                ? lastColumnPosition(zombie)
                : zombie.getPosition();

        PushableStructure structure = new PushableStructure(type, spawnPosition);
        zombie.setPushedStructure(structure);
        placeStructureAt(structure, spawnPosition);

        if (type == PushableType.ICE_BLOCK) {
            int totalIceBlocks = ((Number) data.getOrDefault("NumberOfIceblocksToSpawnWith", 1)).intValue();
            zombie.setPushableRespawnsRemaining(Math.max(0, totalIceBlocks - 1));
        }
    }

    private static Vec2 lastColumnPosition(Zombie zombie) {
        GameSession session = App.getGameSession();
        int row = (int) zombie.getPosition().y();
        int lastCol = (session != null && session.getLawn() != null)
                ? session.getLawn().getCols() - 1
                : (int) zombie.getPosition().x();
        return new Vec2(lastCol, row);
    }

    public static void respawnPushedStructureIfNeeded(Zombie zombie) {
        PushableStructure current = zombie.getPushedStructure();
        if (current == null || current.isAlive() || zombie.getPushableRespawnsRemaining() <= 0) {
            return;
        }

        PushableStructure fresh = new PushableStructure(current.getType(), zombie.getPosition());
        zombie.setPushedStructure(fresh);
        zombie.setPushableRespawnsRemaining(zombie.getPushableRespawnsRemaining() - 1);
        placeStructureAt(fresh, zombie.getPosition());
    }

    private static void placeStructureAt(PushableStructure structure, Vec2 position) {
        GameSession session = App.getGameSession();
        if (session == null || session.getLawn() == null) return;

        int row = (int) position.y();
        int col = (int) position.x();
        Cell cell = session.getLawn().getCell(row, col);

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

    public static java.util.Set<String> getAllZombieAliases() {
        init();
        return blueprints.keySet();
    }

    public static Map<String, Object> getBlueprint(String alias) {
        init();
        Map<String, Object> blueprint = blueprints.get(alias);
        return blueprint == null ? null : new HashMap<>(blueprint);
    }

    public static String getZombieTextureRegion(String alias) {
        init();
        String explicit = textureRegions.get(alias);
        if (explicit != null && !explicit.isEmpty()) {
            return explicit;
        }

        String pam = pamLocations.get(alias);
        if (pam != null && !pam.isEmpty()) {
            String fileName = pam.substring(pam.lastIndexOf('/') + 1);
            int dot = fileName.lastIndexOf('.');
            String withoutExt = dot >= 0 ? fileName.substring(0, dot) : fileName;
            String stripped = withoutExt.toUpperCase();
            if (stripped.startsWith("ZOMBIE_")) {
                stripped = stripped.substring(7);
            } else if (stripped.startsWith("ZOMBIE")) {
                stripped = stripped.substring(6);
            }
            if (!stripped.isEmpty()) {
                return "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_" + stripped;
            }
        }

        String base = alias.startsWith("Zombie") ? alias.substring(6) : alias;
        String underscored = base.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
        return "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_" + underscored;
    }
}