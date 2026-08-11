package com.ussr.pvztest.model.entities.plants.animation;

import com.ussr.pvz.model.entities.plants.animation.PlantAnimationController;
import com.ussr.pvz.model.entities.plants.animation.PlantAnimationState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlantAnimationControllerTest {

    @Test
    void standardAttackReturnsToIdleAfterItsDuration() {
        PlantAnimationController controller = new PlantAnimationController();

        controller.playAttack("Cactus", 1, 0.5f);

        assertEquals(PlantAnimationState.ATTACKING, controller.getState());
        assertEquals("attack", controller.getCurrentClip());

        controller.update(0.5f);

        assertEquals(PlantAnimationState.IDLE, controller.getState());
        assertEquals("idle", controller.getCurrentClip());
    }

    @Test
    void resolvesPlantsWhoseActionClipIsNotNamedAttack() {
        PlantAnimationController controller = new PlantAnimationController();

        controller.playAttack("Fume-shroom", 1, 0.5f);
        assertEquals("special", controller.getCurrentClip());

        controller.playAttack("Chomper", 1, 0.5f);
        assertEquals("swallow", controller.getCurrentClip());

        controller.playAttack("Puff-shroom", 3, 0.5f);
        assertEquals("special_stage3", controller.getCurrentClip());
    }

    @Test
    void goldBloomUsesItsAvailableProduceClip() {
        PlantAnimationController controller = new PlantAnimationController();

        controller.playProduce("Gold Bloom", 0.5f);

        assertEquals(PlantAnimationState.PRODUCING, controller.getState());
        assertEquals("attack", controller.getCurrentClip());
    }
}
