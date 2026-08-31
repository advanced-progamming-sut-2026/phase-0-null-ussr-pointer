package com.ussr.pvz.view.mainmenu.gamemenu.collection;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.ussr.pvz.model.dto.PlantTypeRequest;
import com.ussr.pvz.notification.NotificationCenter;
import com.ussr.pvz.service.CollectionService;
import com.ussr.pvz.service.CollectionService.PlantData;
import com.ussr.pvz.view.animation.PlantPamActor;
import com.ussr.pvz.view.components.SeedPacketPurchaseOverlay;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

public class PlantCardOverlay extends Dialog {

    public PlantCardOverlay(
            PlantData plant,
            Skin skin,
            TextureBank textures,
            PamPlayer pamPlayer,
            TextureRegionDrawable dimBg,
            CollectionService service,
            Runnable onChanged
    ) {
        super("", buildStyle(skin, dimBg));

        buildPlantContent(plant, skin, textures, pamPlayer);
        buildActionButtons(plant, skin, dimBg, service, onChanged);
        button(new TextButton("Close", skin, "brown"), true);
    }

    private void buildPlantContent(
            PlantData plant,
            Skin skin,
            TextureBank textures,
            PamPlayer pamPlayer
    ) {
        Table leftSide = new Table();
        Table rightSide = new Table();

        PlantPamActor pamActor = new PlantPamActor(
                pamPlayer,
                plant.pamPath,
                textures.region("IMAGE_UI_ARCADEMENU_BG_PIRATE")
        );
        leftSide.add(pamActor).size(200, 200);
        buildPlantDetails(rightSide, plant, skin);

        Table content = getContentTable();
        content.add(leftSide).pad(20);
        content.add(rightSide).pad(20).top().left();
    }

    private void buildPlantDetails(
            Table rightSide,
            PlantData plant,
            Skin skin
    ) {
        rightSide.add(new Label(plant.name, skin, "big_outline")).left().padBottom(5).row();
        rightSide.add
                (new Label("Category: " + plant.category, skin, "secondary")).left().padBottom(15).row();
        rightSide.add(buildProgressTable(plant, skin)).left().padBottom(15).row();
        buildStatsTable(rightSide, plant, skin);
    }

    private Table buildProgressTable(PlantData plant, Skin skin) {
        int nextLevelCost = (plant.level == 0) ? 0 : plant.level * 10;
        Table progressTable = new Table();
        progressTable.add
                (new Label(plant.level > 0 ? "Level " + plant.level : "Locked", skin, "medium")).padRight(10);

        if (plant.level > 0 && plant.level < 4) {
            ProgressBar pb = new
                    ProgressBar(0, nextLevelCost, 1, false, skin, "ingame_progress");
            pb.setValue(Math.min(plant.ownedPackets, nextLevelCost));
            progressTable.add(pb).width(120).padRight(10);
            progressTable.add(new Label(plant.ownedPackets + "/" + nextLevelCost, skin, "default"));
        } else if (plant.level >= 4) {
            progressTable.add(new Label("MAX", skin, "medium"));
        }
        return progressTable;
    }

    private void buildStatsTable(Table rightSide, PlantData plant, Skin skin) {
        Table statsTable = new Table();
        statsTable.add(new Label("Sun Cost:\n" + plant.cost, skin, "secondary")).padRight(20);
        statsTable.add(new Label
                ("Recharge:\n" + formatStat(plant.recharge) + "s", skin, "secondary")).padRight(20);
        statsTable.add(new Label("Toughness:\n" + formatStat(plant.baseHp), skin, "secondary"));
        rightSide.add(statsTable).left().padTop(10).row();
        rightSide.add(new Label
                ("Damage: " + formatStat(plant.damage), skin, "secondary")).left().padTop(10).row();
        if (plant.actionInterval > 0) {
            rightSide.add(new Label(
                    "Action interval: " + formatStat(plant.actionInterval) + "s",
                    skin,
                    "secondary"
            )).left().padTop(10).row();
        }
    }

