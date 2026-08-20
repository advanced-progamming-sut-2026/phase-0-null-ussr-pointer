package com.ussr.pvz.network.match;

import com.google.gson.JsonObject;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.PlantFactory;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieActivity;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.util.Vec2;
import com.ussr.pvz.shared.multiplayer.MatchAction;

import java.util.Objects;

public final class RemoteActionApplier {
    private final GameSession session;
    private final NetworkEntityRegistry registry;

    public RemoteActionApplier(GameSession session, NetworkEntityRegistry registry) {
        this.session = Objects.requireNonNull(session, "session");
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    public void apply(MatchAction action) {
        Objects.requireNonNull(action, "action");
        JsonObject payload = action.payload();
        switch (action.type()) {
            case PLANT_PLACED -> applyPlantPlaced(payload);
            case PLANT_PLUCKED, PLANT_DIED -> removePlant(payload);
            case PLANT_FOOD_USED -> applyPlantFood(payload);
            case ZOMBIE_SPAWNED -> applyZombieSpawned(payload);
            case ZOMBIE_EATING -> applyZombieEating(payload);
            case ZOMBIE_DIED -> removeZombie(payload);
            case MOWER_TRIGGERED -> applyMower(payload);
            case ENTITY_CORRECTION -> applyCorrection(payload);
            case BRAIN_DAMAGED, BRAIN_EATEN, REACTION, GAME_OVER -> {
                // Handled by the multiplayer behavior or HUD layer.
            }
        }
    }

    private void applyPlantPlaced(JsonObject payload) {
        String id = string(payload, "entityId");
        if (registry.contains(id)) return;
        int row = integer(payload, "row");
        int col = integer(payload, "col");
        Cell cell = requireCell(row, col);
        if (cell.getPlant() != null) {
            throw new IllegalStateException("Remote plant cell is occupied");
        }
        Plant plant = PlantFactory.createPlantByName(string(payload, "plantName"), 1);
        plant.setLocation(new Plant.Location(col, row));
        plant.setPosition(Vec2.of(col, row));
        plant.setState(Plant.PlantState.ACTIVE);
        plant.setAlive(true);
        cell.setPlant(plant);
        registry.register(id, plant);
        session.addPlant(plant);
    }

    private void removePlant(JsonObject payload) {
        String id = string(payload, "entityId");
        Plant plant = registry.require(id, Plant.class);
        Plant.Location location = plant.getLocation();
        session.removePlantAt(location.x(), location.y());
        registry.unregister(id);
    }

    private void applyPlantFood(JsonObject payload) {
        Plant plant = registry.require(string(payload, "entityId"), Plant.class);
        if (plant.getPlantFoodEffect() != null) {
            plant.setBuffed(true);
            plant.getPlantFoodEffect().triggerSuperpower(plant, session);
        }
        session.notifyPlantFoodUsed(plant);
    }

    private void applyZombieSpawned(JsonObject payload) {
        String id = string(payload, "entityId");
        if (registry.contains(id)) return;
        int lane = integer(payload, "lane");
        double x = number(payload, "x");
        Zombie zombie = ZombieFactory.create(string(payload, "alias"), lane, (int) x);
        zombie.setPosition(Vec2.of(x, lane));
        if (payload.has("isGlowing")) zombie.setGlowing(payload.get("isGlowing").getAsBoolean());
        registry.register(id, zombie);
        session.spawnZombie(zombie);
    }

    private void applyZombieEating(JsonObject payload) {
        Zombie zombie = registry.require(string(payload, "zombieId"), Zombie.class);
        registry.require(string(payload, "plantId"), Plant.class);
        zombie.setState(ZombieActivity.EATING);
    }

    private void removeZombie(JsonObject payload) {
        String id = string(payload, "entityId");
        Zombie zombie = registry.require(id, Zombie.class);
        zombie.setHp(0);
        zombie.setAlive(false);
        zombie.startDeathTimer();
        registry.unregister(id);
    }

    private void applyMower(JsonObject payload) {
        int lane = integer(payload, "lane");
        session.getLawnMowers().stream()
                .filter(mower -> mower.getLane() == lane)
                .findFirst()
                .ifPresent(mower -> {
                    if (!mower.isActivated()) mower.activate();
                });
    }

    private void applyCorrection(JsonObject payload) {
        GameEntity entity = registry.require(string(payload, "entityId"));
        entity.setPosition(Vec2.of(number(payload, "x"), number(payload, "y")));
        entity.setAlive(bool(payload, "alive"));
        int hp = integer(payload, "hp");
        if (entity instanceof Plant plant) plant.setHp(hp);
        else if (entity instanceof Zombie zombie) zombie.setHp(hp);
    }

    private Cell requireCell(int row, int col) {
        if (session.getLawn() == null || row < 0 || col < 0
                || row >= session.getLawn().getRows() || col >= session.getLawn().getCols()) {
            throw new IllegalArgumentException("Invalid remote cell: " + row + "," + col);
        }
        return session.getLawn().getCell(row, col);
    }

    private static String string(JsonObject p, String key) {
        if (!p.has(key) || p.get(key).isJsonNull() || p.get(key).getAsString().isBlank())
            throw new IllegalArgumentException("Missing payload field: " + key);
        return p.get(key).getAsString();
    }
    private static int integer(JsonObject p, String key) {
        if (!p.has(key)) throw new IllegalArgumentException("Missing payload field: " + key);
        return p.get(key).getAsInt();
    }
    private static double number(JsonObject p, String key) {
        if (!p.has(key)) throw new IllegalArgumentException("Missing payload field: " + key);
        double value = p.get(key).getAsDouble();
        if (!Double.isFinite(value)) throw new IllegalArgumentException(key + " must be finite");
        return value;
    }
    private static boolean bool(JsonObject p, String key) {
        if (!p.has(key)) throw new IllegalArgumentException("Missing payload field: " + key);
        return p.get(key).getAsBoolean();
    }
}
