package com.ussr.pvz.controller.maincontroller.gamecontroller;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.Vase;
import com.ussr.pvz.model.dto.LocationRequest;
import com.ussr.pvz.model.dto.PlantPlantRequest;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.items.GroundItem;
import com.ussr.pvz.model.entities.items.ItemType;
import com.ussr.pvz.model.entities.items.SeedPackDrop;
import com.ussr.pvz.model.level.behavior.BeghouledBehavior;
import com.ussr.pvz.model.level.behavior.CouchIZombieBehavior;
import com.ussr.pvz.model.level.behavior.IZombieBehavior;
import com.ussr.pvz.model.level.behavior.LevelBehavior;
import com.ussr.pvz.model.level.behavior.MultiplayerIZombieBehavior;
import com.ussr.pvz.model.level.behavior.VaseBreakerBehavior;
import com.ussr.pvz.model.level.behavior.WallnutBowlingBehavior;
import com.ussr.pvz.service.game.GameService;
import com.ussr.pvz.service.minigame.BeghouledService;
import com.ussr.pvz.service.minigame.IZombieService;
import com.ussr.pvz.service.minigame.MultiplayerIZombieService;
import com.ussr.pvz.service.minigame.VaseBreakerService;
import com.ussr.pvz.view.gameplay.LawnGridLayout;

public final class GameplayController {

    private final GameService gameService =
            new GameService();

    private final BeghouledService beghouledService =
            new BeghouledService();

    private final VaseBreakerService vaseBreakerService =
            new VaseBreakerService();

    private final IZombieService iZombieService =
            new IZombieService();

    private boolean manuallyPaused;
    private boolean dialoguePaused;

    private boolean shovelModeActive;
    private boolean plantFoodModeActive;

    private String selectedSeedKey;
    private String selectedZombieKey;

    private Runnable onPlantingCompleted;
    private Runnable onPlantFoodDeactivated;

    private int beghouledSelectedRow = -1;
    private int beghouledSelectedCol = -1;

    private SeedPackDrop heldSeedPack;

    private int zombieCursorColumn = -1;
    private int zombieCursorRow = 0;

    public GameplayController() {
        manuallyPaused = App.consumeResumeToPauseMenu();
    }

    // =========================================================
    // Pause
    // =========================================================

    public void togglePauseMenu() {
        manuallyPaused = !manuallyPaused;
    }

    public boolean isPauseMenuOpen() {
        return manuallyPaused;
    }

    public void setDialoguePaused(
            boolean paused
    ) {
        dialoguePaused = paused;
    }

    public boolean isPaused() {
        return manuallyPaused
                || dialoguePaused;
    }

    // =========================================================
    // Player-role checks
    // =========================================================

    private MultiplayerIZombieBehavior
    multiplayerBehavior() {
        GameSession session =
                App.getGameSession();

        if (session == null
                || session.getLevel() == null) {
            return null;
        }

        if (session.getLevel().getBehavior()
                instanceof MultiplayerIZombieBehavior behavior) {
            return behavior;
        }

        return null;
    }

    public boolean isMultiplayerMatch() {
        return multiplayerBehavior() != null;
    }

    public boolean isMultiplayerPlantsPlayer() {
        MultiplayerIZombieBehavior behavior =
                multiplayerBehavior();

        return behavior != null
                && behavior.isPlantsPlayer();
    }

    public boolean isMultiplayerZombiesPlayer() {
        MultiplayerIZombieBehavior behavior =
                multiplayerBehavior();

        return behavior != null
                && behavior.isZombiesPlayer();
    }

    /**
     * Offline modes retain their normal plant controls.
     * During multiplayer, only the plants player may use them.
     */
    private boolean canUsePlantControls() {
        MultiplayerIZombieBehavior behavior =
                multiplayerBehavior();

        return behavior == null
                || behavior.isPlantsPlayer();
    }

    /**
     * Ordinary i,Zombie and the multiplayer zombies role may use
     * zombie placement controls.
     */
    private boolean canUseZombieControls() {
        GameSession session =
                App.getGameSession();

        if (session == null
                || session.getLevel() == null) {
            return false;
        }

        LevelBehavior behavior =
                session.getLevel().getBehavior();

        if (behavior instanceof IZombieBehavior
                || behavior instanceof CouchIZombieBehavior) {
            return true;
        }

        return behavior
                instanceof MultiplayerIZombieBehavior multiplayer
                && multiplayer.isZombiesPlayer();
    }

