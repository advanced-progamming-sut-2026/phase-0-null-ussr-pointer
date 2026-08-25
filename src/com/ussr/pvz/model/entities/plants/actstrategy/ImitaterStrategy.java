package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;

public class ImitaterStrategy implements ActStrategy {

    @Override
    public void act(Plant user, GameSession session) {
    }

    public void beginImitation(Plant self, Plant target) {
        if (self == null || target == null) return;

        self.setImitationTargetName(target.getName());
        self.setImitationSourcePlant(target);
        self.setImitationOverlayActive(true);
        self.setState(Plant.PlantState.IMITATE_IDLE);

        if (App.getGameSession() != null) {
            App.getGameSession().registerImitatedType(target.getName());
        }
    }

    public void onIdleClipFinished(Plant self) {
        if (self.getState() == Plant.PlantState.IMITATE_IDLE) {
            self.setState(Plant.PlantState.IMITATE_ATTACK);
        }
    }

    public void onAttackClipFinished(Plant self) {
        if (self.getState() != Plant.PlantState.IMITATE_ATTACK) return;

        String targetName = self.getImitationTargetName();
        if (targetName == null) {
            self.setState(Plant.PlantState.ACTIVE);
            return;
        }

        try {
            self.transformInto(targetName);
        } catch (IllegalArgumentException e) {
            self.setState(Plant.PlantState.ACTIVE);
        }
    }
}