package com.ussr.pvz.network.match;

import com.ussr.pvz.model.engine.GameEntity;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class NetworkEntityRegistry {

    /*
     * Network ID -> local entity.
     */
    private final Map<String, GameEntity> entitiesById =
            new HashMap<>();

    /*
     * Local entity instance -> network ID.
     *
     * IdentityHashMap is intentional: entities are identified by
     * their actual object instance, not equals().
     */
    private final Map<GameEntity, String> idsByEntity =
            new IdentityHashMap<>();

    /**
     * Generates and registers a new network ID for a locally
     * created entity.
     */
    public synchronized String registerNew(GameEntity entity) {
        Objects.requireNonNull(entity, "entity");

        String existingId = idsByEntity.get(entity);

        if (existingId != null) {
            return existingId;
        }

        String entityId;

        do {
            entityId = UUID.randomUUID().toString();
        } while (entitiesById.containsKey(entityId));

        register(entityId, entity);
        return entityId;
    }

    /**
     * Registers an entity using an ID received from the network.
     */
    public synchronized void register(
            String entityId,
            GameEntity entity
    ) {
        requireNonBlank(entityId, "entityId");
        Objects.requireNonNull(entity, "entity");

        GameEntity entityWithSameId =
                entitiesById.get(entityId);

        if (entityWithSameId != null
                && entityWithSameId != entity) {
            throw new IllegalStateException(
                    "Entity ID is already registered: " + entityId
            );
        }

        String existingId = idsByEntity.get(entity);

        if (existingId != null
                && !existingId.equals(entityId)) {
            throw new IllegalStateException(
                    "Entity is already registered as " + existingId
            );
        }

        entitiesById.put(entityId, entity);
        idsByEntity.put(entity, entityId);
    }

    public synchronized Optional<GameEntity> find(
            String entityId
    ) {
        requireNonBlank(entityId, "entityId");
        return Optional.ofNullable(entitiesById.get(entityId));
    }

    /**
     * Finds an entity and verifies its expected subtype.
     */
    public synchronized <T extends GameEntity> Optional<T> find(
            String entityId,
            Class<T> expectedType
    ) {
        requireNonBlank(entityId, "entityId");
        Objects.requireNonNull(expectedType, "expectedType");

        GameEntity entity = entitiesById.get(entityId);

        if (entity == null || !expectedType.isInstance(entity)) {
            return Optional.empty();
        }

        return Optional.of(expectedType.cast(entity));
    }

    public synchronized GameEntity require(
            String entityId
    ) {
        return find(entityId).orElseThrow(
                () -> new IllegalStateException(
                        "Unknown network entity: " + entityId
                )
        );
    }

    public synchronized <T extends GameEntity> T require(
            String entityId,
            Class<T> expectedType
    ) {
        return find(entityId, expectedType).orElseThrow(
                () -> new IllegalStateException(
                        "Unknown network entity or incorrect type: "
                                + entityId
                )
        );
    }

    public synchronized Optional<String> networkIdOf(
            GameEntity entity
    ) {
        Objects.requireNonNull(entity, "entity");
        return Optional.ofNullable(idsByEntity.get(entity));
    }

    public synchronized boolean contains(String entityId) {
        requireNonBlank(entityId, "entityId");
        return entitiesById.containsKey(entityId);
    }

    public synchronized Optional<GameEntity> unregister(
            String entityId
    ) {
        requireNonBlank(entityId, "entityId");

        GameEntity removed = entitiesById.remove(entityId);

        if (removed != null) {
            idsByEntity.remove(removed);
        }

        return Optional.ofNullable(removed);
    }

    public synchronized Optional<String> unregister(
            GameEntity entity
    ) {
        Objects.requireNonNull(entity, "entity");

        String entityId = idsByEntity.remove(entity);

        if (entityId != null) {
            entitiesById.remove(entityId);
        }

        return Optional.ofNullable(entityId);
    }

    public synchronized int size() {
        return entitiesById.size();
    }

    public synchronized void clear() {
        entitiesById.clear();
        idsByEntity.clear();
    }

    private static void requireNonBlank(
            String value,
            String name
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    name + " must not be blank"
            );
        }
    }
}