    /**
     * Couch play runs the plant side (mouse) and the zombie side (keyboard)
     * at the same time, so neither role should clear the other's selection.
     */
    private boolean isCouchMatch() {
        GameSession session =
                App.getGameSession();

        return session != null
                && session.getLevel() != null
                && session.getLevel().getBehavior()
                instanceof CouchIZombieBehavior;
    }

    private CouchIZombieBehavior couchBehavior() {
        GameSession session =
                App.getGameSession();

        if (session == null
                || session.getLevel() == null) {
            return null;
        }

        if (session.getLevel().getBehavior()
                instanceof CouchIZombieBehavior behavior) {
            return behavior;
        }

        return null;
    }

    // =========================================================
    // Plant modes
    // =========================================================

    public void toggleShovelMode(
            boolean active
    ) {
        if (active && !canUsePlantControls()) {
            clearPlantControls();
            return;
        }

        shovelModeActive = active;

        if (active) {
            plantFoodModeActive = false;
            selectedSeedKey = null;

            if (onPlantFoodDeactivated != null) {
                onPlantFoodDeactivated.run();
            }

            if (onPlantingCompleted != null) {
                onPlantingCompleted.run();
            }
        }
    }

    public void togglePlantFoodMode(
            boolean active
    ) {
        if (active && !canUsePlantControls()) {
            clearPlantControls();
            return;
        }

        plantFoodModeActive = active;

        if (active) {
            shovelModeActive = false;
            selectedSeedKey = null;

            if (onPlantingCompleted != null) {
                onPlantingCompleted.run();
            }
        } else if (onPlantFoodDeactivated != null) {
            onPlantFoodDeactivated.run();
        }
    }

    public void setSelectedSeed(
            String plantKey
    ) {
        if (plantKey != null
                && !canUsePlantControls()) {
            clearPlantControls();
            return;
        }

        selectedSeedKey = plantKey;

        if (plantKey != null) {
            shovelModeActive = false;

            if (plantFoodModeActive) {
                plantFoodModeActive = false;

                if (onPlantFoodDeactivated != null) {
                    onPlantFoodDeactivated.run();
                }
            }

            /*
             * Plant and zombie selections must never be active at
             * the same time, except in couch play where both sides
             * act independently and simultaneously.
             */
            if (!isCouchMatch()) {
                selectedZombieKey = null;
            }
        }
    }

    private void clearPlantControls() {
        shovelModeActive = false;
        plantFoodModeActive = false;
        selectedSeedKey = null;

        if (onPlantFoodDeactivated != null) {
            onPlantFoodDeactivated.run();
        }

        if (onPlantingCompleted != null) {
            onPlantingCompleted.run();
        }
    }

    public void setOnPlantingCompleted(
            Runnable callback
    ) {
        onPlantingCompleted = callback;
    }

    public void setOnPlantFoodDeactivated(
            Runnable callback
    ) {
        onPlantFoodDeactivated = callback;
    }

    // =========================================================
    // Main lawn click
    // =========================================================

    public void handleGridClick(
            int gridX,
            int gridY
    ) {
        if (isPaused()) {
            return;
        }

        GameSession session =
                App.getGameSession();

        if (session == null
                || session.getLevel() == null) {
            return;
        }

        LevelBehavior behavior =
                session.getLevel().getBehavior();

        if (behavior instanceof BeghouledBehavior) {
            handleBeghouledClick(
                    gridX,
                    gridY
            );
            return;
        }

        if (behavior instanceof VaseBreakerBehavior) {
            handleVaseBreakerClick(
                    gridX,
                    gridY,
                    session
            );
            return;
        }

        if (behavior instanceof CouchIZombieBehavior) {
            /*
             * In couch play the mouse always drives the plant side;
             * the zombie side is placed through the keyboard cursor.
             */
            handlePlantPlayerClick(
                    gridX,
                    gridY
            );
            return;
        }

        if (behavior instanceof IZombieBehavior) {
            handleIZombieClick(
                    gridX,
                    gridY
            );
            return;
        }

        if (behavior
                instanceof MultiplayerIZombieBehavior multiplayer) {
            if (multiplayer.isZombiesPlayer()) {
                handleIZombieClick(
                        gridX,
                        gridY
                );
            } else {
                handlePlantPlayerClick(
                        gridX,
                        gridY
                );
            }

            return;
        }

        handlePlantPlayerClick(
                gridX,
                gridY
        );
    }

