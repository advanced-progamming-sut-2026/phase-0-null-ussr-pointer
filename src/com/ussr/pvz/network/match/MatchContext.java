package com.ussr.pvz.network.match;

import com.google.gson.JsonObject;
import com.ussr.pvz.shared.multiplayer.MatchAction;
import com.ussr.pvz.shared.multiplayer.MatchActionType;
import com.ussr.pvz.shared.multiplayer.MatchCommand;
import com.ussr.pvz.shared.multiplayer.MatchDescriptor;
import com.ussr.pvz.shared.multiplayer.MatchRole;
import com.ussr.pvz.shared.multiplayer.MatchServerMessage;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public final class MatchContext {

    private final MatchDescriptor descriptor;

    /*
     * Null while the match is active.
     * Set once when the match closes.
     */
    private final AtomicReference<String> closeReason =
            new AtomicReference<>();

    public MatchContext(MatchDescriptor descriptor) {
        this.descriptor =
                Objects.requireNonNull(descriptor, "descriptor");
    }

    public MatchDescriptor descriptor() {
        return descriptor;
    }

    public String matchId() {
        return descriptor.matchId();
    }

    public MatchRole role() {
        return descriptor.role();
    }

    public String opponentUsername() {
        return descriptor.opponentUsername();
    }

    public String levelId() {
        return descriptor.levelId();
    }

    public long seed() {
        return descriptor.seed();
    }

    public long startTimeMillis() {
        return descriptor.startTimeMillis();
    }

    public boolean isPlantsPlayer() {
        return role() == MatchRole.PLANTS;
    }

    public boolean isZombiesPlayer() {
        return role() == MatchRole.ZOMBIES;
    }

    public boolean isActive() {
        return closeReason.get() == null;
    }

    public Optional<String> closeReason() {
        return Optional.ofNullable(closeReason.get());
    }

    public MatchCommand createCommand(
            MatchActionType type,
            JsonObject payload
    ) {
        if (!isActive()) {
            throw new IllegalStateException(
                    "Cannot create a command for a closed match."
            );
        }

        Objects.requireNonNull(type, "type");

        return new MatchCommand(
                matchId(),
                UUID.randomUUID().toString(),
                type,
                payload
        );
    }

    public boolean belongsToThisMatch(MatchAction action) {
        return action != null
                && matchId().equals(action.matchId());
    }

    public boolean belongsToThisMatch(
            MatchServerMessage message
    ) {
        return message != null
                && matchId().equals(message.matchId());
    }

    public boolean close(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Close reason must not be blank."
            );
        }

        return closeReason.compareAndSet(null, reason);
    }
}