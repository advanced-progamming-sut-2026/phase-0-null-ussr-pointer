package com.ussr.pvz.model.entities.plants;

public enum Tag {
    DAY("Day"),
    SHROOM("Shroom"),
    WARM_UP("Warm-up"),
    NIGHT("Night"),
    PEA("Pea"),
    ICE("Ice"),
    STACK("Stack"),
    CHARGE("Charge"),
    MAGIC("Magic"),
    FIRE("Fire"),
    POISON("Poison"),
    WATER("Water"),
    AOE("AoE"),
    TRAP("Trap"),
    MOVE_ZOMBIE("MoveZombie"),
    EXPLOSIVE("Explosive"),
    SUN("Sun");

    private final String name;

    //constructor
    Tag(String name) {
        this.name = name;
    }

    //getter
    public String getName() { return this.name; }

    //helper
    public static Tag getByName(String name) {
        for(Tag t : Tag.values()) {
            if(t.getName().equals(name))
                return t;
        }
        return null;
    }
}