    private void handlePlantPlayerClick(
            int gridX,
            int gridY
    ) {
        if (!canUsePlantControls()) {
            return;
        }

        if (shovelModeActive) {
            executeShovelAction(
                    gridX,
                    gridY
            );

        } else if (plantFoodModeActive) {
            executePlantFoodAction(
                    gridX,
                    gridY
            );

        } else if (selectedSeedKey != null) {
            executePlantingAction(
                    gridX,
                    gridY
            );

        } else {
            executeSunCollection(
                    gridX,
                    gridY
            );
        }
    }

    // =========================================================
    // Beghouled
    // =========================================================

    private void handleBeghouledClick(
            int column,
            int row
    ) {
        if (beghouledSelectedRow == -1) {
            beghouledSelectedRow = row;
            beghouledSelectedCol = column;
            return;
        }

        beghouledService.swapPlants(
                beghouledSelectedRow,
                beghouledSelectedCol,
                row,
                column
        );

        beghouledSelectedRow = -1;
        beghouledSelectedCol = -1;
    }

    public int getBeghouledSelectedRow() {
        return beghouledSelectedRow;
    }

    public int getBeghouledSelectedCol() {
        return beghouledSelectedCol;
    }

    // =========================================================
    // VaseBreaker
    // =========================================================

    private void handleVaseBreakerClick(
            int column,
            int row,
            GameSession session
    ) {
        if (heldSeedPack != null) {
            int sourceX =
                    (int) heldSeedPack
                            .getLocation()
                            .x();

            int sourceY =
                    (int) heldSeedPack
                            .getLocation()
                            .y();

            String result =
                    vaseBreakerService
                            .plantFromSeedPack(
                                    sourceX,
                                    sourceY,
                                    column,
                                    row
                            );

            if (result.contains("Successfully")) {
                heldSeedPack = null;
            }

            return;
        }

        float clickX =
                LawnGridLayout.cellX(column)
                        + LawnGridLayout.CELL_WIDTH / 2f;

        float clickY =
                LawnGridLayout.cellY(row)
                        + LawnGridLayout.CELL_HEIGHT / 2f;

        for (GroundItem item : session.getItems()) {
            if (item.getItemType()
                    != ItemType.SEED_PACK) {
                continue;
            }

            if (!item.isAlive()
                    || item.isCollected()) {
                continue;
            }

            float itemX =
                    LawnGridLayout.worldX(
                            item.getPosition().x()
                    )
                            + LawnGridLayout.CELL_WIDTH / 2f;

            float itemY =
                    LawnGridLayout.worldY(
                            item.getPosition().y()
                    );

            float deltaX = clickX - itemX;
            float deltaY = clickY - itemY;

            if (deltaX * deltaX
                    + deltaY * deltaY
                    < 75f * 75f) {
                heldSeedPack =
                        (SeedPackDrop) item;
                return;
            }
        }

        Cell cell =
                session.getLawn()
                        .getCell(row, column);

        if (cell != null
                && cell.getInteractableStructure()
                instanceof Vase vase
                && vase.isAlive()) {
            vaseBreakerService.smashVase(
                    column,
                    row
            );
        }
    }

    public SeedPackDrop getHeldSeedPack() {
        return heldSeedPack;
    }

    // =========================================================
    // Plant actions
    // =========================================================

    private void executeShovelAction(
            int x,
            int y
    ) {
        if (!canUsePlantControls()) {
            return;
        }

        LocationRequest request =
                new LocationRequest(
                        String.valueOf(x),
                        String.valueOf(y)
                );

        String result =
                gameService.pluckPlant(request);

        if (result.contains("plucked")) {
            toggleShovelMode(false);
        }
    }

