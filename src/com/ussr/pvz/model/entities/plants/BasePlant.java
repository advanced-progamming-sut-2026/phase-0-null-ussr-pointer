package com.ussr.pvz.model.entities.plants;

import com.ussr.pvz.model.engine.GameEntity;

import java.util.ArrayList;

public abstract class BasePlant extends GameEntity implements Plant {
    private int recharge;
    private double actionInterval;
    private int cost;
    private final ArrayList<Tag> tags = new ArrayList<>();
    private PlantType type;
    private BasePlant bottom = null;
    private int hp;

}
