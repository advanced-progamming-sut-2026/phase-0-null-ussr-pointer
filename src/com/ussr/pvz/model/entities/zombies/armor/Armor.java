package com.ussr.pvz.model.entities.zombies.armor;

public class Armor {
    private final ArmorType armorType;
    private int armorHp;
    private final int maxArmorHp;

    private static final double LAYER_1_THRESHOLD = 0.666;
    private static final double LAYER_2_THRESHOLD = 0.333;

    public Armor(ArmorType armorType, int startingHp) {
        this.armorType = armorType;
        this.maxArmorHp = startingHp;
        this.armorHp = startingHp;
    }

    public int takeDamage(int damage) {
        if (isDestroyed()) return damage;
        armorHp -= damage;
        if (armorHp <= 0) {
            int leftover = -armorHp;
            armorHp = 0;
            return leftover;
        }
        return 0;
    }

    public boolean isDestroyed() {
        return armorHp <= 0;
    }

    public int getArmorHp() {
        return armorHp;
    }

    public int getMaxArmorHp() {
        return maxArmorHp;
    }

    public ArmorType getArmorType() {
        return armorType;
    }

    public int getDamageLayer() {
        double ratio = (double) armorHp / maxArmorHp;
        if (ratio > LAYER_1_THRESHOLD) return 0;
        if (ratio > LAYER_2_THRESHOLD) return 1;
        return 2;
    }
}