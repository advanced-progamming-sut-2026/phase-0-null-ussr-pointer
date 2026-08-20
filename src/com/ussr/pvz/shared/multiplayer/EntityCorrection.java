package com.ussr.pvz.shared.multiplayer;

public record EntityCorrection(
        String entityId,
        double x,
        double y,
        int hp,
        boolean alive
) {
    public EntityCorrection {
        if (entityId == null || entityId.isBlank()) {
            throw new IllegalArgumentException("entityId must not be blank");
        }
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("coordinates must be finite");
        }
        if (hp < 0) {
            throw new IllegalArgumentException("hp must not be negative");
        }
    }
}
