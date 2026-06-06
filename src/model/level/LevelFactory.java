package model.level;

import model.level.delivery.CardDeliveryStrategy;
import model.level.delivery.ConveyorDeliveryStrategy;
import model.level.delivery.DeliveryStrategy;
import model.level.delivery.RegularDeliveryStrategy;
import model.level.special.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class LevelFactory {

    private static final Map<String, Supplier<Level>> LEVEL_REGISTRY = new HashMap<>();
    private static final Map<String, Supplier<DeliveryStrategy>> STRATEGY_REGISTRY = new HashMap<>();

    static {
        LEVEL_REGISTRY.put("RegularLevel", RegularLevel::new);
        LEVEL_REGISTRY.put("ConveyorBeltLevel", ConveyorBeltLevel::new);
        LEVEL_REGISTRY.put("NightOpsLevel", NightOpsLevel::new);
        LEVEL_REGISTRY.put("TimedWarLevel", TimedWarLevel::new);
        LEVEL_REGISTRY.put("DeadLineLevel", DeadLineLevel::new);
        LEVEL_REGISTRY.put("SaveOurSeedsLevel", SaveOurSeedsLevel::new);
        LEVEL_REGISTRY.put("LockedPlantsLevel", LockedPlantsLevel::new);
        LEVEL_REGISTRY.put("LoveYourPlantsLevel", LoveYourPlantsLevel::new);
        LEVEL_REGISTRY.put("PlantWhatYouGetLevel", PlantWhatYouGetLevel::new);

        STRATEGY_REGISTRY.put("RegularDeliveryStrategy", RegularDeliveryStrategy::new);
        STRATEGY_REGISTRY.put("ConveyorDeliveryStrategy", ConveyorDeliveryStrategy::new);
        STRATEGY_REGISTRY.put("CardDeliveryStrategy", CardDeliveryStrategy::new);
    }

    public static Level create(JsonContainer.JsonLevelData data) {
        return null;
    }

    private static List<Wave> buildWaves(List<JsonContainer.JsonWaveData> raw) {
        return null;
    }

    private static <T> Supplier<T> getFromRegistry(Map<String, Supplier<T>> registry, String key, String label) {
        return null;
    }
}
