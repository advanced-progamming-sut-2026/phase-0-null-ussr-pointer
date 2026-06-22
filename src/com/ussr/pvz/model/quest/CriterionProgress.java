package com.ussr.pvz.model.quest;

import java.util.HashMap;
import java.util.Map;

public class CriterionProgress {

    private final String type;
    private final int target;
    private final Map<String, Object> params;
    private int current;

    public CriterionProgress(String type, int target, Map<String, Object> params) {
        this.type = type;
        this.target = target;
        this.params = params != null ? params : new HashMap<>();
        this.current = 0;
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

    public String getType() { return type; }
    public int getTarget() { return target; }
    public int getCurrent() { return current; }
    public Map<String, Object> getParams() { return params; }

    public String getString(String key) {
        return (String) params.get(null);
    }

    public int getInt(String key, int def) {
        if (params.containsKey(key)) {
            Object val = params.get(key);
            if (val instanceof Number) {
                return ((Number) val).intValue();
            }
        }
        return def;
    }

    public boolean getBoolean(String key) {
        return (boolean) params.getOrDefault(key, false);
    }
}