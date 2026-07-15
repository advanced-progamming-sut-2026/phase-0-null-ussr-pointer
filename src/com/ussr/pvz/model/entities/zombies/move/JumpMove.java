package com.ussr.pvz.model.entities.zombies.move;

import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;
import java.util.List;

public class JumpMove implements MoveBehavior {
    private final double addChancePerGrid;
    private final double cooldownSeconds;
    private final double resetChance;
    private final List<String> plantsToFlyOver;

    private double currentChance;
    private double cooldownTimer = 0;
    private double gridAccumulator = 0;

    public JumpMove(double addChance, double cooldown, double initChance, double resetChance, List<String> plantsToFlyOver) {
        this.addChancePerGrid = addChance;
        this.cooldownSeconds = cooldown;
        this.currentChance = initChance;
        this.resetChance = resetChance;
        this.plantsToFlyOver = plantsToFlyOver;
    }

    @Override
    public void move(Zombie zombie, GameSession session) {
        if (cooldownTimer > 0) {
            cooldownTimer -= GameClock.SECONDS_PER_TICK;
            if (cooldownTimer < 0) cooldownTimer = 0;
        }

        Vec2 pos = zombie.getPosition();
        Vec2 vel = zombie.getSpeed();

        // Accumulate distance walked to increase jump probability
        double dx = Math.abs(vel.x() * GameClock.SECONDS_PER_TICK);
        gridAccumulator += dx;
        if (gridAccumulator >= 1.0) {
            currentChance += addChancePerGrid;
            gridAccumulator -= 1.0;
        }

        int currentRow = (int) pos.y();
        int lookAheadCol = (int) (pos.x() - 0.5);
        Cell aheadCell = session.getLawn().getCell(currentRow, lookAheadCol);

        boolean shouldJump = false;
        if (aheadCell != null && cooldownTimer <= 0) {
            Plant p = aheadCell.getPlant();
            if (p != null && p.isAlive()) {
                String pName = p.getName().toLowerCase().replace("-", "").replace(" ", "");
                if (pName.contains("iceberg")) pName = "iceburg"; // Handles the PopCap JSON typo "iceburg"

                if (plantsToFlyOver != null && plantsToFlyOver.contains(pName)) {
                    if (Math.random() <= currentChance) {
                        shouldJump = true;
                    }
                }
            }
        }

        if (shouldJump) {
            double leapX = pos.x() - 1.2;
            zombie.setPosition(Vec2.of(leapX, pos.y()));

            // Reset state
            currentChance = resetChance;
            cooldownTimer = cooldownSeconds;
            gridAccumulator = 0;
        } else {
            Vec2 newPos = pos.add(vel.scale(GameClock.SECONDS_PER_TICK));

            int oldCol = (int) pos.x();
            int newCol = (int) newPos.x();
            if (newCol != oldCol) {
                newPos = applySlipperyShift(newPos, session); // Handle Ice Age slider tiles!
            }
            zombie.setPosition(newPos);
        }

        if (zombie.getPosition().x() < 0) {
            session.onZombieReachedEnd();
        }
    }
}