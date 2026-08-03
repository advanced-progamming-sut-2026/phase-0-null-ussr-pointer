package com.ussr.pvz.view.mainmenu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.ussr.pvz.controller.maincontroller.LeaderBoardController;
import com.ussr.pvz.model.leaderboard.LeaderboardColumn;
import com.ussr.pvz.model.leaderboard.LeaderboardEntry;
import com.ussr.pvz.model.leaderboard.SortDirection;
import java.util.List;

public final class LeaderBoardMenu extends Table {
    private final Skin skin;
    private final LeaderBoardController controller;
    private final Table rows;

    private SelectBox<LeaderboardColumn> columnSelect;
    private SelectBox<SortDirection> directionSelect;

    public LeaderBoardMenu(Skin skin) {
        this.skin = skin;
        this.controller = new LeaderBoardController();
        this.rows = new Table();

        setFillParent(true);

        buildUi();
        refreshRows();
    }

    private void buildUi() {
        Table panel = new Table();

        panel.setBackground(skin.getDrawable(
                "image_ui_dialog_asset_dialogborder_10"
        ));

        panel.pad(28f);

        Label title = new Label(
                "Leaderboard",
                skin,
                "big_outline"
        );

        panel.add(title)
                .growX()
                .center()
                .padBottom(18f)
                .row();

        panel.add(createSortControls())
                .growX()
                .padBottom(12f)
                .row();

        ScrollPane scrollPane =
                new ScrollPane(rows, skin);

        scrollPane.setFadeScrollBars(false);
        scrollPane.setOverscroll(false, false);
        scrollPane.setScrollingDisabled(true, false);

        panel.add(scrollPane)
                .width(900f)
                .height(470f)
                .grow();

        add(panel)
                .width(1000f)
                .height(620f);
    }

    private Table createSortControls() {
        Table controls = new Table();

        columnSelect = new SelectBox<>(skin);
        columnSelect.setItems(LeaderboardColumn.values());
        columnSelect.setSelected(LeaderboardColumn.SCORE);

        directionSelect = new SelectBox<>(skin);
        directionSelect.setItems(SortDirection.values());
        directionSelect.setSelected(SortDirection.DESCENDING);

        controls.add(new Label(
                "Order by:",
                skin,
                "medium"
        )).padRight(8f);

        controls.add(columnSelect)
                .width(220f)
                .padRight(18f);

        controls.add(directionSelect)
                .width(190f);

        columnSelect.addListener(listener(this::refreshRows));
        directionSelect.addListener(listener(this::refreshRows));

        return controls;
    }

    private void refreshRows() {
        rows.clearChildren();
        rows.top();

        addHeaderRow();

        boolean ascending =
                directionSelect.getSelected()
                        == SortDirection.ASCENDING;

        List<LeaderboardEntry> entries =
                controller.getEntries(
                                columnSelect.getSelected(),
                        ascending
                );

        if (entries.isEmpty()) {
            addEmptyState();
            return;
        }

        for (int index = 0; index < entries.size(); index++) {
            addEntryRow(index + 1, entries.get(index));
        }
    }

    private void addHeaderRow() {
        Table header = new Table();

        header.setBackground(skin.newDrawable(
                "white-pixel",
                new Color(0.28f, 0.45f, 0.22f, 1f)
        ));

        addCell(header, "#", 55f);
        addCell(header, "Username", 180f);
        addCell(header, "Progress", 120f);
        addCell(header, "Minigames", 125f);
        addCell(header, "Daily", 100f);
        addCell(header, "Other", 100f);
        addCell(header, "MooPoints", 140f);

        rows.add(header)
                .growX()
                .height(48f)
                .padBottom(6f)
                .row();
    }

    private void addCell(
            Table row,
            String text,
            float width
    ) {
        row.add(new Label(
                text,
                skin,
                "medium_outline"
        )).width(width).center();
    }

    private void addEntryRow(
            int rank,
            LeaderboardEntry entry
    ) {
        Table row = new Table();

        row.setBackground(skin.newDrawable(
                "image_ui_dialog_asset_inner_bkgd_10",
                rowColor(rank)
        ));

        addValue(row, String.valueOf(rank), 55f);
        addValue(row, entry.username(), 180f);
        addValue(
                row,
                entry.chapter() + "-" + entry.level(),
                120f
        );
        addValue(row, String.valueOf(entry.minigames()), 125f);
        addValue(row, String.valueOf(entry.dailyQuests()), 100f);
        addValue(row, String.valueOf(entry.otherQuests()), 100f);
        addValue(row, String.valueOf(entry.score()), 140f);

        rows.add(row)
                .growX()
                .height(54f)
                .padBottom(6f)
                .row();
    }

    private void addValue(
            Table row,
            String text,
            float width
    ) {
        Label label = new Label(text, skin, "default");

        label.setColor(new Color(
                0.25f,
                0.14f,
                0.07f,
                1f
        ));

        row.add(label).width(width).center();
    }

    private Color rowColor(int rank) {
        return switch (rank) {
            case 1 -> new Color(1f, 0.82f, 0.35f, 1f);
            case 2 -> new Color(0.82f, 0.84f, 0.86f, 1f);
            case 3 -> new Color(0.78f, 0.55f, 0.34f, 1f);
            default -> new Color(0.86f, 0.82f, 0.68f, 1f);
        };
    }

    private void addEmptyState() {
        rows.add(new Label(
                "No accounts found.",
                skin,
                "medium_outline"
        )).padTop(100f);
    }

    private ChangeListener listener(Runnable action) {
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
}
