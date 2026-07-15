package com.ussr.pvz.model.entities.plants.factory;

import com.ussr.pvz.model.entities.plants.AbilityType;
import com.ussr.pvz.model.entities.plants.PlantType;
import com.ussr.pvz.model.entities.plants.actstrategy.*;

import java.util.HashMap;
import java.util.Map;

public final class ActStrategyRegistry {
    private static final Map<AbilityType, ActStrategyFactory> FACTORIES = new HashMap<>();

    static {
        register(AbilityType.PRODUCE_SUN, data -> new SunProduceStrategy());
        register(AbilityType.INSTANT_SUN_BURST, data -> new SunProduceStrategy());
        register(AbilityType.SHOOT_PROJECTILE, data -> {
            String catStr = (String) data.get("category");
            PlantType category = catStr != null ? PlantType.valueOf(catStr) : null;
            if (category == null) return new ShootStrategy();
            return switch (category) {
                case HOMING -> new HomingStrategy();
                case STRIKE_THROUGH -> new StrikeStrategy();
                case LOBBER -> new LobberStrategy();
                default -> new ShootStrategy();
            };
        });
        register(AbilityType.DELAYED_EXPLOSIVE, data -> new ExplodeStrategy());
        register(AbilityType.INSTANT_EXPLOSIVE, data -> new ExplodeStrategy());
        register(AbilityType.MELEE_ATTACK, data -> new MeleeStrategy());
        register(AbilityType.PASSIVE_SHIELD, data -> new WallNutStrategy());
        register(AbilityType.MODIFIER_UTILITY, data -> new ModifyStrategy(1));
        register(AbilityType.MINT_FAMILY_BOOST, data -> new MintStrategy());
    }

    private ActStrategyRegistry() {}

    public static void register(AbilityType type, ActStrategyFactory factory) {
        FACTORIES.put(type, factory);
    }

    public static ActStrategy create(Map<String, Object> data) {
        String abilityTypeStr = (String) data.get("abilityType");
        if (abilityTypeStr == null || abilityTypeStr.equals("NONE")) return null;

        AbilityType type = AbilityType.valueOf(abilityTypeStr);
        ActStrategyFactory factory = FACTORIES.get(type);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown ActStrategy type: " + type);
        }
        return factory.create(data);
    }
}