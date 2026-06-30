package com.ussr.pvz.model.entities.zombies.projectiles;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.util.Vec2;

public class SnowballProjectile extends ZombieProjectile {

    public SnowballProjectile(Vec2 startPosition, Vec2 targetPosition, double flightTime) {
        super(startPosition, targetPosition, flightTime, "IceAgeHunter");
    }

    @Override
    protected void updateFlightPath(double progress) {
        double currentX = startPosition.x() + (targetPosition.x() - startPosition.x()) * progress;
        double currentY = startPosition.y() + (targetPosition.y() - startPosition.y()) * progress;

        this.setPosition(Vec2.of(currentX, currentY));
    }

    @Override
    protected void onDestinationReached(GameSession session) {
        int targetRow = (int) Math.round(targetPosition.y());
        int targetCol = (int) Math.round(targetPosition.x());

        Cell targetCell = session.getLawn().getCell(targetRow, targetCol);

        if (targetCell != null && targetCell.getPlant() != null && targetCell.getPlant().isAlive()) {
            Plant targetPlant = targetCell.getPlant();

            /*
             * TODO: IMPLEMENTING THE CHILL/FREEZE LOGIC
             * * 1. Add `private int chillLevel = 0;` to Plant.java
             * 2. When hit by a snowball, increment `chillLevel`.
             * 3. If `chillLevel == 1`, slow down the plant's action interval (e.g., multiply actionInterval by 1.5).
             * 4. If `chillLevel >= 2`, the plant becomes an ICE BLOCK.
             * - Add `private boolean isFrozen = false;` to Plant.java.
             * - Update Plant.tick(): `if (!isAlive || isCat || isFrozen) return;`
             * - Create an IceBlock structure (like Grave/Octopus) to absorb damage, OR handle it internally inside takeDamage().
             * 5. If the plant gets hit by a fire projectile (like FirePea), reset chillLevel to 0 and isFrozen to false!
             */

            // System.out.println("Snowball hit " + targetPlant.getName());
        }
    }
}