package com.ussr.pvz.server;

import com.ussr.pvz.model.account.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-singleton that tracks:
 *  - which tokens are currently "in the lobby" (online players)
 *  - pending invites (inviter token → invited username)
 *  - the random matchmaking queue
 *  - confirmed random matches (token → opponent username)
 *
 * All public methods are thread-safe via synchronized or ConcurrentHashMap.
 */
public class LobbyManager {

    // ── Singleton ─────────────────────────────────────────────────────────────

    private static final LobbyManager INSTANCE = new LobbyManager();
    private final Map<String, String> inviteResults = new ConcurrentHashMap<>();
    public static LobbyManager getInstance() {
        return INSTANCE;
    }

    private LobbyManager() {}

    // ── Online presence ───────────────────────────────────────────────────────

    /** token → Account for every client currently in the lobby screen */
    private final Map<String, Account> onlinePlayers =
            new ConcurrentHashMap<>();

    public void playerEntered(String token, Account account) {
        onlinePlayers.put(token, account);
    }

    public void playerLeft(String token) {
        onlinePlayers.remove(token);
        cancelInviteByInviter(token);
        leaveRandomQueue(token);
        confirmedMatches.remove(token);
        // also remove any invite aimed at this account
        String username = usernameOf(token);
        if (username != null) {
            pendingInvites.values().removeIf(
                    inv -> inv.invitedUsername().equalsIgnoreCase(username)
            );
        }
    }

    /** Returns a snapshot list of (username, token) for every online player. */
    public synchronized List<OnlinePlayerInfo> getOnlinePlayers() {
        List<OnlinePlayerInfo> result = new ArrayList<>();
        for (Map.Entry<String, Account> e : onlinePlayers.entrySet()) {
            result.add(new OnlinePlayerInfo(
                    e.getValue().getName(),
                    e.getKey()
            ));
        }
        return result;
    }

    // ── Invites ───────────────────────────────────────────────────────────────

    /** inviter-token → pending invite */
    private final Map<String, PendingInvite> pendingInvites =
            new ConcurrentHashMap<>();

    /**
     * Send an invite from the given token to the player with the given username.
     *
     * @return error message, or null on success
     */
    public synchronized String sendInvite(String inviterToken, String invitedUsername) {
        Account inviter = onlinePlayers.get(inviterToken);
        if (inviter == null) {
            return "You are not in the lobby.";
        }
        if (inviter.getName().equalsIgnoreCase(invitedUsername)) {
            return "You cannot invite yourself.";
        }

        // find invited player's token
        Optional<Map.Entry<String, Account>> targetEntry = onlinePlayers.entrySet()
                .stream()
                .filter(e -> e.getValue().getName().equalsIgnoreCase(invitedUsername))
                .findFirst();

        if (targetEntry.isEmpty()) {
            return "That player is not online.";
        }

        // check if inviter already has a pending invite out
        if (pendingInvites.containsKey(inviterToken)) {
            return "You already have a pending invite. Cancel it first.";
        }

        // check if the invited player already has an invite pending at them
        String invitedToken = targetEntry.get().getKey();
        boolean alreadyInvited = pendingInvites.values().stream()
                .anyMatch(inv -> inv.invitedToken().equals(invitedToken));
        if (alreadyInvited) {
            return "That player already has a pending invite.";
        }

        pendingInvites.put(inviterToken, new PendingInvite(
                inviterToken,
                inviter.getName(),
                invitedToken,
                invitedUsername
        ));
        return null; // success
    }

    public synchronized void cancelInviteByInviter(String inviterToken) {
        pendingInvites.remove(inviterToken);
    }

    /**
     * Check whether there is an invite waiting for the given token.
     *
     * @return the invite, or null
     */
    public synchronized PendingInvite getInviteFor(String recipientToken) {
        return pendingInvites.values().stream()
                .filter(inv -> inv.invitedToken().equals(recipientToken))
                .findFirst()
                .orElse(null);
    }

    /**
     * Accept or reject an invite addressed to recipientToken.
     *
     * @param accepted true = accept, false = reject
     * @return error string on failure, null on success
     */
    public synchronized String respondToInvite(String recipientToken, boolean accepted) {
        PendingInvite invite = getInviteFor(recipientToken);
        if (invite == null) {
            return "No pending invite found.";
        }

        pendingInvites.remove(invite.inviterToken());

        // Always notify the inviter of the result
        inviteResults.put(
                invite.inviterToken(),
                accepted ? "ACCEPTED" : "REJECTED"
        );

        if (accepted) {
            Account inviter = onlinePlayers.get(invite.inviterToken());
            Account invited = onlinePlayers.get(recipientToken);
            String inviterName = inviter != null ? inviter.getName() : invite.inviterUsername();
            String invitedName = invited != null ? invited.getName() : invite.invitedUsername();

            confirmedMatches.put(invite.inviterToken(), invitedName);
            confirmedMatches.put(recipientToken, inviterName);
        }
        return null;
    }
    public String pollInviteResult(String inviterToken) {
        return inviteResults.remove(inviterToken);
    }

    // ── Random matchmaking queue ──────────────────────────────────────────────

    /** Ordered list of tokens waiting for a random opponent */
    private final List<String> randomQueue = new ArrayList<>();

    /** token → opponent username once a random match is confirmed */
    private final Map<String, String> confirmedMatches = new ConcurrentHashMap<>();

    public synchronized boolean joinRandomQueue(String token) {
        if (!onlinePlayers.containsKey(token)) return false;
        if (randomQueue.contains(token)) return true; // already queued

        // if someone else is already waiting, match immediately
        if (!randomQueue.isEmpty()) {
            String opponentToken = randomQueue.remove(0);
            Account me = onlinePlayers.get(token);
            Account opponent = onlinePlayers.get(opponentToken);

            if (me != null && opponent != null) {
                confirmedMatches.put(token, opponent.getName());
                confirmedMatches.put(opponentToken, me.getName());
                return true;
            }
        }

        randomQueue.add(token);
        return true;
    }

    public synchronized void leaveRandomQueue(String token) {
        randomQueue.remove(token);
    }

    /**
     * Returns the opponent username if a match was found, null otherwise.
     * Consuming this clears the entry so it is only delivered once.
     */
    public String pollRandomMatch(String token) {
        return confirmedMatches.remove(token);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String usernameOf(String token) {
        Account a = onlinePlayers.get(token);
        return a != null ? a.getName() : null;
    }

    // ── Value types ───────────────────────────────────────────────────────────

    public record OnlinePlayerInfo(String username, String token) {}

    public record PendingInvite(
            String inviterToken,
            String inviterUsername,
            String invitedToken,
            String invitedUsername
    ) {}
}