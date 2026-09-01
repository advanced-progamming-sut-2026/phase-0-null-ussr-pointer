package com.ussr.pvz.model.entities.plants;

import com.ussr.pvz.model.engine.modifiers.ModifiableStat;

import java.util.ArrayList;

public final class PlantStateCopier {

    public static void copy(Plant target, Plant source) {
        target.setId(source.getId());
        target.setName(source.getName());
        target.setLevel(source.getLevel());
        target.setType(source.getType());
        target.getTags().clear();
        target.getTags().addAll(source.getTags());
        target.specialUpgrades.clear();
        target.specialUpgrades.putAll(source.specialUpgrades);
        target.setState(Plant.PlantState.ACTIVE);
        target.setLocation(source.getLocation());
        target.setActStrategy(source.getActStrategy());
        target.setPlantFoodEffect(source.getPlantFoodEffect());
        target.setWrampUp(source.getWrampUp());
        target.setPlantFoodType(source.getPlantFoodType());
        target.setShootingVectors(new ArrayList<>(source.getShootingVectors()));
        target.setProjectileOrigins(new ArrayList<>(source.getProjectileOrigins()));
        target.setHp(source.getHp());
        target.maxHp = source.maxHp;
        target.setCost(source.getCost());
        target.setDamage(source.getDamage());
        target.setActionInterval(source.getActionInterval());
        target.setAttackOffset(source.attackOffset);
        target.setRecharge(source.getRecharge());
        target.setMaxRecharge(source.getMaxRecharge());
        target.setAbilityValue(source.getAbilityValue());
        target.setLifetime(source.lifetime);
        target.remainingSmashes = source.remainingSmashes;
        target.hpStat = new ModifiableStat(target.getHp());
        target.actionIntervalStat =
                new ModifiableStat((float) target.getActionInterval());

        target.setPlantFoodTimer(source.getPlantFoodTimer());
        target.setPlantFoodDuration(source.plantFoodDuration);
        target.setArmor(source.getArmor());

        target.setProjectilePam(source.getProjectilePam());
        target.setHitPam(source.getHitPam());
        target.setPlantFoodHitPam(source.getPlantFoodHitPam());
        target.setButterHitPam(source.getButterHitPam());
        target.setPlantFoodProjectilePam(source.getPlantFoodProjectilePam());

        target.growthTracker = source.growthTracker;
        target.setPamPath(source.getPamPath());

        target.updateInternalTimer();
    }
}
