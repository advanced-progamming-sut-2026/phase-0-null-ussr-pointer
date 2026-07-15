package com.ussr.pvz.model.entities.plants.factory;

import com.ussr.pvz.model.entities.plants.actstrategy.ActStrategy;
import java.util.Map;

public interface ActStrategyFactory {
    ActStrategy create(Map<String, Object> data);
}