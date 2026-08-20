package com.ussr.pvz.network.match;

import com.google.gson.JsonObject;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.engine.event.GameEventBus;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.shared.multiplayer.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class NetworkEventBridge {
    private final MatchContext context;
    private final GameEventBus eventBus;
    private final NetworkEntityRegistry registry;
    private final MatchActionBuffer actionBuffer;
    private final RemoteActionApplier actionApplier;
    private final Consumer<MatchCommand> commandSender;
    private final List<GameEventBus.Subscription> subscriptions = new ArrayList<>();
    private boolean initialized;
    private boolean applyingRemoteAction;

    public NetworkEventBridge(MatchContext context, GameEventBus eventBus,
                              NetworkEntityRegistry registry, MatchActionBuffer actionBuffer,
                              RemoteActionApplier actionApplier,
                              Consumer<MatchCommand> commandSender) {
        this.context = Objects.requireNonNull(context, "context");
        this.eventBus = Objects.requireNonNull(eventBus, "eventBus");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.actionBuffer = Objects.requireNonNull(actionBuffer, "actionBuffer");
        this.actionApplier = Objects.requireNonNull(actionApplier, "actionApplier");
        this.commandSender = Objects.requireNonNull(commandSender, "commandSender");
    }

    public synchronized void init() {
        if (initialized) return;
        subscriptions.add(eventBus.subscribe(GameEvent.PlantPlanted.class, this::plantPlaced));
        subscriptions.add(eventBus.subscribe(GameEvent.PlantPlucked.class, this::plantPlucked));
        subscriptions.add(eventBus.subscribe(GameEvent.PlantFoodUsed.class, this::plantFoodUsed));
        subscriptions.add(eventBus.subscribe(GameEvent.PlantDied.class, this::plantDied));
        subscriptions.add(eventBus.subscribe(GameEvent.ZombieSpawned.class, this::zombieSpawned));
        subscriptions.add(eventBus.subscribe(GameEvent.ZombieDied.class, this::zombieDied));
        subscriptions.add(eventBus.subscribe(GameEvent.LawnMowerTriggered.class, this::mowerTriggered));
        subscriptions.add(eventBus.subscribe(GameEvent.GameWon.class,
                event -> send(MatchActionType.GAME_OVER, property("winnerRole", context.role().name()))));
        subscriptions.add(eventBus.subscribe(GameEvent.GameOver.class,
                event -> send(MatchActionType.GAME_OVER, property("winnerRole", oppositeRole().name()))));
        initialized = true;
    }

    public void receive(MatchServerMessage message) {
        if (!context.belongsToThisMatch(message)) return;
        switch (message.type()) {
            case MATCH_STARTED -> { }
            case MATCH_CLOSED -> context.close(message.reason());
            case MATCH_ACTION -> receiveAction(message.action());
        }
    }

    private void receiveAction(MatchAction incoming) {
        for (MatchAction action : actionBuffer.offer(incoming)) {
            if (action.senderRole() == context.role()) continue;
            applyingRemoteAction = true;
            try { actionApplier.apply(action); }
            finally { applyingRemoteAction = false; }
        }
    }

    private void plantPlaced(GameEvent.PlantPlanted event) {
        if (!canSend(MatchRole.PLANTS)) return;
        Plant plant = event.plant();
        JsonObject p = entityPayload(plant);
        p.addProperty("plantName", plant.getName());
        p.addProperty("row", event.row());
        p.addProperty("col", event.col());
        send(MatchActionType.PLANT_PLACED, p);
    }
    private void plantPlucked(GameEvent.PlantPlucked e) { sendRemovedPlant(MatchActionType.PLANT_PLUCKED, e.plant()); }
    private void plantFoodUsed(GameEvent.PlantFoodUsed e) { sendEntity(MatchRole.PLANTS, MatchActionType.PLANT_FOOD_USED, e.plant()); }
    private void plantDied(GameEvent.PlantDied e) { sendRemovedPlant(MatchActionType.PLANT_DIED, e.plant()); }
    private void zombieSpawned(GameEvent.ZombieSpawned event) {
        if (!canSend(MatchRole.ZOMBIES)) return;
        Zombie zombie = event.zombie();
        JsonObject p = entityPayload(zombie);
        p.addProperty("alias", zombie.getAlias());
        p.addProperty("lane", event.lane());
        p.addProperty("x", zombie.getPosition().x());
        p.addProperty("isGlowing", zombie.isGlowing());
        send(MatchActionType.ZOMBIE_SPAWNED, p);
    }
    private void zombieDied(GameEvent.ZombieDied e) {
        if (!canSendAnyRole()) return;
        registry.networkIdOf(e.zombie()).ifPresent(id -> {
            JsonObject p = new JsonObject(); p.addProperty("entityId", id);
            send(MatchActionType.ZOMBIE_DIED, p);
            registry.unregister(id);
        });
    }
    private void mowerTriggered(GameEvent.LawnMowerTriggered e) {
        if (!canSend(MatchRole.PLANTS)) return;
        JsonObject p = new JsonObject(); p.addProperty("lane", e.lane());
        send(MatchActionType.MOWER_TRIGGERED, p);
    }
    private void sendEntity(MatchRole role, MatchActionType type, Plant plant) {
        if (!canSend(role)) return;
        registry.networkIdOf(plant).ifPresent(id -> {
            JsonObject p = new JsonObject(); p.addProperty("entityId", id); send(type, p);
        });
    }
    private void sendRemovedPlant(MatchActionType type, Plant plant) {
        if (!canSend(MatchRole.PLANTS)) return;
        registry.networkIdOf(plant).ifPresent(id -> {
            JsonObject p = new JsonObject(); p.addProperty("entityId", id);
            send(type, p);
            registry.unregister(id);
        });
    }
    private JsonObject entityPayload(com.ussr.pvz.model.engine.GameEntity entity) {
        JsonObject p = new JsonObject(); p.addProperty("entityId", registry.registerNew(entity)); return p;
    }
    private boolean canSend(MatchRole role) { return canSendAnyRole() && context.role() == role; }
    private boolean canSendAnyRole() { return initialized && context.isActive() && !applyingRemoteAction; }
    private void send(MatchActionType type, JsonObject payload) {
        if (canSendAnyRole()) commandSender.accept(context.createCommand(type, payload));
    }
    private MatchRole oppositeRole() { return context.role() == MatchRole.PLANTS ? MatchRole.ZOMBIES : MatchRole.PLANTS; }
    private static JsonObject property(String key, String value) { JsonObject p = new JsonObject(); p.addProperty(key, value); return p; }

    public synchronized void dispose() {
        subscriptions.forEach(GameEventBus.Subscription::unsubscribe);
        subscriptions.clear(); initialized = false;
        actionBuffer.clear(); registry.clear();
    }
}
