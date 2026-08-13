package com.ussr.pvz.model.entities.zombies.effect;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.Faction;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.projectiles.OctopusProjectile;
import com.ussr.pvz.model.util.Vec2;

public class OctopusThrowEffect implements EffectStatus {
    private final double throwCooldown;
    private double timer;

    public OctopusThrowEffect(double throwCooldown) {
        this.throwCooldown = throwCooldown;
        this.timer = throwCooldown;
    }

    @Override
    public void effect(
            Zombie zombie,
            GameSession session,
            float delta
    ) {
        if (!zombie.isAlive()) return;

        timer += delta;
        if (timer >= throwCooldown) {
            if (throwOctopus(zombie, session)) {
                timer = 0;
            }
        }
    }

    private boolean throwOctopus(Zombie zombie, GameSession session) {
        int zRow = (int) zombie.getPosition().y();
        double zCol = zombie.getPosition().x();
        int cols = session.getLawn().getCols();
        Vec2 startPos = zombie.getPosition();

        if (zombie.getFaction() == Faction.ZOMBIES) {
            for (int c = (int) zCol; c >= 0; c--) {
                if (c >= cols) continue;
                Cell cell = session.getLawn().getCell(zRow, c);
                if (cell != null && cell.getPlant() != null && cell.getPlant().isAlive()) {
                    Vec2 targetPos = Vec2.of(cell.getPlant().getLocation().x(), cell.getPlant().getLocation().y());
                    zombie.queueAnimEvent("toss");
                    session.addZombieProjectile(new OctopusProjectile(startPos, targetPos, 1.5, zombie));
                    return true;
                }
            }
        } else {
            for (Zombie target : session.getZombies()) {
                if (target.isAlive() && target.getFaction() == Faction.ZOMBIES
                        && (int) target.getPosition().y() == zRow && target.getPosition().x() > zCol) {
                    zombie.queueAnimEvent("toss");
                    session.addZombieProjectile(new OctopusProjectile(startPos, target.getPosition(), 1.5, zombie));
                    return true;
                }
            }
        }
        return false;
    }
}
