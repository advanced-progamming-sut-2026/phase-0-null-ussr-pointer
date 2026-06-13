package com.ussr.pvz.model.quest;

import java.util.HashMap;
import java.util.Map;

public class CriterionProgress {

    private final String type;
    private final int target;
    private int current;
    private final Map<String, Object> params;

    public CriterionProgress(String type, int target, Map<String, Object> params) {
        this.type = type;
        this.target = target;
        this.params = params != null ? params : new HashMap<>();
    }

    public boolean isMet() {
        return current >= target;
    }

    public void increment(int amount) {
        current = Math.min(current + amount, target);
    }

    public void set(int value) {
        current = value;
    }

    public void reset() {
        current = 0;
    }

    public String getType() {
        return type;
    }

    public int getTarget() {
        return target;
    }

    public int getCurrent() {
        return current;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    // convenience — criteria check their own params without casting everywhere
    public String getString(String key) {
        return (String) params.getOrDefault(key, null);
    }

    public int getInt(String key, int def) {
        return params.containsKey(key) ? ((Number) params.get(key)).intValue() : def;
    }

    public boolean getBoolean(String key) {
        return (boolean) params.getOrDefault(key, false);
    }
}
