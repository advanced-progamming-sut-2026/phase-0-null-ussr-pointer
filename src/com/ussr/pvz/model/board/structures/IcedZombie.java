package com.ussr.pvz.model.board.structures;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;

public class IcedZombie extends InteractableStructure {
    private int hp = 300;

    @Override
    public void onDestroy(GameSession session) {
        int col = (int) this.getPosition().x();
        int row = (int) this.getPosition().y();
        session.spawnZombie(ZombieFactory.create("ZombieImp", row, col));
    }

    @Override
    public void takeDamage(int damage) {
        if (!isAlive()) return;
        hp -= damage;
        if (hp <= 0) {
            setAlive(false);
        }
    }

    public String getPamLocation() {
        return "768/INITIAL/EFFECTS/ICEBLOOM_ICE_BLOCK_ZOMBIE/ICEBLOOM_ICE_BLOCK_ZOMBIE.PAM";
    }
}
