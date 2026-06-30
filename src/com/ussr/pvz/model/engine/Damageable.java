package com.ussr.pvz.model.engine;

public interface Damageable {
    // TODO: implement this from Plant.java and InteractableStructure.java
    boolean isAlive();
    void takeDamage(int damage);
}