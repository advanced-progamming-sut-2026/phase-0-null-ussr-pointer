package com.ussr.pvz.model.entities.zombies.projectiles;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.util.Vec2;

/**
 * Shared behavior for zomboss "missile" attacks (Egypt rocket, Ice Age rocket,
 * etc.): a reticle locks onto the target tile immediately, then — after a
 * launch delay (giving the boss's own launch/throw animation time to play) —
 * the missile drops straight down from above the screen, then explodes and
 * destroys whatever plant occupied the tile.
 *
 * Three phases:
 *  TARGETING -> reticle shown on target tile from frame 0; missile itself not
 *               visible yet. Lasts `launchDelay` seconds.
 *  FALLING   -> missile actor drops straight down onto the target tile.
 *  EXPLODING -> impact effect already applied; explosion clip plays briefly.
 */
public abstract class MissileProjectile extends ZombieBossProjectile {

    public enum Phase { TARGETING, FALLING, EXPLODING }

    private static final double EXPLOSION_DURATION = 0.5;
    private static final double DROP_HEIGHT = 6.0;

    protected final int targetRow;
    protected final int targetCol;

    private final double launchDelay;

    private Phase phase = Phase.TARGETING;
    private double phaseTimer = 0.0;

    /**
     * @param launchDelay seconds between the reticle appearing (move starts)
     *                    and the missile actually beginning to fall. Tune this
     *                    to match the length of the boss's launch clip.
     */
    public MissileProjectile(Vec2 startPos, Vec2 targetPos, double fallTime, double launchDelay,
                             int targetRow, int targetCol, String sourceZombieAlias) {
        super(startPos, targetPos, fallTime, sourceZombieAlias);
        this.launchDelay = Math.max(0.0, launchDelay);
        this.targetRow = targetRow;
        this.targetCol = targetCol;
        // Pin the model position to the target immediately; only the visual
        // height/offset changes during TARGETING and FALLING.
        this.setPosition(targetPos);
    }

    @Override
    public void update(float delta) {
        if (!isAlive) return;
        phaseTimer += delta;

        switch (phase) {
            case TARGETING -> {
                this.setVisualHeight(0.0);
                if (phaseTimer >= launchDelay) {
                    phase = Phase.FALLING;
                    phaseTimer = 0.0;
                }
            }
            case FALLING -> {
                double progress = Math.min(1.0, phaseTimer / flightTime);
                updateFlightPath(progress);
                if (progress >= 1.0) {
                    this.setPosition(targetPosition);
                    this.setVisualHeight(0.0);
                    onDestinationReached(App.getGameSession());
                    phase = Phase.EXPLODING;
                    phaseTimer = 0.0;
                }
            }
            case EXPLODING -> {
                if (phaseTimer >= EXPLOSION_DURATION) {
                    this.isAlive = false;
                }
            }
        }
    }

    /** Straight drop from above the screen onto the target tile (no arc). */
    @Override
    protected void updateFlightPath(double progress) {
        this.setPosition(targetPosition);
        this.setVisualHeight(DROP_HEIGHT * (1.0 - progress));
    }

    public Phase getPhase() {
        return phase;
    }

    public int getTargetRow() {
        return targetRow;
    }

    public int getTargetCol() {
        return targetCol;
    }
}