package com.ussr.pvz.view.mainmenu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.quest.ConfigurableQuest;
import com.ussr.pvz.model.quest.CriterionProgress;
import com.ussr.pvz.service.QuestService;
import com.ussr.pvz.view.FadingMenu;

import java.util.List;

public class TravelLogMenu extends FadingMenu {
    private final Skin skin;
    private final QuestService questService;

    private Table contentTable;
    private ScrollPane scrollPane;
    private Table mainLayout;

    public TravelLogMenu(Skin skin) {
        this.skin = skin;
        this.questService = new QuestService();
        buildUI();
    }

    private void buildUI() {
        mainLayout = new Table();
        mainLayout.setFillParent(true);
        // Fallback to a solid background or general UI background if present in skin
        if (skin.has("image_ui_quests_travel_log_corner", com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)) {
            mainLayout.setBackground(skin.getDrawable("image_ui_quests_travel_log_corner"));
        }

        // --- TABS BORDER/HEADER ---
        Table tabsTable = new Table();

        TextButton btnDaily = new TextButton("Daily", skin, "green");
        TextButton btnChallenge = new TextButton("Challenge", skin, "green");
        TextButton btnEpic = new TextButton("Epic", skin, "purple");
        TextButton btnMini = new TextButton("Minigames", skin, "brown");

        tabsTable.add(btnDaily).pad(10).uniformX().fillX();
        tabsTable.add(btnChallenge).pad(10).uniformX().fillX();
        tabsTable.add(btnEpic).pad(10).uniformX().fillX();
        tabsTable.add(btnMini).pad(10).uniformX().fillX();

        // --- CONTENT AREA ---
        contentTable = new Table();
        contentTable.top(); // Align items to the top of the scroll pane

        scrollPane = new ScrollPane(contentTable, skin);
        scrollPane.setScrollingDisabled(true, false); // Only allow vertical scrolling
        scrollPane.setFadeScrollBars(false);

        // --- ASSEMBLE MAIN LAYOUT ---
        mainLayout.add(tabsTable).top().expandX().fillX().padTop(20).row();
        mainLayout.add(scrollPane).expand().fill().pad(20);

        // Assuming FadingMenu has a Stage, add the main layout to it
        this.addActor(mainLayout);
        // Note: You may need to adapt this depending on how FadingMenu exposes its Stage.

        // --- TAB LISTENERS ---
        btnDaily.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { loadQuestPage("daily"); }
        });
        btnChallenge.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { loadQuestPage("challenge"); }
        });
        btnEpic.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { loadQuestPage("epic"); }
        });
        btnMini.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { loadMinigamesPage(); }
        });

        // Load default tab
        loadQuestPage("daily");
    }

    private void loadQuestPage(String type) {
        contentTable.clearChildren();
        List<ConfigurableQuest> quests = questService.getActiveQuestsAsList(type);

        if (quests.isEmpty()) {
            contentTable.add(new Label("No active " + type + " quests.", skin, "medium")).pad(20);
            return;
        }

        for (ConfigurableQuest quest : quests) {
            contentTable.add(buildQuestRow(quest)).expandX().fillX().padBottom(15).row();
        }
    }

    private void loadMinigamesPage() {
        contentTable.clearChildren();
        List<Level> minigames = questService.getMinigamesAsList();

        if (minigames.isEmpty()) {
            contentTable.add(new Label("No minigames unlocked.", skin, "medium")).pad(20);
            return;
        }

        for (Level level : minigames) {
            Table row = new Table();
            if (skin.has("image_ui_quests_quest_panel_default", com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)) {
                row.setBackground(skin.getDrawable("image_ui_quests_quest_panel_default"));
            }

            Label lblTitle = new Label("Minigame: " + level.getId(), skin, "medium");
            TextButton playBtn = new TextButton("Play", skin, "green_small");

            playBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    questService.playMinigame(level.getId());
                }
            });

            row.add(lblTitle).expandX().left().pad(15);
            row.add(playBtn).right().pad(15);

            contentTable.add(row).expandX().fillX().padBottom(15).row();
        }
    }

    private Table buildQuestRow(ConfigurableQuest quest) {
        Table row = new Table();
        row.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);

        // Setup row background based on atlas
        if (skin.has("image_ui_quests_quest_panel_default", com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)) {
            row.setBackground(skin.getDrawable("image_ui_quests_quest_panel_default"));
        }

        // Icon
        Image icon = new Image();
        if (skin.has("image_ui_quests_quest_icon_brown", com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)) {
            icon.setDrawable(skin.getDrawable("image_ui_quests_quest_icon_brown"));
        }

        // Text block
        Table textTable = new Table();
        Label titleLabel = new Label(quest.getTitle(), skin, "medium");
        textTable.add(titleLabel).left().padBottom(5).row();

        // Target / Progress Bar tracking logic (grabbing the first criteria)
        int current = 0;
        int target = 1;
        if (quest.getCriteria() != null && !quest.getCriteria().isEmpty()) {
            CriterionProgress c = quest.getCriteria().get(0);
            current = c.getCurrent();
            target = c.getTarget();
            if (target <= 0) target = 1; // Prevent division by zero visually
        }

        ProgressBar pb = new ProgressBar(0, target, 1, false, skin, "ingame_progress");
        pb.setValue(current);

        Label progressText = new Label(current + " / " + target, skin, "default");

        Table progressTable = new Table();
        progressTable.add(pb).width(200).left();
        progressTable.add(progressText).left().padLeft(10);

        textTable.add(progressTable).left().row();

        // Assemble row
        row.add(icon).size(64, 64).pad(15);
        row.add(textTable).expandX().fillX().left().pad(15);

        // Quest completion indicator
        if (quest.isCompleted()) {
            Image check = new Image(skin.getDrawable("image_ui_generic_check_mark_anim_check_mark_anim_102x102"));
            row.add(check).size(40, 40).pad(15).right();
        }

        // Overlay listener
        row.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showQuestDetailsOverlay(quest);
            }
        });

        return row;
    }

    private void showQuestDetailsOverlay(ConfigurableQuest quest) {
        // 1. Manually build a WindowStyle because the skin file lacks a "default" one
        Window.WindowStyle dialogStyle = new Window.WindowStyle();

        // Borrow the font from your default LabelStyle so the Dialog doesn't crash missing a font
        dialogStyle.titleFont = skin.get("default", Label.LabelStyle.class).font;

        // Apply your custom dialog border directly to the style
        if (skin.has("image_ui_dialog_asset_dialogborder", com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)) {
            dialogStyle.background = skin.getDrawable("image_ui_dialog_asset_dialogborder");
        }

        // 2. Instantiate the Dialog using our custom style instead of searching the skin
        Dialog dialog = new Dialog("", dialogStyle);

        Table content = dialog.getContentTable();
        content.pad(20);

        Label title = new Label(quest.getTitle(), skin, "big_outline");
        title.setAlignment(Align.center);
        content.add(title).expandX().fillX().padBottom(20).row();

        Label typeLabel = new Label("Type: " + quest.getType().name() + " | Priority: " + quest.getPriority().name(), skin, "secondary");
        content.add(typeLabel).left().padBottom(15).row();

        // Criteria dump
        if (quest.getCriteria() != null) {
            for (CriterionProgress c : quest.getCriteria()) {
                Label cLbl = new Label("Task: " + c.getType() + " (" + c.getCurrent() + "/" + c.getTarget() + ")", skin, "medium");
                content.add(cLbl).left().padBottom(5).row();
            }
        }

        // Reward section
        if (quest.getReward() != null) {
            Table rewardTable = new Table();
            rewardTable.add(new Label("Reward: ", skin, "medium_outline"));

            // Map the reward icon dynamically
            String iconKey = "image_ui_generic_coin_icon_small";
            if (quest.getReward().rewardType().contains("GEM")) {
                iconKey = "image_ui_generic_gem_icon_small";
            }

            if (skin.has(iconKey, com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)) {
                rewardTable.add(new Image(skin.getDrawable(iconKey))).size(30, 30).padRight(5);
            }
            rewardTable.add(new Label(String.valueOf(quest.getReward().amount()), skin, "medium"));
            content.add(rewardTable).left().padTop(10).row();
        }

        // Action Buttons
        dialog.getButtonTable().pad(15);
        TextButton closeBtn = new TextButton("Close", skin, "brown");
        dialog.button(closeBtn, true);

        // Display the dialog
        dialog.show(getStage());
    }
}