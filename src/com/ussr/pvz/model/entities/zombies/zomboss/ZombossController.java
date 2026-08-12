package com.ussr.pvz.model.entities.zombies.zomboss;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieActivity;
import com.ussr.pvz.model.entities.zombies.effect.EffectStatus;
import com.ussr.pvz.model.entities.zombies.factory.BehaviorSpec;
import com.ussr.pvz.model.entities.zombies.move.StunnedMoveBehavior;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayList;
import java.util.HashMap;
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
        final List<String> clips;
        final boolean randomVariant;
        final double cooldown;
        double cooldownRemaining;

        MoveEntry(String name, ZombossMove move, int weight, double cooldown, List<String> clips, boolean randomVariant) {
            this.name = name;
            this.move = move;
            this.weight = weight;
            this.cooldown = cooldown;
            this.clips = clips;
            this.randomVariant = randomVariant;
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

    // ------------------------------------------------------------------
    private List<String> lastMoveClips = List.of();

    // -------------------------------------------------------------------
    // Dash state — used by moves like ForwardDash to physically move the
    // Zomboss's position forward and back instead of just applying effects
    // in place.
    // -------------------------------------------------------------------
    private boolean dashing = false;
    private double dashElapsed = 0;
    private double dashOutDuration = 0.3;
    private double dashHoldDuration = 0.15;
    private double dashReturnDuration = 0.35;
    private double dashDistance = 0;
    private double dashBaseX = 0;

    // -------------------------------------------------------------------
    // Animation config — all optional/data-driven from the zombie JSON.
    // -------------------------------------------------------------------
    private final Map<String, String> clipOverrides;
    private final String stunClip;
    private final String stunStartClip;
    private final String stunEndClip;
    private final String preIntroClip;
    private final String introClip;
    private final List<String> dieSequence;
    private final double deathAnimSeconds;
    private boolean wasStunned = false;

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

        this.stunClip = BehaviorSpec.getString(data, "ZombossStunClip", "idle");
        this.stunStartClip = BehaviorSpec.getString(data, "ZombossStunStartClip", null);
        this.stunEndClip = BehaviorSpec.getString(data, "ZombossStunEndClip", null);
        this.introClip = BehaviorSpec.getString(data, "ZombossIntroClip", null);
        this.clipOverrides = loadClipOverrides(data);
        this.preIntroClip = BehaviorSpec.getString(data, "ZombossPreIntroClip", null);
        this.dieSequence = BehaviorSpec.getStringList(data, "ZombossDieSequence");
        this.deathAnimSeconds = BehaviorSpec.getDouble(data, "ZombossDeathAnimSeconds", 6.0);

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
            List<String> clips = BehaviorSpec.getStringList(entry, "clips");
            if (clips.isEmpty()) {
                String legacy = BehaviorSpec.getString(entry, "clip", null);
                if (legacy != null && !legacy.isBlank()) clips = List.of(legacy);
            }
            boolean randomVariant = "random".equalsIgnoreCase(BehaviorSpec.getString(entry, "clipMode", "sequence"));
            moves.add(new MoveEntry(name, move, weight, cooldown, clips, randomVariant));
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> loadClipOverrides(Map<String, Object> data) {
        Map<String, String> overrides = new HashMap<>();
        Object raw = data.get("ZombossClipOverrides");
        if (raw instanceof Map<?, ?> m) {
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (e.getKey() instanceof String k && e.getValue() instanceof String v) {
                    overrides.put(k, v);
                }
            }
        }
        return overrides;
    }

    private double randomInterval() {
        if (moveIntervalMax <= moveIntervalMin) return moveIntervalMin;
        return moveIntervalMin + RAND.nextDouble() * (moveIntervalMax - moveIntervalMin);
    }

    @Override
    public void effect(Zombie zombie, GameSession session, float delta) {
        if (!primary.isAlive()) return;

        updateDash(delta);
        syncMirrorPosition();
        tickMoveCooldowns(delta);

        boolean stunnedNow = isStunned();
        if (stunnedNow && !wasStunned && stunStartClip != null && !stunStartClip.isBlank()) {
            primary.queueAnimEvent(stunStartClip);
        } else if (!stunnedNow && wasStunned && stunEndClip != null && !stunEndClip.isBlank()) {
            primary.queueAnimEvent(stunEndClip);
        }
        wasStunned = stunnedNow;

        if (stunnedNow) return;

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
                List<String> playingClips = List.of();
                if (!entry.clips.isEmpty()) {
                    if (entry.randomVariant) {
                        String chosen = entry.clips.get(RAND.nextInt(entry.clips.size()));
                        primary.queueAnimEvent(chosen);
                        playingClips = List.of(chosen);
                    } else {
                        primary.queueAnimSequence(entry.clips);
                        playingClips = entry.clips;
                    }
                }
                lastMoveClips = playingClips;
                entry.move.execute(this, session, playingClips);
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
        primary.setState(ZombieActivity.DEAD);
        primary.setAlive(false);
        primary.startDeathTimer((float) deathAnimSeconds);
        if (!dieSequence.isEmpty()) primary.queueAnimSequence(dieSequence);
        if (mirror != null) {
            mirror.setState(ZombieActivity.DEAD);
            mirror.setAlive(false);
            mirror.startDeathTimer((float) deathAnimSeconds);
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

    public void startDash(double distance, double outDuration, double holdDuration, double returnDuration) {
        if (primary.getPosition() == null) return;
        this.dashDistance = distance;
        this.dashOutDuration = Math.max(0.01, outDuration);
        this.dashHoldDuration = Math.max(0, holdDuration);
        this.dashReturnDuration = Math.max(0.01, returnDuration);
        this.dashBaseX = primary.getPosition().x();
        this.dashElapsed = 0;
        this.dashing = true;
    }

    private void updateDash(float delta) {
        if (!dashing || primary.getPosition() == null) return;

        dashElapsed += delta;
        double total = dashOutDuration + dashHoldDuration + dashReturnDuration;
        double offset;

        if (dashElapsed <= dashOutDuration) {
            offset = -dashDistance * (dashElapsed / dashOutDuration);
        } else if (dashElapsed <= dashOutDuration + dashHoldDuration) {
            offset = -dashDistance;
        } else if (dashElapsed <= total) {
            double t = (dashElapsed - dashOutDuration - dashHoldDuration) / dashReturnDuration;
            offset = -dashDistance * (1 - t);
        } else {
            offset = 0;
            dashing = false;
        }

        Vec2 pos = primary.getPosition();
        primary.setPosition(Vec2.of(dashBaseX + offset, pos.y()));
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

    public String resolveClip(String baseClip) {
        return clipOverrides.getOrDefault(baseClip, baseClip);
    }

    public String getStunClip() {
        return stunClip;
    }

    public String getIntroClip() {
        return introClip;
    }

    public String getPreIntroClip() { return preIntroClip; }

    public List<String> getLastMoveClips() {
        return lastMoveClips;
    }

    /**
     * Called by EntityRenderLayer to determine the initial animation clip
     * for this boss unit.
     */
    public String getPreferredClip() {
        if (isStunned()) {
            return getStunClip();
        }
        return resolveClip("idle");
    }

    public List<String> getIntroSequence() {
        List<String> seq = new ArrayList<>(2);
        if (preIntroClip != null && !preIntroClip.isBlank()) seq.add(preIntroClip);
        if (introClip != null && !introClip.isBlank()) seq.add(introClip);
        return seq;
    }
}