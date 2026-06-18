package com.ussr.pvz.model.entities.items.sun;

public enum SunDropType {
    REGULAR(80,25),
    SPECIAL(15,100),
    RADIOACTIVE(5,25);

    private final int probability;
    private final int value;

    SunDropType(int probability,int value) {
        this.probability = probability;
        this.value = value;
    }

    public int getValue() {
        return value;
    }
    public int getProbability() {
        return probability;
    }
}
