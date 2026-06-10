package model.entities.zombies.armor;

public enum ArmorType {
    CONE("cone" , 370),
    BUCKET("bucket" , 1100),
    HELMET("helmet" , 1600),
    BRICK("brick" , 2200);

    private final String name;
    private final int armorHp;

    //constructor
    ArmorType(String name , int armorHp) {
        this.name = name;
        this.armorHp = armorHp;
    }

    //getter
    public String getName() { return this.name; }
    public int getArmorHp() { return this.armorHp; }

    //helper
    public static ArmorType getByName(String name) {
        for(ArmorType armor : ArmorType.values()) {
            if(armor.getName().equals(name))
                return armor;
        }
        return null;
    }

}
