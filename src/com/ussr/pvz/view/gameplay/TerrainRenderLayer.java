package com.ussr.pvz.view.gameplay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.structures.*;
import com.ussr.pvz.model.board.terrain.Tile;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.level.behavior.SaveOurSeedsBehavior;
import com.ussr.pvz.model.level.behavior.LevelBehavior;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.level.chaptereffect.BigWaveBeachEffect;
import com.ussr.pvz.view.animation.PamActor;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

import java.util.*;

/**
 * Renders static, cell-bound structures (Grave, Vase, Brain, Container)
 * and tile-type overlays (water, ice, fire, crater, etc.).
 * <p>
 * NOT responsible for:
 * - IceBlock / OctopusWrap  → EntityRenderLayer overlayGroup
 * - PushableStructure       → EntityRenderLayer zombieGroup
 * - LawnMower               → EntityRenderLayer zombieGroup
 */
public class TerrainRenderLayer extends Group {

    private final PamPlayer pamPlayer;
    private final TextureBank textures;

    /**
     * Only Grave, Vase, Brain, Container actors live here
     */
    private final Map<InteractableStructure, PamActor> structureActors = new HashMap<>();
    /**
     * Slippery tile PAM actors — keyed by "row,col"
     */
    private final Map<String, PamActor> slipperyActors = new HashMap<>();

    public TerrainRenderLayer(PamPlayer pamPlayer, TextureBank textures) {
        this.pamPlayer = pamPlayer;
        this.textures = textures;
        setTouchable(Touchable.disabled);
    }

    // =========================================================================
    // act
    // =========================================================================
    @Override
    public void act(float delta) {
        super.act(delta);

        GameSession session = App.getGameSession();
        if (session == null || session.getLawn() == null) {
            clearStructureActors();
            clearSlipperyActors();
            return;
        }

        synchronizeStructures(session.getLawn());
        synchronizeSlipperyTiles(session.getLawn());   // ← add this
    }

