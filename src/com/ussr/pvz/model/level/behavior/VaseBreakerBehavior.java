package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.structures.Vase;
import com.ussr.pvz.model.board.structures.VaseType;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.entities.items.SeedPackDrop;
import com.ussr.pvz.model.entities.items.ItemType;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class VaseBreakerBehavior implements LevelBehavior {
    private final List<Vase> vases = new ArrayList<>();
    private final Random rand = new Random();

    @Override
    public void onStart(Level level) {
        GameSession session = App.getGameSession();
        if (session == null || session.getLawn() == null) return;

        Lawn lawn = session.getLawn();
        int rows = lawn.getRows();
        int cols = lawn.getCols();

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

                // Assign the contained entity based on the VaseType
                if (assignedType == VaseType.NORMAL) {
                    // Standard zombie alias
                    vase.setContainedZombie(
                            com.ussr.pvz.model.entities.zombies.ZombieFactory.create("Zombie", r, c)
                    );
                } else if (assignedType == VaseType.GARGANTAUR) {
                    // Gargantuar alias
                    vase.setContainedZombie(
                            com.ussr.pvz.model.entities.zombies.ZombieFactory.create("Gargantuar", r, c)
                    );
                } else if (assignedType == VaseType.PLANT) {
                    vase.setSeedPackDrop(new SeedPackDrop(ItemType.SEED_PACK, -1, 1));
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
    public void onWaveComplete(Level level, int waveNumber) {
    }

    @Override
    public void onComplete(Level level) {
        vases.clear();
    }

    @Override
    public boolean isFailed(Level level) {
        // Relies on standard game over conditions managed externally (zombie reaches house)
        return false;
    }

    public boolean isWon() {
        // Win condition: All vases broken AND no zombies left on the lawn
        GameSession session = App.getGameSession();
        boolean noVasesLeft = vases.stream().noneMatch(Vase::isAlive);
        boolean noZombiesLeft = session.getZombies().stream().noneMatch(z -> z.isAlive());
        return noVasesLeft && noZombiesLeft;
    }
}