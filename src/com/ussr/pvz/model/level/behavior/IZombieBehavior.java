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
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class IZombieBehavior extends LevelBehavior {
    private final int redLineColumn;
    private final int startingSun;
    private final List<Brain> brains = new ArrayList<>();

    private boolean missionFailed = false;
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

        placeBrains(session, rows);
        spawnDynamicPlants(session, rows);
        spawnSunProducers(session, rows, cols);
    }

    private void placeBrains(GameSession session, int rows) {
        // Remove standard lawnmowers
        session.getLawnMowers().clear();

        for (int r = 0; r < rows; r++) {
            Brain brain = new Brain();
            // Place brains outside the grid where LawnMowers normally sit
            brain.setPosition(Vec2.of(-0.5, r));
            brains.add(brain);
            session.registerStructure(brain);
        }
    }

    public Brain getBrainInLane(int lane) {
        return brains.stream()
                .filter(b -> (int)b.getPosition().y() == lane)
                .findFirst()
                .orElse(null);
    }

    private void spawnDynamicPlants(GameSession session, int rows) {
        for (int r = 0; r < rows; r++) {
            for (int c = 1; c < redLineColumn; c++) {
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
    }

    private void spawnSunProducers(GameSession session, int rows, int cols) {
        // Collect available columns past the red line to ensure unique X values
        List<Integer> availableCols = new ArrayList<>();
        for (int c = redLineColumn; c < cols; c++) {
            availableCols.add(c);
        }
        Collections.shuffle(availableCols, random);

        for (int r = 0; r < rows; r++) {
            // Assign a unique column per row if enough columns exist
            int c = cols - 1;
            if (!availableCols.isEmpty()) {
                c = availableCols.removeFirst();
            } else {
                c = redLineColumn + random.nextInt(cols - redLineColumn);
            }

            Zombie sunZombie = new Zombie("SunProducerZombie", null, false);
            sunZombie.setMaxHp(1300);
            sunZombie.setHp(1300);
            sunZombie.setEatDps(0);
            sunZombie.setSize(ZombieSize.DEFAULT);
            sunZombie.setPosition(Vec2.of(c, r));

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

        // 2. Defeat Evaluation (Out of options entirely)
        boolean outOfSun = session.getSunCount() < 50;
        boolean anyAttackersAlive = session.getZombies().stream()
                .filter(z -> !"SunProducerZombie".equals(z.getAlias()))
                .anyMatch(GameEntity::isAlive);
        boolean anyProducersAlive = session.getZombies().stream()
                .filter(z -> "SunProducerZombie".equals(z.getAlias()))
                .anyMatch(GameEntity::isAlive);

        if (outOfSun && !anyAttackersAlive && !anyProducersAlive) {
            this.missionFailed = true;
            session.getEventBus().publish(new GameEvent.GameOver());
        }
    }

    @Override
    public void onZombieBreach(GameSession session, Zombie zombie) {
        // Do nothing! In i,Zombie, zombies walking off screen past the brain is standard behavior.
        // Game victory relies solely on all Brain structures dying.
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