package com.ussr.pvz.model.quest;

import java.util.HashMap;
import java.util.Map;

public class CriterionProgress {

    private final String type;
    private int target;
    private final Map<String, Object> params;
    private int current;

    private static final int DEFAULT_COLS = 9;
    private static final int DEFAULT_ROWS = 5;
    private static final String[] CHAPTER_IDS = {
            "ancient_egypt", "big_wave_beach", "dark_ages", "frostbite_caves"
    };


    public CriterionProgress(String type, int target, Map<String, Object> params) {
        this.type = type;
        this.target = target;
        this.params = params != null ? params : new HashMap<>();
        this.current = 0;
    }

    public void resolveVariableOptions() {
        Object opts = params.get("variableOptions");
        if (opts instanceof java.util.List<?> options && !options.isEmpty()) {
            int choice = ((Number) options.get(
                    new java.util.Random().nextInt(options.size()))).intValue();
            if (params.containsKey("maxPlantsLost")) {
                params.put("maxPlantsLost", choice);
            } else {
                this.target = choice;
            }
        }

        if ("n".equals(params.get("targetColumnIndex"))) {
            params.put("targetColumnIndex", new java.util.Random().nextInt(DEFAULT_COLS));
        }
        if ("n".equals(params.get("targetRowIndex"))) {
            params.put("targetRowIndex", new java.util.Random().nextInt(DEFAULT_ROWS));
        }
        if ("any".equals(params.get("chapter"))) {
            params.put("chapter", CHAPTER_IDS[new java.util.Random().nextInt(CHAPTER_IDS.length)]);
        }
        if ("any_offensive".equals(params.get("plantType"))) {
            String chosen = pickRandomOffensivePlant();
            if (chosen != null) {
                params.put("plantType", chosen);
            }
        }
        if ("chosen_family".equals(params.get("familyType"))) {
            params.put("familyType", pickRandomFamily());
        }
        if ("chosen_family".equals(params.get("forbiddenFamilyType"))) {
            params.put("forbiddenFamilyType", pickRandomFamily());
        }
    }

    private String pickRandomOffensivePlant() {
        java.util.List<java.util.Map<String, Object>> plants = com.ussr.pvz.model.App.getCachedPlantsData();
        if (plants == null || plants.isEmpty()) return null;
        java.util.List<String> offensive = new java.util.ArrayList<>();
        for (java.util.Map<String, Object> p : plants) {
            Object dmg = p.get("damage");
            if (dmg instanceof Number n && n.doubleValue() > 0) {
                offensive.add((String) p.get("name"));
            }
        }
        if (offensive.isEmpty()) return null;
        return offensive.get(new java.util.Random().nextInt(offensive.size()));
    }

    private String pickRandomFamily() {
        java.util.List<java.util.Map<String, Object>> plants = com.ussr.pvz.model.App.getCachedPlantsData();
        if (plants == null) return null;
        java.util.Set<String> categories = new java.util.LinkedHashSet<>();
        for (java.util.Map<String, Object> p : plants) {
            Object dmg = p.get("damage");
            if (dmg instanceof Number n && n.doubleValue() > 0) {
                Object cat = p.get("category");
                if (cat != null) categories.add((String) cat);
            }
        }
        if (categories.isEmpty()) return null;
        java.util.List<String> list = new java.util.ArrayList<>(categories);
        return list.get(new java.util.Random().nextInt(list.size()));
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
        return (String) params.get(key);
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