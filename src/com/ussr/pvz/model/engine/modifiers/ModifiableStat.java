package com.ussr.pvz.model.engine.modifiers;

import java.util.ArrayList;
import java.util.List;

public class ModifiableStat {
    private float baseValue;
    private final List<StatModifier> modifiers = new ArrayList<>();

    public ModifiableStat(float baseValue) {
        this.baseValue = baseValue;
    }

    public void addModifier(StatModifier mod) {
        modifiers.removeIf(existing -> existing.getId().equals(mod.getId()));
        modifiers.add(mod);
    }

    public void update(float deltaTime) {
        modifiers.removeIf(mod -> mod.tickAndCheckExpired(deltaTime));
    }

    public float getValue() {
        float finalValue = baseValue;

        for (StatModifier mod : modifiers) {
            if (mod.apply(0) > 0 && mod.apply(1) == mod.apply(0) + 1) {
                finalValue = mod.apply(finalValue);
            }
        }

        for (StatModifier mod : modifiers) {
            if (mod.apply(1) != mod.apply(0) + 1) {
                finalValue = mod.apply(finalValue);
            }
        }
        return finalValue;
    }

    public void setBaseValue(int baseValue) {
        this.baseValue = baseValue;
    }
}