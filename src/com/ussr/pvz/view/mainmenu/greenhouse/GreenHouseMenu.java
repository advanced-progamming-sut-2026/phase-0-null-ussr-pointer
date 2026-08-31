package com.ussr.pvz.view.mainmenu.greenhouse;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.ussr.pvz.controller.maincontroller.GreenHouseController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.view.FadingMenu;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GreenHouseMenu extends FadingMenu {

    private final Skin skin;
    private final TextureBank textures;
    private final PamPlayer pamPlayer;
    private final GreenHouseController controller;
    private final Group potContainer;

    public static final float[] COL_X = { 460f, 590f, 715f, 835f };
    public static final float[] ROW_Y = { 150f, 310f, 440f };

    public GreenHouseMenu(Skin skin) {
        this.skin = skin;
        this.controller = new GreenHouseController();

        FileHandle assetsFolder = Gdx.files.local("pvz-assets");
        this.textures = new TextureBank("768", assetsFolder);
        this.pamPlayer = new PamPlayer(textures,assetsFolder);

        setupBackground();

        this.potContainer = new Group();
        this.addActor(potContainer);

        refreshGrid();
    }

    private void setupBackground() {
        TextureRegion bgRegion = textures.region("IMAGE_BACKGROUNDS_ZEN_GARDEN");
        if (bgRegion != null) {
            Image bgImage = new Image(bgRegion);
            bgImage.setFillParent(true);
            this.addActor(bgImage);
            bgImage.toBack();
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (textures != null) {
            textures.update();
        }
    }

    @SuppressWarnings("unchecked")
    public void refreshGrid() {
        potContainer.clearChildren();

        Map<String, Object> data = App.getAccount().getGreenhouse().toMap();
        List<Map<String, Object>> potsList = (List<Map<String, Object>>) data.get("pots");

        int maxRows = com.ussr.pvz.model.greenhouse.Greenhouse.MAX_ROWS;
        int maxCols = com.ussr.pvz.model.greenhouse.Greenhouse.MAX_COLS;

        Map<String, Map<String, Object>> potLookup = new HashMap<>();
        if (potsList != null) {
            for (Map<String, Object> potMap : potsList) {
                int x = ((Number) potMap.get("x")).intValue();
                int y = ((Number) potMap.get("y")).intValue();
                potLookup.put(x + "," + y, potMap);
            }
        }

        // Inside GreenHouseMenu.java refreshGrid():
        for (int row = 0; row < maxRows; row++) {
            for (int col = 0; col < maxCols; col++) {
                Map<String, Object> potMap = potLookup.get(col + "," + row);

                // Passed 'textures' parameter here
                PotWidget potWidget =
                        new PotWidget(col, row, potMap, skin, pamPlayer, textures, controller, this::refreshGrid);

                potWidget.setPosition(COL_X[col] - 60f, ROW_Y[row] - 60f);
                potWidget.setSize(120f, 120f);
                potContainer.addActor(potWidget);
            }
        }
    }
}