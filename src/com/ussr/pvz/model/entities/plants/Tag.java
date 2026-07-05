package com.ussr.pvz.model.entities.plants;

import com.google.gson.annotations.SerializedName;

public enum Tag {
    DAY("Day"),
    SHROOM("Shroom"),
    @SerializedName("WRAMP-UP") WRAMP_UP("wramp-up"),
    NIGHT("night"),
    PEA("Pea"),
    ICE("Ice"),
    STACK("stack"),
    CHARGE("charge"),
    MAGIC("Magic"),
    FIRE("Fire"),
    POISON("Poison"),
    WATER("Water"),
    BOUNCE("bounce"),
    PIERCE("pierce"),
    DELAYED("delayed"),
    SPLASH("Splash"),
    INSTANT("Instant"),
    LANE("Lane"),
    STUN("Stun"),
    MULTIDIRECTIONAL("multidirectional"),
    GATLING("Gatling"),
    TIMED("timed"),
    SHIELD("Shield"),
    TALL("Tall"),
    MELEE("Melee"),
    DIVERT("Divert"),
    ATTRACT("Attract"),
    POCKETS("pockets"),
    DISARM("Disarm"),
    HYPNO("Hypno"),
    CLONE("Clone"),
    BESTER("Bester"),
    SUN("Sun"),
    AOE("Area damage"),
    @SerializedName("MOVE_ZOMBIE") MOVE_ZOMBIES("Move Zombies"),
    BUTTER("butter"),
    @SerializedName("EXPLOSIVE") EXPLOSIVE("Explosive"),
    @SerializedName("TRAP") TRAP("Trap");

    private final String name;

    Tag(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static Tag getByName(String name) {
        for (Tag t : Tag.values()) {
            if (t.getName().equalsIgnoreCase(name.strip()) || t.name().equalsIgnoreCase(name.strip()))
                return t;
        }
        return null;
    }
}