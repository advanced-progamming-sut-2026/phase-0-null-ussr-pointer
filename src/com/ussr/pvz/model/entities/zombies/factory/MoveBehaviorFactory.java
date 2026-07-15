package com.ussr.pvz.model.entities.zombies.factory;

import com.ussr.pvz.model.entities.zombies.move.MoveBehavior;
import java.util.Map;

public interface MoveBehaviorFactory {
    MoveBehavior create(Map<String, Object> params, Map<String, Object> zombieData);
}