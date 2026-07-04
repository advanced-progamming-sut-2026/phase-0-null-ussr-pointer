package com.ussr.pvz.model.entities.zombies.factory;

import com.ussr.pvz.model.entities.zombies.attack.AttackBehavior;

import java.util.Map;

public interface AttackBehaviorFactory {
    AttackBehavior create(Map<String, Object> params, Map<String, Object> zombieData);
}