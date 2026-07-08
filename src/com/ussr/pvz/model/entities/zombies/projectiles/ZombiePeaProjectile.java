package com.ussr.pvz.model.entities.zombies.projectiles;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.util.Vec2;

public class ZombiePeaProjectile extends ZombieProjectile {
    private final int damage;

    public ZombiePeaProjectile(Vec2 startPosition, int damage) {
        // Projects the target off-screen to the left (x = -1) based on a speed of 4 tiles/sec
        super(startPosition, Vec2.of(-1, startPosition.y()), (startPosition.x() + 1) / 4.0, "PeashooterZombie");
        this.damage = damage;
    }

    @Override
    protected void updateFlightPath(double progress) {
        double currentX = startPosition.x() + (targetPosition.x() - startPosition.x()) * progress;
        this.setPosition(Vec2.of(currentX, startPosition.y()));

        GameSession session = App.getGameSession();
        if (session == null || session.getLawn() == null) return;

        int row = (int) startPosition.y();
        int col = (int) Math.round(currentX);

        // Custom collision detection against Plants
        if (col >= 0 && col < session.getLawn().getCols()) {
            Cell cell = session.getLawn().getCell(row, col);
            if (cell != null && cell.getPlant() != null && cell.getPlant().isAlive()) {
                cell.getPlant().takeDamage(damage, null);
                this.isAlive = false; // Destroy the pea on impact
            }
        }
    }

    @Override
    protected void onDestinationReached(GameSession session) {
        // Projectile flew off-screen safely
    }
}