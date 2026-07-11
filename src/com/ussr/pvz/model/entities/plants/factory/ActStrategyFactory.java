package com.ussr.pvz.model.entities.plants.factory;

import com.ussr.pvz.model.entities.plants.actstrategy.ActStrategy;
import com.ussr.pvz.model.entities.plants.PlantJsonParser.PlantConfig;

public interface ActStrategyFactory {
    ActStrategy create(PlantConfig config);
}