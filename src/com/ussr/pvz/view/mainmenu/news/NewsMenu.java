package com.ussr.pvz.view.mainmenu.news;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.ussr.pvz.controller.maincontroller.NewsController;
import com.ussr.pvz.model.account.NewsItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.badlogic.gdx.utils.Align;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

public final class NewsMenu extends Table {
    private enum NewsTab {
        UNREAD,
        ALL
    }

    private final Skin skin;
    private final NewsController controller;
    private final Runnable onClose;
    private final Table newsList;

    private TextButton unreadTab;
    private TextButton allTab;
    private NewsTab selectedTab;

    public NewsMenu(Skin skin, Runnable onClose) {
        this.skin = skin;
        this.onClose = onClose;
        this.controller = new NewsController();
        this.newsList = new Table();
        this.selectedTab = NewsTab.UNREAD;

        buildUi();
        refreshNews();
        animateOpen();
    }

    private void buildUi() {
        setTransform(true);

        setBackground(skin.getDrawable(
                "image_ui_dialog_asset_dialogborder_10"
        ));

        pad(28f);

        addHeader();
        addTabs();
        addNewsScrollPane();
    }

    private void addHeader() {
        Label title = new Label(
                "News",
                skin,
                "big_outline"
        );

        TextButton closeButton = new TextButton(
                "X",
                skin,
                "brown"
        );

        closeButton.addListener(
                NewsUiFactory.listener(this::close)
        );

        add(title)
                .growX()
                .left()
                .padBottom(18f);

        add(closeButton)
                .size(52f)
                .right()
                .padBottom(18f)
                .row();
    }

    private void addTabs() {
        unreadTab = new TextButton(
                "Unread",
                skin,
                "green"
        );

        allTab = new TextButton(
                "All Messages",
                skin,
                "brown"
        );

        unreadTab.addListener(
                NewsUiFactory.listener(
                        () -> switchTab(NewsTab.UNREAD)
                )
        );

        allTab.addListener(
                NewsUiFactory.listener(
                        () -> switchTab(NewsTab.ALL)
                )
        );

        add(unreadTab)
                .width(220f)
                .height(52f)
                .padRight(8f);

        add(allTab)
                .width(220f)
                .height(52f)
                .padBottom(12f)
                .row();
    }

    private void addNewsScrollPane() {
        newsList.top();
        newsList.defaults().growX();

        ScrollPane scrollPane =
                new ScrollPane(newsList, skin);

        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setOverscroll(false, false);

        add(scrollPane)
                .colspan(2)
                .grow()
                .minHeight(400f)
                .row();
    }

    private void switchTab(NewsTab target) {
        if (target == selectedTab) {
            return;
        }

        selectedTab = target;
        refreshNews();
        updateTabStyles();
    }

    private void refreshNews() {
        newsList.clearChildren();

        List<NewsItem> items =
                new ArrayList<>(getSelectedNews());

        Collections.reverse(items);

        if (items.isEmpty()) {
            showEmptyState();
            return;
        }

        for (NewsItem item : items) {
            addNewsCard(item);
        }
    }

    private List<NewsItem> getSelectedNews() {
        if (selectedTab == NewsTab.UNREAD) {
            return controller.getUnreadNews();
        }

        return controller.getAllNews();
    }

    private void addNewsCard(NewsItem item) {
        NewsCard card = new NewsCard(
                skin,
                item,
                () -> openNews(item)
        );

        newsList.add(card)
                .growX()
                .padRight(8f)
                .padBottom(10f)
                .row();
    }

    private void openNews(NewsItem item) {
        controller.markAsRead(item);
        refreshNews();
    }

    private void showEmptyState() {
        String message = selectedTab == NewsTab.UNREAD
                ? "No unread messages. Your lawn is peaceful!"
                : "Your inbox is currently empty.";

        Label label = NewsUiFactory.body(skin, message);

        label.setWrap(true);
        label.setAlignment(Align.center);

        newsList.add(label)
                .width(600f)
                .padTop(140f)
                .padBottom(40f)
                .center()
                .row();
    }

    private void updateTabStyles() {
        boolean unreadSelected =
                selectedTab == NewsTab.UNREAD;

        unreadTab.setStyle(skin.get(
                unreadSelected ? "green" : "brown",
                TextButton.TextButtonStyle.class
        ));

        allTab.setStyle(skin.get(
                unreadSelected ? "brown" : "green",
                TextButton.TextButtonStyle.class
        ));
    }

    private void animateOpen() {
        getColor().a = 0f;
        setScale(0.92f);

        addAction(parallel(
                fadeIn(0.2f, Interpolation.fade),
                scaleTo(
                        1f,
                        1f,
                        0.22f,
                        Interpolation.smooth
                )
        ));
    }

    private void close() {
        clearActions();

        addAction(sequence(
                parallel(
                        fadeOut(0.15f, Interpolation.fade),
                        scaleTo(
                                0.92f,
                                0.92f,
                                0.15f,
                                Interpolation.smooth
                        )
                ),
                run(onClose)
        ));
    }
}