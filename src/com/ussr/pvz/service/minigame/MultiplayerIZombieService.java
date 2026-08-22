package com.ussr.pvz.service.minigame;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.level.behavior.MultiplayerIZombieBehavior;
import com.ussr.pvz.network.NetworkClient;
import com.ussr.pvz.network.match.MatchActionBuffer;
import com.ussr.pvz.network.match.MatchContext;
import com.ussr.pvz.network.match.NetworkEntityRegistry;
import com.ussr.pvz.network.match.NetworkEventBridge;
import com.ussr.pvz.network.match.RemoteActionApplier;
import com.ussr.pvz.shared.multiplayer.MatchDescriptor;
import com.ussr.pvz.shared.multiplayer.MatchRole;
import com.ussr.pvz.shared.multiplayer.MatchServerMessage;
import com.ussr.pvz.shared.multiplayer.MatchServerMessageType;

import java.util.List;

/**
 * Client-side service that bootstraps a multiplayer i,Zombie session
 * from a {@link MatchDescriptor} received from the server.
 *
 * Call {@link #startMatch(MatchDescriptor)} exactly once, on the GL/game thread,
 * after the server sends MATCH_STARTED.
 */
public class MultiplayerIZombieService {

    // ── Layout constants — must match server's level config ───────────────────
    private static final int ROWS             = 5;
    private static final int COLS             = 9;
    private static final int RED_LINE_COLUMN  = 5;
    private static final int STARTING_SUN     = 150;

    // ── Singleton ─────────────────────────────────────────────────────────────
    private static final MultiplayerIZombieService INSTANCE =
            new MultiplayerIZombieService();

    public static MultiplayerIZombieService getInstance() {
        return INSTANCE;
    }

    private MultiplayerIZombieService() {}

    // ── Active bridge (kept so we can dispose on match end) ───────────────────
    private NetworkEventBridge activeBridge;

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Bootstraps the full multiplayer session.
     * Must be called on the LibGDX rendering thread (or inside postRunnable).
     */
    public void startMatch(MatchDescriptor descriptor) {
        MatchRole role = descriptor.role();

        // 1. Build the game session
        GameSession session = buildSession(descriptor);
        App.setGameSession(session);

        // 2. Build network layer
        MatchContext context         = new MatchContext(descriptor);
        NetworkEntityRegistry registry = new NetworkEntityRegistry();
        MatchActionBuffer buffer     = new MatchActionBuffer(descriptor.matchId());
        RemoteActionApplier applier  = new RemoteActionApplier(session, registry);

        NetworkEventBridge bridge = new NetworkEventBridge(
                context,
                session.getEventBus(),
                registry,
                buffer,
                applier,
                command -> {
                    try {
                        NetworkClient.getInstance().sendMatchCommand(command);
                    } catch (Exception e) {
                        System.err.println(
                                "[MultiplayerIZombieService] Failed to send command: "
                                        + e.getMessage());
                    }
                }
        );

        this.activeBridge = bridge;

        // 3. Register the bridge as the match-message handler
        NetworkClient.getInstance().setMatchMessageHandler(bridge::receive);

        // 4. Wire up session — run onStart (plants + brain setup etc.)
        Level level = session.getLevel();
        level.onStart();

        // 5. Init clock (registers tickables, lawn mowers, etc.)
        session.initClock();

        // 6. Init the bridge (subscribe to game events)
        bridge.init();

        // 7. Navigate to gameplay
        // AppView/the navigation layer will pick this up because
        // App.getGameSession() is now non-null.
        // If your project uses a MenuState for this, set it here:
        // App.setMenuState(MenuState.GAMEPLAY);
    }

    /**
     * Call when the match ends (MATCH_CLOSED received, or game over).
     * Safe to call multiple times.
     */
    public void endMatch() {
        if (activeBridge != null) {
            activeBridge.dispose();
            activeBridge = null;
        }
        NetworkClient.getInstance().setMatchMessageHandler(null);
    }

    // ── Session factory ───────────────────────────────────────────────────────

    private GameSession buildSession(MatchDescriptor descriptor) {
        GameSession session = new GameSession();

        // Build a fresh lawn
        Lawn lawn = new Lawn(ROWS, COLS);
        session.setLawn(lawn);
        session.setPlants(new java.util.ArrayList<>());
        session.setZombies(new java.util.ArrayList<>());
        session.setItems(new java.util.ArrayList<>());

        // Build the level
        Level level = buildLevel(descriptor);
        session.setLevel(level);

        // Progress must not be tracked for multiplayer
        session.setProgressTracked(false);

        return session;
    }

    private Level buildLevel(MatchDescriptor descriptor) {
        Level level = new Level();
        level.setId(descriptor.levelId());
        level.setChapter("multiplayer");
        level.setSunFalling(false);
        level.setWaves(List.of());

        // Zombie pool drawn from the actual i,Zombie minigame levels in levels.json.
        // Weights reflect relative cost/danger — cheaper/weaker zombies have higher weight.
        level.setAllowedZombies(List.of(
                new Level.AllowedZombie("ZombieImp",         1000),
                new Level.AllowedZombie("ZombieDefault",      500),
                new Level.AllowedZombie("ZombieArmor1",       400),
                new Level.AllowedZombie("ZombieArmor2",       300),
                new Level.AllowedZombie("ZombieNewspaper",    250),
                new Level.AllowedZombie("ZombieExplorer",     250),
                new Level.AllowedZombie("ZombieCrystalSkull", 200),
                new Level.AllowedZombie("ZombiePiano",        200),
                new Level.AllowedZombie("ZombieProspector",   200),
                new Level.AllowedZombie("ZombieGargantuar",    75)
        ));

        MultiplayerIZombieBehavior behavior =
                new MultiplayerIZombieBehavior(
                        RED_LINE_COLUMN,
                        STARTING_SUN,
                        descriptor.role()
                );
        level.setBehavior(behavior);

        return level;
    }

    // ── Incoming server-push dispatch (called from NetworkClient reader thread) ─

    /**
     * Feed a raw server-push message into the active match.
     * The NetworkClient already routes this to the bridge via
     * setMatchMessageHandler; this method exists as a manual escape hatch.
     */
    public void dispatch(MatchServerMessage message) {
        switch (message.type()) {
            case MATCH_STARTED -> {
                // Must post to GL thread if called from network thread
                com.badlogic.gdx.Gdx.app.postRunnable(
                        () -> startMatch(message.descriptor())
                );
            }
            case MATCH_ACTION, MATCH_CLOSED -> {
                if (activeBridge != null) activeBridge.receive(message);
                if (message.type() == MatchServerMessageType.MATCH_CLOSED) endMatch();
            }
        }
    }
}