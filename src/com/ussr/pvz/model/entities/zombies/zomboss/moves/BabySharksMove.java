package com.ussr.pvz.model.entities.zombies.zomboss.moves;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.projectiles.BabySharkProjectile;
import com.ussr.pvz.model.entities.zombies.factory.BehaviorSpec;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossMove;
import com.ussr.pvz.model.util.Vec2;

import java.util.Map;
import java.util.Random;

public class BabySharksMove implements ZombossMove {
    private final int count;
    private final Random random = new Random();

    public BabySharksMove(Map<String, Object> params) {
        this.count = BehaviorSpec.getInt(params, "count", 4);
    }

    @Override
    public void execute(ZombossController controller, GameSession session) {
        for (int i = 0; i < count; i++) {
            int row = random.nextInt(session.getLawn().getRows());
            int col = random.nextInt(session.getLawn().getCols());

            BabySharkProjectile projectile = new BabySharkProjectile(controller.getPrimary().getPosition(), Vec2.of(col, row), row, col);
            session.addZombieProjectile(projectile);
        }
    }
}