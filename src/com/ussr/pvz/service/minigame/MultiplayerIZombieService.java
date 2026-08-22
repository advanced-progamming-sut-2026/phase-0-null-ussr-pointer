package com.ussr.pvz.service.minigame;

import com.badlogic.gdx.Gdx;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.board.structures.Brain;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.level.TerrainFactory;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MultiplayerIZombieService {

    private static final int ROWS = 5;
    private static final int COLS = 9;
    private static final int RED_LINE_COLUMN = 5;
    private static final int STARTING_SUN = 150;

    private static final List<String> MULTIPLAYER_PLANTS =
            List.of(
                    "Peashooter",
                    "Sunflower",
                    "WallNut",
                    "PotatoMine",
                    "SnowPea"
            );

    private static final MultiplayerIZombieService INSTANCE =
            new MultiplayerIZombieService();

    private NetworkEventBridge activeBridge;
    private MatchContext activeContext;
    private NetworkEntityRegistry activeRegistry;

    private MultiplayerIZombieService() {
    }

    public static MultiplayerIZombieService getInstance() {
        return INSTANCE;
    }

    /**
     * Install this handler after login and keep it installed for
     * the entire authenticated connection.
     */
    public void installNetworkHandler() {
        NetworkClient.getInstance()
                .setMatchMessageHandler(this::dispatch);
    }

    /**
     * Must run on the LibGDX rendering thread.
     */
    public void startMatch(
            MatchDescriptor descriptor
    ) {
        Objects.requireNonNull(
                descriptor,
                "descriptor"
        );

        /*
         * Ignore a retransmitted MATCH_STARTED for the current
         * match.
         */
        if (activeContext != null
                && activeContext.matchId().equals(
                descriptor.matchId()
        )
                && activeContext.isActive()) {
            return;
        }

        /*
         * Clean up a previous match before creating another one.
         */
        disposeActiveMatch();

        GameSession session =
                buildSession(descriptor);

        App.setGameSession(session);

        MatchContext context =
                new MatchContext(descriptor);

        NetworkEntityRegistry registry =
                new NetworkEntityRegistry();

        MatchActionBuffer buffer =
                new MatchActionBuffer(
                        descriptor.matchId()
                );

        RemoteActionApplier applier =
                new RemoteActionApplier(
                        session,
                        registry,
                        context
                );

        NetworkEventBridge bridge =
                new NetworkEventBridge(
                        context,
                        session.getEventBus(),
                        registry,
                        buffer,
                        applier,
                        command -> {
                            try {
                                NetworkClient
                                        .getInstance()
                                        .sendMatchCommand(command);

                            } catch (Exception exception) {
                                System.err.println(
                                        "[MultiplayerIZombieService] "
                                                + "Failed to send command: "
                                                + exception.getMessage()
                                );
                            }
                        }
                );

        activeContext = context;
        activeRegistry = registry;
        activeBridge = bridge;

        /*
         * initClock() creates ordinary lawnmowers. The multiplayer
         * behavior then removes them and replaces them with brains.
         */
        session.initClock();

        Level level =
                session.getLevel();

        level.onStart();

        registerInitialEntities(
                level,
                registry
        );

        /*
         * Subscribe only after initial entities have been created.
         * Their creation is deterministic and should not produce
         * outgoing spawn commands.
         */
        bridge.init();

        /*
         * Keep the service-level dispatcher installed. Do not
         * replace it with bridge::receive because this service is
         * responsible for MATCH_STARTED and MATCH_CLOSED.
         */
        installNetworkHandler();

        App.setMenuState(MenuState.GAME);
    }

    private GameSession buildSession(
            MatchDescriptor descriptor
    ) {
        GameSession session =
                new GameSession();

        /*
         * Lawn's constructor stores only its dimensions; it does not create
         * rows, cells, or tiles. The gameplay renderers expect a complete
         * grid, so build a normal deterministic lawn through TerrainFactory.
         */
        session.setLawn(
                TerrainFactory.build(
                        null,
                        ROWS,
                        COLS
                )
        );

        session.setPlants(
                new ArrayList<>()
        );

        session.setZombies(
                new ArrayList<>()
        );

        session.setItems(
                new ArrayList<>()
        );

        session.setLevel(
                buildLevel(descriptor)
        );

        session.setProgressTracked(false);

        if (descriptor.role() == MatchRole.PLANTS) {
            session.setSelectedPlants(
                    new ArrayList<>(
                            MULTIPLAYER_PLANTS
                    )
            );
        } else {
            session.setSelectedPlants(
                    new ArrayList<>()
            );
        }

        return session;
    }

    private Level buildLevel(
            MatchDescriptor descriptor
    ) {
        Level level = new Level();

        level.setId(
                descriptor.levelId()
        );

        level.setChapter("multiplayer");
        level.setSunFalling(false);
        level.setWaves(List.of());

        level.setAllowedZombies(
                List.of(
                        new Level.AllowedZombie(
                                "ZombieImp",
                                1000
                        ),
                        new Level.AllowedZombie(
                                "ZombieDefault",
                                500
                        ),
                        new Level.AllowedZombie(
                                "ZombieArmor1",
                                400
                        ),
                        new Level.AllowedZombie(
                                "ZombieArmor2",
                                300
                        ),
                        new Level.AllowedZombie(
                                "ZombieNewspaper",
                                250
                        ),
                        new Level.AllowedZombie(
                                "ZombieExplorer",
                                250
                        ),
                        new Level.AllowedZombie(
                                "ZombieCrystalSkull",
                                200
                        ),
                        new Level.AllowedZombie(
                                "ZombiePiano",
                                200
                        ),
                        new Level.AllowedZombie(
                                "ZombieProspector",
                                200
                        ),
                        new Level.AllowedZombie(
                                "ZombieGargantuar",
                                75
                        )
                )
        );

        level.setBehavior(
                new MultiplayerIZombieBehavior(
                        RED_LINE_COLUMN,
                        STARTING_SUN,
                        descriptor.role(),
                        descriptor.seed(),
                        descriptor.startTimeMillis()
                )
        );

        return level;
    }

    /**
     * Both clients create these entities deterministically, so
     * they must also assign exactly the same network IDs.
     */
    private void registerInitialEntities(
            Level level,
            NetworkEntityRegistry registry
    ) {
        if (!(level.getBehavior()
                instanceof MultiplayerIZombieBehavior behavior)) {
            throw new IllegalStateException(
                    "Incorrect multiplayer level behavior."
            );
        }

        for (Brain brain : behavior.getBrains()) {
            int lane =
                    (int) brain.getPosition().y();

            registry.register(
                    "brain-" + lane,
                    brain
            );
        }

        for (Zombie producer :
                behavior.getSunProducers()) {
            int lane =
                    (int) producer.getPosition().y();

            registry.register(
                    "sun-producer-" + lane,
                    producer
            );
        }
    }

    /**
     * Called by NetworkClient's dedicated reader thread.
     */
    public void dispatch(
            MatchServerMessage message
    ) {
        if (message == null) {
            return;
        }

        /*
         * Every branch is posted to the game thread. This ensures
         * neither the bridge nor the action applier mutates a
         * GameSession from the socket thread.
         */
        Gdx.app.postRunnable(
                () -> handleOnGameThread(message)
        );
    }

    private void handleOnGameThread(
            MatchServerMessage message
    ) {
        switch (message.type()) {
            case MATCH_STARTED ->
                    startMatch(
                            message.descriptor()
                    );

            case MATCH_ACTION -> {
                if (activeBridge == null
                        || activeContext == null
                        || !activeContext
                        .belongsToThisMatch(message)) {
                    return;
                }

                activeBridge.receive(message);
            }

            case MATCH_CLOSED -> {
                if (activeContext == null
                        || !activeContext
                        .belongsToThisMatch(message)) {
                    return;
                }

                activeBridge.receive(message);
                endMatch();
            }
        }
    }

    public void endMatch() {
        disposeActiveMatch();

        /*
         * Keep listening for the next MATCH_STARTED message.
         */
        installNetworkHandler();
    }

    private void disposeActiveMatch() {
        if (activeBridge != null) {
            activeBridge.dispose();
        }

        activeBridge = null;
        activeContext = null;
        activeRegistry = null;
    }

    public boolean hasActiveMatch() {
        return activeContext != null
                && activeContext.isActive();
    }

    public MatchContext getActiveContext() {
        return activeContext;
    }

    public NetworkEntityRegistry getActiveRegistry() {
        return activeRegistry;
    }
}