    private void buildActionButtons(
            PlantData plant,
            Skin skin,
            TextureRegionDrawable dimBg,
            CollectionService service,
            Runnable onChanged
    ) {
        getButtonTable().pad(20);

        if (plant.level > 0 && plant.level < 4) {
            addUpgradeButton(plant, skin, dimBg, service, onChanged);
        } else if (plant.level == 0) {
            addBuyButton(plant, skin, service, onChanged);
        }
    }

    private void addUpgradeButton(
            PlantData plant,
            Skin skin,
            TextureRegionDrawable dimBg,
            CollectionService service,
            Runnable onChanged
    ) {
        int nextLevelCost = plant.level * 10;
        int currentPackets = plant.ownedPackets;
        boolean hasEnoughPackets = currentPackets >= nextLevelCost;
        TextButton upgradeBtn = new TextButton(
                "Upgrade (" + (plant.level * 1000) + " Coins)", skin, "green");
        upgradeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleUpgrade(plant, skin, dimBg, service, onChanged,
                        nextLevelCost, currentPackets, hasEnoughPackets);
            }
        });
        getButtonTable().add(upgradeBtn).padRight(10);
    }

    private void handleUpgrade(
            PlantData plant,
            Skin skin,
            TextureRegionDrawable dimBg,
            CollectionService service,
            Runnable onChanged,
            int nextLevelCost,
            int currentPackets,
            boolean hasEnoughPackets
    ) {
        if (!hasEnoughPackets) {
            showSeedPacketPurchase(plant, skin, dimBg, service, onChanged,
                    nextLevelCost, currentPackets);
            return;
        }

        String result = service.upgradePlant(new PlantTypeRequest(plant.id));
        if (result != null && result.contains("Upgraded")) {
            NotificationCenter.success(plant.name + " upgraded!");
        } else {
            NotificationCenter.error(result);
        }
        runOnChanged(onChanged);
        hide();
    }

    private void showSeedPacketPurchase(
            PlantData plant,
            Skin skin,
            TextureRegionDrawable dimBg,
            CollectionService service,
            Runnable onChanged,
            int nextLevelCost,
            int currentPackets
    ) {
        com.badlogic.gdx.scenes.scene2d.Stage stage = getStage();
        hide();
        int missingPackets = nextLevelCost - currentPackets;
        int gemCost = CollectionService.seedPacketGemCost(missingPackets);
        new SeedPacketPurchaseOverlay(
                plant, missingPackets, gemCost, skin, dimBg, service, onChanged
        ).show(stage);
    }

    private void addBuyButton(
            PlantData plant,
            Skin skin,
            CollectionService service,
            Runnable onChanged
    ) {
        TextButton buyBtn = new TextButton("Buy (2000 Coins)", skin, "green");
        buyBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String result = service.purchasePlant(new PlantTypeRequest(plant.id));
                if (result != null && result.contains("Purchased")) {
                    NotificationCenter.success(plant.name + " purchased!");
                } else {
                    NotificationCenter.error(result);
                }
                runOnChanged(onChanged);
                hide();
            }
        });
        getButtonTable().add(buyBtn).padRight(10);
    }

    private void runOnChanged(Runnable onChanged) {
        if (onChanged != null) {
            onChanged.run();
        }
    }

    private static String formatStat(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001) {
            return String.valueOf((int) Math.rint(value));
        }

        return String.format(java.util.Locale.US, "%.2f", value)
                .replaceAll("0+$", "")
                .replaceAll("\\.$", "");
    }

    private static WindowStyle buildStyle(Skin skin, TextureRegionDrawable dimBg) {
        WindowStyle dialogStyle = new WindowStyle();
        dialogStyle.titleFont = skin.get("default", Label.LabelStyle.class).font;
        if (skin.has
                ("image_ui_dialog_asset_dialogborder", com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)) {
            dialogStyle.background = skin.getDrawable("image_ui_dialog_asset_dialogborder");
        }
        dialogStyle.stageBackground = dimBg;
        return dialogStyle;
    }
}
