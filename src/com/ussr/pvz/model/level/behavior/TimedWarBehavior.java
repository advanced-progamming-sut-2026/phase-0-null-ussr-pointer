package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.level.Level;

import java.util.Random;

public class TimedWarBehavior implements LevelBehavior {
    //todo it should set as behavior if the limitation time of the level is bigger than -1 in the level factory
    //todo the is fail should check every tick
    public enum LimitationType {
        Zombie(5),
        sun(500);

        private final int value;

        LimitationType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private LimitationType limitationType;
    private int counter = 0;

    @Override
    public void onStart(Level level) {
        Random rand = new Random();
        rand.setSeed(System.currentTimeMillis());
        int chance = rand.nextInt();
        if (chance % 2 == 0) {
            limitationType = LimitationType.Zombie;
        } else {
            limitationType = LimitationType.sun;
        }
    }

    @Override
    public void onWaveComplete(Level level, int waveNumber) {

    }

    @Override
    public void onComplete(Level level) {

    }

    @Override
    public boolean isFailed(Level level) {
        if (App.getGameSession().getElapsedSeconds() > level.getTimeLimitSeconds()) {
            return limitationType.value > counter;
        }
        return false;
    }

    public LimitationType getLimitationType() {
        return limitationType;
    }

    public void triggerSunCollected(int amount) {
        counter += amount;
    }

    public void triggerZombieDied() {
        counter++;
    }
}
