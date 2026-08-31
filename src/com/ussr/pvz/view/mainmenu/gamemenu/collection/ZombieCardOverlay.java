package com.ussr.pvz.view.mainmenu.gamemenu.collection;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.ussr.pvz.service.CollectionService.ZombieData;
import com.ussr.pvz.view.animation.ZombiePamActor;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

public class ZombieCardOverlay extends Dialog {

    public ZombieCardOverlay(ZombieData zombie, Skin skin, TextureBank textures, PamPlayer pamPlayer,
                             TextureRegionDrawable dimBg) {
        super("", buildStyle(skin, dimBg));
        Table content = getContentTable();
        Table leftSide = new Table();
        Table rightSide = new Table();
        Stack animStack = new Stack();
        if (textures.region("image_ui_dialog_asset_dialogtexture") != null) {
            Image floorImage = new Image(new TextureRegionDrawable(
                    textures.region("image_ui_dialog_asset_dialogtexture")
            ));
            if (!zombie.encountered) floorImage.setColor(com.badlogic.gdx.graphics.Color.DARK_GRAY);
            animStack.add(floorImage);
        }
        if (zombie.encountered) {
            ZombiePamActor pamActor = new ZombiePamActor(pamPlayer, zombie.pamPath);
            pamActor.setPamScale(0.8f);
            animStack.add(pamActor);
        }
        leftSide.add(animStack).size(200, 200);
        rightSide.add(new Label(zombie.encountered ? zombie.name : "Unknown zombie", skin, "big_outline")).
                left().padBottom(15).row();
        if (zombie.encountered) {
            Table statsTable = new Table();
            if (textures.region("image_ui_almanac_zombies_zombietoughness_icon") != null) {
                statsTable.add(new Image(new TextureRegionDrawable(textures.region(
                        "image_ui_almanac_zombies_zombietoughness_icon")))).size(40, 40);
            }
            statsTable.add(new Label("Toughness:\n" + zombie.hitpoints, skin, "medium")).padRight(30);
            if (textures.region("image_ui_almanac_zombies_zombiespeed_icon") != null) {
                statsTable.add(new Image(new TextureRegionDrawable(textures.region(
                        "image_ui_almanac_zombies_zombiespeed_icon")))).size(40, 40);
            }
            statsTable.add(new Label("Speed:\n" + String.format("%.2f", zombie.speed), skin, "medium"));
            rightSide.add(statsTable).left().padBottom(10).row();
            rightSide.add(new Label("Attack Power (DPS): " + zombie.eatDPS, skin, "secondary")).left()
                    .row();
        } else {
            rightSide.add(new Label("Meet it in battle to reveal its stats.", skin, "secondary"
            )).left().row();
        }
        content.add(leftSide).pad(20);
        content.add(rightSide).pad(20).top().left();
        getButtonTable().pad(20);
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