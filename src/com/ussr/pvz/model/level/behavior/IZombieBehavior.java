package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.Brain;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.GameSession;
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

public class IZombieBehavior implements LevelBehavior {
    private final int redLineColumn;
    private final int startingSun;
    private final List<Brain> brains = new ArrayList<>();
    private final List<PrePlacedPlant> plantLayout;

    public IZombieBehavior(int redLineColumn, int startingSun, List<PrePlacedPlant> plantLayout) {
        this.redLineColumn = redLineColumn;
        // Enforce the 150 starting sun requirement
        this.startingSun = startingSun > 0 ? startingSun : 150;
        this.plantLayout = plantLayout != null ? plantLayout : new ArrayList<>();
    }

    @Override
    public void onStart(Level level) {
        GameSession session = App.getGameSession();
        if (session == null || session.getLawn() == null) return;

        level.setSunFalling(false);

        // Give exactly starting sun (150)
        int currentSun = session.getSunCount();
        session.addSun(startingSun - currentSun);

        int rows = session.getLawn().getRows();
        int cols = session.getLawn().getCols();

        // 1. Place Brains at column 0 for every row
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

        // 2. Pre-place cardboard plants
        for (PrePlacedPlant pp : plantLayout) {
            int plantId = 1; // TODO: Replace with PlantFactory.getIdByName(pp.plantName());
            com.ussr.pvz.model.entities.plants.Plant plant =
                    com.ussr.pvz.model.entities.plants.PlantFactory.createPlant(plantId, 1);

            plant.setLocation(new com.ussr.pvz.model.entities.plants.Plant.Location(pp.col(), pp.row()));
            session.getLawn().getCell(pp.row(), pp.col()).setPlant(plant);
            session.getPlants().add(plant);
        }

        // 3. Spawn the special Sun-Producing Zombies (one per row, rightmost column)
        for (int r = 0; r < rows; r++) {
            Zombie sunZombie = new Zombie("SunProducerZombie", null, false);
            // Health equal to a bucket zombie (200 base + 1100 bucket = 1300)
            sunZombie.setMaxHp(1300);
            sunZombie.setHp(1300);
            sunZombie.setEatDps(0);
            sunZombie.setSize(ZombieSize.DEFAULT);
            sunZombie.setPosition(Vec2.of(cols - 1, r));

            // Stationary so it acts as a passive generator
            sunZombie.setMoveBehavior(new StationaryMove());
            sunZombie.setAttackBehavior(new ChompAttack());
            sunZombie.setDefenseBehavior(new NormalDefense());
            sunZombie.setEffectStatus(new SunProducerZombieEffect());

            session.spawnZombie(sunZombie);
        }
    }

    @Override
    public void onWaveComplete(Level level, int waveNumber) {
    }

    @Override
    public void onComplete(Level level) {
        brains.clear();
    }

    @Override
    public boolean isFailed(Level level) {
        GameSession session = App.getGameSession();
        if (session == null) return false;

        // Note: 50 is used as a baseline cheapest zombie cost. You can dynamically fetch the
        // minimum cost of the 5 available zombies for the current stage if needed.
        boolean outOfSun = session.getSunCount() < 50;
        boolean noZombiesAlive = session.getZombies().stream().noneMatch(GameEntity::isAlive);

        // Fails if no sun to place units AND no zombies exist on the lawn
        return outOfSun && noZombiesAlive;
    }

    public boolean isWon() {
        return brains.stream().noneMatch(Brain::isAlive);
    }

    public int getRedLineColumn() {
        return redLineColumn;
    }

    public record PrePlacedPlant(String plantName, int row, int col) {
    }
}