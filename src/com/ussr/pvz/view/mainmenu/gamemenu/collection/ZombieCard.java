package com.ussr.pvz.view.mainmenu.gamemenu.collection;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.ussr.pvz.service.CollectionService.ZombieData;
import com.ussr.pvz.view.animation.ZombiePamActor;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

/** Horizontal collection card for a discovered or unknown zombie. */
public final class ZombieCard extends Table {
    private static final Color TEXT_DARK =
            new Color(0.16f, 0.12f, 0.08f, 1f);
    private static final Color TEXT_MUTED =
            new Color(0.34f, 0.29f, 0.23f, 1f);
    private static final Color DISCOVERED_COLOR =
            new Color(0.18f, 0.42f, 0.12f, 1f);
    private static final Color UNKNOWN_COLOR =
            new Color(0.55f, 0.12f, 0.10f, 1f);

    public ZombieCard(
            ZombieData zombie,
            Skin skin,
            TextureBank textures,
            PamPlayer pamPlayer,
            Runnable onClick
    ) {
        setTouchable(Touchable.enabled);
        pad(5f);

        if (skin.has(
                "image_ui_dialog_asset_inner_bkgd_10",
                Drawable.class
        )) {
            setBackground(skin.getDrawable(
                    "image_ui_dialog_asset_inner_bkgd_10"
            ));
        }

        Table body = new Table();
        body.pad(4f);
        body.add(buildPreview(zombie, textures, pamPlayer))
                .width(Value.percentWidth(0.42f, body))
                .growY()
                .padRight(6f);
        body.add(buildInformation(zombie, skin))
                .grow()
                .left();
        add(body).grow();

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onClick != null) {
                    onClick.run();
                }
            }
        });
    }

    private Stack buildPreview(
            ZombieData zombie,
            TextureBank textures,
            PamPlayer pamPlayer
    ) {
        Stack preview = new Stack();

        TextureRegion floor = textures.region(
                "image_ui_dialog_asset_dialogtexture"
        );
        if (floor != null) {
            Image floorImage = new Image(floor);
            floorImage.setScaling(Scaling.fit);
            if (!zombie.encountered) {
                floorImage.setColor(Color.DARK_GRAY);
            }
            preview.add(floorImage);
        }

        if (zombie.encountered) {
            ZombiePamActor animation = new ZombiePamActor(
                    pamPlayer,
                    zombie.pamPath
            );
            animation.setPamScale(0.29f);
            animation.setOffsetY(-12f);
            animation.setTouchable(Touchable.disabled);
            preview.add(animation);
        }

        return preview;
    }

    private Table buildInformation(ZombieData zombie, Skin skin) {
        Table information = new Table();
        information.left().top();

        Label name = new Label(
                zombie.encountered ? zombie.name : "Unknown zombie",
                skin,
                "default"
        );
        name.setFontScale(0.66f);
        name.setAlignment(Align.left);
        name.setEllipsis("...");
        name.setColor(TEXT_DARK);
        information.add(name).growX().left().row();

        Label status = new Label(
                zombie.encountered ? "DISCOVERED" : "NOT ENCOUNTERED",
                skin,
                "default"
        );
        status.setFontScale(0.52f);
        status.setColor(
                zombie.encountered
                        ? DISCOVERED_COLOR
                        : UNKNOWN_COLOR
        );
        information.add(status).growX().left().padTop(2f).row();
        information.add().growY().row();

        if (zombie.encountered) {
            addStat(information, "Toughness", zombie.hitpoints, skin);
            addStat(
                    information,
                    "Speed",
                    String.format("%.2f", zombie.speed),
                    skin
            );
        } else {
            Label hint = new Label("Meet it in battle\nto reveal its stats", skin, "default");
            hint.setFontScale(0.48f);
            hint.setColor(TEXT_MUTED);
            hint.setAlignment(Align.left);
            information.add(hint).growX().left().row();
        }

        return information;
    }

    private void addStat(
            Table information,
            String title,
            Object value,
            Skin skin
    ) {
        Label stat = new Label(
                title + ": " + value,
                skin,
                "default"
        );
        stat.setFontScale(0.52f);
        stat.setColor(TEXT_MUTED);
        information.add(stat).growX().left().padTop(1f).row();
    }
}