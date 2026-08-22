package com.ussr.pvz.server.match;

import com.ussr.pvz.shared.multiplayer.MatchAction;
import com.ussr.pvz.shared.multiplayer.MatchDescriptor;

public interface MatchPeer {
    String token();
    String username();
    void sendMatchStarted(MatchDescriptor descriptor);
    void sendMatchAction(MatchAction action);
    void sendMatchClosed(String matchId, String reason);   // matchId added
}