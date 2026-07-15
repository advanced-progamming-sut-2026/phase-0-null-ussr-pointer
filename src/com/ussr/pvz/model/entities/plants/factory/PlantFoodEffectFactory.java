package com.ussr.pvz.model.entities.plants.factory;

import com.ussr.pvz.model.entities.plants.plantfood.PlantFoodEffect;
import java.util.Map;

public interface PlantFoodEffectFactory {
    PlantFoodEffect create(Map<String, Object> data);
}