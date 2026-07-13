package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.structures.Vase;
import com.ussr.pvz.model.board.structures.VaseType;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.items.SeedPackDrop;
import com.ussr.pvz.model.entities.items.ItemType;
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
        int rows = lawn.getRows();
        int cols = lawn.getCols();

        // 25-vase pool distribution
        List<VaseType> vasePool = new ArrayList<>();
        for (int i = 0; i < 8; i++) vasePool.add(VaseType.PLANT);
        for (int i = 0; i < 2; i++) vasePool.add(VaseType.GARGANTAUR);
        for (int i = 0; i < 15; i++) vasePool.add(VaseType.NORMAL);

        Collections.shuffle(vasePool);

        int poolIndex = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 4; c < cols; c++) {
                if (poolIndex >= vasePool.size()) break;

                Vase vase = new Vase();
                VaseType assignedType = vasePool.get(poolIndex++);
                vase.setType(assignedType);
                vase.setPosition(Vec2.of(c, r));
                vase.setAlive(true);

                // Assign the internal entities inside the structural containers
                if (assignedType == VaseType.GARGANTAUR) {
                    // Guaranteed Gargantuar
                    vase.setContainedZombie(
                            com.ussr.pvz.model.entities.zombies.ZombieFactory.create("Gargantuar", r, c)
                    );
                } else if (assignedType == VaseType.PLANT) {
                    // Guaranteed Plant Seed Packet
                    int randomPlantId = rand.nextInt(50) + 1;
                    vase.setSeedPackDrop(new SeedPackDrop(ItemType.SEED_PACK, randomPlantId, 1));
                } else if (assignedType == VaseType.NORMAL) {

                    int roll = rand.nextInt(3); // 0 = Zombie, 1 = Plant, 2 = Empty

                    if (roll == 0) {
                        vase.setContainedZombie(
                                com.ussr.pvz.model.entities.zombies.ZombieFactory.create("Zombie", r, c)
                        );
                    } else if (roll == 1) {
                        int randomPlantId = rand.nextInt(50) + 1;
                        vase.setSeedPackDrop(new SeedPackDrop(ItemType.SEED_PACK, randomPlantId, 1));
                    }
                    // If roll == 2, both remain null, meaning the vase is safely empty!
                }

                Cell cell = lawn.getCell(r, c);
                if (cell != null) {
                    cell.setStructure(vase);
                    vases.add(vase);
                    session.registerStructure(vase);
                }
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