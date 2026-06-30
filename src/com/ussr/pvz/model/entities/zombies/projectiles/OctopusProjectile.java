package com.ussr.pvz.model.entities.zombies.projectiles;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.util.Vec2;

public class OctopusProjectile extends ZombieProjectile {

    private final double arcHeight = 2.5; // Octopuses get thrown in a high arc

    public OctopusProjectile(Vec2 startPosition, Vec2 targetPosition, double flightTime) {
        super(startPosition, targetPosition, flightTime, "OctopusZombie");
    }

    @Override
    protected void updateFlightPath(double progress) {
        double currentX = startPosition.x() + (targetPosition.x() - startPosition.x()) * progress;
        double currentY = startPosition.y() + (targetPosition.y() - startPosition.y()) * progress;

        // Visual arc calculation
        double visualY = currentY - (arcHeight * Math.sin(progress * Math.PI));

        this.setPosition(Vec2.of(currentX, visualY));
    }

    @Override
    protected void onDestinationReached(GameSession session) {
        int targetRow = (int) Math.round(targetPosition.y());
        int targetCol = (int) Math.round(targetPosition.x());

        Cell targetCell = session.getLawn().getCell(targetRow, targetCol);

        if (targetCell != null && targetCell.getPlant() != null && targetCell.getPlant().isAlive()) {
            Plant targetPlant = targetCell.getPlant();

            /*
             * TODO: IMPLEMENTING THE OCTOPUS BINDING LOGIC
             * * The Persian docs explicitly state:
             * 1. The octopus disables the plant (like freezing).
             * 2. Plant projectiles don't pass through it (it acts as a shield).
             * 3. Other plants must destroy the octopus to free the bound plant.
             * * APPROACH A (The Flag Method - Simpler):
             * - Add `private boolean isOctopused = false;` to Plant.java
             * - Add `private int octopusHp = 0;` to Plant.java
             * - Inside Plant.tick(): `if (!isAlive || isCat || isOctopused) return;`
             * - Inside Plant.takeDamage(): Intercept damage. If `isOctopused` is true,
             * subtract damage from `octopusHp` instead of `plantHp`. If `octopusHp <= 0`, set `isOctopused = false`.
             * - Here in this method:
             * targetPlant.setOctopused(true);
             * targetPlant.setOctopusHp(800); // Or whatever the JSON dictates
             * * APPROACH B (The Structure Method - Cleaner Architecture):
             * - Create a new class `OctopusWrap extends InteractableStructure` (similar to Grave).
             * - Here in this method: `targetCell.setStructure(new OctopusWrap(targetPlant));`
             * - Update Cell/Projectile collision logic so that if a Cell has a Structure,
             * the Structure takes damage BEFORE the Plant takes damage.
             * - Inside OctopusWrap.tick(), forcefully disable the underlying plant's actions.
             * - Inside OctopusWrap.onDestroy(), re-enable the underlying plant.
             */

            // Placeholder for future implementation:
            // System.out.println("Octopus landed on " + targetPlant.getName() + " at " + targetCol + "," + targetRow);
        }
    }
}