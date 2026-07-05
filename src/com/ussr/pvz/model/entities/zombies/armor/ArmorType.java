package com.ussr.pvz.model.entities.zombies.armor;

public enum ArmorType {
    CONE("cone", 370,false),
    BUCKET("bucket", 1100,true),
    HELMET("helmet", 1600,true),
    BRICK("brick", 2200,false),
    NEWSPAPER("newspaper", 800,false),
    CROWN("crown", 1600,true),
    SHOULDER_ARMOR("shoulderArmor", 1600,false);

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