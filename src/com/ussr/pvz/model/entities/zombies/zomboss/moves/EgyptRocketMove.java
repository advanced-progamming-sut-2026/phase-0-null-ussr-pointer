package com.ussr.pvz.model.entities.zombies.zomboss.moves;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.projectiles.EgyptRocketProjectile;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossMove;
import com.ussr.pvz.model.util.Vec2;

import java.util.Random;

public class EgyptRocketMove implements ZombossMove {
    @Override
    public void execute(ZombossController controller, GameSession session) {
        Random random = new Random();
        int row = random.nextInt(session.getLawn().getRows());
        int col = random.nextInt(session.getLawn().getCols());

        Vec2 startPos = controller.getPrimary().getPosition();
        Vec2 targetPos = Vec2.of(col, row);

        EgyptRocketProjectile projectile = new EgyptRocketProjectile(startPos, targetPos, row, col);
        session.addZombieProjectile(projectile);
    }
}