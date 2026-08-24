package com.ussr.pvz.view.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.quest.ConfigurableQuest;
import com.ussr.pvz.model.quest.CriterionProgress;
import com.ussr.pvz.service.QuestService;
import com.ussr.pvz.view.FadingMenu;
import pvz.libpvz.textures.TextureBank;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TravelLogMenu extends FadingMenu {

    // ── Atlas keys (SettingMenu panel / tab textures) ─────────────────────────

    private static final String CONTENT_PANEL = "IMAGE_UI_SETTINGS_CONTENT_PANEL";
    private static final String ROW_LARGE     = "IMAGE_UI_SETTINGS_ROW_LARGE";
    private static final String TAB_DARK      = "IMAGE_UI_SETTINGS_TAB_DARK";
    private static final String TAB_GREEN     = "IMAGE_UI_SETTINGS_TAB_GREEN";

    // Tab icons — one per tab (reuse settings icons; swap for quest-specific ones when available)
    private static final String ICON_DAILY     = "IMAGE_UI_SETTINGS_ICON_GAMEPLAY";
    private static final String ICON_CHALLENGE = "IMAGE_UI_SETTINGS_ICON_DISPLAY";
    private static final String ICON_EPIC      = "IMAGE_UI_SETTINGS_ICON_ACCESSIBILITY";
    private static final String ICON_MINI      = "IMAGE_UI_SETTINGS_ICON_AUDIO";

    // Quest-specific atlas keys (original code)
    private static final String BG_KEY        = "IMAGE_UI_QUESTS_TRAVEL_LOG_FINAL";
    private static final String BG_FALLBACK    = "IMAGE_UI_QUESTS_TRAVEL_LOG_CORNER";
    private static final String TITLE_PLAQUE  = "IMAGE_UI_QUESTS_QUESTS_TITLE_PLAQUE_TIER0";

    // ── Dependencies ──────────────────────────────────────────────────────────

    private final Skin        skin;
    private final QuestService questService;
    private final TextureBank textures;

    // ── Widgets ───────────────────────────────────────────────────────────────

    private Table      contentTable;
    private ScrollPane scrollPane;

    /** All four tab buttons so we can toggle checked state. */
    private final Button[] tabs = new Button[4];

    private TextureRegionDrawable dimBackground;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────

    public TravelLogMenu(Skin skin) {
        this.skin         = skin;
        this.questService = new QuestService();
        this.textures     = new TextureBank("ATLASES", Gdx.files.local("pvz-assets"));

        createDimBackground();
        buildUI();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI construction
    // ─────────────────────────────────────────────────────────────────────────

    private void createDimBackground() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0f, 0f, 0f, 0.7f));
        pixmap.fill();
        dimBackground = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        pixmap.dispose();
    }

    private void buildUI() {
        Table mainLayout = new Table();
        mainLayout.setFillParent(true);

        // ── Background ────────────────────────────────────────────────────────
        TextureRegion bgRegion = textures.region(BG_KEY);
        if (bgRegion == null) bgRegion = textures.region(BG_FALLBACK);
        if (bgRegion != null) mainLayout.setBackground(new TextureRegionDrawable(bgRegion));

        // ── Title plaque ──────────────────────────────────────────────────────
        Table titleTable = new Table();
        TextureRegion titleRegion = textures.region(TITLE_PLAQUE);
        if (titleRegion != null) {
            titleTable.add(new Image(new TextureRegionDrawable(titleRegion))).padTop(10f);
        } else {
            titleTable.add(new Label("Travel Log", skin, "big_outline")).padTop(10f);
        }

        // ── Tab bar ───────────────────────────────────────────────────────────
        // Vertical tab list inside a CONTENT_PANEL card, matching SettingMenu's
        // navigation column exactly: TAB_DARK up, TAB_GREEN checked/down,
        // icon on the left, label on the right.
        Table tabColumn = buildTabColumn();

        // ── Content area ──────────────────────────────────────────────────────
        contentTable = new Table();
        contentTable.top().pad(10f);

        scrollPane = new ScrollPane(contentTable, skin);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);

        // Wrap scroll pane in a ROW_LARGE card so it sits on its own panel
        Table scrollCard = new Table();
        scrollCard.setBackground(panelDrawable(ROW_LARGE, 24, 24, 24, 24));
        scrollCard.add(scrollPane).grow().pad(8f);

        // ── Centre panel: tab column + content ────────────────────────────────
        Table centrePanel = new Table();
        centrePanel.setBackground(panelDrawable(CONTENT_PANEL, 28, 28, 28, 28));
        centrePanel.pad(14f, 18f, 14f, 18f);
        centrePanel.add(tabColumn).width(200f).growY().padRight(18f);
        centrePanel.add(scrollCard).grow();

        // ── Assemble ──────────────────────────────────────────────────────────
        mainLayout.add(titleTable).top().expandX().row();
        mainLayout.add(centrePanel).expand().fill().pad(20f);

        this.addActor(mainLayout);

        // Default tab
        selectTab(0);
        loadQuestPage("daily");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tab column
    // ─────────────────────────────────────────────────────────────────────────

    private Table buildTabColumn() {
        Table nav = new Table();
        nav.top().padTop(8f);

        tabs[0] = tab("Daily",      ICON_DAILY,     0, () -> loadQuestPage("daily"));
        tabs[1] = tab("Challenge",  ICON_CHALLENGE, 1, () -> loadQuestPage("challenge"));
        tabs[2] = tab("Epic",       ICON_EPIC,      2, () -> loadQuestPage("epic"));
        tabs[3] = tab("Minigames",  ICON_MINI,      3, this::loadMinigamesPage);

        for (Button t : tabs) {
            nav.add(t).width(185f).height(68f).padBottom(8f).row();
        }
        nav.add().growY(); // push tabs to the top
        return nav;
    }

    /**
     * Builds one tab button exactly like SettingMenu:
     * TAB_DARK background by default, TAB_GREEN when checked/pressed,
     * atlas icon on the left, medium_outline label on the right.
     */
    private Button tab(String text, String iconKey, int index, Runnable action) {
        Button.ButtonStyle style = new Button.ButtonStyle();
        style.up      = panelDrawable(TAB_DARK,  24, 24, 24, 24);
        style.down    = panelDrawable(TAB_GREEN, 24, 24, 24, 24);
        style.checked = panelDrawable(TAB_GREEN, 24, 24, 24, 24);

        Button b = new Button(style);

        // Icon
        TextureRegion iconRegion = textures.region(iconKey);
        if (iconRegion != null) {
            Image icon = new Image(iconRegion);
            icon.setScaling(Scaling.fit);
            b.add(icon).size(40f).padLeft(10f).padRight(8f);
        } else {
            b.add().width(10f); // small indent if icon is missing
        }

        // Label
        Label lbl = new Label(text, skin, "medium_outline");
        lbl.setAlignment(Align.left);
        b.add(lbl).growX().left().padRight(8f);

        b.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                selectTab(index);
                action.run();
            }
        });

        return b;
    }

    /** Marks one tab as checked and unchecks the rest. */
    private void selectTab(int index) {
        for (int i = 0; i < tabs.length; i++) {
            tabs[i].setChecked(i == index);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Content pages (unchanged logic, same skin usage as before)
    // ─────────────────────────────────────────────────────────────────────────

    private void loadQuestPage(String type) {
        contentTable.clearChildren();
        List<ConfigurableQuest> quests = questService.getActiveQuestsAsList(type);

        if (quests.isEmpty()) {
            contentTable.add(new Label("No active " + type + " quests.", skin, "medium")).pad(20f);
            return;
        }

        int columns     = 2;
        int currentCount = 0;

        for (ConfigurableQuest quest : quests) {
            contentTable.add(buildQuestRow(quest, type)).expandX().fillX().pad(10f);
            if (++currentCount >= columns) {
                contentTable.row();
                currentCount = 0;
            }
        }
    }

    private void loadMinigamesPage() {
        contentTable.clearChildren();
        List<Level> minigames = questService.getMinigamesAsList();

        if (minigames.isEmpty()) {
            contentTable.add(new Label("No minigames unlocked.", skin, "medium")).pad(20f);
            return;
        }

        Map<String, List<Level>> byKind = new LinkedHashMap<>();
        for (Level level : minigames) {
            byKind.computeIfAbsent(kindOf(level), k -> new java.util.ArrayList<>()).add(level);
        }

        int columns     = 2;
        int currentCount = 0;

        for (Map.Entry<String, List<Level>> entry : byKind.entrySet()) {
            String      kind         = entry.getKey();
            List<Level> levelsOfKind = entry.getValue();

            Table row = new Table();
            TextureRegion toastRegion = textures.region("IMAGE_UI_QUEST_TOAST_QUEST_TOAST_DEFAULT");
            if (toastRegion != null) row.setBackground(new TextureRegionDrawable(toastRegion));

            Label lblTitle = new Label(kindLabel(kind), skin, "medium");
            Label lblCount = new Label(
                    levelsOfKind.size() + " level" + (levelsOfKind.size() == 1 ? "" : "s"),
                    skin, "default"
            );
            Table textCol = new Table();
            textCol.add(lblTitle).left().row();
            textCol.add(lblCount).left();

            TextButton enterBtn = new TextButton("Enter", skin, "green_small");
            enterBtn.addListener(new ClickListener() {
                @Override public void clicked(InputEvent event, float x, float y) {
                    loadMinigameLevelsPage(kind, levelsOfKind);
                }
            });

            row.add(textCol).expandX().left().pad(15f);
            row.add(enterBtn).right().pad(15f);

            contentTable.add(row).expandX().fillX().pad(10f);
            if (++currentCount >= columns) {
                contentTable.row();
                currentCount = 0;
            }
        }
    }

    private void loadMinigameLevelsPage(String kind, List<Level> levelsOfKind) {
        contentTable.clearChildren();

        // Back button — uses skin (same as before) but placed inside a ROW_LARGE header card
        Table header = new Table();
        header.setBackground(panelDrawable(ROW_LARGE, 24, 24, 24, 24));
        header.pad(6f, 14f, 6f, 14f);

        TextButton backBtn = new TextButton("< Back", skin, "green_small");
        backBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                loadMinigamesPage();
            }
        });
        Label headerLabel = new Label(kindLabel(kind), skin, "medium");
        header.add(backBtn).left().padRight(15f);
        header.add(headerLabel).left().expandX();
        contentTable.add(header).expandX().fillX().colspan(2).padBottom(10f).row();

        int columns     = 2;
        int currentCount = 0;
        int index       = 0;

        for (Level level : levelsOfKind) {
            Table row = new Table();
            TextureRegion toastRegion = textures.region("IMAGE_UI_QUEST_TOAST_QUEST_TOAST_DEFAULT");
            if (toastRegion != null) row.setBackground(new TextureRegionDrawable(toastRegion));

            boolean isCouch = level.getBehavior() != null
                    && level.getBehavior().getClass().getSimpleName().equals("CouchIZombieBehavior");
            String levelLabel = isCouch
                    ? kindLabel(kind) + " Coop"
                    : kindLabel(kind) + " " + (++index);

            Label lblTitle = new Label(levelLabel, skin, "medium");
            TextButton playBtn = new TextButton("Play", skin, "green_small");
            playBtn.addListener(new ClickListener() {
                @Override public void clicked(InputEvent event, float x, float y) {
                    questService.playMinigame(level.getId());
                }
            });

            row.add(lblTitle).expandX().left().pad(15f);
            row.add(playBtn).right().pad(15f);

            contentTable.add(row).expandX().fillX().pad(10f);
            if (++currentCount >= columns) {
                contentTable.row();
                currentCount = 0;
            }
        }
    }

    private Table buildQuestRow(ConfigurableQuest quest, String type) {
        Table row = new Table();
        row.setTouchable(Touchable.enabled);

        String bgKey = "IMAGE_UI_QUEST_TOAST_QUEST_TOAST_DEFAULT";
        if (type.equalsIgnoreCase("epic"))  bgKey = "IMAGE_UI_QUEST_TOAST_QUEST_TOAST_EPIC";
        else if (quest.isCompleted())       bgKey = "IMAGE_UI_QUEST_TOAST_QUEST_TOAST_YELLOW_BANNER";

        TextureRegion toastRegion = textures.region(bgKey);
        if (toastRegion != null) row.setBackground(new TextureRegionDrawable(toastRegion));

        // Icon (skin drawable — unchanged)
        Image icon = new Image();
        if (skin.has("image_ui_quests_quest_icon_brown", com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)) {
            icon.setDrawable(skin.getDrawable("image_ui_quests_quest_icon_brown"));
        }

        // Text + progress
        Table textTable = new Table();
        Label titleLabel = new Label(quest.getTitle(), skin, "medium");
        textTable.add(titleLabel).left().padBottom(5f).row();

        int current = 0, target = 1;
        if (quest.getCriteria() != null && !quest.getCriteria().isEmpty()) {
            CriterionProgress c = quest.getCriteria().get(0);
            current = c.getCurrent();
            target  = Math.max(1, c.getTarget());
        }

        ProgressBar pb = new ProgressBar(0, target, 1, false, skin, "ingame_progress");
        pb.setValue(current);
        Label progressText = new Label(current + " / " + target, skin, "default");

        Table progressTable = new Table();
        progressTable.add(pb).width(150f).left();
        progressTable.add(progressText).left().padLeft(10f);
        textTable.add(progressTable).left().row();

        row.add(icon).size(64f, 64f).pad(15f);
        row.add(textTable).expandX().fillX().left().pad(15f);

        // Completion twinkle
        if (quest.isCompleted()) {
            TextureRegion twinkle = textures.region(
                    "IMAGE_UI_QUEST_TOAST_QUEST_COMPLETE_TWINKLE_QUEST_COMPLETE_TWINKLE_537X453");
            if (twinkle == null) twinkle = textures.region(
                    "IMAGE_UI_QUEST_TOAST_QUEST_COMPLETE_TWINKLE_QUEST_COMPLETE_TWINKLE_609X575");
            if (twinkle != null) {
                row.add(new Image(new TextureRegionDrawable(twinkle))).size(40f, 40f).pad(15f).right();
            }
        }

        row.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                showQuestDetailsOverlay(quest);
            }
        });

        return row;
    }

    private void showQuestDetailsOverlay(ConfigurableQuest quest) {
        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = skin.get("default", Label.LabelStyle.class).font;

        if (skin.has("image_ui_dialog_asset_dialogborder",
                com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)) {
            dialogStyle.background = skin.getDrawable("image_ui_dialog_asset_dialogborder");
        }
        dialogStyle.stageBackground = dimBackground;

        Dialog dialog = new Dialog("", dialogStyle);
        Table content = dialog.getContentTable();
        content.pad(30f);

        Label title = new Label(quest.getTitle(), skin, "big_outline");
        title.setAlignment(Align.center);
        content.add(title).expandX().fillX().padBottom(15f).row();

        Label typeLabel = new Label(
                "Type: " + quest.getType().name() + " | Priority: " + quest.getPriority().name(),
                skin, "secondary"
        );
        content.add(typeLabel).center().padBottom(20f).row();

        if (quest.getCriteria() != null) {
            for (CriterionProgress c : quest.getCriteria()) {
                int cur = c.getCurrent();
                int tgt = Math.max(1, c.getTarget());

                content.add(new Label("Task: " + c.getType(), skin, "medium")).left().padBottom(5f).row();

                Table pTable = new Table();
                ProgressBar pb = new ProgressBar(0, tgt, 1, false, skin, "ingame_progress");
                pb.setValue(cur);
                pTable.add(pb).width(250f).left();
                pTable.add(new Label(cur + " / " + tgt, skin, "default")).left().padLeft(10f);
                content.add(pTable).left().padBottom(15f).row();
            }
        }

        if (quest.getReward() != null) {
            Table rewardTable = new Table();
            rewardTable.add(new Label("Reward: ", skin, "medium_outline"));

            String iconKey = quest.getReward().rewardType().contains("GEM")
                    ? "image_ui_generic_gem_icon_small"
                    : "image_ui_generic_coin_icon_small";

            if (skin.has(iconKey, com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)) {
                rewardTable.add(new Image(skin.getDrawable(iconKey))).size(40f, 40f).padRight(5f);
            }
            rewardTable.add(new Label(String.valueOf(quest.getReward().amount()), skin, "big"));
            content.add(rewardTable).center().padTop(10f).row();
        }

        dialog.getButtonTable().pad(20f);
        dialog.button(new TextButton("Close", skin, "brown"), true);
        dialog.show(getStage());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Atlas helpers (mirrors SettingMenu exactly)
    // ─────────────────────────────────────────────────────────────────────────

    private NinePatchDrawable panelDrawable(String name, int left, int right, int top, int bottom) {
        TextureRegion region = textures.region(name);
        if (region == null) {
            // Graceful fallback: transparent drawable so layout still works
            return new NinePatchDrawable(new NinePatch(
                    skin.newDrawable("white-pixel", new Color(0f, 0f, 0f, 0f)).getLeftWidth() > 0
                            ? new TextureRegion()
                            : new TextureRegion()
            ));
        }
        return new NinePatchDrawable(new NinePatch(region, left, right, top, bottom));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Minigame kind helpers (unchanged)
    // ─────────────────────────────────────────────────────────────────────────

    private static String kindLabel(String behaviorSimpleName) {
        return switch (behaviorSimpleName) {
            case "WallnutBowlingBehavior" -> "Wall-nut Bowling";
            case "BeghouledBehavior"      -> "Beghouled";
            case "IZombieBehavior"        -> "I, Zombie";
            case "VaseBreakerBehavior"    -> "Vasebreaker";
            case "NormalBehavior"         -> "Zombotany";
            default                       -> behaviorSimpleName;
        };
    }

    private String kindOf(Level level) {
        var behavior = level.getBehavior();
        if (behavior == null) return "Unknown";
        String simpleName = behavior.getClass().getSimpleName();
        return simpleName.equals("CouchIZombieBehavior") ? "IZombieBehavior" : simpleName;
    }
}