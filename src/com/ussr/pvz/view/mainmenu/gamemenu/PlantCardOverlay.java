package com.ussr.pvz.view.mainmenu.gamemenu;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.ussr.pvz.model.dto.PlantTypeRequest;
import com.ussr.pvz.service.CollectionService;
import com.ussr.pvz.service.CollectionService.PlantData;
import com.ussr.pvz.view.animation.PlantPamActor;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

public class PlantCardOverlay extends Dialog {

    public PlantCardOverlay(PlantData plant, Skin skin, TextureBank textures, PamPlayer pamPlayer, TextureRegionDrawable dimBg, CollectionService service) {
        super("", buildStyle(skin, dimBg));

        Table content = getContentTable();
        Table leftSide = new Table();
        Table rightSide = new Table();

        // Left Side: Plant Animation with Pirate Background
        PlantPamActor pamActor = new PlantPamActor(pamPlayer, plant.pamPath, textures.region("IMAGE_UI_ARCADEMENU_BG_PIRATE"));
        leftSide.add(pamActor).size(200, 200);

        // Right Side: Plant Name and Level
        rightSide.add(new Label(plant.name, skin, "big_outline")).left().padBottom(5).row();
        rightSide.add(new Label("Category: " + plant.category, skin, "secondary")).left().padBottom(15).row();

        // Progress Bar & Level Display
        int nextLevelCost = (plant.level == 0) ? 0 : plant.level * 10;
        int currentPackets = plant.ownedPackets; // Requires updated DTO

        Table progressTable = new Table();
        progressTable.add(new Label(plant.level > 0 ? "Level " + plant.level : "Locked", skin, "medium")).padRight(10);

        if (plant.level > 0 && plant.level < 4) {
            ProgressBar pb = new ProgressBar(0, nextLevelCost, 1, false, skin, "ingame_progress");
            pb.setValue(Math.min(currentPackets, nextLevelCost));
            progressTable.add(pb).width(120).padRight(10);
            progressTable.add(new Label(currentPackets + "/" + nextLevelCost, skin, "default"));
        } else if (plant.level >= 4) {
            progressTable.add(new Label("MAX", skin, "medium"));
        }
        rightSide.add(progressTable).left().padBottom(15).row();

        // Right Side: Stats
        Table statsTable = new Table();
        statsTable.add(new Label("Sun Cost:\n" + plant.cost, skin, "secondary")).padRight(20);
        statsTable.add(new Label("Recharge:\n" + plant.recharge + "s", skin, "secondary")).padRight(20);
        statsTable.add(new Label("Toughness:\n" + plant.baseHp, skin, "secondary"));
        rightSide.add(statsTable).left().padTop(10).row();
        rightSide.add(new Label("Damage: " + plant.damage, skin, "secondary")).left().padTop(10).row();

        content.add(leftSide).pad(20);
        content.add(rightSide).pad(20).top().left();

        // Action Buttons
        getButtonTable().pad(20);

        if (plant.level > 0 && plant.level < 4) {
            TextButton upgradeBtn = new TextButton("Upgrade (" + (plant.level * 1000) + " Coins)", skin, "green");
            upgradeBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    service.upgradePlant(new PlantTypeRequest(plant.id));
                    hide(); // Close dialog after action
                }
            });
            getButtonTable().add(upgradeBtn).padRight(10);
        } else if (plant.level == 0) {
            TextButton buyBtn = new TextButton("Buy (2000 Coins)", skin, "green");
            buyBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    service.purchasePlant(new PlantTypeRequest(plant.id));
                    hide(); // Close dialog after action
                }
            });
            getButtonTable().add(buyBtn).padRight(10);
        }

        TextButton closeBtn = new TextButton("Close", skin, "brown");
        button(closeBtn, true);
    }

    private static WindowStyle buildStyle(Skin skin, TextureRegionDrawable dimBg) {
        WindowStyle dialogStyle = new WindowStyle();
        dialogStyle.titleFont = skin.get("default", Label.LabelStyle.class).font;
        if (skin.has("image_ui_dialog_asset_dialogborder", com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)) {
            dialogStyle.background = skin.getDrawable("image_ui_dialog_asset_dialogborder");
        }
        dialogStyle.stageBackground = dimBg;
        return dialogStyle;
    }
}