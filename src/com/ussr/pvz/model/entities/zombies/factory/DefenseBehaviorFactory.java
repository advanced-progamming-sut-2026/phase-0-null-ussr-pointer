package com.ussr.pvz.model.entities.zombies.factory;

import com.ussr.pvz.model.entities.zombies.defense.DefenseBehavior;

import java.util.Map;

public interface DefenseBehaviorFactory {
    DefenseBehavior create(Map<String, Object> params);
}