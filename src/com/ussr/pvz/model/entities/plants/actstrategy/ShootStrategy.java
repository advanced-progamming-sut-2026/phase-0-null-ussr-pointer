package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;

import java.rmi.server.ServerNotActiveException;

public class ShootStrategy implements ActStrategy {

    @Override
    public void act(Plant user, GameSession session) {
        if(user.getTimeLeft() <= 0) {
            switch (user.getName()) {
                case "Peashooter":
                    peaShooter();
                    break;
                case "Repeater":
                    repeater();
                    break;
                case "Threepeater":
                    threepeater();
                    break;
                case "Snow Pea":
                    snowPea();
                    break;
                case "Rotobaga":
                    rotobaga();
                case "Pea Pod":
                    peaPod();
                    break;
                case "Split Pea":
                    splitPea();
                    break;
                case "Citron":
                    citron();
                    break;
                case "Bowling Bulb":
                    bowlingBulb();
                    break;
                case "Fire Peashooter":
                    firePeashooter();
                    break;
                case "Starfruit":
                    starfruit();
                    break;
                case "Goo Peashooter":
                    gooPeashooter();
                    break;
                case "Mega Gatling Pea":
                    megaGatlingPea();
                    break;
                case "Sea-shroom":
                    seaShroom();
                    break;
                case "Puff-shroom":
                    puffShroom();
                    break;
            }
            user.setTimeLeft(user.getActionInterval());
        }
    }

    private void peaShooter() {

    }
    private void repeater() {}
    private void threepeater() {}
    private void snowPea() {}
    private void rotobaga() {}
    private void peaPod() {}
    private void splitPea() {}
    private void citron() {}
    private void bowlingBulb() {}
    private void firePeashooter() {}
    private void starfruit() {}
    private void gooPeashooter() {}
    private void megaGatlingPea() {}
    private void seaShroom() {}
    private void puffShroom() {}
}
