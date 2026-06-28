package com.ussr.pvz.model.board.structures;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.util.Vec2;

public class PushableStructure extends InteractableStructure {
    private final PushableType type;
    private int hp;

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
        // Leave empty or implement custom periodic updates (e.g., Arcade generation)
    }

    public PushableType getType() { return type; }
    public int getHp() { return hp; }
}