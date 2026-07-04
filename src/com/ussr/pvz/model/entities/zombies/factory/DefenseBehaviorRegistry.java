package com.ussr.pvz.model.entities.zombies.factory;

import com.ussr.pvz.model.entities.zombies.defense.ArmorDefense;
import com.ussr.pvz.model.entities.zombies.defense.DefenseBehavior;
import com.ussr.pvz.model.entities.zombies.defense.ImmuneDefense;
import com.ussr.pvz.model.entities.zombies.defense.JesterDefense;
import com.ussr.pvz.model.entities.zombies.defense.NormalDefense;
import com.ussr.pvz.model.entities.zombies.defense.ParasolDefense;

import java.util.HashMap;
import java.util.Map;

public final class DefenseBehaviorRegistry {

    private static final Map<String, DefenseBehaviorFactory> FACTORIES = new HashMap<>();

    static {
        register("NormalDefense", params -> new NormalDefense());
        register("ArmorDefense", params -> new ArmorDefense());
        register("ImmuneDefense", params -> new ImmuneDefense());
        register("ParasolDefense", params -> new ParasolDefense());
        register("JesterDefense", params -> new JesterDefense());
    }

    private DefenseBehaviorRegistry() {
    }

    public static void register(String type, DefenseBehaviorFactory factory) {
        FACTORIES.put(type, factory);
    }

    public static DefenseBehavior create(Object rawSpec) {
        BehaviorSpec spec = BehaviorSpec.parse(rawSpec);
        DefenseBehaviorFactory factory = FACTORIES.get(spec.getType());
        if (factory == null) {
            throw new IllegalArgumentException("Unknown defense behavior type: " + spec.getType());
        }
        return factory.create(spec.params());
    }
}