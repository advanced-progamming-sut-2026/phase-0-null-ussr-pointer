package com.ussr.pvz.view.mainmenu.profile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.ussr.pvz.notification.NotificationCenter;

public final class ProfileUiFactory {
    private static final Color TITLE_COLOR =
            new Color(0.22f, 0.11f, 0.05f, 1f);

    private static final Color TEXT_COLOR =
            new Color(0.42f, 0.25f, 0.12f, 1f);

    private ProfileUiFactory() {
    }

    public static Table card(Skin skin, String title) {
        Table card = new Table();

        card.setBackground(skin.getDrawable(
                "image_ui_dialog_asset_inner_bkgd_10"
        ));

        card.pad(18f);
        card.add(sectionTitle(skin, title))
                .colspan(2)
                .left()
                .padBottom(15f)
                .row();

        return card;
    }

    public static Label sectionTitle(
            Skin skin,
            String text
    ) {
        Label.LabelStyle style = new Label.LabelStyle(
                skin.get("medium", Label.LabelStyle.class)
        );

        style.fontColor = new Color(TITLE_COLOR);
        return new Label(text, style);
    }

    public static Label cardText(
            Skin skin,
            String text
    ) {
        Label.LabelStyle style = new Label.LabelStyle(
                skin.get("default", Label.LabelStyle.class)
        );

        style.fontColor = new Color(TEXT_COLOR);
        return new Label(text, style);
    }

    public static ChangeListener listener(Runnable action) {
        return new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        };
    }

    public static boolean showResult(String result) {
        boolean successful = result != null
                && result.endsWith("successfully");

        if (successful) {
            NotificationCenter.success(result);
        } else {
            NotificationCenter.error(result);
        }

        return successful;
    }
}