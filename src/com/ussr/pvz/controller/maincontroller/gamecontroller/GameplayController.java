package com.ussr.pvz.controller.maincontroller.gamecontroller;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.dto.LocationRequest;
import com.ussr.pvz.model.dto.PlantPlantRequest;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.level.delivery.ConveyorDeliveryStrategy;
import com.ussr.pvz.notification.NotificationCenter;
import com.ussr.pvz.service.ChoosePlantService;
import com.ussr.pvz.service.game.GameService;

/**
 * Handles all real-time interactions between the InGameHud/Grid and the GameSession.
 * Enforces MVC by isolating Scene2D inputs and execution states from the simulation layer.
 */
public class GameplayController {

    private final GameService gameService;

    // Execution and Interaction States
    private boolean paused = false;
    private boolean shovelModeActive = false;
    private boolean plantFoodModeActive = false;
    private String selectedSeedKey = null;

    public GameplayController() {
        this.gameService = new GameService();
    }

    /**
     * Toggles the execution state of the game loop[cite: 8].
     * The View layer must poll isPaused() to halt GameClock ticks and Stage.act() delta accumulation.
     */
    public void togglePauseMenu() {
        this.paused = !this.paused;

        if (this.paused) {
            NotificationCenter.info("Game Paused");
            // EventBus.post(new PauseEvent(true)); // Hook for UI to show PauseOverlay
        } else {
            NotificationCenter.info("Game Resumed");
            // EventBus.post(new PauseEvent(false));
        }
    }

    public boolean isPaused() {
        return this.paused;
    }

    /**
     * Sets internal state for plant removal mechanics[cite: 8].
     */
    public void toggleShovelMode(boolean active) {
        this.shovelModeActive = active;
        if (active) {
            this.plantFoodModeActive = false;
            this.selectedSeedKey = null;
        }
    }

    /**
     * Sets internal state for applying plant food[cite: 8].
     */
    public void togglePlantFoodMode(boolean active) {
        this.plantFoodModeActive = active;
        if (active) {
            this.shovelModeActive = false;
            this.selectedSeedKey = null;
        }
    }

    /**
     * Sets the currently selected plant seed from the SeedBank.
     */
    public void setSelectedSeed(String plantKey) {
        this.selectedSeedKey = plantKey;
        if (plantKey != null) {
            this.shovelModeActive = false;
            this.plantFoodModeActive = false;
        }
    }

    /**
     * Translates a grid cell click into a simulation action based on current state.
     *
     * @param gridX The grid column index
     * @param gridY The grid row index
     */
    public void handleGridClick(int gridX, int gridY) {
        if (this.paused) return; // Block interactions while paused

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

        if (result.contains("fed")) {
            NotificationCenter.success(result);
            togglePlantFoodMode(false);
        } else {
            NotificationCenter.error(result);
        }
    }

    public void plantAt(String plantKey, int gridX, int gridY) {
        if (this.paused || plantKey == null) return;

        PlantPlantRequest req = new PlantPlantRequest(plantKey, String.valueOf(gridX), String.valueOf(gridY));
        String result = gameService.plantPlant(req);

        if (result.contains("placed")) {
            NotificationCenter.success(result);
            consumeFromConveyorIfNeeded(plantKey);
        } else {
            NotificationCenter.warning(result);
        }
    }

    private void executePlantingAction(int x, int y) {
        plantAt(selectedSeedKey, x, y);
    }

    /**
     * On conveyor-delivery levels, a planted packet is removed from the belt and the
     * player has to pick a new one — mirrors ChoosePlantMenu being skipped for these levels.
     */
    private void consumeFromConveyorIfNeeded(String plantedKey) {
        GameSession session = App.getGameSession();
        Level level = session != null ? session.getLevel() : null;
        if (level == null || !(level.getDeliveryStrategy() instanceof ConveyorDeliveryStrategy conveyor)) {
            return;
        }

        conveyor.getConveyorBelt().stream()
                .filter(name -> ChoosePlantService.normalizePlantKey(name).equals(plantedKey))
                .findFirst()
                .ifPresent(name -> conveyor.getConveyorBelt().remove(name));

        this.selectedSeedKey = null;
    }

    private void executeSunCollection(int x, int y) {
        LocationRequest req = new LocationRequest(String.valueOf(x), String.valueOf(y));
        String result = gameService.collectSun(req);

        if (result.contains("collected")) {
            // Silently collect to avoid spamming the notification center
        }
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