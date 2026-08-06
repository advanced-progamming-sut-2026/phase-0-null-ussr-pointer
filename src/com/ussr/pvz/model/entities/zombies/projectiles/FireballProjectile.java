package com.ussr.pvz.model.entities.zombies.projectiles;

import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.util.Vec2;

public class FireballProjectile extends ZombieBossProjectile {
    private static final double FLIGHT_TIME = 1.2;

    private final int targetRow;
    private final int targetCol;

    public FireballProjectile(Vec2 startPos, Vec2 targetPos, int row, int col) {
        super(startPos, targetPos, FLIGHT_TIME, "ZombossDragon");
        this.targetRow = row;
        this.targetCol = col;
    }

    @Override
    protected void applyDestinationEffect(GameSession session) {
        session.removePlantAt(targetCol, targetRow);
        if (session.getLawn().getTile(targetRow, targetCol) != null) {
            session.getLawn().getTile(targetRow, targetCol).setType(TileType.Burning);
        }

        try {
            Zombie imp = ZombieFactory.create("ZombieDarkImpDragon", targetRow, targetCol);
            session.spawnZombie(imp);
        } catch (Exception e) {
            Zombie imp = ZombieFactory.create("ZombieImp", targetRow, targetCol);
            session.spawnZombie(imp);
        }
    }

    @Override
    public void onDestinationReached() {

    }

    @Override
    public String getPamLocation() {
        return "768/FULL/EFFECTS/ZOMBOSS_DARK_FIREBALL/ZOMBOSS_DARK_FIREBALL.PAM";
    }
}