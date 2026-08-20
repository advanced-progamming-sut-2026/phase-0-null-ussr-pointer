package com.ussr.pvz.network.match;

import com.ussr.pvz.shared.multiplayer.MatchAction;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

public final class MatchActionBuffer {

    private static final int MAX_PENDING_ACTIONS = 1024;

    private final String matchId;

    private final NavigableMap<Long, MatchAction> pending =
            new TreeMap<>();

    private long nextExpectedSequence;

    public MatchActionBuffer(String matchId) {
        this(matchId, 1);
    }

    public MatchActionBuffer(
            String matchId,
            long firstExpectedSequence
    ) {
        if (matchId == null || matchId.isBlank()) {
            throw new IllegalArgumentException(
                    "matchId must not be blank"
            );
        }

        if (firstExpectedSequence <= 0) {
            throw new IllegalArgumentException(
                    "firstExpectedSequence must be positive"
            );
        }

        this.matchId = matchId;
        this.nextExpectedSequence = firstExpectedSequence;
    }

    public synchronized List<MatchAction> offer(
            MatchAction action
    ) {
        Objects.requireNonNull(action, "action");

        if (!matchId.equals(action.matchId())) {
            throw new IllegalArgumentException(
                    "Action belongs to another match: "
                            + action.matchId()
            );
        }

        long sequence = action.sequence();

        if (sequence < nextExpectedSequence) {
            return List.of();
        }

        MatchAction existing = pending.get(sequence);

        if (existing != null) {
            if (existing.equals(action)) {
                return List.of();
            }

            throw new IllegalStateException(
                    "Conflicting actions received for sequence "
                            + sequence
            );
        }

        if (pending.size() >= MAX_PENDING_ACTIONS) {
            throw new IllegalStateException(
                    "Too many out-of-order match actions"
            );
        }

        pending.put(sequence, action);

        return drainReadyActions();
    }

    private List<MatchAction> drainReadyActions() {
        List<MatchAction> ready = new ArrayList<>();

        while (true) {
            MatchAction next =
                    pending.remove(nextExpectedSequence);

            if (next == null) {
                break;
            }

            ready.add(next);
            nextExpectedSequence++;
        }

        return List.copyOf(ready);
    }

    public synchronized long nextExpectedSequence() {
        return nextExpectedSequence;
    }

    public synchronized int pendingCount() {
        return pending.size();
    }

    public synchronized boolean hasGap() {
        return !pending.isEmpty()
                && pending.firstKey() > nextExpectedSequence;
    }

    public synchronized boolean isEmpty() {
        return pending.isEmpty();
    }

    public synchronized void clear() {
        pending.clear();
    }
}