    private void synchronizeSlipperyTiles(Lawn lawn) {
        Set<String> activeKeys = new HashSet<>();

        for (int row = 0; row < lawn.getRows(); row++) {
            for (int col = 0; col < lawn.getCols(); col++) {
                Cell cell = lawn.getCell(row, col);
                if (cell == null || cell.getTile() == null) continue;
                if (cell.getTile().getType() != TileType.Slippery) continue;

                String key = row + "," + col;
                activeKeys.add(key);

                Tile.SlipperyDirection dir = cell.getTile().getSlipperyDirection();
                String pamPath = dir == Tile.SlipperyDirection.UP
                        ? "768/FULL/EFFECTS/TILESLIDER_ICEAGE_DOWN/TILESLIDER_ICEAGE_DOWN.PAM"
                        : "768/FULL/EFFECTS/TILESLIDER_ICEAGE_UP/TILESLIDER_ICEAGE_UP.PAM";

                final int c = col, r = row;
                PamActor actor = slipperyActors.computeIfAbsent(key, k -> {
                    PamActor pa = new PamActor(pamPlayer, pamPath, "animation");
                    pa.setPamScale(1f);
                    pa.setLooping(true);
                    pa.setTouchable(Touchable.disabled);
                    addActor(pa);
                    return pa;
                });

                actor.setPosition(
                        LawnGridLayout.cellX(c) + LawnGridLayout.SLIPPERY_DRAW_OFFSET_X,
                        LawnGridLayout.cellY(r) + LawnGridLayout.SLIPPERY_DRAW_OFFSET_Y
                );
            }
        }

        // Remove actors for tiles that are no longer slippery
        Iterator<Map.Entry<String, PamActor>> it = slipperyActors.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, PamActor> entry = it.next();
            if (!activeKeys.contains(entry.getKey())) {
                entry.getValue().remove();
                it.remove();
            }
        }
    }

    private void clearSlipperyActors() {
        slipperyActors.values().forEach(PamActor::remove);
        slipperyActors.clear();
    }

    private void synchronizeStructures(Lawn lawn) {
        Map<InteractableStructure, Boolean> activeStructures = new HashMap<>();

        for (int row = 0; row < lawn.getRows(); row++) {
            for (int col = 0; col < lawn.getCols(); col++) {
                Cell cell = lawn.getCell(row, col);
                if (cell == null) continue;

                InteractableStructure structure = cell.getInteractableStructure();
                if (structure == null || !structure.isAlive()) continue;

                // IceBlock and OctopusWrap are handled by EntityRenderLayer — skip here
                if (structure instanceof IceBlock) continue;
                if (structure instanceof OctopusWrap) continue;
                // PushableStructure moves freely — handled by EntityRenderLayer
                if (structure instanceof PushableStructure) continue;

                activeStructures.put(structure, true);
                synchronizeStructure(structure, col, row);
            }
        }

        removeMissingStructures(activeStructures);
    }

    private void synchronizeStructure(InteractableStructure structure, int col, int row) {
        float worldX = LawnGridLayout.centeredActorX(col, 80f)
                + LawnGridLayout.GRAVE_DRAW_OFFSET_X
                + structureOffsetX(structure);
        float worldY = LawnGridLayout.centeredActorY(row, 80f)
                + LawnGridLayout.GRAVE_DRAW_OFFSET_Y
                + structureOffsetY(structure);

        if (structure instanceof Grave grave) {
            PamActor actor = structureActors.computeIfAbsent(grave, k -> createGraveActor(grave));
            actor.setClip(getGraveDamageClip(grave.getHp()));
            actor.setPosition(worldX, worldY);

        } else if (structure instanceof Vase vase) {
            PamActor actor = structureActors.computeIfAbsent(vase,
                    k -> createSimplePamActor(vase.getType().getPamLocation(), 0.5f, "idle"));
            actor.setPosition(worldX, worldY);

        } else if (structure instanceof Brain brain) {
            PamActor actor = structureActors.computeIfAbsent(brain,
                    k -> createSimplePamActor(brain.getPamLocation(), 0.5f, "idle"));
            actor.setPosition(worldX, worldY);

        } else if (structure instanceof Container) {
            // No PAM defined yet — add here when Container gets one
        } else if (structure instanceof IcedZombie icedZombie) {
            PamActor actor = structureActors.computeIfAbsent(icedZombie,
                    k -> createSimplePamActor(icedZombie.getPamLocation(), 0.55f, "idle"));
            actor.setPosition(worldX, worldY);
        }
    }

    // =========================================================================
    // Actor factories
    // =========================================================================
    private PamActor createSimplePamActor(String pamPath, float scale, String clip) {
        PamActor actor = new PamActor(pamPlayer, pamPath, clip);
        actor.setPamScale(scale);
        actor.setLooping(true);
        actor.setTouchable(Touchable.disabled);
        addActor(actor);
        return actor;
    }

    private PamActor createGraveActor(Grave grave) {
        String pamPath = grave.getContent().getPamLocation();

        if (App.getLevelManager() != null && App.getLevelManager().getCurrentChapter() != null) {
            String chapterId = App.getLevelManager().getCurrentChapter().getId();
            if ("ancient_egypt".equals(chapterId)) {
                pamPath = "768/INITIAL/GRAVESTONES/EGYPT_HIEROGLYPH/EGYPT_HIEROGLYPH.PAM";
            }
            // dark_ages: content's own pamLocation is already correct
        }

        PamActor actor = new PamActor(pamPlayer, pamPath, "undamaged");
        actor.setPamScale(0.55f);
        actor.setLooping(false);
        actor.setTouchable(Touchable.disabled);
        addActor(actor);
        return actor;
    }

    // =========================================================================
    // Per-type offsets
    // =========================================================================
    private float structureOffsetX(InteractableStructure s) {
        return 0f;
    }

    private float structureOffsetY(InteractableStructure s) {
        if (s instanceof Vase) return 4f;
        if (s instanceof Brain) return 8f;
        return 0f;
    }

    // =========================================================================
    // Cleanup
    // =========================================================================
    private void removeMissingStructures(Map<InteractableStructure, Boolean> active) {
        Iterator<Map.Entry<InteractableStructure, PamActor>> it =
                structureActors.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<InteractableStructure, PamActor> entry = it.next();
            if (!active.containsKey(entry.getKey())) {
                entry.getValue().remove();
                it.remove();
            }
        }
    }

    private void clearStructureActors() {
        structureActors.values().forEach(PamActor::remove);
        structureActors.clear();
    }

    // =========================================================================
    // draw — tile overlays first, then structure actors on top
    // =========================================================================
    @Override
    public void draw(Batch batch, float parentAlpha) {
        GameSession session = App.getGameSession();
        if (session != null && session.getLawn() != null) {
            drawTerrain(batch, parentAlpha, session.getLawn());
        }
        super.draw(batch, parentAlpha); // PamActors (Graves, Vases, Brain)
    }

    private void drawTerrain(
            Batch batch,
            float parentAlpha,
            Lawn lawn
    ) {
        for (int row = 0; row < lawn.getRows(); row++) {
            for (int col = 0; col < lawn.getCols(); col++) {
                Cell cell = lawn.getCell(row, col);

                if (cell == null || cell.getTile() == null) {
                    continue;
                }

                drawTile(
                        batch,
                        parentAlpha,
                        cell.getTile(),
                        col,
                        row
                );
            }
        }

        if (findCoastColumn(lawn) >= 0) {
            drawWaterLimitLine(
                    batch,
                    parentAlpha,
                    lawn
            );
        }

        // SaveOurSeeds: draw a protective tile highlight under each endangered plant.
        drawProtectTiles(batch, parentAlpha);
    }

    /**
     * If the current level uses {@link SaveOurSeedsBehavior}, draws the
     * IMAGE_BACKGROUNDS_PROTECT_TILE texture under each endangered plant cell.
     */
    private void drawProtectTiles(Batch batch, float parentAlpha) {
        GameSession session = App.getGameSession();
        if (session == null || session.getLevel() == null) return;

        LevelBehavior behavior = session.getLevel().getBehavior();
        if (!(behavior instanceof SaveOurSeedsBehavior sos)) return;

        TextureRegion tile = textures.region(
                "IMAGE_BACKGROUNDS_PROTECT_TILE_PROTECT_TILE_112X125");
        if (tile == null) return;

        Color prev = new Color(batch.getColor());
        batch.setColor(1f, 1f, 1f, parentAlpha);

        for (Plant plant : sos.getEndangeredPlants()) {
            if (plant == null || !plant.isAlive()) continue;
            Plant.Location loc = plant.getLocation();
            if (loc == null) continue;

            float x = LawnGridLayout.cellX(loc.x()) + LawnGridLayout.TILE_DRAW_OFFSET_X;
            float y = LawnGridLayout.cellY(loc.y()) + LawnGridLayout.TILE_DRAW_OFFSET_Y;
            batch.draw(tile, x, y, LawnGridLayout.CELL_WIDTH, LawnGridLayout.CELL_HEIGHT);
        }

        batch.setColor(prev);
    }

    private void drawWaterTile(
            Batch batch,
            float parentAlpha,
            int col,
            int row
    ) {
        TextureRegion water = textures.region(
                "IMAGE_BACKGROUNDS_WATER_SQUARE_WATER_SQUARE_174X209"
        );

        if (water == null) {
            return;
        }

        float x =
                LawnGridLayout.cellX(col)
                        + LawnGridLayout.WATER_DRAW_OFFSET_X
                        + (LawnGridLayout.CELL_WIDTH
                        - LawnGridLayout.WATER_DRAW_WIDTH) / 2f;

        float y =
                LawnGridLayout.cellY(row)
                        + LawnGridLayout.WATER_DRAW_OFFSET_Y
                        + (LawnGridLayout.CELL_HEIGHT
                        - LawnGridLayout.WATER_DRAW_HEIGHT) / 2f;

        Color previous = new Color(batch.getColor());
        float drawHeight = LawnGridLayout.WATER_DRAW_HEIGHT
                + (row == LawnGridLayout.ROWS - 1
                ? LawnGridLayout.WATER_TOP_ROW_EXTENSION
                : 0f);

        TextureRegion base = textures.region(
                "IMAGE_BACKGROUNDS_WATER_UNDERLAYER_WATER_UNDERLAYER_1586X49"
        );
        if (base != null) {
            batch.setColor(0.38f, 0.70f, 0.96f, parentAlpha);
            batch.draw(
                    base,
                    x,
                    y,
                    LawnGridLayout.WATER_DRAW_WIDTH,
                    drawHeight
            );
        }

        batch.setColor(
                0.68f,
                0.90f,
                1f,
                parentAlpha * 0.78f
        );

        batch.draw(
                water,
                x,
                y,
                LawnGridLayout.WATER_DRAW_WIDTH,
                drawHeight
        );

        batch.setColor(previous);
    }

    private void drawShallowCoastTile(
            Batch batch,
            float parentAlpha,
            int col,
            int row
    ) {
        drawWaterTile(
                batch,
                parentAlpha,
                col,
                row
        );

        TextureRegion coast = textures.region(
                "IMAGE_EFFECTS_SHALLOW_PUDDLE_TILE_SHALLOW_PUDDLE_TILE_38X105"
        );

        if (coast == null) {
            return;
        }

        float x =
                LawnGridLayout.cellX(col)
                        + LawnGridLayout.WATER_DRAW_OFFSET_X
                        + (LawnGridLayout.CELL_WIDTH
                        - LawnGridLayout.WATER_DRAW_WIDTH) / 2f;

        float y =
                LawnGridLayout.cellY(row)
                        + LawnGridLayout.WATER_DRAW_OFFSET_Y
                        + (LawnGridLayout.CELL_HEIGHT
                        - LawnGridLayout.WATER_DRAW_HEIGHT) / 2f;

        float coastWidth =
                LawnGridLayout.CELL_WIDTH * 0.40f;
        float coastHeight = LawnGridLayout.WATER_DRAW_HEIGHT
                + (row == LawnGridLayout.ROWS - 1
                ? LawnGridLayout.WATER_TOP_ROW_EXTENSION
                : 0f);

        Color previous = new Color(batch.getColor());

        batch.setColor(1f, 1f, 1f, parentAlpha);

        batch.draw(
                coast,
                x,
                y,
                coastWidth,
                coastHeight
        );

        batch.setColor(previous);
    }

    private void drawTile(
            Batch batch,
            float parentAlpha,
            Tile tile,
            int col,
            int row
    ) {
        TileType type = tile.getType();

        if (type == null
                || type == TileType.Normal
                || type == TileType.Beghouled
                || type == TileType.Slippery) {
            return;
        }

        if (type == TileType.Water) {
            drawWaterTile(
                    batch,
                    parentAlpha,
                    col,
                    row
            );
            return;
        }

        if (type == TileType.ShallowCoast) {
            drawShallowCoastTile(
                    batch,
                    parentAlpha,
                    col,
                    row
            );
            return;
        }

        TextureRegion region = findTerrainRegion(tile);

        if (region != null) {
            drawTerrainRegion(
                    batch,
                    parentAlpha,
                    region,
                    col,
                    row
            );
        } else {
            drawTerrainColor(
                    batch,
                    parentAlpha,
                    terrainColor(type),
                    col,
                    row
            );
        }
    }

    private TextureRegion findTerrainRegion(Tile tile) {
        TileType type = tile.getType();
        return switch (type) {
            case Crater -> textures.region("IMAGE_EFFECTS_CRATER_CRATER_129X131");
            case Frozen -> textures.region(
                    "IMAGE_EFFECTS_CHILLYPEPPER_TILE_ICE_CHILLYPEPPER_TILE_ICE_248X147");
            case Slippery -> textures.region(slipperyRegion(tile));
            case Burning -> textures.region(
                    "IMAGE_EFFECTS_POWER_UP_FIRE_IMPACT_POWER_UP_FIRE_IMPACT_302X89");
            case Necromancy -> textures.region(
                    "IMAGE_EFFECTS_GRAVEBUSTER_DIRT_GRAVEBUSTER_DIRT_PILE");
            // Grave tile: no overlay — the Grave structure actor is sufficient
            default -> null;
        };
    }

    private String slipperyRegion(Tile tile) {
        if (tile.getSlipperyDirection() == Tile.SlipperyDirection.UP) {
            return "IMAGE_EFFECTS_TILESLIDER_ICEAGE_UP_TILESLIDER_ICEAGE_UP_95X92";
        }
        return "IMAGE_EFFECTS_TILESLIDER_ICEAGE_DOWN_TILESLIDER_ICEAGE_DOWN_141X169";
    }

    private void drawTerrainRegion(Batch batch, float parentAlpha,
                                   TextureRegion region, int col, int row) {
        Color prev = new Color(batch.getColor());
        batch.setColor(1f, 1f, 1f, parentAlpha);
        batch.draw(region,
                LawnGridLayout.cellX(col) + LawnGridLayout.TILE_DRAW_OFFSET_X,
                LawnGridLayout.cellY(row) + LawnGridLayout.TILE_DRAW_OFFSET_Y,
                LawnGridLayout.CELL_WIDTH, LawnGridLayout.CELL_HEIGHT);
        batch.setColor(prev);
    }

    private void drawTerrainColor(Batch batch, float parentAlpha,
                                  Color color, int col, int row) {
        TextureRegion fallback = textures.region("IMAGE_BACKGROUNDS_LINKTILE_05_LINKTILE_05_145X169");
        if (fallback == null) return;

        Color prev = new Color(batch.getColor());
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        batch.draw(fallback,
                LawnGridLayout.cellX(col) + LawnGridLayout.TILE_DRAW_OFFSET_X,
                LawnGridLayout.cellY(row) + LawnGridLayout.TILE_DRAW_OFFSET_Y,
                LawnGridLayout.CELL_WIDTH, LawnGridLayout.CELL_HEIGHT);
        batch.setColor(prev);
    }
    private Color terrainColor(TileType type) {
        return switch (type) {
            case Water, ShallowCoast -> new Color(0.1f, 0.45f, 0.9f, 0.45f);
            case Frozen, Slippery -> new Color(0.65f, 0.9f, 1f, 0.55f);
            case Burning -> new Color(1f, 0.35f, 0.05f, 0.5f);
            case Grave -> new Color(0.35f, 0.25f, 0.2f, 0.45f);
            case Necromancy -> new Color(0.45f, 0.15f, 0.55f, 0.5f);
            case Crater -> new Color(0.2f, 0.12f, 0.08f, 0.6f);
            default -> new Color(1f, 1f, 1f, 0.25f);
        };
    }

    // =========================================================================
    // Grave damage clip
    // =========================================================================
    private String getGraveDamageClip(int hp) {
        if (hp >= 700) return "undamaged";
        if (hp >= 525) return "damage1";
        if (hp >= 350) return "damage2";
        if (hp >= 175) return "damage3";
        return "damage4";
    }

    private int findCoastColumn(Lawn lawn) {
        for (int col = 0; col < lawn.getCols(); col++) {
            for (int row = 0; row < lawn.getRows(); row++) {
                Tile tile = lawn.getTile(row, col);

                if (tile != null
                        && tile.getType() == TileType.ShallowCoast) {
                    return col;
                }
            }
        }

        return -1;
    }

    private void drawWaterLimitLine(
            Batch batch,
            float parentAlpha,
            Lawn lawn
    ) {
        TextureRegion marker = textures.region(
                "IMAGE_BACKGROUNDS_WATER_TIDE_LINE_WATER_TIDE_LINE_161X397"
        );
        if (marker == null) {
            return;
        }

        float height = LawnGridLayout.CELL_HEIGHT * lawn.getRows();
        float width = height
                * marker.getRegionWidth()
                / marker.getRegionHeight();
        float x = LawnGridLayout.cellX(BigWaveBeachEffect.WATER_LIMIT_COLUMN)
                + LawnGridLayout.TILE_DRAW_OFFSET_X
                - width * 0.5f;
        float y = LawnGridLayout.cellY(0)
                + LawnGridLayout.TILE_DRAW_OFFSET_Y;

        Color previous = new Color(batch.getColor());
        batch.setColor(1f, 1f, 1f, parentAlpha);
        batch.draw(marker, x, y, width, height);
        batch.setColor(previous);
    }
}