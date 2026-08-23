package com.ussr.pvz.view.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.view.components.PlantCard;
import com.ussr.pvz.view.gameplay.LawnGridLayout;
import pvz.libpvz.textures.TextureBank;

public class HoverCursorWidget extends Actor {

    private static final String REGION_PLANTFOOD =
            "IMAGE_BACKGROUNDS_TILE_PLANTFOOD_TILE_PLANTFOOD_45X46";

    private final TextureBank textures;
    private final GameplayController controller;

    public HoverCursorWidget(
            TextureBank textures,
            GameplayController controller
    ) {
        this.textures = textures;
        this.controller = controller;
        setTouchable(Touchable.disabled);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (controller.isPaused()) return;

        Vector2 mouse = getLocalMousePosition();

        // ── Plant food mode ───────────────────────────────────────────────
        if (controller.isPlantFoodModeActive()) {
            drawPlantFoodPreview(batch, parentAlpha, mouse);
            return;
        }

        // ── Shovel mode ───────────────────────────────────────────────────
        if (controller.isShovelModeActive()) {
            drawShovelPreview(batch, parentAlpha, mouse);
            return;
        }

        // ── Plant selection mode ──────────────────────────────────────────
        String selectedZombieKey = controller.getSelectedZombieKey();
        if (selectedZombieKey != null) {
            if (!LawnGridLayout.contains(mouse.x, mouse.y)) return;

            int column = LawnGridLayout.columnAt(mouse.x);
            int row = LawnGridLayout.rowAt(mouse.y);
            drawHighlight(batch, parentAlpha,
                    LawnGridLayout.cellX(column),
                    LawnGridLayout.cellY(row));
            drawZombiePreview(batch, parentAlpha,
                    selectedZombieKey, mouse.x, mouse.y);
            return;
        }

        String selectedKey =
                controller.getSelectedSeedKey();
        if (selectedKey == null) return;

        if (!LawnGridLayout.contains(mouse.x, mouse.y)) return;

        int column = LawnGridLayout.columnAt(mouse.x);
        int row    = LawnGridLayout.rowAt(mouse.y);

        float cellX = LawnGridLayout.cellX(column);
        float cellY = LawnGridLayout.cellY(row);

        drawHighlight(batch, parentAlpha, cellX, cellY);
        drawPlantPreview(batch, parentAlpha, selectedKey, mouse.x, mouse.y);
    }

    // ── Plant food hover preview ──────────────────────────────────────────

    private void drawPlantFoodPreview(Batch batch, float parentAlpha, Vector2 mouse) {
        // Snap cell highlight to the hovered grid cell
        if (LawnGridLayout.contains(mouse.x, mouse.y)) {
            int column = LawnGridLayout.columnAt(mouse.x);
            int row    = LawnGridLayout.rowAt(mouse.y);
            drawHighlight(batch, parentAlpha,
                    LawnGridLayout.cellX(column),
                    LawnGridLayout.cellY(row));
        }

        // Resolve the plant food icon — same texture the widget uses
        TextureRegion icon = textures.region(REGION_PLANTFOOD);
        if (icon == null) {
            icon = textures.region("IMAGE_UI_HUD_INGAME_PLANTFOOD_BUTTON");
        }
        if (icon == null) return;

        float size = LawnGridLayout.CELL_WIDTH * 0.7f;
        float drawX = mouse.x - size / 2f;
        float drawY = mouse.y - size / 2f + 8f;   // slight upward offset like plant preview

        Color prev = new Color(batch.getColor());
        batch.setColor(1f, 1f, 1f, 0.80f * parentAlpha);
        batch.draw(icon, drawX, drawY, size, size);
        batch.setColor(prev);
    }

    // ── Shared helpers ────────────────────────────────────────────────────

    private Vector2 getLocalMousePosition() {
        Vector2 mouse = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        getStage().getViewport().unproject(mouse);
        stageToLocalCoordinates(mouse);
        return mouse;
    }

    private void drawHighlight(Batch batch, float parentAlpha, float x, float y) {
        TextureRegion highlight = textures.region("IMAGE_UI_HUD_TILE_HIGHLIGHT");
        if (highlight == null) return;

        Color prev = new Color(batch.getColor());
        batch.setColor(1f, 1f, 1f, 0.4f * parentAlpha);
        batch.draw(highlight, x, y, LawnGridLayout.CELL_WIDTH, LawnGridLayout.CELL_HEIGHT);
        batch.setColor(prev);
    }

    private void drawPlantPreview(
            Batch batch, float parentAlpha,
            String selectedKey, float mouseX, float mouseY
    ) {
        String packetKey = PlantCard.resolvePacketKey(selectedKey);
        TextureRegion plantRegion = textures.region("IMAGE_UI_PACKETS_" + packetKey);
        if (plantRegion == null) return;

        float previewWidth  = LawnGridLayout.CELL_WIDTH  * 0.8f;
        float previewHeight = LawnGridLayout.CELL_HEIGHT * 0.8f;
        float drawX = mouseX - previewWidth  / 2f;
        float drawY = mouseY - previewHeight / 2f - 10f;

        Color prev = new Color(batch.getColor());
        batch.setColor(1f, 1f, 1f, 0.65f * parentAlpha);
        batch.draw(plantRegion, drawX, drawY, previewWidth, previewHeight);
        batch.setColor(prev);
    }

    private void drawShovelPreview(Batch batch, float parentAlpha, Vector2 mouse) {
        TextureRegion shovel = textures.region("IMAGE_SHOVEL");
        if (shovel == null) shovel = textures.region("IMAGE_UI_HUD_INGAME_SHOVEL_BUTTON");
        if (shovel == null) return;

        float height = 70f;
        float width  = height * shovel.getRegionWidth() / Math.max(1f, shovel.getRegionHeight());

        Color prev = new Color(batch.getColor());
        batch.setColor(1f, 1f, 1f, 0.9f * parentAlpha);
        batch.draw(shovel, mouse.x - width / 2f, mouse.y - height / 2f, width, height);
        batch.setColor(prev);
    }

    private void drawZombiePreview(
            Batch batch, float parentAlpha,
            String selectedKey, float mouseX, float mouseY
    ) {
        TextureRegion zombieRegion = textures.region(
                ZombieFactory.getZombieTextureRegion(selectedKey)
        );
        if (zombieRegion == null) return;

        float maxWidth = LawnGridLayout.CELL_WIDTH * 0.8f;
        float maxHeight = LawnGridLayout.CELL_HEIGHT * 0.85f;
        float scale = Math.min(
                maxWidth / Math.max(1f, zombieRegion.getRegionWidth()),
                maxHeight / Math.max(1f, zombieRegion.getRegionHeight())
        );
        float previewWidth = zombieRegion.getRegionWidth() * scale;
        float previewHeight = zombieRegion.getRegionHeight() * scale;

        Color prev = new Color(batch.getColor());
        batch.setColor(1f, 1f, 1f, 0.7f * parentAlpha);
        batch.draw(zombieRegion,
                mouseX - previewWidth / 2f,
                mouseY - previewHeight / 2f - 10f,
                previewWidth,
                previewHeight);
        batch.setColor(prev);
    }
}
