package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

import java.util.Random;


public class WallNutStrategy implements ActStrategy {

    private static final Random RANDOM = new Random();
    private static final double DIVERT_RADIUS = 5.0;
    private static final int TOP_ROW = 0;
    private static final int BOTTOM_ROW = 4;

    @Override
    public void act(Plant user, GameSession session) {
        if (!user.getTags().contains(Tag.MOVE_ZOMBIES)) return;

        for (Zombie zombie : session.getZombies()) {
            if (zombie == null || !zombie.isAlive()) continue;
            if (zombie.getPosition().distanceTo(user.getPosition()) < DIVERT_RADIUS) {
                divertZombie(zombie);
                break;
            }
        }

        user.setInternalTimer(0.0);
    }


    private void divertZombie(Zombie zombie) {
        Vec2 zomPos = zombie.getPosition();
        int currentRow = (int) zomPos.y();

        int dy;
        if (currentRow <= TOP_ROW) {
            dy = 1;
        } else if (currentRow >= BOTTOM_ROW) {
            dy = -1;
        } else {
            dy = RANDOM.nextBoolean() ? 1 : -1;
        }
        zombie.setPosition(new Vec2(zomPos.x(), zomPos.y() + dy));
    }
}