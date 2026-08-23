package com.ussr.pvz.server.match;

import com.ussr.pvz.shared.multiplayer.MatchCommand;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class MatchManager {

    private static final String DEFAULT_LEVEL_ID =
            "multiplayer_izombie";

    private final Map<String, MatchRoom> rooms =
            new ConcurrentHashMap<>();

    private final Map<String, MatchRoom> roomByToken =
            new ConcurrentHashMap<>();

    /**
     * Used only to create a shared deterministic seed for both
     * clients. The server still contains no game simulation.
     */
    private final SecureRandom seedGenerator =
            new SecureRandom();

    /**
     * Authenticated token -> connected MatchPeer.
     */
    private final Map<
            String,
            ? extends MatchPeer
            > connectedPeers;

    public MatchManager(
            Map<String, ? extends MatchPeer> connectedPeers
    ) {
        this.connectedPeers =
                Objects.requireNonNull(
                        connectedPeers,
                        "connectedPeers"
                );
    }

    /**
     * Creates and starts one room for the supplied players.
     *
     * Existing callers may ignore the returned room.
     *
     * @throws IllegalArgumentException if either token is invalid
     * @throws IllegalStateException if a peer is unavailable or
     *                               already belongs to another room
     */
    public synchronized MatchRoom createRoom(
            String plantsToken,
            String zombiesToken
    ) {
        requireNonBlank(
                plantsToken,
                "plantsToken"
        );

        requireNonBlank(
                zombiesToken,
                "zombiesToken"
        );

        if (plantsToken.equals(zombiesToken)) {
            throw new IllegalArgumentException(
                    "A player cannot be matched with itself."
            );
        }

        MatchRoom plantsExistingRoom =
                roomByToken.get(plantsToken);

        MatchRoom zombiesExistingRoom =
                roomByToken.get(zombiesToken);

        /*
         * Repeated matchmaking confirmation for the same pair is
         * idempotent.
         */
        if (plantsExistingRoom != null
                && plantsExistingRoom
                == zombiesExistingRoom) {
            return plantsExistingRoom;
        }

        if (plantsExistingRoom != null) {
            throw new IllegalStateException(
                    "Plants player is already in a match."
            );
        }

        if (zombiesExistingRoom != null) {
            throw new IllegalStateException(
                    "Zombies player is already in a match."
            );
        }

        MatchPeer plants =
                connectedPeers.get(plantsToken);

        MatchPeer zombies =
                connectedPeers.get(zombiesToken);

        if (plants == null) {
            throw new IllegalStateException(
                    "Plants player is not connected."
            );
        }

        if (zombies == null) {
            throw new IllegalStateException(
                    "Zombies player is not connected."
            );
        }

        validatePeer(
                plants,
                plantsToken,
                "plants"
        );

        validatePeer(
                zombies,
                zombiesToken,
                "zombies"
        );

        MatchRoom room =
                new MatchRoom(
                        plants,
                        zombies
                );

        /*
         * Register the room before sending MATCH_STARTED. If a
         * client sends a command immediately, routing is already
         * available.
         */
        rooms.put(
                room.matchId(),
                room
        );

        roomByToken.put(
                plantsToken,
                room
        );

        roomByToken.put(
                zombiesToken,
                room
        );

        long seed =
                seedGenerator.nextLong();

        try {
            room.start(
                    DEFAULT_LEVEL_ID,
                    seed
            );

            System.out.println(
                    "[MatchManager] Match started: "
                            + plants.username()
                            + " (PLANTS) vs "
                            + zombies.username()
                            + " (ZOMBIES) - "
                            + room.matchId()
            );

            return room;

        } catch (RuntimeException exception) {
            cleanupRoom(room);

            try {
                room.close(
                        "MATCH_START_FAILED"
                );
            } catch (RuntimeException ignored) {
                /*
                 * The original start exception is more useful.
                 */
            }

            throw new IllegalStateException(
                    "Failed to start match.",
                    exception
            );
        }
    }

    /**
     * Routes a client command to the room containing the sender.
     *
     * MatchRoom performs match-ID, role, duplicate-action and
     * lifecycle validation.
     */
    public void handleCommand(
            MatchCommand command,
            String senderToken
    ) {
        Objects.requireNonNull(
                command,
                "command"
        );

        requireNonBlank(
                senderToken,
                "senderToken"
        );

        MatchRoom room =
                roomByToken.get(senderToken);

        if (room == null) {
            throw new IllegalStateException(
                    "Player is not currently in a match."
            );
        }

        if (!room.hasPeer(senderToken)) {
            throw new IllegalStateException(
                    "Player does not belong to the routed room."
            );
        }

        try {
            room.relay(
                    command,
                    senderToken
            );

        } finally {
            if (room.isClosed()) {
                cleanupRoom(room);
            }
        }
    }

    /**
     * Closes a room explicitly by match ID.
     */
    public void closeRoom(
            String matchId,
            String reason
    ) {
        requireNonBlank(
                matchId,
                "matchId"
        );

        requireNonBlank(
                reason,
                "reason"
        );

        MatchRoom room =
                rooms.get(matchId);

        if (room == null) {
            return;
        }

        try {
            room.close(reason);

        } finally {
            cleanupRoom(room);
        }
    }

    /**
     * Called before the disconnected peer's token is cleared.
     */
    public void onPeerDisconnected(
            MatchPeer peer
    ) {
        if (peer == null) {
            return;
        }

        String token =
                peer.token();

        if (token == null || token.isBlank()) {
            return;
        }

        MatchRoom room =
                roomByToken.get(token);

        if (room == null) {
            return;
        }

        try {
            room.peerDisconnected(token);

        } finally {
            cleanupRoom(room);
        }
    }

    /**
     * Allows lobby code to prevent a matched player from joining
     * another queue or sending another invitation.
     */
    public boolean isPlayerInMatch(
            String token
    ) {
        return token != null
                && roomByToken.containsKey(token);
    }

    public MatchRoom findRoomByToken(
            String token
    ) {
        if (token == null) {
            return null;
        }

        return roomByToken.get(token);
    }

    public MatchRoom findRoomById(
            String matchId
    ) {
        if (matchId == null) {
            return null;
        }

        return rooms.get(matchId);
    }

    public int activeRoomCount() {
        return rooms.size();
    }

    private void cleanupRoom(
            MatchRoom room
    ) {
        if (room == null) {
            return;
        }

        /*
         * Conditional removal prevents cleanup of an old room
         * from accidentally removing a player's newer room.
         */
        rooms.remove(
                room.matchId(),
                room
        );

        roomByToken.entrySet().removeIf(
                entry -> entry.getValue() == room
        );
    }

    private static void validatePeer(
            MatchPeer peer,
            String expectedToken,
            String peerName
    ) {
        if (peer.token() == null
                || !expectedToken.equals(
                peer.token()
        )) {
            throw new IllegalStateException(
                    peerName
                            + " peer token does not match "
                            + "the connected-peer registry."
            );
        }

        if (peer.username() == null
                || peer.username().isBlank()) {
            throw new IllegalStateException(
                    peerName
                            + " peer has no username."
            );
        }
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
