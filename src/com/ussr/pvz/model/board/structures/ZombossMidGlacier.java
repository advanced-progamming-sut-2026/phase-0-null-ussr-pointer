package com.ussr.pvz.model.board.structures;

/**
 * Shared "brain" for the Ice Age Zomboss's 3x3 mid-body ice armor.
 * One instance is shared by all 9 ZombossMidGlacierCell structures that
 * occupy the actual lawn cells (3 mid rows x last 3 columns), so the whole
 * 3x3 block behaves as a single hittable object with one HP pool and one
 * damage state, instead of 9 independent blocks.
 *
 * States map to PAM clips:
 *   FULL      -> "animation2" (default, plays once)
 *   DAMAGED   -> "animation"  (loops while damaged)
 *   DESTROYED -> "animation3" (plays once, first time it breaks)
 *
 * It never "dies" for good — ZombossController calls revertToNormal()
 * when the boss recovers from its stun, putting it back to FULL and
 * re-covering the boss again.
 */
public class ZombossMidGlacier {

    public enum State { FULL, DAMAGED, DESTROYED }

    private final int maxHp;
    private int hp;
    private State state = State.FULL;
    private final Runnable onDestroyed;

    public ZombossMidGlacier(int maxHp, Runnable onDestroyed) {
        this.maxHp = Math.max(2, maxHp);
        this.hp = this.maxHp;
        this.onDestroyed = onDestroyed;
    }

    public void takeDamage(int damage) {
        if (state == State.DESTROYED || damage <= 0) return;

        hp = Math.max(0, hp - damage);
        State previous = state;

        if (hp <= 0) {
            state = State.DESTROYED;
        } else if (hp <= maxHp / 2) {
            state = State.DAMAGED;
        }

        if (state == State.DESTROYED && previous != State.DESTROYED && onDestroyed != null) {
            onDestroyed.run();
        }
    }

    /** Called when the Zomboss recovers from its stun — re-covers the boss. */
    public void revertToNormal() {
        this.state = State.FULL;
        this.hp = maxHp;
    }

    public State getState() {
        return state;
    }
}