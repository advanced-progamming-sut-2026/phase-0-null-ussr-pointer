package com.ussr.pvz.model.board.structures;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameSession;
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
        if (!this.isAlive()) return;
        this.hp -= damage;
        if (this.hp <= 0) {
            this.setAlive(false);
        }
    }

    @Override
    public void onDestroy(GameSession session) {
        int row = (int) this.getPosition().y();
        int col = (int) this.getPosition().x();

        session.notifyStructureDestroyed(type.name(), row, col);

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
    public void tick() {
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