package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.structures.Vase;
import com.ussr.pvz.model.board.structures.VaseType;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.items.ItemType;
import com.ussr.pvz.model.entities.items.SeedPackDrop;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class VaseBreakerBehavior extends LevelBehavior {
    private final List<Vase> vases = new ArrayList<>();
    private final Random rand = new Random();

    @Override
    public void onStart(Level level) {
        super.onStart(level);

        GameSession session = App.getGameSession();
        if (session == null || session.getLawn() == null) return;

        level.setSunFalling(false);
        Lawn lawn = session.getLawn();
        List<VaseType> vasePool = generateVasePool();
        placeVases(session, lawn, vasePool);
    }

    private List<VaseType> generateVasePool() {
        List<VaseType> pool = new ArrayList<>();
        for (int i = 0; i < 8; i++) pool.add(VaseType.PLANT);
        for (int i = 0; i < 2; i++) pool.add(VaseType.GARGANTAUR);
        for (int i = 0; i < 15; i++) pool.add(VaseType.NORMAL);
        Collections.shuffle(pool);
        return pool;
    }

    private void placeVases(GameSession session, Lawn lawn, List<VaseType> vasePool) {
        int poolIndex = 0;
        for (int r = 0; r < lawn.getRows(); r++) {
            for (int c = 4; c < lawn.getCols(); c++) {
                if (poolIndex >= vasePool.size()) break;

                Vase vase = new Vase();
                VaseType assignedType = vasePool.get(poolIndex++);
                vase.setType(assignedType);
                vase.setPosition(Vec2.of(c, r));
                vase.setAlive(true);

                assignVaseContents(vase, assignedType, r, c);

                Cell cell = lawn.getCell(r, c);
                if (cell != null) {
                    cell.setStructure(vase);
                    vases.add(vase);
                    session.registerStructure(vase);
                }
            }
        }
    }

    private void assignVaseContents(Vase vase, VaseType type, int r, int c) {
        if (type == VaseType.GARGANTAUR) {
            vase.setContainedZombie(com.ussr.pvz.model.entities.zombies.ZombieFactory.create("ZombieGargantuar", r, c));
        } else if (type == VaseType.PLANT) {
            // FIX: Uses correct parameters (type, lifetime, radius, plantId)
            vase.setSeedPackDrop(new SeedPackDrop(ItemType.SEED_PACK, 40f, 20f, rand.nextInt(50) + 1));
        } else if (type == VaseType.NORMAL) {
            int roll = rand.nextInt(3);
            if (roll == 0) {
                vase.setContainedZombie(com.ussr.pvz.model.entities.zombies.ZombieFactory.create("ZombieDefault", r, c));
            } else if (roll == 1) {
                // FIX: Uses correct parameters
                vase.setSeedPackDrop(new SeedPackDrop(ItemType.SEED_PACK, 40f, 20f, rand.nextInt(50) + 1));
            }
        }
    }

    @Override
    public void tick(GameSession session, double deltaTime) {
        super.tick(session, deltaTime);

        if (levelCompleted || session.isGameOver()) return;

        if (isWon()) {
            onComplete(session.getLevel());
        }
    }

    @Override
    public void onWaveComplete(Level level, int waveNumber) {
    }

    @Override
    public void onComplete(Level level) {
        super.onComplete(level);
        vases.clear();
    }

    @Override
    public boolean isFailed(Level level) {
        return false;
    }

    public boolean isWon() {
        GameSession session = App.getGameSession();
        if (session == null) return false;

        // Win condition: All generated vases are broken AND no remaining zombies breathe
        boolean noVasesLeft = vases.stream().noneMatch(Vase::isAlive);
        boolean noZombiesLeft = session.getZombies().stream().noneMatch(GameEntity::isAlive);

        return noVasesLeft && noZombiesLeft;
    }
}