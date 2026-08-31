package com.ussr.pvz.view.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TravelLogMenu extends FadingMenu {

    private enum Page {
        DAILY("daily", "daily_notebook_page", "daily_sunflower_badge"),
        CHALLENGE("challenge", "challenge_blueprint_board", "challenge_scanner_orb"),
        EPIC("epic", "epic_wood_board", "epic_skull_badge"),
        MINIGAMES("minigames", "minigames_arcade_machine", "minigames_star_purple");

        final String id;
        final String background;
        final String header;

        Page(String id, String background, String header) {
            this.id = id;
            this.background = background;
            this.header = header;
        }
    }

    private static final String BG_KEY = "IMAGE_UI_QUESTS_TRAVEL_LOG_FINAL";
    private static final String BG_FALLBACK = "IMAGE_UI_QUESTS_TRAVEL_LOG_CORNER";
    private static final String TITLE_PLAQUE = "IMAGE_UI_QUESTS_QUESTS_TITLE_PLAQUE_TIER0";

    private final Skin skin;
    private final QuestService questService;
    private final TextureBank textures;
    private final TextureAtlas dailyAtlas;
    private final TextureAtlas challengeAtlas;
    private final TextureAtlas epicAtlas;
    private final TextureAtlas minigamesAtlas;
    private final TextureAtlas sharedAtlas;
    private final Button[] tabs = new Button[4];

    private Stack pageStack;
    private Table pageContent;
    private TextureRegionDrawable dimBackground;

    public TravelLogMenu(Skin skin) {
        this.skin = skin;
        this.questService = new QuestService();
        this.textures = new TextureBank("ATLASES", Gdx.files.local("pvz-assets"));

        String atlasFolder = "travellog/travellog_real_libgdx_atlases/";
        dailyAtlas = loadAtlas(atlasFolder + "travellog_daily.atlas");
        challengeAtlas = loadAtlas(atlasFolder + "travellog_challenge.atlas");
        epicAtlas = loadAtlas(atlasFolder + "travellog_epic.atlas");
        minigamesAtlas = loadAtlas(atlasFolder + "travellog_minigames.atlas");
        sharedAtlas = loadAtlas(atlasFolder + "travellog_shared.atlas");

        createDimBackground();
        buildUi();
    }

    private TextureAtlas loadAtlas(String path) {
        com.badlogic.gdx.files.FileHandle file = Gdx.files.internal(path);
        if (!file.exists()) {
            file = Gdx.files.internal("assets/" + path);
        }
        if (!file.exists()) {
            throw new IllegalStateException(
                    "Missing Travel Log atlas. Checked: " + path + " and assets/" + path
            );
        }
        return new TextureAtlas(file);
    }

    private void createDimBackground() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0f, 0f, 0f, 0.70f));
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        dimBackground = new TextureRegionDrawable(new TextureRegion(texture));
    }

    private void buildUi() {
        setFillParent(true);

        Table root = new Table();
        root.setFillParent(true);

        TextureRegion outerBackground = textures.region(BG_KEY);
        if (outerBackground == null) outerBackground = textures.region(BG_FALLBACK);
        if (outerBackground != null) {
            root.setBackground(new TextureRegionDrawable(outerBackground));
        }

        Table title = new Table();
        TextureRegion plaque = textures.region(TITLE_PLAQUE);
        if (plaque != null) {
            title.add(new Image(plaque)).height(90f).width(360f).padTop(8f);
        } else {
            title.add(new Label("Travel Log", skin, "big_outline")).padTop(8f);
        }

        Table center = new Table();
        center.setBackground(atlasPanelDrawable(
                sharedAtlas, "shared_dark_panel", 28, 28, 22, 22
        ));
        center.pad(14f, 18f, 14f, 18f);

        pageStack = new Stack();
        center.add(buildNavigation()).width(220f).growY().padRight(14f);
        center.add(pageStack).grow();

        root.add(title).expandX().top().row();
        root.add(center).grow().pad(18f);
        addActor(root);

        showPage(Page.DAILY, 0);
    }

    private Table buildNavigation() {
        Table navigation = new Table();
        navigation.top().padTop(8f);

        tabs[0] = tab(
                "Daily", dailyAtlas, "daily_sunflower_badge", Page.DAILY, 0
        );
        tabs[1] = tab(
                "Challenge", challengeAtlas, "challenge_scanner_orb", Page.CHALLENGE, 1
        );
        tabs[2] = tab(
                "Epic", epicAtlas, "epic_skull_badge", Page.EPIC, 2
        );
        tabs[3] = tab(
                "Minigames", minigamesAtlas, "minigames_star_purple", Page.MINIGAMES, 3
        );

        for (Button tab : tabs) {
            navigation.add(tab).width(205f).height(70f).padBottom(8f).row();
        }
        navigation.add().growY();
        return navigation;
    }

    private Button tab(
            String text,
            TextureAtlas iconAtlas,
            String iconRegionName,
            Page page,
            int index
    ) {
        Button.ButtonStyle style = new Button.ButtonStyle();
        style.up = drawable(sharedAtlas, unselectedTabRegion(index));
        style.down = drawable(sharedAtlas, "shared_tab_green");
        style.checked = drawable(sharedAtlas, "shared_tab_green");

        Button button = new Button(style);
        TextureRegion iconRegion = iconAtlas.findRegion(iconRegionName);
        if (iconRegion != null) {
            Image icon = new Image(iconRegion);
            icon.setScaling(Scaling.fit);
            button.add(icon).size(42f).padLeft(10f).padRight(8f);
        }
        Label label = new Label(text, skin, "medium_outline");
        label.setAlignment(Align.left);
        button.add(label).growX().left().padRight(8f);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showPage(page, index);
            }
        });
        return button;
    }

    private void showPage(Page page, int tabIndex) {
        for (int i = 0; i < tabs.length; i++) tabs[i].setChecked(i == tabIndex);

        pageStack.clearChildren();
        TextureAtlas atlas = atlasFor(page);
        Image background = image(atlas, page.background);
        background.setScaling(Scaling.stretch);
        background.setTouchable(Touchable.disabled);
        pageStack.add(background);

        pageContent = new Table();
        pageContent.top().pad(24f, 30f, 24f, 30f);

        Table header = new Table();
        header.add(image(atlas, page.header)).size(72f).padRight(12f);
        header.add(new Label(pageTitle(page), skin, "big_outline"));
        pageContent.add(header).height(82f).colspan(2).padBottom(8f).row();

        Table cards = new Table();
        cards.top();
        ScrollPane scroll = new ScrollPane(cards, skin);
        scroll.setScrollingDisabled(true, false);
        scroll.setFadeScrollBars(false);
        pageContent.add(scroll).grow().colspan(2);
        pageStack.add(pageContent);

        if (page == Page.MINIGAMES) {
            fillMinigames(cards);
        } else {
            fillQuests(cards, page);
        }
    }

    private void fillQuests(Table cards, Page page) {
        List<ConfigurableQuest> quests = questService.getActiveQuestsAsList(page.id);
        if (quests.isEmpty()) {
            cards.add(new Label("No active " + page.id + " quests.", skin, "medium"))
                    .colspan(2).pad(40f);
            return;
        }

        int column = 0;
        for (ConfigurableQuest quest : quests) {
            cards.add(buildQuestCard(quest, page)).width(cardWidth(page)).height(cardHeight(page)).pad(10f);
            if (++column == 2) {
                cards.row();
                column = 0;
            }
        }
    }

    private Actor buildQuestCard(ConfigurableQuest quest, Page page) {
        TextureAtlas atlas = atlasFor(page);
        String regionName = switch (page) {
            case DAILY -> quest.isCompleted() ? "daily_note_green" : "daily_note_large";
            case CHALLENGE -> quest.isCompleted() ? "challenge_panel_blue" : "challenge_panel_cyan";
            case EPIC -> "epic_wanted_poster";
            default -> throw new IllegalArgumentException("Quest card requested for " + page);
        };
        Stack stack = new Stack();
        Image background = image(atlas, regionName);
        background.setScaling(Scaling.stretch);
        background.setTouchable(Touchable.disabled);
        stack.add(background);
        Table content = new Table();
        content.pad(page == Page.EPIC ? 30f : 20f);
        Label title = new Label(quest.getTitle(), skin, "medium_outline");
        title.setAlignment(Align.center);
        title.setWrap(true);
        content.add(title).growX().padBottom(10f).row();
        int current = 0 , target = 1;
        if (quest.getCriteria() != null && !quest.getCriteria().isEmpty()) {
            CriterionProgress criterion = quest.getCriteria().get(0);
            current = criterion.getCurrent();
            target = Math.max(1, criterion.getTarget());}
        content.add(progressMeter(current, target)).width(230f).height(40f).padBottom(8f).row();
        Label progress = new Label(current + " / " + target, skin, "default");
        content.add(progress).center().row();
        if (quest.getReward() != null) {
            Table reward = new Table();
            String iconName = quest.getReward().rewardType().contains("GEM") ? "shared_gem" : "shared_coin";
            reward.add(image(sharedAtlas, iconName)).size(30f).padRight(5f);
            reward.add(new Label(String.valueOf(quest.getReward().amount()), skin, "default"));
            content.add(reward).center().padTop(5f);}
        stack.add(content);
        if (quest.isCompleted()) {
            Table stampLayer = new Table();
            stampLayer.bottom().right();
            if (page == Page.EPIC)
                stampLayer.add(image(epicAtlas, "epic_defeated_poster")).width(120f).height(70f).pad(8f);
            else if (page == Page.CHALLENGE) stampLayer.add(image(challengeAtlas, "challenge_passed_stamp"))
                        .width(90f).height(62f).pad(8f);
            else stampLayer.add(image(sharedAtlas, "shared_done_stamp")).width(120f).height(56f).pad(8f);
            stampLayer.setTouchable(Touchable.disabled);
            stack.add(stampLayer);}
        stack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {showQuestDetails(quest);}});
        return stack;
    }

    private Actor progressMeter(int current, int target) {
        float ratio = Math.min(1f, Math.max(0f, current / (float) Math.max(1, target)));
        Stack meter = new Stack();

        String progressRegion;
        if (ratio <= 0f) progressRegion = "shared_progress_empty";
        else if (ratio < 0.34f) progressRegion = "shared_progress_blue";
        else if (ratio < 0.67f) progressRegion = "shared_progress_orange";
        else progressRegion = "shared_progress_green";
        Image progress = image(sharedAtlas, progressRegion);
        progress.setScaling(Scaling.stretch);
        meter.add(progress);
        return meter;
    }

    private void fillMinigames(Table cards) {
        List<Level> minigames = questService.getMinigamesAsList();
        if (minigames.isEmpty()) {
            cards.add(new Label("No minigames unlocked.", skin, "medium")).colspan(2).pad(40f);
            return;
        }

        Map<String, List<Level>> grouped = new LinkedHashMap<>();
        for (Level level : minigames) {
            grouped.computeIfAbsent(kindOf(level), ignored -> new ArrayList<>()).add(level);
        }

        String[] ticketRegions = {
                "minigames_panel_orange", "minigames_panel_blue",
                "minigames_panel_purple", "minigames_panel_green"
        };
        int index = 0;
        for (Map.Entry<String, List<Level>> entry : grouped.entrySet()) {
            final String kind = entry.getKey();
            final List<Level> levels = entry.getValue();
            cards.add(buildMinigameTicket(kind, levels, ticketRegions[index % ticketRegions.length]))
                    .width(340f).height(240f).pad(10f);
            if (++index % 2 == 0) cards.row();
        }
    }

    private Actor buildMinigameTicket(String kind, List<Level> levels, String ticketRegion) {
        Stack stack = new Stack();
        Image background = image(minigamesAtlas, ticketRegion);
        background.setScaling(Scaling.stretch);
        background.setTouchable(Touchable.disabled);
        stack.add(background);

        Table content = new Table();
        content.pad(20f);
        Label title = new Label(kindLabel(kind), skin, "medium_outline");
        title.setAlignment(Align.center);
        content.add(title).growX().padBottom(8f).row();
        content.add(new Label(levels.size() + " level" + (levels.size() == 1 ? "" : "s"), skin, "default"));
        stack.add(content);

        stack.setTouchable(Touchable.enabled);
        stack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showMinigameLevels(kind, levels);
            }
        });
        return stack;
    }

    private void showMinigameLevels(String kind, List<Level> levels) {
        pageContent.clearChildren();

        TextButton back = new TextButton("< Back", skin, "green_small");
        back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showPage(Page.MINIGAMES, 3);
            }
        });
        pageContent.add(back).left().width(150f).height(50f).padBottom(8f);
        pageContent.add(new Label(kindLabel(kind), skin, "medium_outline")).expandX().left().row();

        Table list = new Table();
        list.top();
        ScrollPane scroll = new ScrollPane(list, skin);
        scroll.setScrollingDisabled(true, false);
        pageContent.add(scroll).grow().colspan(2);

        int number = 0;
        for (Level level : levels) {
            Table row = new Table();
            row.setBackground(drawable(minigamesAtlas, "minigames_ticket_long"));
            row.pad(10f, 18f, 10f, 18f);
            boolean couch = level.getBehavior() != null
                    && "CouchIZombieBehavior".equals(level.getBehavior().getClass().getSimpleName());
            String label = couch ? kindLabel(kind) + " Coop" : kindLabel(kind) + " " + (++number);
            row.add(new Label(label, skin, "medium")).growX().left();

            TextButton play = atlasTextButton("", "shared_button_play");
            play.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    questService.playMinigame(level.getId());
                }
            });
            row.add(play).width(150f).height(52f);
            list.add(row).growX().height(86f).pad(7f).row();
        }
    }

    private TextButton atlasTextButton(String text, String regionName) {
        TextButton.TextButtonStyle base = skin.get("green_small", TextButton.TextButtonStyle.class);
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(base);
        TextureRegionDrawable up = drawable(sharedAtlas, regionName);
        style.up = up;
        style.down = up.tint(new Color(0.82f, 0.82f, 0.82f, 1f));
        return new TextButton(text, style);
    }

    private void showQuestDetails(ConfigurableQuest quest) {
        Window.WindowStyle style = new Window.WindowStyle();
        style.titleFont = skin.get("default", Label.LabelStyle.class).font;
        if (skin.has("image_ui_dialog_asset_dialogborder",
                com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)) {
            style.background = skin.getDrawable("image_ui_dialog_asset_dialogborder");
        }
        style.stageBackground = dimBackground;

        Dialog dialog = new Dialog("", style);
        Table content = dialog.getContentTable();
        content.pad(30f);
        Label title = new Label(quest.getTitle(), skin, "big_outline");
        title.setAlignment(Align.center);
        content.add(title).growX().padBottom(18f).row();

        if (quest.getCriteria() != null) {
            for (CriterionProgress criterion : quest.getCriteria()) {
                content.add(new Label("Task: " + criterion.getType(), skin, "medium"))
                        .left().padBottom(5f).row();
                content.add(new Label(
                        criterion.getCurrent() + " / " + Math.max(1, criterion.getTarget()),
                        skin, "default")).left().padBottom(12f).row();
            }
        }

        dialog.getButtonTable().pad(18f);
        dialog.button(new TextButton("Close", skin, "brown"), true);
        dialog.show(getStage());
    }

    private TextureAtlas atlasFor(Page page) {
        switch (page) {
            case DAILY: return dailyAtlas;
            case CHALLENGE: return challengeAtlas;
            case EPIC: return epicAtlas;
            case MINIGAMES: return minigamesAtlas;
            default: throw new IllegalArgumentException("Unknown page " + page);
        }
    }

    private float cardWidth(Page page) {
        return page == Page.EPIC ? 300f : 350f;
    }

    private float cardHeight(Page page) {
        return page == Page.EPIC ? 360f : (page == Page.CHALLENGE ? 290f : 225f);
    }

    private String pageTitle(Page page) {
        switch (page) {
            case DAILY: return "Daily";
            case CHALLENGE: return "Challenge";
            case EPIC: return "Epic";
            case MINIGAMES: return "Minigames";
            default: throw new IllegalArgumentException("Unknown page " + page);
        }
    }

    private Image image(TextureAtlas atlas, String regionName) {
        Image image = new Image(required(atlas, regionName));
        image.setScaling(Scaling.fit);
        return image;
    }

    private TextureRegionDrawable drawable(TextureAtlas atlas, String regionName) {
        return new TextureRegionDrawable(required(atlas, regionName));
    }

    private TextureRegion required(TextureAtlas atlas, String regionName) {
        TextureRegion region = atlas.findRegion(regionName);
        if (region == null) {
            throw new IllegalStateException("Missing Travel Log atlas region: " + regionName);
        }
        return region;
    }

    private String unselectedTabRegion(int index) {
        switch (index) {
            case 0: return "shared_tab_beige";
            case 1: return "shared_tab_blue";
            case 2: return "shared_tab_purple";
            case 3: return "shared_tab_yellow";
            default: throw new IllegalArgumentException("Unknown tab index " + index);
        }
    }

    private NinePatchDrawable atlasPanelDrawable(
            TextureAtlas atlas,
            String regionName,
            int left,
            int right,
            int top,
            int bottom
    ) {
        return new NinePatchDrawable(new NinePatch(
                required(atlas, regionName), left, right, top, bottom
        ));
    }

    private static String kindLabel(String behaviorSimpleName) {
        switch (behaviorSimpleName) {
            case "WallnutBowlingBehavior": return "Wall-nut Bowling";
            case "BeghouledBehavior": return "Beghouled";
            case "IZombieBehavior": return "I, Zombie";
            case "VaseBreakerBehavior": return "Vasebreaker";
            case "NormalBehavior": return "Zombotany";
            default: return behaviorSimpleName;
        }
    }

    private String kindOf(Level level) {
        if (level.getBehavior() == null) return "Unknown";
        String simpleName = level.getBehavior().getClass().getSimpleName();
        return "CouchIZombieBehavior".equals(simpleName) ? "IZombieBehavior" : simpleName;
    }
}
