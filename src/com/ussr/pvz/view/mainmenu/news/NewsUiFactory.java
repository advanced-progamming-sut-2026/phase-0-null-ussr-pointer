package com.ussr.pvz.view.mainmenu.news;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public final class NewsUiFactory {
    private static final Color TITLE_COLOR =
            new Color(0.22f, 0.11f, 0.05f, 1f);

    private static final Color TEXT_COLOR =
            new Color(0.42f, 0.25f, 0.12f, 1f);

    private static final Color DATE_COLOR =
            new Color(0.52f, 0.35f, 0.18f, 1f);

    private static final Color UNREAD_COLOR =
            new Color(0.15f, 0.48f, 0.16f, 1f);

    private NewsUiFactory() {
    }

    public static Label title(Skin skin, String text) {
        return createLabel(
                skin,
                "medium",
                text,
                TITLE_COLOR
        );
    }

    public static Label body(Skin skin, String text) {
        Label label = createLabel(
                skin,
                "default",
                text,
                TEXT_COLOR
        );

        label.setWrap(true);
        return label;
    }

    public static Label date(Skin skin, String text) {
        return createLabel(
                skin,
                "default",
                text,
                DATE_COLOR
        );
    }

    public static Label unreadBadge(Skin skin) {
        return createLabel(
                skin,
                "default",
                "NEW",
                UNREAD_COLOR
        );
    }

    private static Label createLabel(
            Skin skin,
            String styleName,
            String text,
            Color color
    ) {
        Label.LabelStyle style = new Label.LabelStyle(
                skin.get(styleName, Label.LabelStyle.class)
        );

        style.fontColor = new Color(color);
        return new Label(text, style);
    }

    public static ChangeListener listener(Runnable action) {
        return new ChangeListener() {
            @Override
            public void changed(
                    ChangeEvent event,
                    Actor actor
            ) {
                action.run();
            }
        };
    }

    public static ClickListener clickListener(
            Runnable action
    ) {
        return new ClickListener() {
            @Override
            public void clicked(
                    com.badlogic.gdx.scenes.scene2d.InputEvent event,
                    float x,
                    float y
            ) {
                action.run();
            }
        };
    }
}