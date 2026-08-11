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
import com.ussr.pvz.model.level.behavior.LevelBehavior;
import com.ussr.pvz.model.level.behavior.VaseBreakerBehavior;
import com.ussr.pvz.model.level.behavior.WallnutBowlingBehavior;
import com.ussr.pvz.notification.NotificationCenter;
import com.ussr.pvz.service.game.GameService;
import com.ussr.pvz.service.minigame.BeghouledService;
import com.ussr.pvz.service.minigame.VaseBreakerService;
import com.ussr.pvz.view.gameplay.LawnGridLayout;

public class GameplayController {

    private final GameService      gameService      = new GameService();
    private final BeghouledService beghouledService = new BeghouledService();
    private final VaseBreakerService vaseBreakerService = new VaseBreakerService();

    private boolean manuallyPaused;
    private boolean dialoguePaused;
    private boolean shovelModeActive    = false;
    private boolean plantFoodModeActive = false;
    private String  selectedSeedKey     = null;
    private Runnable onPlantingCompleted;
    private Runnable onPlantFoodDeactivated;

    // ── Beghouled state ───────────────────────────────────────────────────────
    private int beghouledSelectedRow = -1;
    private int beghouledSelectedCol = -1;

    // ── VaseBreaker state ─────────────────────────────────────────────────────
    private SeedPackDrop heldSeedPack = null;

    public GameplayController() {}

    // ── Pause / modes ─────────────────────────────────────────────────────────

    public void togglePauseMenu() {
        manuallyPaused = !manuallyPaused;

        NotificationCenter.info(
                manuallyPaused ? "Game Paused" : "Game Resumed"
        );
    }

    public boolean isPauseMenuOpen() {
        return manuallyPaused;
    }

    public void setDialoguePaused(boolean paused) {
        dialoguePaused = paused;
    }

    public boolean isPaused() {
        return manuallyPaused || dialoguePaused;
    }

