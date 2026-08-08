package com.ussr.pvz.controller.maincontroller.gamecontroller;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.dto.LocationRequest;
import com.ussr.pvz.model.dto.PlantPlantRequest;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.level.behavior.WallnutBowlingBehavior;
import com.ussr.pvz.notification.NotificationCenter;
import com.ussr.pvz.service.game.GameService;

public class GameplayController {

    private final GameService gameService;

    private boolean paused = false;
    private boolean shovelModeActive = false;
    private boolean plantFoodModeActive = false;
    private String selectedSeedKey = null;
    private Runnable onPlantingCompleted;
    private Runnable onPlantFoodDeactivated;  // NEW

    public GameplayController() {
        this.gameService = new GameService();
    }

    public void togglePauseMenu() {
        this.paused = !this.paused;
        if (this.paused) {
            NotificationCenter.info("Game Paused");
        } else {
            NotificationCenter.info("Game Resumed");
        }
    }

    public boolean isPaused() {
        return this.paused;
    }

    public void toggleShovelMode(boolean active) {
        this.shovelModeActive = active;
        if (active) {
            this.plantFoodModeActive = false;
            this.selectedSeedKey = null;
            if (onPlantFoodDeactivated != null) onPlantFoodDeactivated.run();
        }
    }

    public void togglePlantFoodMode(boolean active) {
        this.plantFoodModeActive = active;
        if (active) {
            this.shovelModeActive = false;
            this.selectedSeedKey = null;
            if (onPlantingCompleted != null) onPlantingCompleted.run();
        } else {
            if (onPlantFoodDeactivated != null) onPlantFoodDeactivated.run();
        }
    }

    public void setSelectedSeed(String plantKey) {
        this.selectedSeedKey = plantKey;
        if (plantKey != null) {
            this.shovelModeActive = false;
            if (this.plantFoodModeActive) {
                this.plantFoodModeActive = false;
                if (onPlantFoodDeactivated != null) onPlantFoodDeactivated.run();
            }
        }
    }

    public void setOnPlantingCompleted(Runnable callback) {
        this.onPlantingCompleted = callback;
    }

    /** Registered by PlantFoodWidget so it auto-resets visuals after a feed or cancel. */
    public void setOnPlantFoodDeactivated(Runnable callback) {
        this.onPlantFoodDeactivated = callback;
    }

    public void handleGridClick(int gridX, int gridY) {
        if (this.paused) return;

        if (shovelModeActive) {
            executeShovelAction(gridX, gridY);
        } else if (plantFoodModeActive) {
            executePlantFoodAction(gridX, gridY);
        } else if (selectedSeedKey != null) {
            executePlantingAction(gridX, gridY);
        } else {
            executeSunCollection(gridX, gridY);
        }
    }

    private void executeShovelAction(int x, int y) {
        LocationRequest req = new LocationRequest(String.valueOf(x), String.valueOf(y));
        String result = gameService.pluckPlant(req);
        if (result.contains("plucked")) {
            NotificationCenter.success(result);
            toggleShovelMode(false);
        } else {
            NotificationCenter.error(result);
        }
    }

    private void executePlantFoodAction(int x, int y) {
        LocationRequest req = new LocationRequest(String.valueOf(x), String.valueOf(y));
        String result = gameService.feedPlant(req);
        // Always deactivate — don't leave mode stuck on a miss-click
        togglePlantFoodMode(false);
        if (result.contains("fed")) {
            NotificationCenter.success(result);
        } else {
            NotificationCenter.warning(result);
        }
    }

    public void plantAt(String plantKey, int gridX, int gridY) {
        if (this.paused || plantKey == null) return;

        String result = executeSelectedPlant(plantKey, gridX, gridY);

        if (result.contains("placed") || result.contains("Rolled")) {
            NotificationCenter.success(result);
            setSelectedSeed(null);
            if (onPlantingCompleted != null) onPlantingCompleted.run();
        } else {
            NotificationCenter.warning(result);
        }
    }

    private String executeSelectedPlant(String plantKey, int gridX, int gridY) {
        GameSession session = App.getGameSession();
        if (session != null
                && session.getLevel() != null
                && session.getLevel().getBehavior() instanceof WallnutBowlingBehavior bowling) {
            return bowling.rollNut(plantKey, gridX, gridY);
        }

        PlantPlantRequest request = new PlantPlantRequest(
                plantKey,
                String.valueOf(gridX),
                String.valueOf(gridY)
        );
        return gameService.plantPlant(request);
    }

    private void executePlantingAction(int x, int y) {
        plantAt(selectedSeedKey, x, y);
    }

    private void executeSunCollection(int x, int y) {
        LocationRequest req = new LocationRequest(String.valueOf(x), String.valueOf(y));
        gameService.collectSun(req);
    }

    public boolean isShovelModeActive() {
        return shovelModeActive;
    }

    public boolean isPlantFoodModeActive() {
        return plantFoodModeActive;
    }

    public String getSelectedSeedKey() {
        return selectedSeedKey;
    }
}
