package com.ussr.pvz.model.entities.plants.factory;

import com.ussr.pvz.model.entities.plants.AbilityType;
import com.ussr.pvz.model.entities.plants.PlantJsonParser.PlantConfig;
import com.ussr.pvz.model.entities.plants.actstrategy.*;

import java.util.HashMap;
import java.util.Map;

public final class ActStrategyRegistry {
    private static final Map<AbilityType, ActStrategyFactory> FACTORIES = new HashMap<>();

    static {
        register(AbilityType.PRODUCE_SUN, config -> new SunProduceStrategy());
        register(AbilityType.INSTANT_SUN_BURST, config -> new SunProduceStrategy());
        register(AbilityType.SHOOT_PROJECTILE, config -> {
            if (config.category == null) return new ShootStrategy();
            return switch (config.category) {
                case HOMING -> new HomingStrategy();
                case STRIKE_THROUGH -> new StrikeStrategy();
                case LOBBER -> new LobberStrategy();
                default -> new ShootStrategy();
            };
        });
        register(AbilityType.DELAYED_EXPLOSIVE, config -> new ExplodeStrategy());
        register(AbilityType.INSTANT_EXPLOSIVE, config -> new ExplodeStrategy());
        register(AbilityType.MELEE_ATTACK, config -> new MeleeStrategy());
        register(AbilityType.PASSIVE_SHIELD, config -> new WallNutStrategy());
        register(AbilityType.MODIFIER_UTILITY, config -> new ModifyStrategy(1));
        register(AbilityType.MINT_FAMILY_BOOST, config -> new MintStrategy());
    }

    private ActStrategyRegistry() {}

    public static void register(AbilityType type, ActStrategyFactory factory) {
        FACTORIES.put(type, factory);
    }

    public static ActStrategy create(PlantConfig config) {
        if (config.abilityType == null) return null;
        ActStrategyFactory factory = FACTORIES.get(config.abilityType);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown ActStrategy type: " + config.abilityType);
        }
        return factory.create(config);
    }
}