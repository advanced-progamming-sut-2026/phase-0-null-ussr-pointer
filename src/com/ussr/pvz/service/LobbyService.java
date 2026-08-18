package com.ussr.pvz.service;

import com.google.gson.JsonObject;
import com.ussr.pvz.model.util.SessionManager;
import com.ussr.pvz.network.NetworkClient;
import com.ussr.pvz.shared.network.NetworkRequest;
import com.ussr.pvz.shared.network.NetworkResponse;
import com.ussr.pvz.shared.network.RequestType;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side service for all lobby/matchmaking operations.
 *
 * Every call is synchronous and returns a simple result record so
 * the UI layer never has to touch raw JSON.
 */
public class LobbyService {

    private final NetworkClient networkClient =
            NetworkClient.getInstance();

    // ── Online-player list ────────────────────────────────────────────────────

    /**
     * Fetch the list of currently-online player usernames (excludes self).
     * Also signals to the server that this client is in the lobby.
     */
    public List<String> getOnlinePlayers() {
        NetworkResponse response = send(
                new NetworkRequest(
                        RequestType.GET_ONLINE_PLAYERS,
                        token(),
                        null
                )
        );

        List<String> names = new ArrayList<>();

        if (response == null ||
                response.getData() == null ||
                !response.getData().has("players")) {
            return names;
        }

        for (com.google.gson.JsonElement el
                : response.getData().getAsJsonArray("players")) {
            JsonObject obj = el.getAsJsonObject();
            if (obj.has("username")) {
                names.add(obj.get("username").getAsString());
            }
        }

        return names;
    }

    // ── Invite ────────────────────────────────────────────────────────────────

    /** Send a game invite to another player. Returns an error string or null. */
    public String sendInvite(String targetUsername) {
        JsonObject data = new JsonObject();
        data.addProperty("invitedUsername", targetUsername);

        NetworkResponse response = send(
                new NetworkRequest(
                        RequestType.SEND_INVITE,
                        token(),
                        data
                )
        );

        if (response == null) {
            return "Could not connect to server.";
        }

        return response.isSuccess() ? null : response.getMessage();
    }

    /** Withdraw a previously sent invite. */
    public void cancelInvite() {
        send(new NetworkRequest(
                RequestType.CANCEL_INVITE,
                token(),
                null
        ));
    }

    /**
     * Accept or reject an incoming invite.
     *
     * @return error string, or null on success
     */
    public String respondToInvite(boolean accept) {
        JsonObject data = new JsonObject();
        data.addProperty("accepted", accept);

        NetworkResponse response = send(
                new NetworkRequest(
                        RequestType.RESPOND_INVITE,
                        token(),
                        data
                )
        );

        if (response == null) {
            return "Could not connect to server.";
        }

        return response.isSuccess() ? null : response.getMessage();
    }

    /**
     * Poll the server for an incoming invite.
     *
     * @return the inviter's username, or null if none pending
     */
    public String checkIncomingInvite() {
        NetworkResponse response = send(
                new NetworkRequest(
                        RequestType.CHECK_INVITE,
                        token(),
                        null
                )
        );

        if (response == null ||
                response.getData() == null ||
                !response.getData().has("hasInvite")) {
            return null;
        }

        boolean hasInvite =
                response.getData().get("hasInvite").getAsBoolean();

        if (!hasInvite) {
            return null;
        }

        return response.getData().has("fromUsername")
                ? response.getData().get("fromUsername").getAsString()
                : null;
    }

    // ── Random matchmaking ────────────────────────────────────────────────────

    /**
     * Join the random matchmaking queue.
     *
     * @return error string, or null on success
     */
    public String joinRandomQueue() {
        NetworkResponse response = send(
                new NetworkRequest(
                        RequestType.JOIN_RANDOM_QUEUE,
                        token(),
                        null
                )
        );

        if (response == null) {
            return "Could not connect to server.";
        }

        return response.isSuccess() ? null : response.getMessage();
    }

    /** Leave the matchmaking queue (called on cancel or screen exit). */
    public void leaveRandomQueue() {
        send(new NetworkRequest(
                RequestType.LEAVE_RANDOM_QUEUE,
                token(),
                null
        ));
    }

    /**
     * Poll whether a random match has been found.
     *
     * @return opponent username, or null if still searching
     */
    public String checkRandomMatch() {
        NetworkResponse response = send(
                new NetworkRequest(
                        RequestType.CHECK_RANDOM_MATCH,
                        token(),
                        null
                )
        );

        if (response == null ||
                response.getData() == null ||
                !response.getData().has("matched")) {
            return null;
        }

        boolean matched =
                response.getData().get("matched").getAsBoolean();

        if (!matched) {
            return null;
        }

        return response.getData().has("opponentUsername")
                ? response.getData().get("opponentUsername").getAsString()
                : null;
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private String token() {
        return SessionManager.getToken();
    }

    private NetworkResponse send(NetworkRequest request) {
        try {
            return networkClient.send(request);
        } catch (Exception e) {
            System.err.println("LobbyService network error: " + e.getMessage());
            return null;
        }
    }

    public String checkInviteResult() {
        NetworkResponse response = send(
                new NetworkRequest(
                        RequestType.CHECK_INVITE_RESULT,
                        token(),
                        null
                )
        );

        if (response == null ||
                response.getData() == null ||
                !response.getData().has("hasResult")) {
            return null;
        }

        boolean hasResult =
                response.getData().get("hasResult").getAsBoolean();

        if (!hasResult) {
            return null;
        }

        return response.getData().has("result")
                ? response.getData().get("result").getAsString()
                : null;
    }
}