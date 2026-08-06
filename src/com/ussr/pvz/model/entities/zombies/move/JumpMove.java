package com.ussr.pvz.model.entities.zombies.move;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;
import java.util.List;

public class JumpMove implements MoveBehavior {
    private static final double JUMP_DISTANCE = 1.2;
    private static final double JUMP_TRAVEL_SECONDS = 0.95;

    private final double addChancePerGrid;
    private final double cooldownSeconds;
    private final double resetChance;
    private final List<String> plantsToFlyOver;

    private double currentChance;
    private double cooldownTimer = 0;
    private double gridAccumulator = 0;
    private boolean jumping;
    private double jumpStartX;
    private double jumpTargetX;
    private double jumpElapsed;

    public JumpMove(double addChance, double cooldown, double initChance,
                    double resetChance, List<String> plantsToFlyOver) {
        this.addChancePerGrid = addChance;
        this.cooldownSeconds = cooldown;
        this.currentChance = initChance;
        this.resetChance = resetChance;
        this.plantsToFlyOver = plantsToFlyOver;
    }

    @Override
    public void move(
            Zombie zombie,
            GameSession session,
            float delta
    ) {
        if (cooldownTimer > 0) {
            cooldownTimer -= delta;
            if (cooldownTimer < 0) cooldownTimer = 0;
        }
        Vec2 pos = zombie.getPosition();
        Vec2 vel = zombie.getSpeed();
        if (jumping) {
            advanceJump(zombie, delta);
            return;
        }
        double dx = Math.abs(vel.x() * delta);
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
                if (pName.contains("iceberg")) pName = "iceburg";
                if (plantsToFlyOver != null && plantsToFlyOver.contains(pName)) {
                    if (Math.random() <= currentChance) {
                        shouldJump = true;
                    }
                }
            }
        }
        if (shouldJump) {
            zombie.queueAnimEvent("fly_start");
            zombie.queueAnimEvent("fly_loop");
            zombie.queueAnimEvent("fly_end");
            jumping = true;
            jumpStartX = pos.x();
            jumpTargetX = pos.x() - JUMP_DISTANCE;
            jumpElapsed = 0;
            currentChance = resetChance;
            cooldownTimer = cooldownSeconds;
            gridAccumulator = 0;
        } else {
            Vec2 newPos = pos.add(vel.scale(delta));
            newPos = applySlipperyShift(
                    zombie,
                    pos,
                    newPos,
                    session
            );
            zombie.setPosition(newPos);
        }
    }

    private void advanceJump(Zombie zombie, float delta) {
        jumpElapsed = Math.min(JUMP_TRAVEL_SECONDS, jumpElapsed + delta);
        double progress = jumpElapsed / JUMP_TRAVEL_SECONDS;
        double smoothProgress = progress * progress * (3.0 - 2.0 * progress);
        double x = jumpStartX + (jumpTargetX - jumpStartX) * smoothProgress;
        zombie.setPosition(Vec2.of(x, zombie.getPosition().y()));

        if (progress >= 1.0) {
            jumping = false;
        }
    }

    public boolean canFlyOver(Plant plant) {
        if (plant == null || plantsToFlyOver == null) return false;
        String pName = plant.getName().toLowerCase().replace("-", "").replace(" ", "");
        if (pName.contains("iceberg")) pName = "iceburg";
        return plantsToFlyOver.contains(pName);
    }
}
