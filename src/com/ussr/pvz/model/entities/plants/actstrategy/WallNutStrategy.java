package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.structures.InteractableStructure;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.entities.plants.upgrades.SpecialUpgrade;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;
import java.util.Random;


public class WallNutStrategy implements ActStrategy {

    private static final Random RANDOM = new Random();
    private static final double ATTRACT_RADIUS = 1.5;
    private static final int TOP_ROW = 0;
    private static final int BOTTOM_ROW = 4;

    @Override
    public void act(Plant user, GameSession session) {
        if (!user.getTags().contains(Tag.MOVE_ZOMBIES)) return;

        if((int) user.getAbilityValue() == 3) {
            Vec2 userPos = user.getPosition();
            for(Zombie zombie : session.getZombies()) {
                if (!zombie.isAlive()) continue;
                Vec2 zombiePos = zombie.getPosition();
                double rowDistance = Math.abs(zombiePos.y() - userPos.y());
                double horizontalDistance = Math.abs(zombiePos.x() - userPos.x());
                if (rowDistance > 0.5 && rowDistance <= 1.5
                        && horizontalDistance <= ATTRACT_RADIUS) {
                    zombie.setPosition(new Vec2(zombiePos.x(), userPos.y()));
                }
            }
        }
    }


    private void divertZombie(Zombie zombie) {
        Vec2 zomPos = zombie.getPosition();
        int currentRow = (int) zomPos.y();

        int dy;
        if (currentRow <= TOP_ROW) {
            dy = 1;
        } else if (currentRow >= BOTTOM_ROW) {
            dy = -1;
        } else {
            dy = RANDOM.nextBoolean() ? 1 : -1;
        }
        zombie.setPosition(new Vec2(zomPos.x(), zomPos.y() + dy));
    }

    public void onDamageAct(Plant user, Zombie dealer, int receivedDamage) {
        if(dealer == null) return;

        switch ((int) user.getAbilityValue()) {
            case 1 :
                dealer.takeDamage(user.getDamage()
                        + user.getSpecialUpgradeInt(SpecialUpgrade.REFLECT_DAMAGE_BUFF), user);
                break;
            case 2 :
                double eatDps = Math.max(1.0, dealer.getEatDps());
                double secondsEaten = receivedDamage / eatDps;
                if (user.addAndConsumeDefensiveCharge(
                        secondsEaten, Math.max(0.1, user.getActionInterval())) > 0) {
                    divertZombie(dealer);
                }
                break;
            case 3 :
                return;
            case 4 :
                return;
            case 5 :
                int rewards = user.addAndConsumeDefensiveCharge(receivedDamage, 100.0);
                if (rewards > 0) {
                    App.getGameSession().addSun(rewards * (5
                            + user.getSpecialUpgradeInt(SpecialUpgrade.SUN_DROP_INCREMENT)));
                }

        }

        user.setInternalTimer(0.0);
    }

    public void onDie(Plant user) {
        if((int) user.getAbilityValue() == 4)
            handleExplode(user);
    }

    private void handleExplode(Plant user) {
        for(Zombie zombie : App.getGameSession().getZombies()) {
            if(zombie.getPosition().distanceTo(user.getPosition()) < 1.5)
                zombie.takeDamage(user.getDamage());
        }
        for(InteractableStructure structure : App.getGameSession().getLawn().getAllInteractable()) {
            if(structure.getPosition() != null && structure.getPosition().distanceTo(user.getPosition()) < 1.5)
                structure.takeDamage(user.getDamage());
        }
    }
}