package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.structures.Brain;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.event.GameEvent;
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
import java.util.Random;

public class MultiplayerIZombieBehavior extends LevelBehavior {

    private static final float MATCH_DURATION_SECONDS = 120f;

    private final int redLineColumn;
    private final int startingSun;
    private final MatchRole localRole;

    private float timeRemaining = MATCH_DURATION_SECONDS;
    private boolean missionFailed = false;
    private final List<Brain> brains = new ArrayList<>();
    private final Random random = new Random();

    public MultiplayerIZombieBehavior(
            int redLineColumn,
            int startingSun,
            MatchRole localRole
    ) {
        this.redLineColumn = redLineColumn;
        this.startingSun   = startingSun > 0 ? startingSun : 150;
        this.localRole     = localRole;
        this.autoWinOnWavesClear   = false;
        this.waitForManualWaveStart = true;
    }

    @Override
    public void onStart(Level level) {
        // No super.onStart() — we don't want ZombieAIManager or wave start

        GameSession session = App.getGameSession();
        if (session == null || session.getLawn() == null) return;

        level.setSunFalling(false);

        int currentSun = session.getSunCount();
        session.addSun(startingSun - currentSun);

        int rows = session.getLawn().getRows();
        int cols = session.getLawn().getCols();

        placeBrains(session, rows);
        placeSunProducers(session, rows, cols);
        // No plants placed — plants player places them manually
    }

    private void placeBrains(GameSession session, int rows) {
        session.getLawnMowers().clear();
        for (int r = 0; r < rows; r++) {
            Brain brain = new Brain();
            brain.setPosition(Vec2.of(-0.5, r));
            brains.add(brain);
            session.registerStructure(brain);
        }
    }

    private void placeSunProducers(GameSession session, int rows, int cols) {
        int zombieZoneWidth = cols - redLineColumn;
        if (zombieZoneWidth <= 0) return;

        for (int r = 0; r < rows; r++) {
            int c = redLineColumn + random.nextInt(zombieZoneWidth);
            Zombie sun = new Zombie("SunProducerZombie", null, false);
            sun.setMaxHp(1300);
            sun.setHp(1300);
            sun.setEatDps(0);
            sun.setSize(ZombieSize.DEFAULT);
            sun.setPosition(Vec2.of(c, r));
            sun.setMoveBehavior(new StationaryMove());
            sun.setAttackBehavior(new ChompAttack());
            sun.setDefenseBehavior(new NormalDefense());
            sun.setEffectStatus(new SunProducerZombieEffect());
            session.spawnZombie(sun);
        }
    }

    @Override
    public void tick(GameSession session, double deltaTime) {
        // No super.tick() — no aiManager
        if (levelCompleted || session.isGameOver() || missionFailed) return;

        timeRemaining -= (float) deltaTime;

        if (allBrainsEaten()) {
            endMatch(session, MatchRole.ZOMBIES);
            return;
        }

        if (timeRemaining <= 0f) {
            endMatch(session, MatchRole.PLANTS);
            return;
        }

        if (zombiePlayerIsStuck(session)) {
            endMatch(session, MatchRole.PLANTS);
        }
    }

    private boolean allBrainsEaten() {
        return !brains.isEmpty()
                && brains.stream().noneMatch(Brain::isAlive);
    }

    private boolean zombiePlayerIsStuck(GameSession session) {
        int cheapest = session.getLevel()
                .getAllowedZombies().stream()
                .mapToInt(a -> ZombieFactory.getZombieCost(a.id()))
                .filter(c -> c > 0)
                .min()
                .orElse(50);

        boolean broke = session.getSunCount() < cheapest;
        boolean noAttackers = session.getZombies().stream()
                .filter(z -> !"SunProducerZombie".equals(z.getAlias()))
                .noneMatch(GameEntity::isAlive);
        boolean noProducers = session.getZombies().stream()
                .filter(z -> "SunProducerZombie".equals(z.getAlias()))
                .noneMatch(GameEntity::isAlive);

        return broke && noAttackers && noProducers;
    }

    private void endMatch(GameSession session, MatchRole winner) {
        if (levelCompleted) return;
        levelCompleted = true;

        if (winner == localRole) {
            session.concludeVictory();
        } else {
            missionFailed = true;
            session.concludeDefeat();
        }
    }

    @Override
    public void onZombieBreach(GameSession session, Zombie zombie) {
        // Reaching left edge is normal in i,Zombie — brains determine victory
    }

    @Override
    public boolean isFailed(Level level) {
        return missionFailed;
    }

    public float getTimeRemaining() {
        return Math.max(0f, timeRemaining);
    }

    public int getRedLineColumn() {
        return redLineColumn;
    }

    public Brain getBrainInLane(int lane) {
        return brains.stream()
                .filter(b -> (int) b.getPosition().y() == lane)
                .findFirst()
                .orElse(null);
    }

    public List<Brain> getBrains() {
        return brains;
    }
}