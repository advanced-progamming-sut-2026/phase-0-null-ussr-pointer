package com.ussr.pvz.view.mainmenu.greenhouse;

import com.ussr.pvz.model.entities.plants.PlantFactory;
import com.ussr.pvz.view.animation.PamActor;
import pvz.libpvz.pam.PamPlayer;

import java.util.Map;

public class SproutView extends PamActor {

    public static final String SPROUT_PAM = "768/INITIAL/ZEN_GARDEN/PLANT_ANIMATIONS/SPROUT/SPROUT.PAM";
    public static final String MARIGOLD_PAM = "768/INITIAL/PLANT/MARIGOLD/MARIGOLD.PAM";

    private static final float PLANT_PAM_SCALE = 0.22f;

    public SproutView(PamPlayer player, String plantType) {
        super(player, resolvePamPath(plantType), resolveClipName(plantType));

        if ("MARIGOLD".equalsIgnoreCase(plantType)) {
            // Marigold specific bounds & offset above soil
            setSize(80f, 70f);
            setPamScale(0.32f);
            setOffsetY(18f); // Lift higher so the flower sits nicely above the pot rim
        } else if ("SPROUT".equalsIgnoreCase(plantType) || plantType == null || plantType.isBlank()) {
            // Default Sprout bounds & offset
            setSize(80f, 70f);
            setPamScale(0.35f);
            setOffsetY(8f);
        } else {
            setSize(80f, 70f);
            setPamScale(PLANT_PAM_SCALE);
            setOffsetY(10f);
        }
    }

    private static String resolvePamPath(String plantType) {
        if ("MARIGOLD".equalsIgnoreCase(plantType)) {
            return MARIGOLD_PAM;
        }
        if ("SPROUT".equalsIgnoreCase(plantType) || plantType == null || plantType.isBlank()) {
            return SPROUT_PAM;
        }

        Map<String, Object> data = PlantFactory.getPlantData(plantType);
        String pamLocation = data != null ? (String) data.get("pamLocation") : null;
        return pamLocation != null ? pamLocation : SPROUT_PAM;
    }

    private static String resolveClipName(String plantType) {
        if ("MARIGOLD".equalsIgnoreCase(plantType)) {
            return "idle"; // PamActor will fallback to "anim" or "main" automatically if "idle" isn't found
        }
        if ("SPROUT".equalsIgnoreCase(plantType) || plantType == null || plantType.isBlank()) {
            return "sprout";
        }
        return "idle";
    }
}