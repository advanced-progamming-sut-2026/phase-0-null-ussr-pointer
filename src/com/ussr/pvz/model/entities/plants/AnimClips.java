package com.ussr.pvz.model.entities.plants;

import com.ussr.pvz.model.entities.plants.actstrategy.WallNutStrategy;

import static com.ussr.pvz.model.entities.plants.Plant.MAX_PEA_POD_STACK;

public class AnimClips {

    public static String getAnimationClip(Plant plant) {
        if (plant.getState() == Plant.PlantState.DYING)        return resolveDyingClip(plant);
        if (plant.getState() == Plant.PlantState.IMITATE_IDLE) return "idle";
        if (plant.getState() == Plant.PlantState.IMITATE_ATTACK) return "attack";
        String foodClip = resolvePlantFoodClip(plant);
        if (foodClip != null) return foodClip;
        if (plant.getState() == Plant.PlantState.PREPPING) return "prepping";
        if (plant.getActStrategy() instanceof WallNutStrategy) return resolveWallNutClip(plant);
        if (plant.isPotatoMine()) return resolvePotatoMineClip(plant);
        if ("Kiwibeast".equalsIgnoreCase(plant.getName())) return resolveKiwibeastClip(plant);
        return resolvePeaPodClip(plant);
    }

    // ── Dying ─────────────────────────────────────────────────────────────────

    private static String resolveDyingClip(Plant plant) {
        if ("Doom-shroom".equalsIgnoreCase(plant.getName())) {
            int stage = Math.clamp(plant.getCurrentStage(), 1, 3);
            return "stage" + stage + "_explode";
        }
        if ("Squash".equalsIgnoreCase(plant.getName()))      return "jump_down_left";
        if ("Explode-o-nut".equalsIgnoreCase(plant.getName())) return "damage3";
        return "attack";
    }

    // ── Plant food ────────────────────────────────────────────────────────────

    private static String resolvePlantFoodClip(Plant plant) {
        if (plant.getPlantFoodTimer() <= 0 && !plant.isBuffed()) return null;
        if (plant.isPlantFoodIntroActive()) return "plantfood";
        if ("Cactus".equalsIgnoreCase(plant.getName())) {
            return plant.getAnimationController().getCurrentClip() + "_plantfood";
        }
        return null;
    }

    // ── Wall-nut family ───────────────────────────────────────────────────────

    private static String resolveWallNutClip(Plant plant) {
        float hp = (float) plant.getHp() / (float) plant.getMaxHp();
        String name = plant.getName();
        if ("Sweet Potato".equalsIgnoreCase(name))  return resolveSweetPotatoClip(hp);
        if ("Wall-nut".equalsIgnoreCase(name)
                || "Explode-o-nut".equalsIgnoreCase(name)
                || "Endurian".equalsIgnoreCase(name)) return resolveWallNutDamageClip(hp);
        if ("Tall-nut".equalsIgnoreCase(name))      return resolveTallNutClip(hp);
        if ("Garlic".equalsIgnoreCase(name))        return resolveGarlicClip(hp);
        if ("Pumpkin".equalsIgnoreCase(name))       return resolvePumpkinClip(hp);
        return plant.getAnimationController().getCurrentClip();
    }

    private static String resolveSweetPotatoClip(float hp) {
        if (hp <= 0.15f) return "idle_damage3";
        if (hp <= 0.40f) return "idle_damage2";
        if (hp <= 0.70f) return "idle_damage";
        return "idle";
    }

    private static String resolveWallNutDamageClip(float hp) {
        if (hp <= 0.15f) return "damage3";
        if (hp <= 0.40f) return "damage2";
        if (hp <= 0.70f) return "damage";
        return "idle";
    }

    private static String resolveTallNutClip(float hp) {
        if (hp <= 0.40f) return "damage2";
        if (hp <= 0.70f) return "damage";
        return "idle";
    }

    private static String resolveGarlicClip(float hp) {
        if (hp <= 0.40f) return "idle_damage2";
        if (hp <= 0.70f) return "idle_damage";
        return "idle";
    }

    private static String resolvePumpkinClip(float hp) {
        if (hp <= 0.40f) return "idle3";
        if (hp <= 0.70f) return "idle2";
        return "idle";
    }

    // ── Potato Mine ───────────────────────────────────────────────────────────

    private static String resolvePotatoMineClip(Plant plant) {
        if (!plant.isMineArmed())             return "plant_idle";
        if (plant.getMineRecoverTimer() > 0f) return "recover";
        return "idle";
    }

    // ── Kiwibeast ─────────────────────────────────────────────────────────────

    private static String resolveKiwibeastClip(Plant plant) {
        String current = plant.getAnimationController().getCurrentClip();
        if ("idle".equals(current)) return "idle_stage" + Math.clamp(plant.getCurrentStage(), 1, 3);
        return current;
    }

    // ── Pea Pod (and default fallthrough) ────────────────────────────────────

    private static String resolvePeaPodClip(Plant plant) {
        String current = plant.getAnimationController().getCurrentClip();
        if (!"Pea Pod".equalsIgnoreCase(plant.getName())) return current;
        int pods = Math.clamp(plant.getStackNumber(), 1, MAX_PEA_POD_STACK);
        if ("idle".equals(current))   return pods == 1 ? "idle"   : "idle" + pods;
        if ("attack".equals(current)) return pods == 1 ? "attack" : "attack " + pods;
        return current;
    }
}