package com.ussr.pvz.shared.multiplayer;

public record MatchDescriptor(
        String matchId,
        MatchRole role,
        String opponentUsername,
        String levelId,
        long seed,
        long startTimeMillis
) {
    public MatchDescriptor {
        requireNonBlank(matchId, "matchId");
        if (role == null) {
            throw new NullPointerException("role");
        }
        requireNonBlank(opponentUsername, "opponentUsername");
        requireNonBlank(levelId, "levelId");
        if (startTimeMillis <= 0) {
            throw new IllegalArgumentException("startTimeMillis must be positive");
        }
    }

    private static void requireNonBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
