package com.ussr.pvz.model.entities.zombies.projectiles;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.util.Vec2;

public class GargantuarImpProjectile extends ZombieProjectile {

    private final double apex;
    private final int targetRow;
    private final String impAlias;

    public GargantuarImpProjectile(Vec2 startPosition, Vec2 targetPosition, double flightTime,
                                   double apex, int targetRow, String impAlias) {
        super(startPosition, targetPosition, flightTime, "Gargantuar");
        this.apex = apex;
        this.targetRow = targetRow;
        this.impAlias = impAlias;
    }

    @Override
    protected void updateFlightPath(double progress) {
        double currentX = startPosition.x() + (targetPosition.x() - startPosition.x()) * progress;
        double currentY = startPosition.y() + (targetPosition.y() - startPosition.y()) * progress;

        double visualY = currentY - (apex / 100.0) * Math.sin(progress * Math.PI);

        this.setPosition(Vec2.of(currentX, visualY));
    }

    @Override
    protected void onDestinationReached(GameSession session) {
        int targetCol = (int) Math.round(targetPosition.x());

        Cell targetCell = session.getLawn().getCell(targetRow, targetCol);
        if (targetCell == null) return;

        Zombie imp = ZombieFactory.create(impAlias, targetRow, targetCol);
        session.spawnZombie(imp);
    }
}