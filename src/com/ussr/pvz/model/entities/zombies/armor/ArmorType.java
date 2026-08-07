package com.ussr.pvz.model.entities.zombies.armor;

public enum ArmorType {
    CONE("cone", 370, false,
            "IMAGE_ZOMBIE_ZOMBIE_CARNIE_CONEHEAD_ZOMBIE_CARNIE_CONEHEAD_72X82_3",
            "IMAGE_ZOMBIE_ZOMBIE_CARNIE_CONEHEAD_ZOMBIE_CARNIE_CONEHEAD_72X82_2",-8f),
    BUCKET("bucket", 1100, true,
            "IMAGE_ZOMBIE_LNY_BUCKETHEAD_ZOMBIE_LNY_BUCKETHEAD_ZOMBIE_92X120_2",
            "IMAGE_ZOMBIE_LNY_BUCKETHEAD_ZOMBIE_LNY_BUCKETHEAD_ZOMBIE_92X91",-12f),
    HELMET("helmet", 1600, true,
            "IMAGE_ZOMBIE_ZOMBIE_CARNIE_BUCKETHEAD_ZOMBIE_CARNIE_BUCKETHEAD_99X137",
            "IMAGE_ZOMBIE_ZOMBIE_CARNIE_BUCKETHEAD_ZOMBIE_CARNIE_BUCKETHEAD_94X136",-12f),
    BRICK("brick", 2200, false,
            "IMAGE_ZOMBIE_ZOMBIE_FUTURE_BASIC_BRICK_ZOMBIE_FUTURE_BASIC_BRICK_96X97",
            "IMAGE_ZOMBIE_ZOMBIE_FUTURE_BASIC_BRICK_ZOMBIE_FUTURE_BASIC_BRICK_85X59",-10f),
    NEWSPAPER("newspaper", 800, false,
            "",
            "",0),
    CROWN("crown", 1600, true,
            "IMAGE_UI_JOUST_MATCH_RESULTS_CROWN_COLLECT_ANIM_CROWN_COLLECT_ANIM_135X94",
            "IMAGE_UI_JOUST_MATCH_RESULTS_CROWN_COLLECT_ANIM_CROWN_COLLECT_ANIM_135X94",-12f),
    SHOULDER_ARMOR("shoulderArmor", 1600, false,
            "IMAGE_ZOMBIE_ZOMBIE_ROMAN_SHIELD_ZOMBIE_ROMAN_SHIELD_39X40",
            "IMAGE_ZOMBIE_ZOMBIE_ROMAN_SHIELD_ZOMBIE_ROMAN_SHIELD_39X40",-8f);

    private final String name;
    private final int armorHp;
    private final boolean isMetal;
    private final String fullAtlas;
    private final String damagedAtlas;
    private final float offsetX;

    //constructor
    ArmorType(String name, int armorHp, boolean isMetal, String fullAtlas, String damagedAtlas, float offsetX) {
        this.name = name;
        this.armorHp = armorHp;
        this.isMetal = isMetal;
        this.fullAtlas = fullAtlas;
        this.damagedAtlas = damagedAtlas;
        this.offsetX = offsetX;
    }

    //getter
    public String getName() {
        return this.name;
    }

    public int getArmorHp() {
        return this.armorHp;
    }

    public boolean isMetal() {
        return this.isMetal;
    }

    //helper
    public static ArmorType getByName(String name) {
        for (ArmorType armor : ArmorType.values()) {
            if (armor.getName().equals(name))
                return armor;
        }
        return null;
    }

    public String getFullAtlas() {
        return this.fullAtlas;
    }

    public String getDamagedAtlas() {
        return this.damagedAtlas;
    }

    public float getOffsetX() {
        return this.offsetX;
    }
}