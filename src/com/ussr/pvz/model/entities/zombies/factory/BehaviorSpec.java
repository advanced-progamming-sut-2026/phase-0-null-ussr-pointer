package com.ussr.pvz.model.entities.zombies.factory;

import java.util.Collections;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class BehaviorSpec {
    private final String type;
    private final Map<String, Object> params;

    private BehaviorSpec(String type, Map<String, Object> params) {
        this.type = type;
        this.params = params;
    }

    public static BehaviorSpec parse(Object raw) {
        if (raw instanceof String s) {
            return new BehaviorSpec(s, Collections.emptyMap());
        }
        if (raw instanceof Map<?, ?> m) {
            Map<String, Object> params = (Map<String, Object>) m;
            Object type = params.get("type");
            if (type == null) {
                throw new IllegalArgumentException("Behavior block is missing a \"type\" field: " + m);
            }
            return new BehaviorSpec((String) type, params);
        }
        throw new IllegalArgumentException("Unsupported behavior block shape: " + raw);
    }

    public String getType() {
        return type;
    }

    public Map<String, Object> params() {
        return params;
    }

    public static double getDouble(Map<String, Object> params, String key, double fallback) {
        Object v = params.get(key);
        return (v instanceof Number n) ? n.doubleValue() : fallback;
    }

    public static int getInt(Map<String, Object> params, String key, int fallback) {
        Object v = params.get(key);
        return (v instanceof Number n) ? n.intValue() : fallback;
    }

    public static boolean getBoolean(Map<String, Object> params, String key, boolean fallback) {
        Object v = params.get(key);
        return (v instanceof Boolean b) ? b : fallback;
    }

    public static String getString(Map<String, Object> params, String key, String fallback) {
        Object v = params.get(key);
        return (v instanceof String s) ? s : fallback;
    }
}