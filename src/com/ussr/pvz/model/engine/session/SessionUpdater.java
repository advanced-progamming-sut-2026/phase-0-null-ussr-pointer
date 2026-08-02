package com.ussr.pvz.model.engine.session;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.structures.LawnMower;
import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.entities.items.sun.SunToken;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.level.chaptereffect.ChapterEffect;
import com.ussr.pvz.model.level.chaptereffect.ChapterEffectRegistry;

public final class SessionUpdater {
    private final GameSession session;
    private final GameClock clock;
    private final Runnable rewardAction;

    private double skySunTimer;

    public SessionUpdater(
            GameSession session,
            Runnable rewardAction
    ) {
        this.session = session;
        this.rewardAction = rewardAction;
        this.clock = new GameClock();
    }

    public void update(float delta) {
        if (session.isGameOver()
                || !Float.isFinite(delta)
                || delta <= 0f) {
            return;
        }

        clock.update(delta);
        updateFallingSun(delta);
        updateChapterEffect(delta);
        updateLevelBehavior(delta);
        updatePlantRecharge(delta);
        updateGameState();
    }

    private void updateChapterEffect(float delta) {
        Level level = session.getLevel();

        if (level == null) {
            return;
        }

        ChapterEffect effect =
                ChapterEffectRegistry.get(
                        level.getChapter()
                );

        if (effect != null) {
            effect.onTick(session, level, delta);
        }
    }

    private void updateLevelBehavior(float delta) {
        Level level = session.getLevel();

        if (level != null
                && level.getBehavior() != null) {
            level.getBehavior().tick(
                    session,
                    delta
            );
        }
    }

    private void updatePlantRecharge(float delta) {
        if (App.getAccount() == null) {
            return;
        }

        for (Plant plant
                : App.getAccount()
                .getAdventureProgress()
                .getAccountPlants()) {
            plant.tickRecharge(delta);
        }
    }

    private void updateFallingSun(float delta) {
        Level level = session.getLevel();
        Lawn lawn = session.getLawn();

        if (level == null
                || !level.isSunFalling()
                || lawn == null) {
            return;
        }

        double baseInterval = calculateSunInterval();
        double actualInterval =
                baseInterval * difficultyMultiplier();

        skySunTimer += delta;

        if (skySunTimer >= actualInterval) {
            session.addItem(new SunToken(
                    lawn.getRows(),
                    lawn.getCols()
            ));

            skySunTimer -= actualInterval;
        }
    }

    private double calculateSunInterval() {
        return Math.max(
                6.0,
                12.0 - 0.05 * clock.getElapsedSeconds()
        );
    }

    private double difficultyMultiplier() {
        int difficulty = App.getAccount() == null
                ? 3
                : App.getAccount().getDifficultyLvl();

        return difficulty / 3.0;
    }



    private void updateGameState() {
        plantDeath();
        cleanupDeadGridStructures();
        checkZombieBreaches();

        if (shouldFailLevel()) {
            failLevel();
            return;
        }

        if (shouldCompleteLevel()) {
            completeLevel();
        }
    }

    private void checkZombieBreaches() {
        Level level = session.getLevel();

        if (level == null
                || level.getBehavior() == null) {
            return;
        }

        for (Zombie zombie : session.getZombies()) {
            checkZombieBreach(zombie, level);
        }
    }

    private boolean shouldFailLevel() {
        Level level = session.getLevel();

        return !session.isGameOver()
                && level != null
                && level.getBehavior() != null
                && level.getBehavior().isFailed(level);
    }

    private void failLevel() {
        session.setGameOver(true);
        App.setMenuState(MenuState.MAIN);
    }

    private boolean shouldCompleteLevel() {
        Level level = session.getLevel();

        if (session.isGameOver()
                || level == null
                || level.getBehavior() == null) {
            return false;
        }

        boolean allowsAutoWin =
                level.getBehavior()
                        .isAutoWinOnWavesClear();

        return allowsAutoWin
                && session.areWavesDone();
    }

    private void completeLevel() {
        session.setGameOver(true);
        rewardAction.run();
        App.setMenuState(MenuState.GAME);
    }

    private void plantDeath() {
        Lawn lawn = session.getLawn();

        if (lawn == null) {
            return;
        }

        for (Plant plant : session.getPlants()) {
            removeDeadPlantFromLawn(plant, lawn);
        }
    }

    private void removeDeadPlantFromLawn(
            Plant plant,
            Lawn lawn
    ) {
        if (plant.isAlive()
                || plant.getLocation() == null) {
            return;
        }

        Cell cell = lawn.getCell(
                plant.getLocation().y(),
                plant.getLocation().x()
        );

        if (cell != null && cell.getPlant() == plant) {
            cell.setPlant(null);
        }

        session.notifyPlantDied(plant);
    }

    public double getElapsedSeconds() {
        return clock.getElapsedSeconds();
    }

    public void registerEntity(
            com.ussr.pvz.model.engine.Tickable entity
    ) {
        clock.addEntity(entity);
    }

    public void resetClock() {
        clock.reset();
    }

    private void cleanupLawnStructures() {
        Lawn lawn = session.getLawn();

        if (lawn == null) {
            return;
        }

        for (int row = 0; row < lawn.getRows(); row++) {
            for (int column = 0;
                 column < lawn.getCols();
                 column++) {
                cleanupLawnCell(lawn, row, column);
            }
        }
    }

    private void cleanupLawnCell(
            Lawn lawn,
            int row,
            int column
    ) {
        Cell cell = lawn.getCell(row, column);

        if (cell == null
                || cell.getInteractableStructure() == null) {
            return;
        }

        var structure =
                cell.getInteractableStructure();

        if (!structure.isAlive()) {
            structure.onDestroy(session);
            cell.setStructure(null);
        }
    }

    private void checkZombieBreach(
            Zombie zombie,
            Level level
    ) {
        if (session.isGameOver() || !zombie.isAlive()) {
            return;
        }

        double x = zombie.getPosition().x();
        int row = (int) zombie.getPosition().y();

        if (x < 0.0) {
            activateMower(row);
        }

        if (x < -1.5) {
            level.getBehavior()
                    .onZombieBreach(session, zombie);
        }
    }

    private void activateMower(int row) {
        LawnMower mower = getMowerForLane(row);

        if (mower == null || mower.isActivated()) {
            return;
        }

        mower.activate();

        session.getEventBus().publish(
                new GameEvent.LawnMowerTriggered(row)
        );

        session.getEventBus().publish(
                new GameEvent.ZombieBreachedLane(row)
        );
    }

    private LawnMower getMowerForLane(int lane) {
        return session.getLawnMowers()
                .stream()
                .filter(mower -> mower.getLane() == lane)
                .findFirst()
                .orElse(null);
    }

    private void cleanupDeadGridStructures() {
        session.getPlants()
                .removeIf(plant -> !plant.isAlive());

        session.getZombies()
                .removeIf(zombie -> !zombie.isAlive());

        session.getItems()
                .removeIf(item -> !item.isAlive());

        session.getProjectiles()
                .removeIf(projectile -> !projectile.isAlive());

        session.getLawnMowers()
                .removeIf(mower -> !mower.isAlive());

        session.getZombieProjectiles()
                .removeIf(projectile -> !projectile.isAlive());

        cleanupLawnStructures();
    }
}