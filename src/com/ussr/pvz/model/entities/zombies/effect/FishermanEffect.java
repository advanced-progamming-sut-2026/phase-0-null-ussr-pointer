package com.ussr.pvz.model.entities.zombies.effect;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.engine.SmoothMoveTickable;
import com.ussr.pvz.model.engine.Tickable;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Faction;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FishermanEffect implements EffectStatus {
    private static final double REEL_DURATION_SECONDS = 0.4;
    private static final double REPOSITION_DURATION_SECONDS = 0.5;

    private final double delayBetweenCasting;
    private double timer;
    private boolean repositioning = false;
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
            if (!repositioning && zombie.getPosition().x() < rightmostCol) {
                repositioning = true;
                session.registerTickable(new RepositionWatcher(
                        new SmoothMoveTickable(zombie, Vec2.of(rightmostCol, zombie.getPosition().y()), REPOSITION_DURATION_SECONDS),
                        () -> repositioning = false
                ));
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
        zombie.queueAnimEvent("cast");
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
            session.registerTickable(new SmoothMoveTickable(hookedPlant, Vec2.of(targetX, zRow), REEL_DURATION_SECONDS));
            zombie.queueAnimEvent("reel");
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

    private static class RepositionWatcher implements Tickable {
        private final SmoothMoveTickable inner;
        private final Runnable onFinished;
        private boolean notified = false;

        RepositionWatcher(SmoothMoveTickable inner, Runnable onFinished) {
            this.inner = inner;
            this.onFinished = onFinished;
        }

        @Override
        public void update(float delta) {
            inner.update(delta);
            if (inner.isFinished() && !notified) {
                notified = true;
                onFinished.run();
            }
        }
    }
}