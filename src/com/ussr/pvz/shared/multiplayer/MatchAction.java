package com.ussr.pvz.shared.multiplayer;

import com.google.gson.JsonObject;

import java.util.Objects;

public record MatchAction(
        String matchId,
        String actionId,
        long sequence,
        MatchRole senderRole,
        MatchActionType type,
        JsonObject payload
) {
    public MatchAction {
        requireNonBlank(matchId, "matchId");
        requireNonBlank(actionId, "actionId");
        if (sequence <= 0) {
            throw new IllegalArgumentException("sequence must be positive");
        }
        Objects.requireNonNull(senderRole, "senderRole");
        Objects.requireNonNull(type, "type");
        payload = payload == null
                ? new JsonObject()
                : payload.deepCopy();
    }

    @Override
    public JsonObject payload() {
        return payload.deepCopy();
    }

    private static void requireNonBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
