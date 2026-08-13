package com.ussr.pvz.model.entities.zombies.zomboss.moves;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.projectiles.FireballProjectile;
import com.ussr.pvz.model.entities.zombies.factory.BehaviorSpec;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossMove;
import com.ussr.pvz.model.util.Vec2;

import java.util.Map;
import java.util.Random;

public class FireballBarrageMove implements ZombossMove {
    private final int count;
    private final Random random = new Random();

    public FireballBarrageMove(Map<String, Object> params) {
        this.count = BehaviorSpec.getInt(params, "count", 3);
    }

    @Override
    public void execute(ZombossController controller, GameSession session) {
        if (!controller.hasEverBeenStunned()) return;

        for (int i = 0; i < count; i++) {
            int row = random.nextInt(session.getLawn().getRows());
            int col = random.nextInt(session.getLawn().getCols());

            Vec2 startPos = controller.getPrimary().getPosition();
            Vec2 targetPos = Vec2.of(col, row);

            FireballProjectile projectile = new FireballProjectile(startPos, targetPos, row, col);
            session.addZombieProjectile(projectile);
        }
    }
}