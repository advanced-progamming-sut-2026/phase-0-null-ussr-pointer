package com.ussr.pvz.model.entities.plants;

public enum PlantType {
    WALL_NUT("Wall-nut"),
    EXPLOSIVE("Explosive"),
    HOMING("Homing"),
    LOBBER("Lobber"),
    MELEE("Melee"),
    MODIFIER("Modifier"),
    SHOOTER("Shooter"),
    STRIKE_THROUGH("Strike-through"),
    SUN_PRODUCER("Sun Producer"),
    MINT("Mint");

    private final String name;

    //constructor
    PlantType(String name) {
        this.name = name;
    }

    //getter
    public String getName() { return this.name; }

    //helper
    public static PlantType getByName(String name) {
        for(PlantType type : PlantType.values()) {
            if(type.getName().equals(name))
                return type;
        }
        return null;
    }
}
