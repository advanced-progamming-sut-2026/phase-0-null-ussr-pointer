package com.ussr.pvz.model.board.structures;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.Faction;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.util.Vec2;

public class PushableStructure extends InteractableStructure implements Damageable {
    private final PushableType type;
    private int hp;

    private static final double COLLISION_RADIUS = 0.5;

    public PushableStructure(PushableType type, Vec2 position) {
        this.type = type;
        this.setPosition(position);
        this.hp = type.getBaseHp();
        this.setAlive(true);
    }

    @Override
    public void takeDamage(int damage) {
        absorbDamage(damage);
    }

    /**
     * Applies damage to this prop and returns only the amount left after it
     * breaks, allowing a sufficiently strong hit to continue into its carrier.
     */
    public int absorbDamage(int damage) {
        if (!this.isAlive() || damage <= 0) return Math.max(0, damage);
        int absorbed = Math.min(this.hp, damage);
        this.hp -= absorbed;
        if (this.hp <= 0) {
            this.hp = 0;
            this.setAlive(false);
        }
        return damage - absorbed;
    }

    @Override
    public void onDestroy(GameSession session) {
        int row = (int) this.getPosition().y();
        int col = (int) this.getPosition().x();

        session.notifyStructureDestroyed(type.name(), row, col);

        if (type.getSpawnAlias() == null) {
            return;
        }

        if (this.type == PushableType.BARREL) {
            var imp1 = ZombieFactory.create(type.getSpawnAlias(), row, col);
            var imp2 = ZombieFactory.create(type.getSpawnAlias(), row, col);
            session.spawnZombie(imp1);
            session.spawnZombie(imp2);
        } else {
            var spawnedZombie = ZombieFactory.create(type.getSpawnAlias(), row, col);
            session.spawnZombie(spawnedZombie);
        }
    }

    @Override
    public void update(float delta) {
        if (!this.isAlive()) return;

        GameSession session = App.getGameSession();
        if (session == null) return;

        int row = (int) this.getPosition().y();
        double exactCol = this.getPosition().x();
        int gridCol = (int) Math.floor(exactCol);

        if (gridCol >= 0 && gridCol < session.getLawn().getCols()) {
            Cell cell = session.getLawn().getCell(row, gridCol);
            if (cell != null && cell.getPlant() != null && cell.getPlant().isAlive()) {
                cell.getPlant().takeDamage(cell.getPlant().getHp());
            }
        }

        for (Zombie zombie : session.getZombies()) {
            if (zombie.isAlive() && zombie.getFaction() == Faction.PLANTS) {
                if ((int) zombie.getPosition().y() == row) {
                    double distance = Math.abs(zombie.getPosition().x() - exactCol);
                    if (distance <= COLLISION_RADIUS) {
                        zombie.takeDamage(zombie.getHp());
                    }
                }
            }
        }
    }

    public PushableType getType() { return type; }
    public int getHp() { return hp; }
}
