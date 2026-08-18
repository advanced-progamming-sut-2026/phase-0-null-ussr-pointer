package com.ussr.pvz.model.entities.zombies.projectiles;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.util.Vec2;

import java.util.Random;

/**
 * A single baby shark launched by the Turbine/BabySharks Zomboss move.
 *
 * Swims toward its target cell playing "idle" or "idle2" (picked once at
 * spawn and kept for the whole swim, rather than flip-flopping between the
 * two). On arrival it lingers on that same idle clip a moment longer
 * (standing in for "wait for the current idle to finish"), submerges once,
 * then — after a short delay — surfaces into its attack clip at the target
 * cell, which is the moment the plant there is actually destroyed.
 *
 * Phase durations below are rough estimates; tune against the real PAM clip
 * lengths once available.
 */
public class BabySharkProjectile extends ZombieBossProjectile {

    public enum Phase { IDLE, SWIMMING, SUBMERGING, ATTACKING }

    private static final double FLIGHT_TIME = 1.0;
    private static final double IDLE_LINGER_DURATION = 0.5;
    private static final double SUBMERGE_DURATION = 0.3;
    private static final double PRE_ATTACK_DELAY = 0.5;
    private static final double ATTACK_DURATION = 0.4;
    private static final Random RAND = new Random();

    private final int row;
    private final String idleClip;
    private int targetRow;
    private int targetCol;

    private Phase phase = Phase.IDLE;
    private double phaseTimer = 0.0;
    private boolean effectApplied = false;

    /** Spawns resting in its row, idle, until activate() is called. */
    public BabySharkProjectile(Vec2 restPosition, int row) {
        super(restPosition, restPosition, FLIGHT_TIME, "ZombossShark");
        this.row = row;
        this.targetRow = row;
        this.targetCol = (int) restPosition.x();
        this.idleClip = RAND.nextBoolean() ? "idle" : "idle2";
    }

    public int getRow() { return row; }
    public boolean isIdle() { return phase == Phase.IDLE; }

    /** Ends the idle loop and starts submerge → swim → attack. */
    public void activate(Vec2 targetPos, int targetRow, int targetCol) {
        if (phase != Phase.IDLE) return;
        this.startPosition = this.getPosition();
        this.targetPosition = targetPos;
        this.targetRow = targetRow;
        this.targetCol = targetCol;
        this.phase = Phase.SWIMMING;
        this.phaseTimer = 0.0;
    }

    @Override
    public void update(float delta) {
        if (!isAlive || phase == Phase.IDLE) return; // idle never advances on its own
        phaseTimer += delta;

        switch (phase) {
            case SWIMMING -> {
                double progress = Math.min(1.0, phaseTimer / FLIGHT_TIME);
                updateFlightPath(progress);
                if (phaseTimer >= FLIGHT_TIME + IDLE_LINGER_DURATION) {
                    this.setPosition(targetPosition);
                    this.setVisualHeight(0.0);
                    phase = Phase.SUBMERGING;
                    phaseTimer = 0.0;
                }
            }
            case SUBMERGING -> {
                if (phaseTimer >= SUBMERGE_DURATION) {
                    phase = Phase.ATTACKING;
                    phaseTimer = 0.0;
                }
            }
            case ATTACKING -> {
                if (!effectApplied && phaseTimer >= PRE_ATTACK_DELAY) {
                    onDestinationReached(App.getGameSession());
                    effectApplied = true;
                }
                if (phaseTimer >= PRE_ATTACK_DELAY + ATTACK_DURATION) {
                    this.isAlive = false; // shark destroys itself on attack
                }
            }
            default -> {}
        }
    }

    @Override
    protected void applyDestinationEffect(GameSession session) {
        if (session.getLawn().getTile(targetRow, targetCol) != null
                && session.getLawn().getTile(targetRow, targetCol).getType() == TileType.Water) {
            session.removePlantAt(targetCol, targetRow);
        }
    }

    @Override
    public void onDestinationReached() {}

    public Phase getPhase() { return phase; }
    public String getIdleClip() { return idleClip; }

    @Override
    public String getPamLocation() {
        return "768/FULL/EFFECTS/ZOMBOSS_SHARK_PROJECTILE/ZOMBOSS_SHARK_PROJECTILE.PAM";
    }
}