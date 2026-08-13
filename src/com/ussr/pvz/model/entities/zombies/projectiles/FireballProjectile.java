package com.ussr.pvz.model.entities.zombies.projectiles;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.BurningGround;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.util.Vec2;

public class FireballProjectile extends ZombieBossProjectile {
    private static final double FLIGHT_TIME = 1.2;
    private static final double BURN_DURATION = 4.0;

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

        Cell cell = session.getLawn().getCell(targetRow, targetCol);
        if (cell != null && cell.getTile() != null) {
            cell.getTile().setType(TileType.Burning);

            BurningGround burningGround = new BurningGround(BURN_DURATION);
            burningGround.setPosition(Vec2.of(targetCol, targetRow));
            cell.setStructure(burningGround);
            session.registerStructure(burningGround);
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