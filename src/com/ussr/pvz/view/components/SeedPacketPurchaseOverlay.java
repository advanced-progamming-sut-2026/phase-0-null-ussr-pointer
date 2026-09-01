package com.ussr.pvz.view.components;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.ussr.pvz.model.dto.PlantTypeRequest;
import com.ussr.pvz.notification.NotificationCenter;
import com.ussr.pvz.service.CollectionService;
import com.ussr.pvz.service.CollectionService.PlantData;

/**
 * Shown when the player tries to upgrade a plant without owning enough seed
 * packets. Offers to buy exactly the missing packets (never more, never
 * fewer) for a gem price scaled to that exact amount.
 */
public class SeedPacketPurchaseOverlay extends Dialog {

    public SeedPacketPurchaseOverlay(PlantData plant, int missingPackets, int gemCost, Skin skin,
            TextureRegionDrawable dimBg, CollectionService service, Runnable onPurchased) {
        super("", buildStyle(skin, dimBg));
        var content = getContentTable();
        content.pad(20f);
        content.add(new Label("Not Enough Seed Packets", skin, "big_outline")).padBottom(15f).row();

        content.add(new Label("You need " + missingPackets + " more seed packet" + (missingPackets == 1 ? "" : "s")
                        + " to upgrade " + plant.name + ".", skin, "default")).width(320f).row();
        content.add(new Label("Buy " + missingPackets + " seed packet" + (missingPackets == 1 ? "" : "s") + " for "
                + gemCost + " gem" + (gemCost == 1 ? "" : "s") + "?", skin, "secondary")).padTop(10f).row();
        getButtonTable().pad(20f);
        TextButton buyButton = new TextButton(
                "Buy  " + gemCost + " gem" + (gemCost == 1 ? "" : "s"), skin, "green");
        buyButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String result = service.buySeedPacketsWithGems(new PlantTypeRequest(plant.id), missingPackets);
                if (result != null && result.contains("Purchased")) {
                    NotificationCenter.success(missingPackets + " seed packet" + (missingPackets == 1 ? "" : "s")
                                    + " purchased!");
                    if (onPurchased != null) onPurchased.run();
                } else {
                    NotificationCenter.error(result);
                }
                hide();
            }
        });
        getButtonTable().add(buyButton).padRight(10f);
        TextButton cancelButton = new TextButton("Cancel", skin, "brown");
        button(cancelButton, true);
    }

    private static WindowStyle buildStyle(Skin skin, TextureRegionDrawable dimBg) {
        WindowStyle dialogStyle = new WindowStyle();
        dialogStyle.titleFont = skin.get("default", Label.LabelStyle.class).font;

        if (skin.has("image_ui_dialog_asset_dialogborder", Drawable.class)) {
            dialogStyle.background = skin.getDrawable("image_ui_dialog_asset_dialogborder");
        }

        dialogStyle.stageBackground = dimBg;
        return dialogStyle;
    }
}