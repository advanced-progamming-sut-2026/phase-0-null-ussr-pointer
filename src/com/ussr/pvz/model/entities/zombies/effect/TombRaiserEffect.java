package com.ussr.pvz.model.entities.zombies.effect;

import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.entities.zombies.projectiles.BoneProjectile;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TombRaiserEffect implements EffectStatus {
    private final double cooldown;
    private final int numTombsToSpawn;
    private double timer;

    public TombRaiserEffect(double cooldown, int numTombsToSpawn) {
        this.cooldown = cooldown;
        this.numTombsToSpawn = numTombsToSpawn;
        this.timer = cooldown;
    }

    @Override
    public void effect(Zombie zombie, GameSession session) {
        if (!zombie.isAlive()) return;

        timer += GameClock.SECONDS_PER_TICK;

        if (timer >= cooldown) {
            timer = 0;
            throwBones(zombie, session);
        }
    }

    private void throwBones(Zombie zombie, GameSession session) {
        List<Cell> emptyCells = new ArrayList<>();
        int rows = session.getLawn().getRows();
        int cols = session.getLawn().getCols();

        int zCol = (int) zombie.getPosition().x();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (c <= zCol) {
                    Cell cell = session.getLawn().getCell(r, c);
                    if (cell != null && cell.getPlant() == null && cell.getInteractableStructure() == null) {
                        emptyCells.add(cell);
                    }
                }
            }
        }

        if (emptyCells.isEmpty()) return;

        Collections.shuffle(emptyCells);

        int thrown = 0;
        for (Cell targetCell : emptyCells) {
            if (thrown >= numTombsToSpawn) break;

            Vec2 startPos = zombie.getPosition();
            Vec2 targetPos = Vec2.of(targetCell.getCol(), targetCell.getRow());

            BoneProjectile bone = new BoneProjectile(startPos, targetPos, 1.5);

            session.addZombieProjectile(bone);

            thrown++;
        }
    }
}