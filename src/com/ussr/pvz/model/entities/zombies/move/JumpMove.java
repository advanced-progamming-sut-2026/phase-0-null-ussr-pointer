package com.ussr.pvz.model.entities.zombies.move;

import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

// TODO(dodo-jump-probability): this whole class ignores zombies.json's Dodo-specific fields —
//  AddRandomChanceForJumpPerGridWalked, CooldownSecondsUntilNextJumpAvailable,
//  InitialSetRandomChanceForJump, LandedResetRandomChanceForJump, Min/MaxRandomGridSquaresToFlyOver
//  — and instead jumps deterministically every time an obstacle plant is directly ahead. Needs a
//  real probability accumulator (increases per grid square walked, resets after a jump per
//  LandedResetRandomChanceForJump, respects the cooldown between jumps) plus honoring
//  PlantsToFlyOver / GridItemsToFlyOver as actual whitelists instead of "any alive plant ahead".
public class JumpMove implements MoveBehavior {

    @Override
    public void move(Zombie zombie, GameSession session) {
        Vec2 pos = zombie.getPosition();
        if (pos == null) return;

        Vec2 vel = zombie.getSpeed();
        int currentRow = (int) pos.y();

        int lookAheadCol = (int) (pos.x() - 0.5);

        Cell aheadCell = session.getLawn().getCell(currentRow, lookAheadCol);
        Plant obstaclePlant = (aheadCell != null) ? aheadCell.getPlant() : null;

        if (obstaclePlant != null && obstaclePlant.isAlive()) {
            double leapX = pos.x() - 1.2;
            zombie.setPosition(Vec2.of(leapX, pos.y()));
        } else {
            Vec2 newPos = pos.add(vel.scale(GameClock.SECONDS_PER_TICK));
            zombie.setPosition(newPos);
        }

        if (zombie.getPosition().x() < 0) {
            session.onZombieReachedEnd();
        }
    }
}