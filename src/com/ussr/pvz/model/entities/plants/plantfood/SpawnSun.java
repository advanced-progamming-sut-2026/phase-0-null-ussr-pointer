package com.ussr.pvz.model.entities.plants.plantfood;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.items.sun.ProducedSun;
import com.ussr.pvz.model.entities.items.sun.SunSplitter;
import com.ussr.pvz.model.entities.items.sun.SunSpreadUtil;
import com.ussr.pvz.model.entities.plants.Plant;

import java.util.List;

public class SpawnSun implements PlantFoodEffect {
    private final int sunAmount;
    private final boolean instantMaxGrowth;
    private final double duration;

    public SpawnSun(int sunAmount, boolean instantMaxGrowth) {
        this.sunAmount = sunAmount;
        this.instantMaxGrowth = instantMaxGrowth;
        this.duration = 0.0;
    }

    @Override
    public void triggerSuperpower(Plant user, GameSession session) {
        if (this.sunAmount > 0 && session != null) {
            int x = (int) user.getPosition().x();
            int y = (int) user.getPosition().y();

            List<Integer> denominations = SunSplitter.split(sunAmount);
            int count = denominations.size();
            for (int i = 0; i < count; i++) {
                float[] offset = SunSpreadUtil.offsetFor(i, count);
                ProducedSun sun = new ProducedSun(x, y, denominations.get(i), user.getName(),
                        offset[0], offset[1]);
                session.addItem(sun);
            }
        }
        user.setPlantFoodTimer(duration);
    }

    @Override
    public void applyStatusModifiers(Plant user) {
        if (this.instantMaxGrowth && user != null) {
            user.instantlyMature();
        }
    }

    @Override
    public void tickDurationEffect(Plant user, GameSession session, double deltaTime) {
        // Instant superpower effect; no duration or per-tick logic required.
    }
}