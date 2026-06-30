package com.ussr.pvz.model.entities.zombies.attack;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;

public class LaserAttack implements AttackBehavior {
    @Override
    public void attack(Zombie zombie, GameSession session) {
        //todo this maybe useless cause the laser of Turquoise is implemented inside the effect
    }
}
