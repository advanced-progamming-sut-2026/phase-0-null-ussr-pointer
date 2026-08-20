package com.ussr.pvz.shared.multiplayer;

public record MatchServerMessage(
        MatchServerMessageType type,
        String matchId,
        MatchDescriptor descriptor,
        MatchAction action,
        String reason
) {
    public MatchServerMessage {
        if (type == null) {
            throw new NullPointerException("type");
        }
        requireNonBlank(matchId, "matchId");

        switch (type) {
            case MATCH_STARTED -> {
                if (descriptor == null || action != null || reason != null) {
                    throw new IllegalArgumentException(
                            "MATCH_STARTED requires only a descriptor");
                }
                if (!matchId.equals(descriptor.matchId())) {
                    throw new IllegalArgumentException(
                            "message and descriptor matchId values must agree");
                }
            }
            case MATCH_ACTION -> {
                if (action == null || descriptor != null || reason != null) {
                    throw new IllegalArgumentException(
                            "MATCH_ACTION requires only an action");
                }
                if (!matchId.equals(action.matchId())) {
                    throw new IllegalArgumentException(
                            "message and action matchId values must agree");
                }
            }
            case MATCH_CLOSED -> {
                if (descriptor != null || action != null) {
                    throw new IllegalArgumentException(
                            "MATCH_CLOSED cannot contain a descriptor or action");
                }
                requireNonBlank(reason, "reason");
            }
        }
    }

    public static MatchServerMessage started(MatchDescriptor descriptor) {
        if (descriptor == null) {
            throw new NullPointerException("descriptor");
        }
        return new MatchServerMessage(
                MatchServerMessageType.MATCH_STARTED,
                descriptor.matchId(), descriptor, null, null);
    }

    public static MatchServerMessage action(MatchAction action) {
        if (action == null) {
            throw new NullPointerException("action");
        }
        return new MatchServerMessage(
                MatchServerMessageType.MATCH_ACTION,
                action.matchId(), null, action, null);
    }

    public static MatchServerMessage closed(String matchId, String reason) {
        return new MatchServerMessage(
                MatchServerMessageType.MATCH_CLOSED,
                matchId, null, null, reason);
    }

    private static void requireNonBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
