package com.ussr.pvz.model.entities.items.sun;

public enum SunDropType {
    REGULAR(80,25,"768/INITIAL/EFFECTS/SUN/SUN.PAM" ),
    SPECIAL(15,100, "768/INITIAL/EFFECTS/SUN/SUN.PAM"),
    RADIOACTIVE(5,25, "768/FULL/EFFECTS/SUN_BOMB/SUN_BOMB.PAM");

    private final int probability;
    private final int value;
    private final String pamLocation;

    SunDropType(int probability, int value, String pamLocation) {
        this.probability = probability;
        this.value = value;
        this.pamLocation = pamLocation;
    }

    public int getValue() {
        return value;
    }
    public int getProbability() {
        return probability;
    }
    public String getPamLocation() {
        return pamLocation;
    }
}
