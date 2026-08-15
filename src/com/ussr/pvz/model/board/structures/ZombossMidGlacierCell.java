package com.ussr.pvz.model.board.structures;

import com.ussr.pvz.model.engine.session.GameSession;

/**
 * One of the 9 lawn cells (3 mid rows x last 3 columns) covered by the
 * Ice Age Zomboss's mid-glacier armor. All damage taken on any of these
 * cells is forwarded into the shared ZombossMidGlacier core, so hitting
 * any part of the 3x3 block damages the whole armor as one object.
 * <p>
 * Because InteractableStructures are checked before Zombies in the
 * projectile collision order (see Projectile#checkCollision), while the
 * core isn't DESTROYED these cells sit in front of the Zomboss and absorb
 * hits meant for it — this is what makes the glacier "cover" the boss.
 */
public class ZombossMidGlacierCell extends InteractableStructure {

    private final ZombossMidGlacier core;

    public ZombossMidGlacierCell(ZombossMidGlacier core) {
        this.core = core;
    }

    @Override
    public boolean isAlive() {
        return core.getState() != ZombossMidGlacier.State.DESTROYED;
    }

    @Override
    public void takeDamage(int damage) {
        core.takeDamage(damage);
    }

    @Override
    public void onDestroy(GameSession session) {
        // No lawn-tile side effects — purely a hittable shield over the boss.
    }

    public ZombossMidGlacier getCore() {
        return core;
    }
}