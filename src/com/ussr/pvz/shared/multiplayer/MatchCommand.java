package com.ussr.pvz.shared.multiplayer;

import com.google.gson.JsonObject;

import java.util.Objects;

public record MatchCommand(
        String matchId,
        String actionId,
        MatchActionType type,
        JsonObject payload
) {
    public MatchCommand {
        requireNonBlank(matchId, "matchId");
        requireNonBlank(actionId, "actionId");
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
