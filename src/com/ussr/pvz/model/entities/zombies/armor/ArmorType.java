package com.ussr.pvz.model.entities.zombies.armor;

public enum ArmorType {
    CONE("cone", 370),
    BUCKET("bucket", 1100),
    HELMET("helmet", 1600),
    BRICK("brick", 2200),
    NEWSPAPER("newspaper", 800),
    CROWN("crown", 1600),
    SHOULDER_ARMOR("shoulderArmor", 1600);

    private final String name;
    private final int armorHp;
    private final boolean isMetal;

    //constructor
    ArmorType(String name, int armorHp , boolean isMetal) {
        this.name = name;
        this.armorHp = armorHp;
        this.isMetal = isMetal;
    }

    //getter
    public String getName() {
        return this.name;
    }

    public int getArmorHp() {
        return this.armorHp;
    }

    public boolean isMetal() { return this.isMetal; }

    //helper
    public static ArmorType getByName(String name) {
        for (ArmorType armor : ArmorType.values()) {
            if (armor.getName().equals(name))
                return armor;
        }
        return null;
    }

}