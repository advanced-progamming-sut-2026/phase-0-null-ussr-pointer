package com.ussr.pvz.model.entities.zombies.zomboss;

import java.util.Map;

public interface ZombossMoveFactory {
    ZombossMove create(Map<String, Object> params, Map<String, Object> zombieData);
}