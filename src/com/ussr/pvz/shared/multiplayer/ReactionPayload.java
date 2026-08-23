package com.ussr.pvz.shared.multiplayer;

import com.google.gson.JsonObject;

/**
 * Payload for a {@link MatchActionType#REACTION} action.
 *
 * <p>Serialised as a flat {@link JsonObject} and stored in
 * {@link MatchAction#payload()} / {@link MatchCommand#payload()}
 * so the existing relay pipeline carries it without modification.</p>
 *
 * <ul>
 *   <li>{@code kind}  – TEXT | EMOJI | STICKER</li>
 *   <li>{@code index} – 0, 1 or 2 (which of the three options)</li>
 * </ul>
 */
public record ReactionPayload(ReactionKind kind, int index) {

    /** Serialise to the JsonObject used by MatchCommand/MatchAction. */
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("kind",  kind.name());
        obj.addProperty("index", index);
        return obj;
    }

    /** Deserialise from the JsonObject stored in a MatchAction payload. */
    public static ReactionPayload fromJson(JsonObject obj) {
        if (obj == null) throw new IllegalArgumentException("null payload");
        ReactionKind kind  = ReactionKind.valueOf(obj.get("kind").getAsString());
        int          index = obj.get("index").getAsInt();
        return new ReactionPayload(kind, index);
    }
}