package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.structures.Brain;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.entities.zombies.ZombieSize;
import com.ussr.pvz.model.entities.zombies.attack.ChompAttack;
import com.ussr.pvz.model.entities.zombies.defense.NormalDefense;
import com.ussr.pvz.model.entities.zombies.effect.SunProducerZombieEffect;
import com.ussr.pvz.model.entities.zombies.move.StationaryMove;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Couch-play i,Zombie: both sides are human controlled on the same screen,
 * one player planting with a mouse, the other placing zombies with a
 * keyboard. The plant side keeps using the session's normal falling sun,
 * while the zombie side is given its own independent sun pool so neither
 * player can spend the other's income.
 */
public class CouchIZombieBehavior extends LevelBehavior {

    private static final int DEFAULT_ZOMBIE_STARTING_SUN = 150;
    private static final int SUN_PRODUCER_HP = 1300;

    private final int redLineColumn;
    private final int zombieStartingSun;
    private final Random random = new Random();

    private final List<Brain> brains = new ArrayList<>();
    private final List<Zombie> sunProducers = new ArrayList<>();

    private int zombieSun;
    private boolean missionFailed;
    private boolean mowersRemoved = false;

    public CouchIZombieBehavior(int redLineColumn, int startingSun) {
        this.redLineColumn = redLineColumn;
        this.zombieStartingSun = startingSun > 0 ? startingSun : DEFAULT_ZOMBIE_STARTING_SUN;
        this.autoWinOnWavesClear = false;
    }

    @Override
    public void onStart(Level level) {
        super.onStart(level);

        GameSession session = App.getGameSession();
        if (session == null || session.getLawn() == null) return;

        level.setSunFalling(true);
        zombieSun = zombieStartingSun;

        int rows = session.getLawn().getRows();
        int columns = session.getLawn().getCols();

        placeBrains(session, rows);
        placeSunProducers(session, rows, columns);
    }

    private void placeBrains(GameSession session, int rows) {
        for (int lane = 0; lane < rows; lane++) {
            Brain brain = new Brain();
            brain.setPosition(Vec2.of(-0.5, lane));
            brains.add(brain);
            session.registerStructure(brain);
        }
    }

    private void placeSunProducers(GameSession session, int rows, int columns) {
        int zombieZoneWidth = columns - redLineColumn;
        if (zombieZoneWidth <= 0) return;

        for (int lane = 0; lane < rows; lane++) {
            int column = redLineColumn + random.nextInt(zombieZoneWidth);
            Zombie producer = createSunProducer(lane, column);
            sunProducers.add(producer);
            session.spawnZombie(producer);
        }
    }

    private Zombie createSunProducer(int lane, int column) {
        Zombie producer = new Zombie("SunProducerZombie", null, false);
        producer.setMaxHp(SUN_PRODUCER_HP);
        producer.setHp(SUN_PRODUCER_HP);
        producer.setEatDps(0);
        producer.setSize(ZombieSize.DEFAULT);
        producer.setPosition(Vec2.of(column, lane));
        producer.setMoveBehavior(new StationaryMove());
        producer.setAttackBehavior(new ChompAttack());
        producer.setDefenseBehavior(new NormalDefense());
        producer.setEffectStatus(new SunProducerZombieEffect());
        return producer;
    }

    @Override
    public void tick(GameSession session, double deltaTime) {
        if (!mowersRemoved) {
            session.getLawnMowers().forEach(mower -> mower.setAlive(false));
            session.getLawnMowers().clear();
            mowersRemoved = true;
        }

        if (levelCompleted || missionFailed || session.isGameOver()) return;

        // Zombie side wins: all brains eaten → zombie player wins, plant player loses.
        if (allBrainsEaten()) {
            missionFailed = true;         // plant side loses
            session.concludeDefeat();     // session ends as defeat (for the plant player)
            return;
        }

        // Plant side wins: zombie side is completely stuck → plant player wins, zombie player loses.
        if (zombieSidePlayerIsStuck(session)) {
            levelCompleted = true;        // plant side wins
            session.concludeVictory();    // session ends as victory (for the plant player)
        }
    }

    private boolean allBrainsEaten() {
        return !brains.isEmpty() && brains.stream().noneMatch(Brain::isAlive);
    }

    private boolean zombieSidePlayerIsStuck(GameSession session) {
        int cheapestZombieCost = session.getLevel().getAllowedZombies().stream()
                .mapToInt(allowed -> ZombieFactory.getZombieCost(allowed.id()))
                .filter(cost -> cost > 0)
                .min()
                .orElse(50);

        boolean cannotAffordZombie = zombieSun < cheapestZombieCost;

        boolean hasAttackers = session.getZombies().stream()
                .filter(zombie -> !"SunProducerZombie".equalsIgnoreCase(zombie.getAlias()))
                .anyMatch(GameEntity::isAlive);

        boolean hasProducers = session.getZombies().stream()
                .filter(zombie -> "SunProducerZombie".equalsIgnoreCase(zombie.getAlias()))
                .anyMatch(GameEntity::isAlive);

        return cannotAffordZombie && !hasAttackers && !hasProducers;
    }

    @Override
    public void onZombieBreach(GameSession session, Zombie zombie) {
        // Reaching the house is expected in i,Zombie; only eaten brains matter.
    }

    @Override
    public boolean isFailed(Level level) {
        return missionFailed;
    }

    public int getRedLineColumn() {
        return redLineColumn;
    }

    public int getZombieSun() {
        return zombieSun;
    }

    public void addZombieSun(int amount) {
        zombieSun += amount;
    }

    public boolean spendZombieSun(int amount) {
        if (amount > zombieSun) return false;
        zombieSun -= amount;
        return true;
    }

    public Brain getBrainInLane(int lane) {
        return brains.stream()
                .filter(brain -> (int) brain.getPosition().y() == lane)
                .findFirst()
                .orElse(null);
    }

    public List<Brain> getBrains() {
        return List.copyOf(brains);
    }
}