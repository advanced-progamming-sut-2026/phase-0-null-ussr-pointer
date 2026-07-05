package com.ussr.pvz.model.entities.zombies.factory;

import com.ussr.pvz.model.entities.zombies.effect.EffectStatus;

import java.util.Map;

public interface EffectStatusFactory {
    EffectStatus create(Map<String, Object> params, Map<String, Object> zombieData);
}