package com.ussr.pvz.view.animation;

import pvz.libpvz.pam.PamPlayer;

public class ProjectilePamActor extends PamActor {

    public enum Phase { FLYING, HIT, DONE }

    public Phase phase = Phase.FLYING;
    private final String hitPamPath;   // may be null
    private float hitTimer = 0f;
    private static final float HIT_ANIM_DURATION = 0.5f;

    public ProjectilePamActor(PamPlayer player, String projectilePamPath, String hitPamPath) {
        super(player, projectilePamPath != null ? projectilePamPath : "", "idle");
        this.hitPamPath = hitPamPath;
        this.pamScale = 0.4f;
        this.offsetY = 0f;
    }

    /** Call this when the projectile has hit something */
    public void triggerHit(float x, float y) {
        if (phase != Phase.FLYING) return;
        phase = Phase.HIT;
        hitTimer = HIT_ANIM_DURATION;
        setPosition(x, y);

        if (hitPamPath != null && !hitPamPath.isBlank()) {
            // swap to the hit pam, one-shot
            switchToPam(hitPamPath, "idle");
        } else {
            // no hit pam — just disappear
            phase = Phase.DONE;
        }
    }

    public boolean isDone() {
        return phase == Phase.DONE;
    }

    @Override
    public void act(float delta) {
        if (phase == Phase.HIT) {
            hitTimer -= delta;
            if (hitTimer <= 0f) {
                phase = Phase.DONE;
                return;
            }
        }
        // only tick stateTime while flying or hitting
        if (phase != Phase.DONE) {
            super.act(delta);
        }
    }

    // Switches the internal pamPath and resolves a new clip — since pamPath is final in PamActor
    // we just resolve directly
    private void switchToPam(String newPamPath, String clip) {
        this.clipRef = resolveClip(player, newPamPath, clip);
        this.stateTime = 0f;
        this.looping = false; // hit anims play once
    }
}