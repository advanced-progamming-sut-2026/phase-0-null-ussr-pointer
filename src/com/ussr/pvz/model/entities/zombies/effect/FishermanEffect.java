package com.ussr.pvz.model.entities.zombies.effect;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Faction;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FishermanEffect implements EffectStatus {
    private final double delayBetweenCasting;
    private double timer;
    private static final Random RANDOM = new Random();

    public FishermanEffect(double delayBetweenCasting) {
        this.delayBetweenCasting = delayBetweenCasting;
        this.timer = delayBetweenCasting;
    }

    @Override
    public void effect(
            Zombie zombie,
            GameSession session,
            float delta
    ) {
        if (!zombie.isAlive()) return;

        if (zombie.getFaction() == Faction.ZOMBIES) {
            int rightmostCol = session.getLawn().getCols() - 1;
            if (zombie.getPosition().x() < rightmostCol) {
                zombie.setPosition(Vec2.of(rightmostCol, zombie.getPosition().y()));
            }
        }

        timer += delta;
        if (timer >= delayBetweenCasting) {
            if (castHook(zombie, session)) {
                timer = 0;
            }
        }
    }

    private boolean castHook(Zombie zombie, GameSession session) {
        int zRow = (int) zombie.getPosition().y();
        int zCol = (int) zombie.getPosition().x();

        if (zombie.getFaction() == Faction.ZOMBIES) {
            return hookPlant(session, zRow, zCol, zombie);
        } else {
            return hookEnemyZombie(session, zRow, zCol);
        }
    }

    private boolean hookPlant(GameSession session, int zRow, int zCol, Zombie zombie) {
        List<Plant> plantsInRow = new ArrayList<>();
        for (int c = 0; c < zCol; c++) {
            Cell currentCell = session.getLawn().getCell(zRow, c);
            if (currentCell != null && currentCell.getPlant() != null && currentCell.getPlant().isAlive()) {
                plantsInRow.add(currentCell.getPlant());
            }
        }
        if (plantsInRow.isEmpty()) return false;

        Plant hookedPlant = plantsInRow.get(RANDOM.nextInt(plantsInRow.size()));
        int hookedX = hookedPlant.getLocation().x();
        int targetX = hookedX + 1;

        Cell targetCell = session.getLawn().getCell(zRow, targetX);

        if (targetX >= zCol || (targetCell != null && targetCell.getPlant() != null)) {
            hookedPlant.takeDamage(hookedPlant.getHp(), zombie);
            return true;
        }

        if (targetCell != null && targetCell.getInteractableStructure() == null) {
            Cell currentCell = session.getLawn().getCell(zRow, hookedX);
            currentCell.setPlant(null);
            targetCell.setPlant(hookedPlant);
            hookedPlant.setLocation(new Plant.Location(targetX, zRow));
            hookedPlant.setPosition(Vec2.of(targetX, zRow));
            return true;
        }

        return false;
    }

    private boolean hookEnemyZombie(GameSession session, int zRow, int zCol) {
        for (Zombie target : session.getZombies()) {
            if (target.isAlive() && target.getFaction() == Faction.ZOMBIES
                    && (int) target.getPosition().y() == zRow && target.getPosition().x() > zCol) {
                target.takeDamage(target.getHp());
                return true;
            }
        }
        return false;
    }
}