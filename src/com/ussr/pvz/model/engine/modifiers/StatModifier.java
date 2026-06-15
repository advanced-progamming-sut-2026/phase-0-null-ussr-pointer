package com.ussr.pvz.model.engine.modifiers;

public class StatModifier {
    private final String id;
    private final ModifierType type;
    private final float value;
    private float duration;
    private final boolean isPermanent;

    public StatModifier(String id, ModifierType type, float value) {
        this.id = id;
        this.type = type;
        this.value = value;
        this.duration = 0;
        this.isPermanent = true;
    }

    public StatModifier(String id, ModifierType type, float value, float duration) {
        this.id = id;
        this.type = type;
        this.value = value;
        this.duration = duration;
        this.isPermanent = false;
    }

    public boolean tickAndCheckExpired(float deltaTime) {
        if (isPermanent) return false;
        duration -= deltaTime;
        return duration <= 0;
    }

    public float apply(float base) {
        return switch (type) {
            case FLAT -> base + value;
            case PERCENTAGE -> base * (1.0f + value);
        };
    }

    public String getId() { return id; }
}