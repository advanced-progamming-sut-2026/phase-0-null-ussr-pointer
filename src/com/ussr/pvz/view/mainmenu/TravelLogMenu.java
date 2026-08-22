package com.ussr.pvz.view.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.quest.ConfigurableQuest;
import com.ussr.pvz.model.quest.CriterionProgress;
import com.ussr.pvz.service.QuestService;
import com.ussr.pvz.view.FadingMenu;
import pvz.libpvz.textures.TextureBank;

// Note: Ensure you import your TextureBank class from libPVZ here.
// import ...TextureBank;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TravelLogMenu extends FadingMenu {
    private final Skin skin;
    private final QuestService questService;
    private final TextureBank textures;

    private Table contentTable;
    private ScrollPane scrollPane;
    private Table mainLayout;

    private TextureRegionDrawable dimBackground;

    public TravelLogMenu(Skin skin) {
        this.skin = skin;
        this.questService = new QuestService();

        // Initialize TextureBank for fetching direct regions
        this.textures = new TextureBank("ATLASES", Gdx.files.local("pvz-assets"));

        createDimBackground();
        buildUI();
    }

    private void createDimBackground() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0, 0, 0, 0.7f)); // 70% opacity black
        pixmap.fill();
        dimBackground = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        pixmap.dispose();
    }

    private void buildUI() {
        mainLayout = new Table();
        mainLayout.setFillParent(true);

        // Use TextureBank for main background
        TextureRegion bgRegion = textures.region("IMAGE_UI_QUESTS_TRAVEL_LOG_FINAL");
        if (bgRegion == null) {
            bgRegion = textures.region("IMAGE_UI_QUESTS_TRAVEL_LOG_CORNER");
        }
        if (bgRegion != null) {
            mainLayout.setBackground(new TextureRegionDrawable(bgRegion));
        }

        // --- TITLE PLAQUE ---
        Table titleTable = new Table();
        TextureRegion titleRegion = textures.region("IMAGE_UI_QUESTS_QUESTS_TITLE_PLAQUE_TIER0");
        if (titleRegion != null) {
            titleTable.add(new Image(new TextureRegionDrawable(titleRegion))).padTop(10);
        } else {
            titleTable.add(new Label("Travel Log", skin, "big_outline")).padTop(10);
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
        contentTable.top().pad(10);

        scrollPane = new ScrollPane(contentTable, skin);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);

        // --- ASSEMBLE MAIN LAYOUT ---
        mainLayout.add(titleTable).top().expandX().row();
        mainLayout.add(tabsTable).top().expandX().fillX().padTop(10).row();
        mainLayout.add(scrollPane).expand().fill().pad(20);

        this.addActor(mainLayout);

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

        loadQuestPage("daily");
    }

    private void loadQuestPage(String type) {
        contentTable.clearChildren();
        List<ConfigurableQuest> quests = questService.getActiveQuestsAsList(type);

        if (quests.isEmpty()) {
            contentTable.add(new Label("No active " + type + " quests.", skin, "medium")).pad(20);
            return;
        }

        int columns = 2;
        int currentCount = 0;

        for (ConfigurableQuest quest : quests) {
            contentTable.add(buildQuestRow(quest, type)).expandX().fillX().pad(10);
            currentCount++;

            if (currentCount >= columns) {
                contentTable.row();
                currentCount = 0;
            }
        }
    }

    private static String kindLabel(String behaviorSimpleName) {
        return switch (behaviorSimpleName) {
            case "WallnutBowlingBehavior" -> "Wall-nut Bowling";
            case "BeghouledBehavior" -> "Beghouled";
            case "IZombieBehavior" -> "I, Zombie";
            case "VaseBreakerBehavior" -> "Vasebreaker";
            case "NormalBehavior" -> "Zombotany";
            default -> behaviorSimpleName;
        };
    }

    private String kindOf(Level level) {
        var behavior = level.getBehavior();
        if (behavior == null) return "Unknown";
        String simpleName = behavior.getClass().getSimpleName();
        // Couch co-op i,Zombie is the same minigame kind as solo i,Zombie —
        // group them under one panel rather than splitting into two.
        if (simpleName.equals("CouchIZombieBehavior")) {
            return "IZombieBehavior";
        }
        return simpleName;
    }

    private void loadMinigamesPage() {
        contentTable.clearChildren();
        List<Level> minigames = questService.getMinigamesAsList();

        if (minigames.isEmpty()) {
            contentTable.add(new Label("No minigames unlocked.", skin, "medium")).pad(20);
            return;
        }

        Map<String, List<Level>> byKind = new LinkedHashMap<>();
        for (Level level : minigames) {
            byKind.computeIfAbsent(kindOf(level), k -> new java.util.ArrayList<>()).add(level);
        }

        int columns = 2;
        int currentCount = 0;

        for (Map.Entry<String, List<Level>> entry : byKind.entrySet()) {
            String kind = entry.getKey();
            List<Level> levelsOfKind = entry.getValue();

            Table row = new Table();
            TextureRegion toastRegion = textures.region("IMAGE_UI_QUEST_TOAST_QUEST_TOAST_DEFAULT");
            if (toastRegion != null) {
                row.setBackground(new TextureRegionDrawable(toastRegion));
            }

            Label lblTitle = new Label(kindLabel(kind), skin, "medium");
            Label lblCount = new Label(levelsOfKind.size() + " level" + (levelsOfKind.size() == 1 ? "" : "s"),
                    skin, "default");
            Table textCol = new Table();
            textCol.add(lblTitle).left().row();
            textCol.add(lblCount).left();

            TextButton enterBtn = new TextButton("Enter", skin, "green_small");
            enterBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    loadMinigameLevelsPage(kind, levelsOfKind);
                }
            });

            row.add(textCol).expandX().left().pad(15);
            row.add(enterBtn).right().pad(15);

            contentTable.add(row).expandX().fillX().pad(10);
            currentCount++;

            if (currentCount >= columns) {
                contentTable.row();
                currentCount = 0;
            }
        }
    }

    /** Shows the individual levels within one minigame kind, with a way back to the category list. */
    private void loadMinigameLevelsPage(String kind, List<Level> levelsOfKind) {
        contentTable.clearChildren();

        Table header = new Table();
        TextButton backBtn = new TextButton("< Back", skin, "green_small");
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                loadMinigamesPage();
            }
        });
        Label headerLabel = new Label(kindLabel(kind), skin, "medium");
        header.add(backBtn).left().padRight(15);
        header.add(headerLabel).left().expandX();
        contentTable.add(header).expandX().fillX().colspan(2).padBottom(10).row();

        int columns = 2;
        int currentCount = 0;
        int index = 0;

        for (Level level : levelsOfKind) {
            Table row = new Table();
            TextureRegion toastRegion = textures.region("IMAGE_UI_QUEST_TOAST_QUEST_TOAST_DEFAULT");
            if (toastRegion != null) {
                row.setBackground(new TextureRegionDrawable(toastRegion));
            }

            boolean isCouch = level.getBehavior() != null
                    && level.getBehavior().getClass().getSimpleName().equals("CouchIZombieBehavior");
            String levelLabel;
            if (isCouch) {
                levelLabel = kindLabel(kind) + " Coop";
            } else {
                index++;
                levelLabel = kindLabel(kind) + " " + index;
            }

            Label lblTitle = new Label(levelLabel, skin, "medium");
            TextButton playBtn = new TextButton("Play", skin, "green_small");

            playBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    questService.playMinigame(level.getId());
                }
            });

            row.add(lblTitle).expandX().left().pad(15);
            row.add(playBtn).right().pad(15);

            contentTable.add(row).expandX().fillX().pad(10);
            currentCount++;

            if (currentCount >= columns) {
                contentTable.row();
                currentCount = 0;
            }
        }
    }

    private Table buildQuestRow(ConfigurableQuest quest, String type) {
        Table row = new Table();
        row.setTouchable(Touchable.enabled);

        // Fetch Background Toast from TextureBank
        String bgKey = "IMAGE_UI_QUEST_TOAST_QUEST_TOAST_DEFAULT";
        if (type.equalsIgnoreCase("epic")) {
            bgKey = "IMAGE_UI_QUEST_TOAST_QUEST_TOAST_EPIC";
        } else if (quest.isCompleted()) {
            bgKey = "IMAGE_UI_QUEST_TOAST_QUEST_TOAST_YELLOW_BANNER";
        }

        TextureRegion toastRegion = textures.region(bgKey);
        if (toastRegion != null) {
            row.setBackground(new TextureRegionDrawable(toastRegion));
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
            if (target <= 0) target = 1;
        }

        ProgressBar pb = new ProgressBar(0, target, 1, false, skin, "ingame_progress");
        pb.setValue(current);

        Label progressText = new Label(current + " / " + target, skin, "default");

        Table progressTable = new Table();
        progressTable.add(pb).width(150).left();
        progressTable.add(progressText).left().padLeft(10);

        textTable.add(progressTable).left().row();

        // Assemble row
        row.add(icon).size(64, 64).pad(15);
        row.add(textTable).expandX().fillX().left().pad(15);

        // Quest completion indicator using TextureBank Twinkle
        if (quest.isCompleted()) {
            TextureRegion twinkleRegion = textures.region("IMAGE_UI_QUEST_TOAST_QUEST_COMPLETE_TWINKLE_QUEST_COMPLETE_TWINKLE_537X453");
            if (twinkleRegion == null) {
                // Fallback to the other twinkle if the first isn't found
                twinkleRegion = textures.region("IMAGE_UI_QUEST_TOAST_QUEST_COMPLETE_TWINKLE_QUEST_COMPLETE_TWINKLE_609X575");
            }
            if (twinkleRegion != null) {
                Image check = new Image(new TextureRegionDrawable(twinkleRegion));
                row.add(check).size(40, 40).pad(15).right();
            }
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
        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = skin.get("default", Label.LabelStyle.class).font;

        if (skin.has("image_ui_dialog_asset_dialogborder", com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)) {
            dialogStyle.background = skin.getDrawable("image_ui_dialog_asset_dialogborder");
        }

        dialogStyle.stageBackground = dimBackground;

        Dialog dialog = new Dialog("", dialogStyle);

        Table content = dialog.getContentTable();
        content.pad(30);

        Label title = new Label(quest.getTitle(), skin, "big_outline");
        title.setAlignment(Align.center);
        content.add(title).expandX().fillX().padBottom(15).row();

        Label typeLabel = new Label("Type: " + quest.getType().name() + " | Priority: " + quest.getPriority().name(), skin, "secondary");
        content.add(typeLabel).center().padBottom(20).row();

        if (quest.getCriteria() != null) {
            for (CriterionProgress c : quest.getCriteria()) {
                int current = c.getCurrent();
                int target = c.getTarget() > 0 ? c.getTarget() : 1;

                Label cLbl = new Label("Task: " + c.getType(), skin, "medium");
                content.add(cLbl).left().padBottom(5).row();

                Table pTable = new Table();
                ProgressBar pb = new ProgressBar(0, target, 1, false, skin, "ingame_progress");
                pb.setValue(current);
                Label pText = new Label(current + " / " + target, skin, "default");

                pTable.add(pb).width(250).left();
                pTable.add(pText).left().padLeft(10);

                content.add(pTable).left().padBottom(15).row();
            }
        }

        if (quest.getReward() != null) {
            Table rewardTable = new Table();
            rewardTable.add(new Label("Reward: ", skin, "medium_outline"));

            String iconKey = "image_ui_generic_coin_icon_small";
            if (quest.getReward().rewardType().contains("GEM")) {
                iconKey = "image_ui_generic_gem_icon_small";
            }

            if (skin.has(iconKey, com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)) {
                rewardTable.add(new Image(skin.getDrawable(iconKey))).size(40, 40).padRight(5);
            }
            rewardTable.add(new Label(String.valueOf(quest.getReward().amount()), skin, "big"));
            content.add(rewardTable).center().padTop(10).row();
        }

        dialog.getButtonTable().pad(20);
        TextButton closeBtn = new TextButton("Close", skin, "brown");
        dialog.button(closeBtn, true);

        dialog.show(getStage());
    }
}