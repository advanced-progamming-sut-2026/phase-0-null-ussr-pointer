package com.ussr.pvz.model.entities.items.sun;

public enum SunDropType {
    SMALL(25),
    NORMAL(50),
    LARGE(75);

    private final int value;
    SunDropType(int value) {

        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
