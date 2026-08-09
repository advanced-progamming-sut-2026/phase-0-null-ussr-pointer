package com.ussr.pvz.model.entities.plants.upgrades;

public enum SpecialUpgrade {
    DOUBLE_SUN_CHANCE,
    GROW_TIME_REDUCTION,
    SUN_AMOUNT_BUFF,
    CHILL_DURATION_EXT,
    PRIORITIZE_GARGANTUARS,
    ADDITIONAL_PIERCE,
    POISON_TICK_BUFF,
    AUTO_PLANT_FOOD_CHANCE,
    TILE_RANGE_EXT,
    LIFESPAN_EXT,
    BUTTER_CHANCE_BUFF,
    SPLASH_DAMAGE_BUFF,
    WARM_RADIUS_EXT,
    BONUS_SMASH_CHARGES,
    GRAPE_BOUNCE_EXT,
    BONUS_GRAB_TARGETS,
    FREEZE_DURATION_EXT,
    GROWTH_STAGE_MAX_UP,
    REFLECT_DAMAGE_BUFF,
    SUN_DROP_INCREMENT,
    DEATH_EXPLOSION_AOE,
    ZOMBIE_HEALTH_MULTIPLIER,
    ZOMBIE_DAMAGE_MULTIPLIER,
    AUTO_PLANTFOOD_ON_ENTER,
    MELT_AREA_3X3,
    EXPLODE_ON_FINISH,
    DURATION_EXT,
    RESET_FAMILY_COOLDOWNS;

    public static SpecialUpgrade fromJson(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            System.err.println("Unknown special upgrade: " + value);
            return null;
        }
    }
}