    public void toggleShovelMode(boolean active) {
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

    public void setOnPlantingCompleted(Runnable callback)    { this.onPlantingCompleted    = callback; }
    public void setOnPlantFoodDeactivated(Runnable callback) { this.onPlantFoodDeactivated = callback; }

    // ── Main click entry point (called by LawnWidget) ─────────────────────────

    public void handleGridClick(int gridX, int gridY) {
        if (isPaused()) return;

        GameSession session = App.getGameSession();
        if (session != null && session.getLevel() != null) {
            LevelBehavior behavior = (LevelBehavior) session.getLevel().getBehavior();

            if (behavior instanceof BeghouledBehavior) {
                handleBeghouledClick(gridX, gridY);
                return;
            }
            if (behavior instanceof VaseBreakerBehavior) {
                handleVaseBreakerClick(gridX, gridY, session);
                return;
            }
        }

        // Standard gameplay
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

    // ── Beghouled ─────────────────────────────────────────────────────────────

    private void handleBeghouledClick(int col, int row) {
        if (beghouledSelectedRow == -1) {
            // First click — select
            beghouledSelectedRow = row;
            beghouledSelectedCol = col;
            NotificationCenter.info("Selected (" + row + ", " + col + ") — click adjacent cell to swap");
        } else {
            // Second click — attempt swap
            String result = beghouledService.swapPlants(
                    beghouledSelectedRow, beghouledSelectedCol, row, col);

            if (result.contains("successfully") || result.contains("Swapped")) {
                NotificationCenter.success(result);
            } else {
                NotificationCenter.warning(result);
            }
            beghouledSelectedRow = -1;
            beghouledSelectedCol = -1;
        }
    }

    /** Read by BeghouledOverlayWidget to draw highlights. */
    public int getBeghouledSelectedRow() { return beghouledSelectedRow; }
    public int getBeghouledSelectedCol() { return beghouledSelectedCol; }

    // ── VaseBreaker ───────────────────────────────────────────────────────────

    /**
     * LawnWidget gives us grid col/row, but we also need raw stage coords to
     * do the radius check against seed pack positions, so we accept them both.
     * LawnWidget calls handleGridClick(col, row) — we reconstruct stage coords
     * from the grid position for the radius check.
     */
    private void handleVaseBreakerClick(int col, int row, GameSession session) {
        // Mode A: holding a seed pack — plant it
        if (heldSeedPack != null) {
            int sX = (int) heldSeedPack.getLocation().x();
            int sY = (int) heldSeedPack.getLocation().y();
            String result = vaseBreakerService.plantFromSeedPack(sX, sY, col, row);
            if (result.contains("Successfully")) {
                NotificationCenter.success(result);
                heldSeedPack = null;
            } else {
                NotificationCenter.warning(result);
            }
            return;
        }

        // Mode B: nothing held
        // Reconstruct the stage-space click centre for this cell
        float clickX = LawnGridLayout.cellX(col) + LawnGridLayout.CELL_WIDTH  / 2f;
        float clickY = LawnGridLayout.cellY(row)  + LawnGridLayout.CELL_HEIGHT / 2f;

        // Priority 1 — seed pack within radius of the clicked cell centre
        for (GroundItem item : session.getItems()) {
            if (item.getItemType() != ItemType.SEED_PACK) continue;
            if (!item.isAlive() || item.isCollected()) continue;
            float iX = LawnGridLayout.worldX(item.getPosition().x()) + LawnGridLayout.CELL_WIDTH / 2f;
            float iY = LawnGridLayout.worldY(item.getPosition().y());
            float dx = clickX - iX, dy = clickY - iY;
            if (dx * dx + dy * dy < 75f * 75f) {   // 75 stage-unit radius
                heldSeedPack = (SeedPackDrop) item;
                NotificationCenter.info("Seed pack picked up — click a tile to plant");
                return;
            }
        }

        // Priority 2 — smash vase at this cell
        Cell cell = session.getLawn().getCell(row, col);
        if (cell != null
                && cell.getInteractableStructure() instanceof Vase vase
                && vase.isAlive()) {
            String result = vaseBreakerService.smashVase(col, row);
            if (result.contains("smashed")) NotificationCenter.success(result);
            else                             NotificationCenter.warning(result);
        }
    }

    /** Read by VaseBreakerOverlayWidget to draw the held-pack cursor. */
    public SeedPackDrop getHeldSeedPack() { return heldSeedPack; }

    // ── Standard actions ──────────────────────────────────────────────────────

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
        togglePlantFoodMode(false);
        if (result.contains("fed")) NotificationCenter.success(result);
        else                        NotificationCenter.warning(result);
    }

    public void plantAt(String plantKey, int gridX, int gridY) {
        if (isPaused() || plantKey == null) return;
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
        return gameService.plantPlant(new PlantPlantRequest(
                plantKey, String.valueOf(gridX), String.valueOf(gridY)));
    }

    private void executePlantingAction(int x, int y) { plantAt(selectedSeedKey, x, y); }

    private void executeSunCollection(int x, int y) {
        gameService.collectSun(new LocationRequest(String.valueOf(x), String.valueOf(y)));
    }

    // ── Beghouled upgrades ────────────────────────────────────────────────────

    /**
     * Called by {@link com.ussr.pvz.view.hud.BeghouledUpgradePanel} when the
     * player clicks an upgrade button.
     *
     * @param plantType the current plant type key (lower-case), e.g. "peashooter"
     */
    public void upgradeBeghouledPlant(String plantType) {
        if (isPaused()) return;
        String result = beghouledService.upgradePlant(plantType);
        if (result.startsWith("Successfully")) {
            NotificationCenter.success(result);
        } else {
            NotificationCenter.warning(result);
        }
    }

    public boolean isShovelModeActive()    { return shovelModeActive; }
    public boolean isPlantFoodModeActive() { return plantFoodModeActive; }
    public String  getSelectedSeedKey()    { return selectedSeedKey; }
}