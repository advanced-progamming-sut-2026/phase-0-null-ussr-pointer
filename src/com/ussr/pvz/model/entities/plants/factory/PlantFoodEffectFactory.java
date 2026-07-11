package com.ussr.pvz.model.entities.plants.factory;

import com.ussr.pvz.model.entities.plants.plantfood.PlantFoodEffect;
import com.ussr.pvz.model.entities.plants.PlantJsonParser.PlantConfig;

public interface PlantFoodEffectFactory {
    PlantFoodEffect create(PlantConfig config);
}