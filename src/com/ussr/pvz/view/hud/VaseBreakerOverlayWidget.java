package com.ussr.pvz.view.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.Vase;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.items.SeedPackDrop;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.PlantFactory;
import com.ussr.pvz.view.components.PlantCard;
import com.ussr.pvz.view.gameplay.LawnGridLayout;
import pvz.libpvz.textures.TextureBank;

/**
 * Draw-only overlay for VaseBreaker.
 * All input is handled by GameplayController via LawnWidget.
 * Reads heldSeedPack from controller to draw cursor + cell highlights.
 */
public class VaseBreakerOverlayWidget extends Actor {

    private static final String REGION_HIGHLIGHT = "IMAGE_UI_HUD_TILE_HIGHLIGHT";

    private static final Color COLOR_VASE_HOVER = new Color(1f,   0.6f, 0f,   0.4f);
    private static final Color COLOR_PLANT_OK   = new Color(0.2f, 1f,   0.2f, 0.35f);
    private static final Color COLOR_PLANT_HOV  = new Color(0.2f, 1f,   0.2f, 0.65f);

    private final TextureBank        textures;
    private final GameplayController controller;

    public VaseBreakerOverlayWidget(TextureBank textures, GameplayController controller) {
        this.textures   = textures;
        this.controller = controller;
        setTouchable(Touchable.disabled); // LawnWidget handles all input
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        GameSession session = App.getGameSession();
        if (session == null || session.getLawn() == null) return;

        TextureRegion highlight = textures.region(REGION_HIGHLIGHT);

        Vector2 mouse = getStage().screenToStageCoordinates(
                new Vector2(Gdx.input.getX(), Gdx.input.getY()));

        Color prev = new Color(batch.getColor());
        SeedPackDrop held = controller.getHeldSeedPack();

        if (held != null) {
            drawHeldPackMode(batch, parentAlpha, mouse, highlight, session);
        } else {
            drawDefaultMode(batch, parentAlpha, mouse, highlight, session);
        }

        batch.setColor(prev);
    }

    private void drawHeldPackMode(Batch batch, float parentAlpha,
                                  Vector2 mouse, TextureRegion highlight,
                                  GameSession session) {
        if (highlight != null) {
            for (int r = 0; r < LawnGridLayout.ROWS; r++) {
                for (int c = 0; c < LawnGridLayout.COLUMNS; c++) {
                    Cell cell = session.getLawn().getCell(r, c);
                    if (cell == null || cell.getPlant() != null) continue;

                    boolean hovered = LawnGridLayout.contains(mouse.x, mouse.y)
                            && LawnGridLayout.columnAt(mouse.x) == c
                            && LawnGridLayout.rowAt(mouse.y)    == r;

                    Color col = hovered ? COLOR_PLANT_HOV : COLOR_PLANT_OK;
                    batch.setColor(col.r, col.g, col.b, col.a * parentAlpha);
                    batch.draw(highlight,
                            LawnGridLayout.cellX(c), LawnGridLayout.cellY(r),
                            LawnGridLayout.CELL_WIDTH, LawnGridLayout.CELL_HEIGHT);
                }
            }
        }
        drawHeldPackIcon(batch, parentAlpha, mouse);
    }

    private void drawHeldPackIcon(Batch batch, float parentAlpha, Vector2 mouse) {
        SeedPackDrop held = controller.getHeldSeedPack();
        if (held == null) return;
        TextureRegion icon = null;
        try {
            Plant plant = PlantFactory.createPlant(held.getPlantId(), 1);
            if (plant != null && plant.getName() != null) {
                String key = PlantCard.resolvePacketKey(plant.getName());
                icon = textures.region("IMAGE_UI_PACKETS_" + key);
            }
        } catch (Exception ignored) {}
        if (icon == null) icon = textures.region("IMAGE_UI_PACKETS_PEASHOOTER");
        if (icon == null) return;

        float w = LawnGridLayout.CELL_WIDTH  * 0.8f;
        float h = LawnGridLayout.CELL_HEIGHT * 0.8f;
        batch.setColor(1f, 1f, 1f, 0.80f * parentAlpha);
        batch.draw(icon, mouse.x - w / 2f, mouse.y - h / 2f - 10f, w, h);
    }

    private void drawDefaultMode(Batch batch, float parentAlpha,
                                 Vector2 mouse, TextureRegion highlight,
                                 GameSession session) {
        if (highlight == null || !LawnGridLayout.contains(mouse.x, mouse.y)) return;
        int  hovCol = LawnGridLayout.columnAt(mouse.x);
        int  hovRow = LawnGridLayout.rowAt(mouse.y);
        Cell cell   = session.getLawn().getCell(hovRow, hovCol);
        if (cell == null) return;

        if (cell.getInteractableStructure() instanceof Vase vase && vase.isAlive()) {
            float pulse = 0.5f + 0.5f * (float) Math.sin(System.currentTimeMillis() / 300.0);
            batch.setColor(COLOR_VASE_HOVER.r, COLOR_VASE_HOVER.g, COLOR_VASE_HOVER.b,
                    COLOR_VASE_HOVER.a * pulse * parentAlpha);
            batch.draw(highlight,
                    LawnGridLayout.cellX(hovCol), LawnGridLayout.cellY(hovRow),
                    LawnGridLayout.CELL_WIDTH, LawnGridLayout.CELL_HEIGHT);
        }
    }
}