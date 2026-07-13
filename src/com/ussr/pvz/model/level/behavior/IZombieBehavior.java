package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.Brain;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieSize;
import com.ussr.pvz.model.entities.zombies.attack.ChompAttack;
import com.ussr.pvz.model.entities.zombies.defense.NormalDefense;
import com.ussr.pvz.model.entities.zombies.effect.SunProducerZombieEffect;
import com.ussr.pvz.model.entities.zombies.move.StationaryMove;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IZombieBehavior extends LevelBehavior {
    private final int redLineColumn;
    private final int startingSun;
    private final List<Brain> brains = new ArrayList<>();

    private boolean missionFailed = false;
    private double sunZombieTimer = 0.0;
    private final Random random = new Random();

    public IZombieBehavior(int redLineColumn, int startingSun) {
        this.redLineColumn = redLineColumn;
        this.startingSun = startingSun > 0 ? startingSun : 150;
        this.autoWinOnWavesClear = false;
    }


    @Override
    public void onStart(Level level) {
        super.onStart(level);

        GameSession session = App.getGameSession();
        if (session == null || session.getLawn() == null) return;

        level.setSunFalling(false);

        // Adjust current sun balance to perfectly hit the target starting sun
        int currentSun = session.getSunCount();
        session.addSun(startingSun - currentSun);

        int rows = session.getLawn().getRows();
        int cols = session.getLawn().getCols();

        // 1. Place Brains at column 0 for every row instead of Lawnmowers
        for (int r = 0; r < rows; r++) {
            Cell cell = session.getLawn().getCell(r, 0);
            if (cell != null) {
                Brain brain = new Brain();
                brain.setPosition(Vec2.of(0, r));
                cell.setStructure(brain);
                brains.add(brain);
                session.registerStructure(brain);
            }
        }

        // 2. Procedural Grid Generation: Populate tiles before the red line with dynamic plant types
        for (int r = 0; r < rows; r++) {
            for (int c = 1; c < redLineColumn; c++) {

                // Generates a random plant ID securely within the factory's allowed range (1 to 50 inclusive)
                int randomPlantId = random.nextInt(50) + 1;
                com.ussr.pvz.model.entities.plants.Plant plant =
                        com.ussr.pvz.model.entities.plants.PlantFactory.createPlant(randomPlantId, 1);

                plant.setLocation(new com.ussr.pvz.model.entities.plants.Plant.Location(c, r));
                Cell cell = session.getLawn().getCell(r, c);
                if (cell != null) {
                    cell.setPlant(plant);
                    session.getPlants().add(plant);
                }
            }
        }

        // 3. Spawn the special stationary Sun-Producing Zombie per row (rightmost edge column)
        //todo : fix this is there is any problem there is a sun producing logic a few lines higher take a look at that too
        for (int r = 0; r < rows; r++) {
            Zombie sunZombie = new Zombie("SunProducerZombie", null, false);
            sunZombie.setMaxHp(1300);
            sunZombie.setHp(1300);
            sunZombie.setEatDps(0);
            sunZombie.setSize(ZombieSize.DEFAULT);
            sunZombie.setPosition(Vec2.of(cols - 1, r));

            sunZombie.setMoveBehavior(new StationaryMove());
            sunZombie.setAttackBehavior(new ChompAttack());
            sunZombie.setDefenseBehavior(new NormalDefense());
            sunZombie.setEffectStatus(new SunProducerZombieEffect());

            session.spawnZombie(sunZombie);
        }
    }

    @Override
    public void tick(GameSession session, double deltaTime) {
        super.tick(session, deltaTime);

        if (levelCompleted || session.isGameOver() || missionFailed) return;

        // 1. Instant Victory Evaluation (All 5 brains eaten)
        if (isWon()) {
            onComplete(session.getLevel());
            return;
        }

        // 2. Scaling Sun Production Logic
        sunZombieTimer += deltaTime;

        // Custom Formula: Starts at a slow 20-second interval, dropping by 1 second
        // every 30 seconds of elapsed gameplay down to a fast 6-second floor cap.
        double currentInterval = Math.max(6.0, 20.0 - (session.getElapsedSeconds() / 10.0));

        if (sunZombieTimer >= currentInterval) {
            sunZombieTimer = 0.0;

            // Count active, living sun-producing generators on the lawn
            long aliveProducers = session.getZombies().stream()
                    .filter(z -> z.isAlive() && "SunProducerZombie".equals(z.getAlias()))
                    .count();

            if (aliveProducers > 0) {
                session.addSun((int) (aliveProducers * 25)); // Generates 25 sun per alive unit
            }
        }

        // 3. Defeat Evaluation (Out of options entirely)
        boolean outOfSun = session.getSunCount() < 50;

        // Are there any player-deployed attacking forces currently pushing lines?
        boolean anyAttackersAlive = session.getZombies().stream()
                .filter(z -> !"SunProducerZombie".equals(z.getAlias()))
                .anyMatch(GameEntity::isAlive);

        // Are there any income units left alive to rescue the player with more sun drops?
        boolean anyProducersAlive = session.getZombies().stream()
                .filter(z -> "SunProducerZombie".equals(z.getAlias()))
                .anyMatch(GameEntity::isAlive);

        // Defeat condition: Broke AND no army active AND all income generators killed by plants
        if (outOfSun && !anyAttackersAlive && !anyProducersAlive) {
            this.missionFailed = true;
            session.getEventBus().publish(new GameEvent.GameOver());
        }
    }

    @Override
    public boolean isFailed(Level level) {
        return missionFailed;
    }

    public boolean isWon() {
        return brains.stream().noneMatch(Brain::isAlive);
    }

    public int getRedLineColumn() {
        return redLineColumn;
    }
}