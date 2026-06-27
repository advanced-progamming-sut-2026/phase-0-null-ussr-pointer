package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.items.GroundItem;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

import java.util.Random;

public class WallNutStrategy implements ActStrategy {
    @Override
    public void act(Plant user, GameSession session) {
        if (user.getIntervalTimer() <= 0) {
            if (user.getTags().contains(Tag.MOVE_ZOMBIES)) {
                if (user.getName().equalsIgnoreCase("garlic")) {
                    for (Zombie zombie : session.getZombies()) {
                        if (zombie != null) {
                            if(zombie.getPosition().distanceTo(user.getPosition()) < 5d) {
                                handleMoveZombie(zombie);
                                break;
                            }
                        }
                    }
                }
            }
            user.setInternalTimer(user.getActionInterval());
        }
    }

    private void handleMoveZombie(Zombie zombie) {
        Vec2 position = zombie.getPosition();
        if(position.x() == 1)
            zombie.setPosition(new Vec2(position.x() + 1 , position.y()));
        else if (position.x() == 5)
            zombie.setPosition(new Vec2(position.x() - 1 , position.y()));
        else {
            Random random = new Random();
            int randomValue = random.nextBoolean() ? 1 : -1;
            zombie.setPosition(new Vec2(position.x() + randomValue , position.y()));
        }

    }
}
