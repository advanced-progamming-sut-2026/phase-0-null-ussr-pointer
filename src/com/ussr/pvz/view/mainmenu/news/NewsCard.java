package com.ussr.pvz.view.mainmenu.news;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.ussr.pvz.model.account.NewsItem;

public final class NewsCard extends Table {

    public NewsCard(
            Skin skin,
            NewsItem item,
            Runnable onOpened
    ) {
        setTouchable(Touchable.enabled);
        buildUi(skin, item);

        if (!item.isRead()) {
            addListener(
                    NewsUiFactory.clickListener(onOpened)
            );
        }
    }

    private void buildUi(
            Skin skin,
            NewsItem item
    ) {
        setBackground(skin.getDrawable(
                "image_ui_dialog_asset_inner_bkgd_10"
        ));

        pad(18f);

        Table header = createHeader(skin, item);

        add(header)
                .growX()
                .row();

        Label content = NewsUiFactory.body(
                skin,
                item.getContent()
        );

        add(content)
                .growX()
                .width(600f)
                .left()
                .padTop(12f)
                .row();
    }

    private Table createHeader(
            Skin skin,
            NewsItem item
    ) {
        Table header = new Table();

        Label title = NewsUiFactory.title(
                skin,
                item.getTitle()
        );

        header.add(title)
                .growX()
                .left();

        if (!item.isRead()) {
            header.add(NewsUiFactory.unreadBadge(skin))
                    .padLeft(10f)
                    .padRight(16f);
        }

        header.add(NewsUiFactory.date(
                        skin,
                        item.getFormattedDate()
                ))
                .right();

        return header;
    }
}