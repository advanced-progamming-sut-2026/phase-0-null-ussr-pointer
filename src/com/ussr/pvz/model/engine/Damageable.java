package com.ussr.pvz.model.engine;

import com.ussr.pvz.model.entities.zombies.Zombie;

public interface Damageable {
    boolean isAlive();
    void takeDamage(int damage);
}