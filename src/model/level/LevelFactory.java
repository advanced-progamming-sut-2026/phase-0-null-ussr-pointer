package model.level;

import model.level.behavior.LevelBehavior;
import model.level.behavior.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class LevelFactory {

    private static final Map<String, Supplier<LevelBehavior>> BEHAVIOR_REGISTRY = new HashMap<>();

    static {
        BEHAVIOR_REGISTRY.put("ConveyorBehavior", ConveyorBehavior::new);
        BEHAVIOR_REGISTRY.put("SaveOurSeedsBehavior", SaveOurSeedsBehavior::new);
        BEHAVIOR_REGISTRY.put("PlantWhatYouGetBehavior", PlantWhatYouGetBehavior::new);
        BEHAVIOR_REGISTRY.put("BossBehavior", BossBehavior::new);
    }

    public static Level create(JsonContainer.JsonLevelData data) {
        Level level = new Level();
        level.setId(data.id);
        level.setOrder(data.order);
        level.setSunFalling(data.sunFalling);
        level.setTimeLimitSeconds(data.timeLimitSeconds);
        level.setDeadlineColumn(data.deadlineColumn);
        level.setAllowedPlantsLost(data.allowedPlantsLost);
        level.setLockedPlants(data.lockedPlants);
        /// the following should be implemented some other place first
        //level.setDeliveryStrategy(StrategyFactory.create(data.deliveryStrategy));
        //level.setAllowedZombies(buildZombies(data.allowedZombies));
        //level.setWaves(buildWaves(data.waves));

        if (data.behavior != null && !data.behavior.isBlank()) {
            level.setBehavior(BEHAVIOR_REGISTRY.get(data.behavior).get());
        }

        return level;
    }
}
