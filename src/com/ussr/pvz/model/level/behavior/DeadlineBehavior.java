package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.structures.LawnMower;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.level.Level;

import java.util.List;

public class DeadlineBehavior implements LevelBehavior {
    @Override
    public void onStart(Level level) {
        //nada
    }

    @Override
    public void onWaveComplete(Level level, int waveNumber) {
        //nada
    }

    @Override
    public void onComplete(Level level) {
        //nada
    }

    @Override
    public boolean isFailed(Level level) {
        List<Zombie> zombies = App.getGameSession().getZombies();
        for (Zombie zombie : zombies) {
            if (!zombie.isAlive()) continue;

            if (zombie.getPosition().x() < 0.0) {
                int col = (int) zombie.getPosition().y();
                return col < level.getDeadlineColumn();
            }
        }

        return false;
    }
}
