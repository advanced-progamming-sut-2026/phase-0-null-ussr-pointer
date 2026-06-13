package com.ussr.pvz.model.entities.plants;

import com.ussr.pvz.model.engine.GameEntity;

import java.util.ArrayList;

public abstract class BasePlant extends GameEntity implements Plant {
    private final int recharge;
    private double actionInterval;
    private final int cost;
    private final ArrayList<Tag> tags = new ArrayList<>();
    private final PlantType type;
    private BasePlant bottom = null;
}
