package com.ussr.pvz.view.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import com.ussr.pvz.view.components.PlantCard;
import com.ussr.pvz.view.gameplay.LawnGridLayout;
import pvz.libpvz.textures.TextureBank;

public class HoverCursorWidget extends Actor {

    private final SeedBankHud seedBankHud;
    private final TextureBank textures;
    private final GameplayController controller;

    public HoverCursorWidget(
            SeedBankHud seedBankHud,
            TextureBank textures,
            GameplayController controller
    ) {
        this.seedBankHud = seedBankHud;
        this.textures = textures;
        this.controller = controller;
        setTouchable(Touchable.disabled);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (controller.isPaused()) {
            return;
        }

        if (controller.isShovelModeActive()) {
            drawShovelPreview(batch, parentAlpha, getLocalMousePosition());
            return;
        }

        String selectedKey =
                seedBankHud.getSelectedPlantKey();

        if (selectedKey == null) {
            return;
        }

        Vector2 mouse = getLocalMousePosition();

        if (!LawnGridLayout.contains(mouse.x, mouse.y)) {
            return;
        }

        int column = LawnGridLayout.columnAt(mouse.x);
        int row = LawnGridLayout.rowAt(mouse.y);

        float cellX = LawnGridLayout.cellX(column);
        float cellY = LawnGridLayout.cellY(row);

        // The tile highlight remains snapped.
        drawHighlight(
                batch,
                parentAlpha,
                cellX,
                cellY
        );

        // The plant image follows the mouse smoothly.
        drawPlantPreview(
                batch,
                parentAlpha,
                selectedKey,
                mouse.x,
                mouse.y
        );
    }

    private Vector2 getLocalMousePosition() {
        Vector2 mouse = new Vector2(
                Gdx.input.getX(),
                Gdx.input.getY()
        );

        screenToLocalCoordinates(mouse);
        return mouse;
    }

    private void drawHighlight(
            Batch batch,
            float parentAlpha,
            float x,
            float y
    ) {
        TextureRegion highlight =
                textures.region("IMAGE_UI_HUD_TILE_HIGHLIGHT");

        if (highlight == null) {
            return;
        }

        Color previousColor = new Color(batch.getColor());

        batch.setColor(1f, 1f, 1f, 0.4f * parentAlpha);
        batch.draw(
                highlight,
                x,
                y,
                LawnGridLayout.CELL_WIDTH,
                LawnGridLayout.CELL_HEIGHT
        );
        batch.setColor(previousColor);
    }

    private void drawPlantPreview(
            Batch batch,
            float parentAlpha,
            String selectedKey,
            float mouseX,
            float mouseY
    ) {
        // Packet atlas names are not always the same as gameplay IDs
        // (for example WALL-NUT -> WALLNUT).
        String packetKey = PlantCard.resolvePacketKey(selectedKey);

        TextureRegion plantRegion =
                textures.region("IMAGE_UI_PACKETS_" + packetKey);

        if (plantRegion == null) {
            return;
        }

        float previewWidth =
                LawnGridLayout.CELL_WIDTH * 0.8f;

        float previewHeight =
                LawnGridLayout.CELL_HEIGHT * 0.8f;

        float drawX = mouseX - previewWidth / 2f;
        float drawY = mouseY - previewHeight / 2f - 10f;

        Color previousColor =
                new Color(batch.getColor());

        batch.setColor(
                1f,
                1f,
                1f,
                0.65f * parentAlpha
        );

        batch.draw(
                plantRegion,
                drawX,
                drawY,
                previewWidth,
                previewHeight
        );

        batch.setColor(previousColor);
    }

    private void drawShovelPreview(
            Batch batch,
            float parentAlpha,
            Vector2 mouse
    ) {
        // Prefer the shovel itself; the HUD button region contains its square
        // button background and should only be used as a fallback.
        TextureRegion shovel = textures.region("IMAGE_SHOVEL");

        if (shovel == null) {
            shovel = textures.region("IMAGE_UI_HUD_INGAME_SHOVEL_BUTTON");
        }
        if (shovel == null) {
            return;
        }

        float height = 70f;
        float width = height * shovel.getRegionWidth()
                / Math.max(1f, shovel.getRegionHeight());
        Color previousColor = new Color(batch.getColor());

        batch.setColor(1f, 1f, 1f, 0.9f * parentAlpha);
        batch.draw(
                shovel,
                mouse.x - width / 2f,
                mouse.y - height / 2f,
                width,
                height
        );
        batch.setColor(previousColor);
    }
}
