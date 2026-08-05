package com.ussr.pvz.model.entities.zombies.zomboss;

import com.ussr.pvz.model.entities.zombies.zomboss.moves.*;

import java.util.HashMap;
import java.util.Map;

public final class ZombossMoveRegistry {

    private static final Map<String, ZombossMoveFactory> FACTORIES = new HashMap<>();

    static {
        register("FireballBarrage", (params, data) -> new FireballBarrageMove(params));
        register("RowIgnite", (params, data) -> new RowIgniteMove());
        register("EgyptRocket", (params, data) -> new EgyptRocketMove());
        register("ForwardDash", (params, data) -> new ForwardDashMove());
        register("IceRocket", (params, data) -> new IceRocketMove());
        register("IceWind", (params, data) -> new IceWindMove());
        register("ColumnFreeze", (params, data) -> new ColumnFreezeMove());
        register("BabySharks", (params, data) -> new BabySharksMove(params));
        register("Turbine", (params, data) -> new TurbinePullMove());
        register("RowSwitch", (params, data) -> new RowSwitchMove());
        register("SpawnMinions", (params, data) -> new SpawnMinionsMove(data));
    }

    private ZombossMoveRegistry() {
    }

    public static void register(String name, ZombossMoveFactory factory) {
        FACTORIES.put(name, factory);
    }

    public static ZombossMove create(String name, Map<String, Object> params, Map<String, Object> zombieData) {
        ZombossMoveFactory factory = FACTORIES.get(name);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown zomboss move: " + name);
        }
        return factory.create(params, zombieData);
    }
}