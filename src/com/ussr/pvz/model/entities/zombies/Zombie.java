package com.ussr.pvz.model.entities.zombies;

import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.entities.zombies.armor.Armor;
import com.ussr.pvz.model.entities.zombies.attack.AttackBehavior;
import com.ussr.pvz.model.entities.zombies.defense.DefenseBehavior;
import com.ussr.pvz.model.entities.zombies.effect.EffectStatus;
import com.ussr.pvz.model.entities.zombies.move.MoveBehavior;

import java.util.ArrayList;

public class Zombie extends GameEntity {
    private final String name;
    private final ArrayList<MoveBehavior> moveBehaviors = new ArrayList<>();
    private final ArrayList<EffectStatus> effectStatuses = new ArrayList<>();
    private final ArrayList<DefenseBehavior> defenseBehaviors = new ArrayList<>();
    private final ArrayList<AttackBehavior> attackBehaviors =  new ArrayList<>();
    private final Armor armor;
    private int hp;

    public Zombie(String name, Armor armor) {
        this.name = name;
        this.armor = armor;
    }


    @Override
    public void tick() {

    }
}
