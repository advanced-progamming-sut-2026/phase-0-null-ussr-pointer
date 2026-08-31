package com.ussr.pvz.view.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.PlantFactory;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.view.animation.PlantPamActor;
import com.ussr.pvz.view.components.PlantCard;
import com.ussr.pvz.view.gameplay.LawnGridLayout;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

public class HoverCursorWidget extends Actor {

    private static final String REGION_PLANTFOOD =
            "IMAGE_BACKGROUNDS_TILE_PLANTFOOD_TILE_PLANTFOOD_45X46";

    private final TextureBank textures;
    private final GameplayController controller;
    private final PamPlayer pamPlayer;

    private PlantPamActor currentPlantActor;
    private String currentPlantKey;
    private TextureRegion whitePixelRegion;

    public HoverCursorWidget(
            TextureBank textures,
            GameplayController controller,
            PamPlayer pamPlayer
    ) {
        this.textures = textures;
        this.controller = controller;
        this.pamPlayer = pamPlayer;
        setTouchable(Touchable.disabled);
    }

    public HoverCursorWidget(
            TextureBank textures,
            GameplayController controller
    ) {
        this(textures, controller, null);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (controller.isPaused()) return;

        // Synchronize plant PAM idle animation actor when selected plant changes
        String selectedKey = controller.getSelectedSeedKey();
        if (selectedKey != null) {
            if (!selectedKey.equals(currentPlantKey)) {
                currentPlantKey = selectedKey;
                currentPlantActor = null;

                if (pamPlayer != null) {
                    try {
                        // Resolve exact pamPath from Plant instance definition
                        Plant plant = PlantFactory.createPlantByName(selectedKey, 1);
                        String pamPath = (plant != null && plant.getPamPath() != null && !plant.getPamPath().isEmpty())
                                ? plant.getPamPath()
                                : null;

                        // Fallback PAM path construction
                        if (pamPath == null) {
                            String sanitized = selectedKey.toUpperCase().replace(" ", "_").replace("-", "");
                            pamPath = "768/INITIAL/PLANTS/" + sanitized + "/" + sanitized + ".PAM";
                        }

                        currentPlantActor = new PlantPamActor(pamPlayer, pamPath, "idle");
                    } catch (Exception e1) {
                        try {
                            String sanitized = selectedKey.toUpperCase().replace(" ", "_").replace("-", "");
                            String altPamPath = "768/INITIAL/PLANTS/" + sanitized + "/" + sanitized + ".PAM";
                            currentPlantActor = new PlantPamActor(pamPlayer, altPamPath, "idle");
                        } catch (Exception e2) {
                            currentPlantActor = null;
                            Gdx.app.error("HoverCursorWidget",
                                    "[PAM ASSET MISSING] Could not load idle PAM animation for plant key: '" + selectedKey + "'");
                        }
                    }
                }
            }

            if (currentPlantActor != null) {
                currentPlantActor.act(delta);
            }
        } else {
            currentPlantKey = null;
            currentPlantActor = null;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (controller.isPaused()) return;

        Vector2 mouse = getLocalMousePosition();
        boolean inBounds = LawnGridLayout.contains(mouse.x, mouse.y);

        int column = -1;
        int row = -1;
        if (inBounds) {
            column = LawnGridLayout.columnAt(mouse.x);
            row = LawnGridLayout.rowAt(mouse.y);
        }

        // ── 1. Plant food mode ───────────────────────────────────────────
        if (controller.isPlantFoodModeActive()) {
            if (inBounds) {
                drawHighlight(batch, parentAlpha,
                        LawnGridLayout.cellX(column),
                        LawnGridLayout.cellY(row));
            }
            drawPlantFoodPreview(batch, parentAlpha, mouse, inBounds);
            return;
        }

        // ── 2. Shovel mode ───────────────────────────────────────────────
        if (controller.isShovelModeActive()) {
            if (inBounds) {
                drawHighlight(batch, parentAlpha,
                        LawnGridLayout.cellX(column),
                        LawnGridLayout.cellY(row));
            }
            drawShovelPreview(batch, parentAlpha, mouse, inBounds);
            return;
        }

        // ── 3. Zombie selection mode ─────────────────────────────────────
        String selectedZombieKey = controller.getSelectedZombieKey();
        if (selectedZombieKey != null) {
            if (inBounds) {
                drawHighlight(batch, parentAlpha,
                        LawnGridLayout.cellX(column),
                        LawnGridLayout.cellY(row));
            }
            drawZombiePreview(batch, parentAlpha,
                    selectedZombieKey, mouse.x, mouse.y, inBounds);
            return;
        }

        // ── 4. Plant selection mode ──────────────────────────────────────
        String selectedKey = controller.getSelectedSeedKey();
        if (selectedKey != null) {
            if (inBounds) {
                drawHighlight(batch, parentAlpha,
                        LawnGridLayout.cellX(column),
                        LawnGridLayout.cellY(row));
            }
            drawPlantPreview(batch, parentAlpha, selectedKey, mouse.x, mouse.y, inBounds);
        }
    }

    // ── Previews & Highlights ─────────────────────────────────────────────

    private void drawPlantFoodPreview(Batch batch, float parentAlpha, Vector2 mouse, boolean inBounds) {
        TextureRegion icon = textures.region(REGION_PLANTFOOD);
        if (icon == null) {
            icon = textures.region("IMAGE_UI_HUD_INGAME_PLANTFOOD_BUTTON");
        }
        if (icon == null) return;

        float size = LawnGridLayout.CELL_WIDTH * 0.7f;
        float drawX = mouse.x - size / 2f;
        float drawY = mouse.y - size / 2f + 8f;

        Color prev = new Color(batch.getColor());
        batch.setColor(inBounds ? 1f : 1f, inBounds ? 1f : 0.25f, inBounds ? 1f : 0.25f, 0.85f * parentAlpha);
        batch.draw(icon, drawX, drawY, size, size);
        batch.setColor(prev);
    }

    private void drawShovelPreview(Batch batch, float parentAlpha, Vector2 mouse, boolean inBounds) {
        TextureRegion shovel = textures.region("IMAGE_SHOVEL");
        if (shovel == null) shovel = textures.region("IMAGE_UI_HUD_INGAME_SHOVEL_BUTTON");
        if (shovel == null) return;

        float height = 70f;
        float width  = height * shovel.getRegionWidth() / Math.max(1f, shovel.getRegionHeight());

        Color prev = new Color(batch.getColor());
        batch.setColor(inBounds ? 1f : 1f, inBounds ? 1f : 0.25f, inBounds ? 1f : 0.25f, 0.9f * parentAlpha);
        batch.draw(shovel, mouse.x - width / 2f, mouse.y - height / 2f, width, height);
        batch.setColor(prev);
    }

    private void drawZombiePreview(
            Batch batch, float parentAlpha,
            String selectedKey, float mouseX, float mouseY, boolean inBounds
    ) {
        TextureRegion zombieRegion = textures.region(
                ZombieFactory.getZombieTextureRegion(selectedKey)
        );
        if (zombieRegion == null) return;

        float maxWidth  = LawnGridLayout.CELL_WIDTH  * 0.8f;
        float maxHeight = LawnGridLayout.CELL_HEIGHT * 0.85f;
        float scale = Math.min(
                maxWidth  / Math.max(1f, zombieRegion.getRegionWidth()),
                maxHeight / Math.max(1f, zombieRegion.getRegionHeight())
        );
        float previewWidth  = zombieRegion.getRegionWidth()  * scale;
        float previewHeight = zombieRegion.getRegionHeight() * scale;

        Color prev = new Color(batch.getColor());
        batch.setColor(inBounds ? 1f : 1f, inBounds ? 1f : 0.25f, inBounds ? 1f : 0.25f, 0.8f * parentAlpha);
        batch.draw(zombieRegion,
                mouseX - previewWidth / 2f,
                mouseY - previewHeight / 2f - 10f,
                previewWidth,
                previewHeight);
        batch.setColor(prev);
    }

    private void drawPlantPreview(
            Batch batch, float parentAlpha,
            String selectedKey, float mouseX, float mouseY, boolean inBounds
    ) {
        Color prev = new Color(batch.getColor());
        Color tint = inBounds
                ? new Color(1f, 1f, 1f, 0.85f * parentAlpha)
                : new Color(1f, 0.25f, 0.25f, 0.85f * parentAlpha);

        if (currentPlantActor != null) {
            float width  = LawnGridLayout.CELL_WIDTH  * 0.8f;
            float height = LawnGridLayout.CELL_HEIGHT * 0.8f;
            currentPlantActor.setSize(width, height);
            currentPlantActor.setPosition(mouseX - width / 2f, mouseY - height / 2f);

            batch.setColor(tint);
            currentPlantActor.draw(batch, parentAlpha);
            batch.setColor(prev);
        }
    }

    private Vector2 getLocalMousePosition() {
        Vector2 mouse = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        getStage().getViewport().unproject(mouse);
        stageToLocalCoordinates(mouse);
        return mouse;
    }

    /**
     * Draws an obvious red border square with semi-transparent fill around the targeted cell.
     */
    private void drawHighlight(Batch batch, float parentAlpha, float x, float y) {
        float width = LawnGridLayout.CELL_WIDTH;
        float height = LawnGridLayout.CELL_HEIGHT;
        float borderWidth = 4f; // Obvious border thickness

        Color prev = new Color(batch.getColor());
        TextureRegion pixel = getWhitePixelRegion();

        if (pixel != null) {
            // 1. Red translucent cell fill
            batch.setColor(1f, 0f, 0f, 0.22f * parentAlpha);
            batch.draw(pixel, x, y, width, height);

            // 2. Thick solid red border outline
            batch.setColor(1f, 0f, 0f, 0.90f * parentAlpha);
            // Bottom edge
            batch.draw(pixel, x, y, width, borderWidth);
            // Top edge
            batch.draw(pixel, x, y + height - borderWidth, width, borderWidth);
            // Left edge
            batch.draw(pixel, x, y, borderWidth, height);
            // Right edge
            batch.draw(pixel, x + width - borderWidth, y, borderWidth, height);
        }

        batch.setColor(prev);
    }

    private TextureRegion getWhitePixelRegion() {
        if (whitePixelRegion == null) {
            try {
                Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
                pixmap.setColor(Color.WHITE);
                pixmap.fill();
                whitePixelRegion = new TextureRegion(new Texture(pixmap));
                pixmap.dispose();
            } catch (Exception e) {
                Gdx.app.error("HoverCursorWidget", "Failed to create white pixel fallback texture.", e);
            }
        }
        return whitePixelRegion;
    }
}