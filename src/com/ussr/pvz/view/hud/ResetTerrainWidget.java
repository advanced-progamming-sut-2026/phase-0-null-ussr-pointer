package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.structures.Grave;
import com.ussr.pvz.model.board.terrain.Tile;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.session.GameSession;
import pvz.libpvz.textures.TextureBank;

/**
 * Cheat button — resets every non-Normal tile on the lawn back to Normal.
 * Structural overlays (graves, vases, ice blocks) are left untouched;
 * only the underlying TileType is changed.
 * Sits at the bottom-left of the HUD alongside NukeMinionWidget.
 */
public class ResetTerrainWidget extends Stack {

    private static final String REGION_ICON = "IMAGE_BACKGROUNDS_TILE_NORMAL";
    private static final Color FLASH_COLOR  = new Color(0.4f, 1f, 0.4f, 1f);
    private static final float FLASH_DURATION = 0.18f;

    private final Image iconImage;
    private float flashTimer = 0f;

    public ResetTerrainWidget(Skin skin, TextureBank textures) {
        setSize(64f, 64f);
        setTouchable(Touchable.enabled);

        // Background
        Image bg = new Image(textures.region("IMAGE_UI_GENERIC_BROWNBUTTON"));
        bg.setScaling(Scaling.fit);
        bg.setTouchable(Touchable.disabled);
        add(bg);

        // Icon — use the normal tile texture; fall back to brown button if missing
        var region = textures.region(REGION_ICON);
        if (region == null) region = textures.region("IMAGE_UI_GENERIC_BROWNBUTTON");
        iconImage = region != null
                ? new Image(new TextureRegionDrawable(region))
                : new Image(skin.getDrawable("image_ui_generic_brownbutton_10"));
        iconImage.setScaling(Scaling.fit);
        iconImage.setTouchable(Touchable.disabled);
        add(iconImage);

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                executeReset();
            }
        });
    }

    private void executeReset() {
        GameSession session = App.getGameSession();
        if (session == null || session.isGameOver()) return;

        Lawn lawn = session.getLawn();
        if (lawn == null) {
            return;
        }

        int count = 0;
        for (int r = 0; r < lawn.getRows(); r++) {
            for (int c = 0; c < lawn.getCols(); c++) {
                Cell cell = lawn.getCell(r, c);
                if (cell == null) continue;
                Tile tile = cell.getTile();
                if (tile == null) continue;
                if (tile.getType() != TileType.Normal) {
                    tile.setType(TileType.Normal);
                    if(cell.getInteractableStructure() != null && cell.getInteractableStructure() instanceof Grave) {
                        cell.setStructure(null);
                    }
                    count++;
                }
            }
        }

        if (count > 0) {
            flashTimer = FLASH_DURATION;
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (flashTimer > 0f) {
            flashTimer -= delta;
            iconImage.setColor(flashTimer > 0f ? FLASH_COLOR : Color.WHITE);
        }
    }
}
