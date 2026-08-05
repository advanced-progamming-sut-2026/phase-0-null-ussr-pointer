package com.ussr.pvz.view.gameplay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.structures.Grave;
import com.ussr.pvz.model.board.structures.InteractableStructure;
import com.ussr.pvz.model.board.terrain.Tile;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.view.animation.PamActor;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TerrainRenderLayer extends Group {

    private final PamPlayer pamPlayer;
    private final TextureBank textures;

    private final Map<InteractableStructure, PamActor> structureActors =
            new HashMap<>();

    public TerrainRenderLayer(
            PamPlayer pamPlayer,
            TextureBank textures
    ) {
        this.pamPlayer = pamPlayer;
        this.textures = textures;
        setTouchable(Touchable.disabled);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        GameSession session = App.getGameSession();
        if (session == null || session.getLawn() == null) {
            clearStructureActors();
            return;
        }

        synchronizeStructures(session.getLawn());
    }

    private void synchronizeStructures(Lawn lawn) {
        Map<InteractableStructure, Boolean> activeStructures =
                new HashMap<>();

        for (int row = 0; row < lawn.getRows(); row++) {
            for (int column = 0; column < lawn.getCols(); column++) {
                Cell cell = lawn.getCell(row, column);

                if (cell == null) {
                    continue;
                }

                InteractableStructure structure =
                        cell.getInteractableStructure();

                if (structure == null || !structure.isAlive()) {
                    continue;
                }

                activeStructures.put(structure, true);
                synchronizeStructure(structure, column, row);
            }
        }

        removeMissingStructures(activeStructures);
    }

    private void synchronizeStructure(
            InteractableStructure structure,
            int column,
            int row
    ) {
        if (!(structure instanceof Grave grave)) {
            return;
        }

        PamActor actor = structureActors.computeIfAbsent(
                grave,
                key -> createGraveActor(grave)
        );

        // Dynamically update the damage clip based on current HP
        actor.setClip(getGraveDamageClip(grave.getHp()));

        actor.setPosition(
                LawnGridLayout.centeredActorX(
                        column,
                        actor.getWidth()
                ) + LawnGridLayout.GRAVE_DRAW_OFFSET_X,

                LawnGridLayout.centeredActorY(
                        row,
                        actor.getHeight()
                ) + LawnGridLayout.GRAVE_DRAW_OFFSET_Y
        );
    }

    /**
     * Determines the correct PAM clip based on the Grave's current HP.
     * Max HP is 700 as defined in Grave.java.
     */
    private String getGraveDamageClip(int hp) {
        // If it's at full health
        if (hp >= 700) {
            return "undamaged";
        }
        // 75% to 99% health
        else if (hp >= 525) {
            return "damage1";
        }
        // 50% to 74% health
        else if (hp >= 350) {
            return "damage2";
        }
        // 25% to 49% health
        else if (hp >= 175) {
            return "damage3";
        }
        // Below 25% health
        else {
            return "damage4";
        }
    }
    private PamActor createGraveActor(Grave grave) {
        // Default to the Grave content's PAM path (which are the Dark Ages ones)
        String pamPath = grave.getContent().getPamLocation();

        if (App.getLevelManager() != null && App.getLevelManager().getCurrentChapter() != null) {
            String chapterId = App.getLevelManager().getCurrentChapter().getId();

            // Override for Ancient Egypt
            if ("ancient_egypt".equals(chapterId)) {
                pamPath = "768/INITIAL/GRAVESTONES/EGYPT_HIEROGLYPH/EGYPT_HIEROGLYPH.PAM";
            }
            // Explicit check for Dark Ages (uses the default PAM paths defined in the Grave Content enum)
            else if ("dark_ages".equals(chapterId)) {
                pamPath = grave.getContent().getPamLocation();
            }
        }

        PamActor actor = new PamActor(
                pamPlayer,
                pamPath,
                "undamaged"
        );

        actor.setPamScale(0.55f);
        actor.setLooping(false);
        actor.setTouchable(Touchable.disabled);
        addActor(actor);

        return actor;
    }

    private void removeMissingStructures(
            Map<InteractableStructure, Boolean> activeStructures
    ) {
        Iterator<Map.Entry<InteractableStructure, PamActor>> iterator =
                structureActors.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<InteractableStructure, PamActor> entry =
                    iterator.next();

            if (!activeStructures.containsKey(entry.getKey())) {
                entry.getValue().remove();
                iterator.remove();
            }
        }
    }

    private void clearStructureActors() {
        for (PamActor actor : structureActors.values()) {
            actor.remove();
        }

        structureActors.clear();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        GameSession session = App.getGameSession();

        if (session != null && session.getLawn() != null) {
            drawTerrain(batch, parentAlpha, session.getLawn());
        }

        // Structures are actors and are rendered here.
        super.draw(batch, parentAlpha);
    }

    private void drawTerrain(
            Batch batch,
            float parentAlpha,
            Lawn lawn
    ) {
        for (int row = 0; row < lawn.getRows(); row++) {
            for (int column = 0; column < lawn.getCols(); column++) {
                Cell cell = lawn.getCell(row, column);

                if (cell == null || cell.getTile() == null) {
                    continue;
                }

                drawTile(
                        batch,
                        parentAlpha,
                        cell.getTile(),
                        column,
                        row
                );
            }
        }
    }

    private void drawTile(
            Batch batch,
            float parentAlpha,
            Tile tile,
            int column,
            int row
    ) {
        TileType type = tile.getType();

        if (type == null || type == TileType.Normal) {
            return;
        }

        TextureRegion region = findTerrainRegion(type);

        if (region != null) {
            drawTerrainRegion(
                    batch,
                    parentAlpha,
                    region,
                    column,
                    row
            );
            return;
        }

        // Temporary fallback until every terrain asset is connected.
        drawTerrainColor(
                batch,
                parentAlpha,
                type,
                column,
                row
        );
    }

    private TextureRegion findTerrainRegion(TileType type) {
        return switch (type) {
            case Crater ->
                    textures.region(
                            "IMAGE_EFFECTS_CRATER_CRATER_129X131"
                    );

            case ShallowCoast ->
                    textures.region(
                            "IMAGE_EFFECTS_SHALLOW_PUDDLE_TILE"
                    );

            default -> null;
        };
    }

    private void drawTerrainRegion(
            Batch batch,
            float parentAlpha,
            TextureRegion region,
            int column,
            int row
    ) {
        Color previousColor = new Color(batch.getColor());

        batch.setColor(1f, 1f, 1f, parentAlpha);
        batch.draw(
                region,
                LawnGridLayout.cellX(column),
                LawnGridLayout.cellY(row),
                LawnGridLayout.CELL_WIDTH,
                LawnGridLayout.CELL_HEIGHT
        );

        batch.setColor(previousColor);
    }

    private void drawTerrainColor(
            Batch batch,
            float parentAlpha,
            TileType type,
            int column,
            int row
    ) {
        TextureRegion fallback =
                textures.region("IMAGE_UI_HUD_TILE_HIGHLIGHT");

        if (fallback == null) {
            return;
        }

        Color color = terrainColor(type);
        Color previousColor = new Color(batch.getColor());

        batch.setColor(
                color.r,
                color.g,
                color.b,
                color.a * parentAlpha
        );

        batch.draw(
                fallback,
                LawnGridLayout.cellX(column),
                LawnGridLayout.cellY(row),
                LawnGridLayout.CELL_WIDTH,
                LawnGridLayout.CELL_HEIGHT
        );

        batch.setColor(previousColor);
    }

    private Color terrainColor(TileType type) {
        return switch (type) {
            case Water, ShallowCoast ->
                    new Color(0.1f, 0.45f, 0.9f, 0.45f);

            case Frozen, Slippery ->
                    new Color(0.65f, 0.9f, 1f, 0.55f);

            case Grave ->
                    new Color(0.35f, 0.25f, 0.2f, 0.45f);

            case Necromancy ->
                    new Color(0.45f, 0.15f, 0.55f, 0.5f);

            case Crater ->
                    new Color(0.2f, 0.12f, 0.08f, 0.6f);

            default ->
                    new Color(1f, 1f, 1f, 0.25f);
        };
    }
}
