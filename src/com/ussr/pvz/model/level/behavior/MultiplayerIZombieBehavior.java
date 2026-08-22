package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.structures.Brain;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.entities.zombies.ZombieSize;
import com.ussr.pvz.model.entities.zombies.attack.ChompAttack;
import com.ussr.pvz.model.entities.zombies.defense.NormalDefense;
import com.ussr.pvz.model.entities.zombies.effect.SunProducerZombieEffect;
import com.ussr.pvz.model.entities.zombies.move.StationaryMove;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.util.Vec2;
import com.ussr.pvz.shared.multiplayer.MatchRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class MultiplayerIZombieBehavior
        extends LevelBehavior {

    private static final float MATCH_DURATION_SECONDS =
            120f;

    private static final int DEFAULT_STARTING_SUN = 150;
    private static final int SUN_PRODUCER_HP = 1300;

    private final int redLineColumn;
    private final int startingSun;
    private final MatchRole localRole;
    private final long matchStartTimeMillis;
    private final Random random;

    private final List<Brain> brains =
            new ArrayList<>();

    private final List<Zombie> sunProducers =
            new ArrayList<>();

    private float timeRemaining =
            MATCH_DURATION_SECONDS;

    private boolean started;
    private boolean missionFailed;
    private MatchRole winner;

    public MultiplayerIZombieBehavior(
            int redLineColumn,
            int startingSun,
            MatchRole localRole,
            long seed,
            long matchStartTimeMillis
    ) {
        if (redLineColumn < 0) {
            throw new IllegalArgumentException(
                    "redLineColumn must not be negative"
            );
        }

        if (matchStartTimeMillis <= 0) {
            throw new IllegalArgumentException(
                    "matchStartTimeMillis must be positive"
            );
        }

        this.redLineColumn = redLineColumn;
        this.startingSun =
                startingSun > 0
                        ? startingSun
                        : DEFAULT_STARTING_SUN;

        this.localRole =
                Objects.requireNonNull(
                        localRole,
                        "localRole"
                );

        this.matchStartTimeMillis =
                matchStartTimeMillis;

        /*
         * Both clients receive the same seed, so initial entity
         * positions are identical.
         */
        this.random = new Random(seed);

        this.autoWinOnWavesClear = false;
        this.waitForManualWaveStart = true;
    }

    @Override
    public void onStart(Level level) {
        Objects.requireNonNull(level, "level");

        if (started) {
            return;
        }

        GameSession session =
                App.getGameSession();

        if (session == null
                || session.getLawn() == null) {
            throw new IllegalStateException(
                    "Cannot start multiplayer behavior "
                            + "without an active game session and lawn."
            );
        }

        int rows = session.getLawn().getRows();
        int columns = session.getLawn().getCols();

        if (redLineColumn >= columns) {
            throw new IllegalStateException(
                    "redLineColumn must be inside the lawn."
            );
        }

        started = true;

        level.setSunFalling(false);

        synchronizeMatchTimer();
        initializeSun(session);

        brains.clear();
        sunProducers.clear();

        /*
         * GameSession.initClock() creates normal lawnmowers.
         * Multiplayer i,Zombie replaces them with brains.
         */
        removeLawnMowers(session);

        placeBrains(session, rows);
        placeSunProducers(
                session,
                rows,
                columns
        );
    }

    private void synchronizeMatchTimer() {
        long elapsedMillis =
                Math.max(
                        0L,
                        System.currentTimeMillis()
                                - matchStartTimeMillis
                );

        float elapsedSeconds =
                elapsedMillis / 1000f;

        timeRemaining =
                Math.max(
                        0f,
                        MATCH_DURATION_SECONDS
                                - elapsedSeconds
                );
    }

    private void initializeSun(GameSession session) {
        int difference =
                startingSun - session.getSunCount();

        if (difference != 0) {
            session.addSun(difference);
        }
    }

    private void removeLawnMowers(
            GameSession session
    ) {
        session.getLawnMowers().forEach(
                mower -> mower.setAlive(false)
        );

        session.getLawnMowers().clear();
    }

    private void placeBrains(
            GameSession session,
            int rows
    ) {
        for (int lane = 0; lane < rows; lane++) {
            Brain brain = new Brain();

            brain.setPosition(
                    Vec2.of(-0.5, lane)
            );

            brains.add(brain);
            session.registerStructure(brain);
        }
    }

    private void placeSunProducers(
            GameSession session,
            int rows,
            int columns
    ) {
        int zombieZoneWidth =
                columns - redLineColumn;

        if (zombieZoneWidth <= 0) {
            throw new IllegalStateException(
                    "The zombie placement zone is empty."
            );
        }

        for (int lane = 0; lane < rows; lane++) {
            int column =
                    redLineColumn
                            + random.nextInt(
                            zombieZoneWidth
                    );

            Zombie producer =
                    createSunProducer(
                            lane,
                            column
                    );

            sunProducers.add(producer);
            session.spawnZombie(producer);
        }
    }

    private Zombie createSunProducer(
            int lane,
            int column
    ) {
        Zombie producer =
                new Zombie(
                        "SunProducerZombie",
                        null,
                        false
                );

        producer.setMaxHp(SUN_PRODUCER_HP);
        producer.setHp(SUN_PRODUCER_HP);
        producer.setEatDps(0);
        producer.setSize(ZombieSize.DEFAULT);

        producer.setPosition(
                Vec2.of(column, lane)
        );

        producer.setMoveBehavior(
                new StationaryMove()
        );

        producer.setAttackBehavior(
                new ChompAttack()
        );

        producer.setDefenseBehavior(
                new NormalDefense()
        );

        producer.setEffectStatus(
                new SunProducerZombieEffect()
        );

        return producer;
    }

    @Override
    public void tick(
            GameSession session,
            double deltaTime
    ) {
        if (!started
                || levelCompleted
                || missionFailed
                || session == null
                || session.isGameOver()) {
            return;
        }

        if (deltaTime > 0) {
            timeRemaining = Math.max(
                    0f,
                    timeRemaining - (float) deltaTime
            );
        }

        if (allBrainsEaten()) {
            endMatch(
                    session,
                    MatchRole.ZOMBIES
            );
            return;
        }

        if (timeRemaining <= 0f) {
            endMatch(
                    session,
                    MatchRole.PLANTS
            );
            return;
        }

        /*
         * Sun is intentionally local. Only the zombies client can
         * determine whether the zombies player is economically
         * unable to continue.
         */
        if (localRole == MatchRole.ZOMBIES
                && zombiePlayerIsStuck(session)) {
            endMatch(
                    session,
                    MatchRole.PLANTS
            );
        }
    }

    private boolean allBrainsEaten() {
        return !brains.isEmpty()
                && brains.stream()
                .noneMatch(Brain::isAlive);
    }

    private boolean zombiePlayerIsStuck(
            GameSession session
    ) {
        int cheapestZombieCost =
                session.getLevel()
                        .getAllowedZombies()
                        .stream()
                        .mapToInt(
                                allowed ->
                                        ZombieFactory.getZombieCost(
                                                allowed.id()
                                        )
                        )
                        .filter(cost -> cost > 0)
                        .min()
                        .orElse(50);

        boolean cannotAffordZombie =
                session.getSunCount()
                        < cheapestZombieCost;

        boolean hasNoAttackers =
                session.getZombies()
                        .stream()
                        .filter(Objects::nonNull)
                        .filter(zombie ->
                                !"SunProducerZombie"
                                        .equalsIgnoreCase(
                                                zombie.getAlias()
                                        )
                        )
                        .noneMatch(GameEntity::isAlive);

        boolean hasNoProducers =
                session.getZombies()
                        .stream()
                        .filter(Objects::nonNull)
                        .filter(zombie ->
                                "SunProducerZombie"
                                        .equalsIgnoreCase(
                                                zombie.getAlias()
                                        )
                        )
                        .noneMatch(GameEntity::isAlive);

        return cannotAffordZombie
                && hasNoAttackers
                && hasNoProducers;
    }

    private void endMatch(
            GameSession session,
            MatchRole winner
    ) {
        if (levelCompleted
                || missionFailed
                || this.winner != null) {
            return;
        }

        this.winner =
                Objects.requireNonNull(
                        winner,
                        "winner"
                );

        if (winner == localRole) {
            levelCompleted = true;
            session.concludeVictory();
        } else {
            missionFailed = true;
            session.concludeDefeat();
        }
    }

    @Override
    public void onZombieBreach(
            GameSession session,
            Zombie zombie
    ) {
        /*
         * Reaching the left edge is expected in i,Zombie.
         * The zombies side wins only when every brain is eaten.
         */
    }

    @Override
    public boolean isFailed(Level level) {
        return missionFailed;
    }

    public boolean isStarted() {
        return started;
    }

    public MatchRole getLocalRole() {
        return localRole;
    }

    public boolean isPlantsPlayer() {
        return localRole == MatchRole.PLANTS;
    }

    public boolean isZombiesPlayer() {
        return localRole == MatchRole.ZOMBIES;
    }

    public MatchRole getWinner() {
        return winner;
    }

    public float getTimeRemaining() {
        return Math.max(
                0f,
                timeRemaining
        );
    }

    public int getRedLineColumn() {
        return redLineColumn;
    }

    public Brain getBrainInLane(int lane) {
        return brains.stream()
                .filter(brain ->
                        brain.getPosition() != null
                                && (int) brain
                                .getPosition()
                                .y() == lane
                )
                .findFirst()
                .orElse(null);
    }

    public List<Brain> getBrains() {
        return List.copyOf(brains);
    }

    public List<Zombie> getSunProducers() {
        return List.copyOf(sunProducers);
    }
}