    private void executePlantFoodAction(
            int x,
            int y
    ) {
        if (!canUsePlantControls()) {
            return;
        }

        LocationRequest request =
                new LocationRequest(
                        String.valueOf(x),
                        String.valueOf(y)
                );

        gameService.feedPlant(request);
        togglePlantFoodMode(false);
    }

    public void plantAt(
            String plantKey,
            int gridX,
            int gridY
    ) {
        if (isPaused()
                || plantKey == null
                || !canUsePlantControls()) {
            return;
        }

        String result =
                executeSelectedPlant(
                        plantKey,
                        gridX,
                        gridY
                );

        if (result.contains("placed")
                || result.contains("Rolled")) {
            setSelectedSeed(null);

            if (onPlantingCompleted != null) {
                onPlantingCompleted.run();
            }
        }
    }

    private String executeSelectedPlant(
            String plantKey,
            int gridX,
            int gridY
    ) {
        GameSession session =
                App.getGameSession();

        if (session != null
                && session.getLevel() != null
                && session.getLevel().getBehavior()
                instanceof WallnutBowlingBehavior bowling) {
            return bowling.rollNut(
                    plantKey,
                    gridX,
                    gridY
            );
        }

        return gameService.plantPlant(
                new PlantPlantRequest(
                        plantKey,
                        String.valueOf(gridX),
                        String.valueOf(gridY)
                )
        );
    }

    private void executePlantingAction(
            int x,
            int y
    ) {
        plantAt(
                selectedSeedKey,
                x,
                y
        );
    }

    private void executeSunCollection(
            int x,
            int y
    ) {
        gameService.collectSun(
                new LocationRequest(
                        String.valueOf(x),
                        String.valueOf(y)
                )
        );
    }

    // =========================================================
    // Beghouled upgrades
    // =========================================================

    public void upgradeBeghouledPlant(
            String plantType
    ) {
        if (isPaused()) {
            return;
        }

        beghouledService.upgradePlant(
                plantType
        );
    }

    // =========================================================
    // I, Zombie
    // =========================================================

    public void setSelectedZombieKey(
            String key
    ) {
        if (key != null
                && !canUseZombieControls()) {
            selectedZombieKey = null;
            return;
        }

        selectedZombieKey = key;

        if (key != null && !isCouchMatch()) {
            clearPlantControls();
        }
    }

    public String getSelectedZombieKey() {
        return selectedZombieKey;
    }

    private void handleIZombieClick(
            int column,
            int row
    ) {
        if (!canUseZombieControls()
                || selectedZombieKey == null) {
            return;
        }

        iZombieService.placeZombie(
                selectedZombieKey,
                column,
                row
        );
    }

    // =========================================================
    // Keyboard-driven zombie cursor (couch play)
    // =========================================================

    public void moveZombieCursor(
            int deltaColumn,
            int deltaRow
    ) {
        CouchIZombieBehavior behavior = couchBehavior();
        GameSession session = App.getGameSession();

        if (behavior == null
                || session == null
                || session.getLawn() == null
                || isPaused()) {
            return;
        }

        int rows = session.getLawn().getRows();
        int columns = session.getLawn().getCols();

        if (zombieCursorColumn < 0) {
            zombieCursorColumn = behavior.getRedLineColumn();
        }

        int minColumn = behavior.getRedLineColumn();
        zombieCursorColumn = clamp(
                zombieCursorColumn + deltaColumn,
                minColumn,
                columns - 1
        );
        zombieCursorRow = clamp(
                zombieCursorRow + deltaRow,
                0,
                rows - 1
        );
    }

    public void confirmZombiePlacement() {
        if (couchBehavior() == null
                || zombieCursorColumn < 0) {
            return;
        }

        handleIZombieClick(
                zombieCursorColumn,
                zombieCursorRow
        );
    }

    public int getZombieCursorColumn() {
        return zombieCursorColumn;
    }

    public int getZombieCursorRow() {
        return zombieCursorRow;
    }

    private static int clamp(
            int value,
            int min,
            int max
    ) {
        return Math.max(min, Math.min(max, value));
    }

    // =========================================================
    // State accessors
    // =========================================================

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