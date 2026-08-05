package com.ussr.pvz.model.entities.zombies.zomboss;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.effect.EffectStatus;
import com.ussr.pvz.model.entities.zombies.factory.BehaviorSpec;
import com.ussr.pvz.model.entities.zombies.move.StunnedMoveBehavior;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ZombossController implements EffectStatus {

    private static final int SEGMENTS = 3;
    private static final Random RAND = new Random();

    private static final class MoveEntry {
        final String name;
        final ZombossMove move;
        final int weight;
        final double cooldown;
        double cooldownRemaining;

        MoveEntry(String name, ZombossMove move, int weight, double cooldown) {
            this.name = name;
            this.move = move;
            this.weight = weight;
            this.cooldown = cooldown;
        }
    }

    private final Zombie primary;
    private final Zombie mirror;
    private final int mirrorRowOffset = 1;

    private final int maxHp;
    private final int segmentSize;
    private int currentHp;
    private int currentSegment;

    private final double stunDuration;
    private final boolean canSpawnZombies;
    private final boolean canSwitchRows;

    private final double moveIntervalMin;
    private final double moveIntervalMax;
    private double nextMoveTimer;

    private final List<MoveEntry> moves = new ArrayList<>();

    public ZombossController(Zombie primary, Zombie mirror, Map<String, Object> data) {
        this.primary = primary;
        this.mirror = mirror;

        this.maxHp = BehaviorSpec.getInt(data, "Hitpoints", primary.getMaxHp());
        this.segmentSize = Math.max(1, maxHp / SEGMENTS);
        this.currentHp = maxHp;
        this.currentSegment = SEGMENTS - 1;

        this.stunDuration = BehaviorSpec.getDouble(data, "ZombossStunDuration", 3.0);
        this.canSpawnZombies = BehaviorSpec.getBoolean(data, "ZombossCanSpawnZombies", true);
        this.canSwitchRows = BehaviorSpec.getBoolean(data, "ZombossCanSwitchRows", true);

        this.moveIntervalMin = BehaviorSpec.getDouble(data, "ZombossMoveIntervalMin", 4.0);
        this.moveIntervalMax = BehaviorSpec.getDouble(data, "ZombossMoveIntervalMax", 7.0);
        this.nextMoveTimer = randomInterval();

        loadMoves(data);
    }

    @SuppressWarnings("unchecked")
    private void loadMoves(Map<String, Object> data) {
        Object raw = data.get("ZombossMoves");
        if (!(raw instanceof List<?> list)) return;

        for (Object o : list) {
            if (!(o instanceof Map<?, ?> m)) continue;
            Map<String, Object> entry = (Map<String, Object>) m;
            String name = (String) entry.get("name");
            if (name == null) continue;

            ZombossMove move = ZombossMoveRegistry.create(name, entry, data);
            int weight = BehaviorSpec.getInt(entry, "weight", 100);
            double cooldown = BehaviorSpec.getDouble(entry, "cooldown", 5.0);
            moves.add(new MoveEntry(name, move, weight, cooldown));
        }
    }

    private double randomInterval() {
        if (moveIntervalMax <= moveIntervalMin) return moveIntervalMin;
        return moveIntervalMin + RAND.nextDouble() * (moveIntervalMax - moveIntervalMin);
    }

    @Override
    public void effect(Zombie zombie, GameSession session, float delta) {
        if (!primary.isAlive()) return;

        syncMirrorPosition();
        tickMoveCooldowns(delta);

        if (isStunned()) return;

        nextMoveTimer -= delta;
        if (nextMoveTimer <= 0) {
            tryExecuteRandomMove(session);
            nextMoveTimer = randomInterval();
        }
    }

    private void syncMirrorPosition() {
        if (mirror == null || !mirror.isAlive() || primary.getPosition() == null) return;
        Vec2 pos = primary.getPosition();
        mirror.setPosition(Vec2.of(pos.x(), pos.y() + mirrorRowOffset));
    }

    private void tickMoveCooldowns(float delta) {
        for (MoveEntry entry : moves) {
            entry.cooldownRemaining = Math.max(0, entry.cooldownRemaining - delta);
        }
    }

    private void tryExecuteRandomMove(GameSession session) {
        List<MoveEntry> ready = new ArrayList<>();
        int totalWeight = 0;
        for (MoveEntry entry : moves) {
            if (entry.cooldownRemaining <= 0) {
                ready.add(entry);
                totalWeight += Math.max(entry.weight, 0);
            }
        }
        if (ready.isEmpty() || totalWeight <= 0) return;

        int roll = RAND.nextInt(totalWeight);
        int cumulative = 0;
        for (MoveEntry entry : ready) {
            cumulative += Math.max(entry.weight, 0);
            if (roll < cumulative) {
                entry.move.execute(this, session);
                entry.cooldownRemaining = entry.cooldown;
                return;
            }
        }
    }

    public boolean isStunned() {
        return primary.getMoveBehavior() instanceof StunnedMoveBehavior;
    }

    private void stun() {
        if (isStunned()) return;
        primary.setMoveBehavior(new StunnedMoveBehavior(primary.getMoveBehavior(), stunDuration));
    }

    public void applyDamage(int rawDamage, GameSession session) {
        if (currentHp <= 0) return;

        currentHp = Math.max(0, currentHp - rawDamage);

        if (currentHp == 0) {
            die(session);
            return;
        }

        int newSegment = computeSegment(currentHp);
        if (newSegment < currentSegment) {
            currentSegment = newSegment;
            stun();
        }
    }

    private int computeSegment(int hp) {
        if (hp <= 0) return -1;
        for (int s = SEGMENTS - 1; s >= 0; s--) {
            if (hp > segmentSize * s) return s;
        }
        return 0;
    }

    private void die(GameSession session) {
        primary.setAlive(false);
        if (mirror != null) {
            mirror.setAlive(false);
        }
        session.notifyZombieDied(primary);
    }

    public void relocateRows(int newPrimaryRow) {
        if (primary.getPosition() == null) return;
        Vec2 pos = primary.getPosition();
        primary.setPosition(Vec2.of(pos.x(), newPrimaryRow));
        if (mirror != null) {
            mirror.setPosition(Vec2.of(pos.x(), newPrimaryRow + mirrorRowOffset));
        }
    }

    public Zombie getPrimary() {
        return primary;
    }

    public Zombie getMirror() {
        return mirror;
    }

    public int getPrimaryRow() {
        return (int) primary.getPosition().y();
    }

    public int getMirrorRow() {
        return getPrimaryRow() + mirrorRowOffset;
    }

    public boolean canSpawnZombies() {
        return canSpawnZombies;
    }

    public boolean canSwitchRows() {
        return canSwitchRows;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public int getCurrentSegment() {
        return currentSegment;
    }
}