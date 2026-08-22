package com.ussr.pvz.server.match;

import com.ussr.pvz.shared.multiplayer.*;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class MatchRoom {

    private final String matchId;
    private final MatchPeer plants;
    private final MatchPeer zombies;
    private final AtomicLong sequenceCounter = new AtomicLong(1);
    private volatile boolean closed = false;

    public MatchRoom(MatchPeer plants, MatchPeer zombies) {
        if (plants == null || zombies == null)
            throw new NullPointerException("peers must not be null");
        this.matchId = UUID.randomUUID().toString();
        this.plants  = plants;
        this.zombies = zombies;
    }

    public String matchId() { return matchId; }

    /** Notifies both players that the match has started. */
    public void start(String levelId, long seed) {
        long startTime = System.currentTimeMillis();

        MatchDescriptor plantsDesc = new MatchDescriptor(
                matchId, MatchRole.PLANTS,
                zombies.username(), levelId, seed, startTime);

        MatchDescriptor zombiesDesc = new MatchDescriptor(
                matchId, MatchRole.ZOMBIES,
                plants.username(), levelId, seed, startTime);

        plants.sendMatchStarted(plantsDesc);
        zombies.sendMatchStarted(zombiesDesc);
    }

    /**
     * Receives a command from one peer, stamps it as a sequenced MatchAction,
     * and relays it to the opponent.
     */
    public synchronized void relay(MatchCommand command, String senderToken) {
        if (closed) return;

        MatchRole senderRole = roleOf(senderToken);
        if (senderRole == null) return;

        MatchAction action = new MatchAction(
                matchId,
                command.actionId(),
                sequenceCounter.getAndIncrement(),
                senderRole,
                command.type(),
                command.payload()
        );

        opponent(senderToken).sendMatchAction(action);

        if (command.type() == MatchActionType.GAME_OVER) {
            closeWith("GAME_OVER");
        }
    }

    /** Closes the room and notifies both peers. */
    public synchronized void close(String reason) {
        if (closed) return;
        closeWith(reason);
    }

    private void closeWith(String reason) {
        closed = true;
        plants.sendMatchClosed(matchId, reason);
        zombies.sendMatchClosed(matchId, reason);
    }

    public boolean isClosed() { return closed; }

    public boolean hasPeer(String token) {
        return plants.token().equals(token)
                || zombies.token().equals(token);
    }

    private MatchRole roleOf(String token) {
        if (plants.token().equals(token))  return MatchRole.PLANTS;
        if (zombies.token().equals(token)) return MatchRole.ZOMBIES;
        return null;
    }

    private MatchPeer opponent(String senderToken) {
        return plants.token().equals(senderToken) ? zombies : plants;
    }
}