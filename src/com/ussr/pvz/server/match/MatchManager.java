package com.ussr.pvz.server.match;

import com.ussr.pvz.shared.multiplayer.MatchCommand;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MatchManager {

    private static final String DEFAULT_LEVEL_ID = "multiplayer_izombie";

    /** matchId → active room */
    private final Map<String, MatchRoom> rooms = new ConcurrentHashMap<>();

    /** token → room (for fast command routing) */
    private final Map<String, MatchRoom> roomByToken = new ConcurrentHashMap<>();

    /**
     * token → ClientHandler — injected from PvZServer so we can look up
     * both peers by token when a match is confirmed.
     */
    private final Map<String, ? extends MatchPeer> connectedPeers;

    public MatchManager(Map<String, ? extends MatchPeer> connectedPeers) {
        this.connectedPeers = connectedPeers;
    }

    /**
     * Called when LobbyManager has confirmed a match between two tokens.
     * Looks both peers up in the peer registry, assigns roles, and starts
     * the room.  Safe to call from any thread.
     *
     * @param plantsToken  token of the player who will be PLANTS
     * @param zombiesToken token of the player who will be ZOMBIES
     */
    public synchronized void createRoom(String plantsToken, String zombiesToken) {
        // Don't create a second room if one already exists for either token
        if (roomByToken.containsKey(plantsToken)
                || roomByToken.containsKey(zombiesToken)) {
            return;
        }

        MatchPeer plants  = connectedPeers.get(plantsToken);
        MatchPeer zombies = connectedPeers.get(zombiesToken);

        if (plants == null || zombies == null) {
            System.err.println("[MatchManager] Cannot create room — peer(s) not found.");
            return;
        }

        long seed = System.currentTimeMillis();
        MatchRoom room = new MatchRoom(plants, zombies);
        rooms.put(room.matchId(), room);
        roomByToken.put(plantsToken,  room);
        roomByToken.put(zombiesToken, room);

        System.out.println("[MatchManager] Match started: "
                + plants.username()  + " (PLANTS) vs "
                + zombies.username() + " (ZOMBIES) — "
                + room.matchId());

        room.start(DEFAULT_LEVEL_ID, seed);
    }

    /**
     * Routes a GAME_ACTION command from the given sender token to its room.
     */
    public void handleCommand(MatchCommand command, String senderToken) {
        MatchRoom room = roomByToken.get(senderToken);
        if (room == null) {
            System.err.println("[MatchManager] No room for token: " + senderToken);
            return;
        }
        room.relay(command, senderToken);
        if (room.isClosed()) {
            cleanupRoom(room);
        }
    }

    /**
     * Called when a client disconnects.
     * Closes their room (if any) and notifies the opponent.
     */
    public void onPeerDisconnected(MatchPeer peer) {
        if (peer == null || peer.token() == null) return;

        MatchRoom room = roomByToken.get(peer.token());
        if (room != null) {
            room.close("OPPONENT_DISCONNECTED");
            cleanupRoom(room);
        }
    }

    private void cleanupRoom(MatchRoom room) {
        rooms.remove(room.matchId());
        roomByToken.values().removeIf(r -> r == room);
    }
}