package com.ussr.pvz.model.engine;

import com.ussr.pvz.model.entities.zombies.Zombie;

public interface Damageable {
    // TODO: implement this from Plant.java and InteractableStructure.java
    boolean isAlive();
    void takeDamage(int damage);
}