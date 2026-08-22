package com.ussr.pvz.server.match;

import com.ussr.pvz.shared.multiplayer.MatchAction;
import com.ussr.pvz.shared.multiplayer.MatchActionType;
import com.ussr.pvz.shared.multiplayer.MatchCommand;
import com.ussr.pvz.shared.multiplayer.MatchDescriptor;
import com.ussr.pvz.shared.multiplayer.MatchRole;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public final class MatchRoom {

    private final String matchId;
    private final MatchPeer plants;
    private final MatchPeer zombies;

    private final AtomicLong sequenceCounter =
            new AtomicLong(1);

    /*
     * actionId -> authoritative action.
     *
     * This makes command retransmission idempotent and detects
     * conflicting reuse of an action ID.
     */
    private final Map<String, MatchAction> actionsById =
            new HashMap<>();

    private boolean started;
    private volatile boolean closed;

    public MatchRoom(
            MatchPeer plants,
            MatchPeer zombies
    ) {
        this.plants =
                Objects.requireNonNull(plants, "plants");

        this.zombies =
                Objects.requireNonNull(zombies, "zombies");

        requireValidPeer(plants, "plants");
        requireValidPeer(zombies, "zombies");

        if (plants.token().equals(zombies.token())) {
            throw new IllegalArgumentException(
                    "A player cannot be matched with itself."
            );
        }

        this.matchId = UUID.randomUUID().toString();
    }

    public String matchId() {
        return matchId;
    }

    public synchronized void start(
            String levelId,
            long seed
    ) {
        requireNonBlank(levelId, "levelId");

        if (closed) {
            throw new IllegalStateException(
                    "Cannot start a closed match."
            );
        }

        if (started) {
            throw new IllegalStateException(
                    "Match has already started."
            );
        }

        started = true;

        long startTimeMillis =
                System.currentTimeMillis();

        MatchDescriptor plantsDescriptor =
                new MatchDescriptor(
                        matchId,
                        MatchRole.PLANTS,
                        zombies.username(),
                        levelId,
                        seed,
                        startTimeMillis
                );

        MatchDescriptor zombiesDescriptor =
                new MatchDescriptor(
                        matchId,
                        MatchRole.ZOMBIES,
                        plants.username(),
                        levelId,
                        seed,
                        startTimeMillis
                );

        /*
         * Both descriptors use the same level, seed and start
         * time, but contain different local roles.
         */
        plants.sendMatchStarted(plantsDescriptor);
        zombies.sendMatchStarted(zombiesDescriptor);
    }

    /**
     * Converts a client command into an authoritative sequenced
     * action and broadcasts it to both clients.
     *
     * Broadcasting to both is required because sequence numbers
     * are global for the room. The sender's client advances its
     * action buffer but does not reapply its own action.
     */
    public synchronized void relay(
            MatchCommand command,
            String senderToken
    ) {
        Objects.requireNonNull(command, "command");
        requireNonBlank(senderToken, "senderToken");

        if (!started) {
            throw new IllegalStateException(
                    "Match has not started."
            );
        }

        if (closed) {
            throw new IllegalStateException(
                    "Match is already closed."
            );
        }

        if (!matchId.equals(command.matchId())) {
            throw new IllegalArgumentException(
                    "Command belongs to another match."
            );
        }

        MatchRole senderRole = roleOf(senderToken);

        if (senderRole == null) {
            throw new IllegalArgumentException(
                    "Sender is not a member of this match."
            );
        }

        validateRolePermission(
                senderRole,
                command.type()
        );

        MatchAction existing =
                actionsById.get(command.actionId());

        if (existing != null) {
            validateRetransmission(
                    existing,
                    command,
                    senderRole
            );

            /*
             * Rebroadcasting is safe. MatchActionBuffer ignores
             * an already-applied sequence.
             */
            broadcast(existing);
            return;
        }

        MatchAction action =
                new MatchAction(
                        matchId,
                        command.actionId(),
                        sequenceCounter.getAndIncrement(),
                        senderRole,
                        command.type(),
                        command.payload()
                );

        actionsById.put(
                action.actionId(),
                action
        );

        broadcast(action);

        if (action.type() == MatchActionType.GAME_OVER) {
            closeWith("GAME_OVER");
        }
    }

    private void broadcast(MatchAction action) {
        plants.sendMatchAction(action);
        zombies.sendMatchAction(action);
    }

    private void validateRetransmission(
            MatchAction existing,
            MatchCommand command,
            MatchRole senderRole
    ) {
        boolean sameCommand =
                existing.matchId().equals(command.matchId())
                        && existing.actionId().equals(
                        command.actionId()
                )
                        && existing.senderRole() == senderRole
                        && existing.type() == command.type()
                        && existing.payload().equals(
                        command.payload()
                );

        if (!sameCommand) {
            throw new IllegalArgumentException(
                    "Action ID was reused for a different command."
            );
        }
    }

    /**
     * Restricts commands to the role that owns the corresponding
     * player decision.
     */
    private void validateRolePermission(
            MatchRole senderRole,
            MatchActionType type
    ) {
        Objects.requireNonNull(type, "type");

        boolean permitted = switch (type) {
            case PLANT_PLACED,
                 PLANT_PLUCKED,
                 PLANT_FOOD_USED,
                 PLANT_DIED,
                 MOWER_TRIGGERED ->
                    senderRole == MatchRole.PLANTS;

            case ZOMBIE_SPAWNED,
                 ZOMBIE_EATING,
                 BRAIN_DAMAGED,
                 BRAIN_EATEN ->
                    senderRole == MatchRole.ZOMBIES;

            /*
             * Either simulation may be the first to detect these
             * events. Duplicate action IDs are still rejected or
             * treated as retransmissions.
             */
            case ZOMBIE_DIED,
                 ENTITY_CORRECTION,
                 REACTION,
                 GAME_OVER -> true;
        };

        if (!permitted) {
            throw new IllegalArgumentException(
                    senderRole
                            + " is not allowed to send "
                            + type
            );
        }
    }

    public synchronized void close(String reason) {
        requireNonBlank(reason, "reason");

        if (closed) {
            return;
        }

        closeWith(reason);
    }

    private void closeWith(String reason) {
        if (closed) {
            return;
        }

        closed = true;

        plants.sendMatchClosed(
                matchId,
                reason
        );

        zombies.sendMatchClosed(
                matchId,
                reason
        );
    }

    public boolean isClosed() {
        return closed;
    }

    public synchronized boolean isStarted() {
        return started;
    }

    public boolean hasPeer(String token) {
        if (token == null) {
            return false;
        }

        return token.equals(plants.token())
                || token.equals(zombies.token());
    }

    public MatchRole roleOf(String token) {
        if (token == null) {
            return null;
        }

        if (token.equals(plants.token())) {
            return MatchRole.PLANTS;
        }

        if (token.equals(zombies.token())) {
            return MatchRole.ZOMBIES;
        }

        return null;
    }

    private static void requireValidPeer(
            MatchPeer peer,
            String name
    ) {
        requireNonBlank(
                peer.token(),
                name + ".token"
        );

        requireNonBlank(
                peer.username(),
                name + ".username"
        );
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