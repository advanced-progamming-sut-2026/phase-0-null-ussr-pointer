package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.items.ItemType;
import com.ussr.pvz.model.entities.items.sun.ProducedSun;
import com.ussr.pvz.model.entities.items.sun.SunSplitter;
import com.ussr.pvz.model.entities.items.sun.SunSpreadUtil;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.level.behavior.MultiplayerIZombieBehavior;
import com.badlogic.gdx.math.MathUtils;
import com.ussr.pvz.model.entities.plants.upgrades.SpecialUpgrade;

import java.util.List;

public class SunProduceStrategy implements ActStrategy {
    private boolean isInstantBurst;

    // Default constructor (keeps Sunflower behaving exactly as it did before)
    public SunProduceStrategy() {
        this.isInstantBurst = false;
    }
    @Override
    public void act(Plant user, GameSession session) {
        if (session.getLevel() != null
                && session.getLevel().getBehavior()
                instanceof MultiplayerIZombieBehavior multiplayer
                && !multiplayer.isPlantsPlayer()) {
            return;
        }

        setInstantBurst(user);
        int x = user.getLocation().x();
        int y = user.getLocation().y();

        boolean sunAlreadyExists = session.getItems().stream()
                .anyMatch(item -> item.getItemType() == ItemType.SUN
                        && !item.isCollected()
                        && item.getLocation().x() == x
                        && item.getLocation().y() == y);

        if (sunAlreadyExists) {
            return;
        }

        int sunValue = (int) Math.round(
                user.getAbilityValue()
                        + user.getSpecialUpgradeValue(SpecialUpgrade.SUN_AMOUNT_BUFF)
        );
        double doubleChance = user.getSpecialUpgradeValue(SpecialUpgrade.DOUBLE_SUN_CHANCE);
        if (doubleChance > 0 && MathUtils.random() < Math.min(1.0, doubleChance)) {
            sunValue *= 2;
        }
        if (isInstantBurst) {
            // Gold Bloom: split the big burst into several 25/50/75 suns instead of one huge one
            List<Integer> denominations = SunSplitter.split(sunValue);
            int count = denominations.size();
            for (int i = 0; i < count; i++) {
                float[] offset = SunSpreadUtil.offsetFor(i, count);
                session.addItem(new ProducedSun(x, y, denominations.get(i), user.getName(),
                        offset[0], offset[1]));
            }
            //todo : we may should make the plant of the sell null
            // when a plant dies ( we can do it somewhere else like in the set alive method)
            user.setAlive(false);
        } else {
            ProducedSun sun = new ProducedSun(x, y, sunValue, user.getName());
            session.addItem(sun);
        }
        user.triggerProduceAnimation();
    }

    private void setInstantBurst(Plant user) {
        if(user.getName().equalsIgnoreCase("gold bloom"))
            isInstantBurst = true;
    }
}