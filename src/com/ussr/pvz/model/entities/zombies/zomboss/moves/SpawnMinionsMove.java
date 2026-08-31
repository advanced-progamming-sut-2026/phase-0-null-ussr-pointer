package com.ussr.pvz.model.entities.zombies.zomboss.moves;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossMove;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SpawnMinionsMove implements ZombossMove {
    private final List<String> minionPool = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public SpawnMinionsMove(Map<String, Object> zombieData) {
        if (zombieData.containsKey("ZombossMinionPool") && zombieData.get("ZombossMinionPool") instanceof List<?> list){
            for (Object obj : list) {
                minionPool.add(obj.toString());
            }
        } else {
            minionPool.add("ZombieDefault");
            minionPool.add("ZombieArmor1");
        }
    }

    @Override
    public void execute(ZombossController controller, GameSession session) {
        if (!controller.canSpawnZombies() || minionPool.isEmpty() || session.getLawn() == null) return;

        Random random = new Random();
        int count = 2 + random.nextInt(3);

        for (int i = 0; i < count; i++) {
            int row = random.nextInt(session.getLawn().getRows());
            int col = session.getLawn().getCols();
            String alias = minionPool.get(random.nextInt(minionPool.size()));
            try {
                Zombie minion = ZombieFactory.create(alias, row, col);
                session.spawnZombie(minion);
            } catch (Exception ignored) {}
        }
    }
}