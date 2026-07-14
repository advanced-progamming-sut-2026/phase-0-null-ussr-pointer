package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.level.Level;

import java.util.List;
import java.util.Random;

public class ZombotanyBehavior extends LevelBehavior {

    private final Random rand = new Random();
    private static final String[] ZOMBOTANY_TYPES = {
            "peashooter-zombie", "wall-nut-zombie", "jalapeno-zombie", "squash-zombie"
    };

    public ZombotanyBehavior() {
    }

    @Override
    public void onStart(Level level) {
        super.onStart(level);
        GameSession session = App.getGameSession();
        if (session != null) {
            session.getEventBus().subscribe(GameEvent.ZombieSpawned.class, this::mutateZombie);
        }
    }

    private void mutateZombie(GameEvent.ZombieSpawned event) {
        GameSession session = App.getGameSession();
        if (session == null) return;

        List<Zombie> zombies = session.getZombies();
        if (zombies.isEmpty()) return;

        Zombie newlySpawned = zombies.get(zombies.size() - 1);
        if (newlySpawned.getAlias().contains("-zombie")) return;

        String zombotanyType = ZOMBOTANY_TYPES[rand.nextInt(ZOMBOTANY_TYPES.length)];

        try {
            Zombie mutated = com.ussr.pvz.model.entities.zombies.ZombieFactory.create(
                    zombotanyType,
                    (int)newlySpawned.getPosition().y(),
                    (int)newlySpawned.getPosition().x()
            );
            zombies.set(zombies.size() - 1, mutated);
        } catch (IllegalArgumentException e) {
            // Failsafe if JSON types are missing
        }
    }

    @Override
    public void onWaveComplete(Level level, int waveNumber) {
        super.onWaveComplete(level, waveNumber);
    }

    @Override
    public void onComplete(Level level) {
        super.onComplete(level);
    }

    @Override
    public boolean isFailed(Level level) {
        return false;
    }
}