package com.ussr.pvz.view.mainmenu.gamemenu.chooseplant;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.ussr.pvz.model.App;
import com.ussr.pvz.notification.NotificationCenter;
import com.ussr.pvz.service.ChoosePlantService;
import com.ussr.pvz.service.CollectionService.PlantData;
import com.ussr.pvz.view.FadingMenu;
import com.ussr.pvz.view.components.PlantCard;
import pvz.libpvz.textures.TextureBank;

import java.util.List;

public class ChoosePlantMenu extends FadingMenu {

    private static final int COLUMNS      = 4;
    private static final int CARD_W       = 190;
    private static final int CARD_H       = 110;
    private static final int SELECTED_W   = 105;
    private static final int SELECTED_H   = 68;

    private final Skin skin;
    private final TextureBank textures;
    private final ChoosePlantService service;

    // UI regions we reuse
    private Table selectedSlotsRow;   // top bar — 8 selected slots
    private Table plantGrid;          // scrollable grid
    private Label slotCountLbl;       // "3 / 8"
    private Table detailPanel;        // right-side detail panel

    // Currently focused card for detail panel
    private PlantData focusedPlant = null;

    public ChoosePlantMenu(Skin skin) {
        this.skin     = skin;
        this.textures = new TextureBank("ATLASES", Gdx.files.local("pvz-assets"));
        this.service  = new ChoosePlantService();
        buildUI();
    }

    // ── Top-level layout ──────────────────────────────────────────────────────

    private void buildUI() {
        setFillParent(true);
        applyBackground();

        // Root: [left: grid+slots] [right: detail panel]
        Table root = new Table();
        root.setFillParent(true);
        root.pad(12);

        root.add(buildLeftPanel()).expand().fill();
        root.add(buildDetailPanel()).width(210).fillY().padLeft(10);

        addActor(root);
    }

    private void applyBackground() {
        TextureRegion bg = textures.region("image_ui_dialog_asset_dialogborder");
        if (bg != null) setBackground(new TextureRegionDrawable(bg));
    }

    // ── Left panel: selected slots + scrollable grid ──────────────────────────

    private Actor buildLeftPanel() {
        Table left = new Table();
        left.top();

        left.add(buildSelectedSlotsBar()).expandX().fillX().padBottom(8).row();
        left.add(buildScrollableGrid()).expand().fill().row();
        left.add(buildLetsRockButton()).right().padTop(8);

        return left;
    }

    // ── Selected slots bar (top) ──────────────────────────────────────────────

    private Actor buildSelectedSlotsBar() {
        Table bar = new Table();
        TextureRegion barBg = textures.region("image_ui_hud_ingame_background_3slice");
        if (barBg != null) bar.setBackground(new TextureRegionDrawable(barBg));
        bar.pad(6);

        selectedSlotsRow = new Table();
        refreshSelectedSlots();

        slotCountLbl = new Label(service.selectedCount() + " / " + service.maxSlots(), skin, "default");
        slotCountLbl.setFontScale(0.75f);
        slotCountLbl.setColor(Color.WHITE);

        bar.add(selectedSlotsRow).expandX().fillX();
        bar.add(slotCountLbl).right().padLeft(10);
        return bar;
    }

    private void refreshSelectedSlots() {
        selectedSlotsRow.clearChildren();
        List<PlantData> all = service.getSelectablePlants();

        // Show only selected plants in the top bar
        List<PlantData> selected = all.stream()
                .filter(p -> service.isSelected(p.id))
                .collect(java.util.stream.Collectors.toList());

        for (PlantData p : selected) {
            PlantCard mini = new PlantCard(p, skin, textures, () -> {
                // clicking selected card deselects
                service.removePlant(new com.ussr.pvz.model.dto.PlantTypeRequest(p.id));
                refresh();
            });
            selectedSlotsRow.add(mini).size(SELECTED_W, SELECTED_H).pad(3);
        }

        // Empty slots
        int empty = service.maxSlots() - selected.size();
        for (int i = 0; i < empty; i++) {
            selectedSlotsRow.add(buildEmptySlot()).size(SELECTED_W, SELECTED_H).pad(3);
        }

        if (slotCountLbl != null)
            slotCountLbl.setText(service.selectedCount() + " / " + service.maxSlots());
    }

    private Actor buildEmptySlot() {
        Image slot = new Image();
        TextureRegion reg = textures.region("image_ui_cards_chooser_chooser_plant_card");
        if (reg != null) {
            slot.setDrawable(new TextureRegionDrawable(reg));
            slot.setScaling(Scaling.stretch);
            slot.setColor(new Color(1, 1, 1, 0.3f));
        }
        return slot;
    }

    // ── Scrollable plant grid ─────────────────────────────────────────────────

    private Actor buildScrollableGrid() {
        plantGrid = new Table();
        plantGrid.top().left();
        ScrollPane scroll = new ScrollPane(plantGrid, skin);
        scroll.setScrollingDisabled(true, false);
        scroll.setFadeScrollBars(false);
        refreshGrid();
        return scroll;
    }

    private void refreshGrid() {
        plantGrid.clearChildren();
        List<PlantData> plants = service.getSelectablePlants();
        int col = 0;
        for (PlantData p : plants) {
            PlantCard card = new PlantCard(p, skin, textures, () -> onCardClicked(p));
            // Tint selected cards green
            card.setSelectionVisible(service.isSelected(p.id));
            plantGrid.add(card).size(CARD_W, CARD_H).pad(4);
            if (++col >= COLUMNS) { plantGrid.row(); col = 0; }
        }
        if (plants.isEmpty()) {
            Label empty = new Label("No plants available.", skin, "default");
            empty.setAlignment(Align.center);
            plantGrid.add(empty).colspan(COLUMNS).pad(40);
        }
    }

    private void onCardClicked(PlantData p) {
        focusedPlant = p;

        if (!service.isSelected(p.id)) {
            String result = service.addPlant(new com.ussr.pvz.model.dto.PlantTypeRequest(p.id));
            if (result.contains("added")) {
                NotificationCenter.success(p.name + " added!");
            } else {
                NotificationCenter.warning(result);
            }
        }
        refresh();
    }

    // ── Detail panel (right side) ─────────────────────────────────────────────

    private Actor buildDetailPanel() {
        detailPanel = new Table();
        TextureRegion bg = textures.region("image_ui_dialog_asset_dialog_center");
        if (bg != null) detailPanel.setBackground(new TextureRegionDrawable(bg));
        detailPanel.top().pad(10);
        refreshDetailPanel();
        return detailPanel;
    }

    // ── In ChoosePlantMenu, replace these three methods ───────────────────────────

    private void refreshDetailPanel() {
        detailPanel.clearChildren();
        if (focusedPlant == null) {
            Label hint = new Label("Select a plant\nto see details", skin, "default");
            hint.setAlignment(Align.center);
            hint.setFontScale(0.8f);
            detailPanel.add(hint).expand().center();
            return;
        }
        PlantData p = focusedPlant;

        // Big card preview
        PlantCard preview = new PlantCard(p, skin, textures, null);
        detailPanel.add(preview).size(190, 110).padBottom(6).row();

        // Plant name
        Label nameLbl = new Label(p.name, skin, "default");
        nameLbl.setAlignment(Align.center);
        nameLbl.setFontScale(0.85f);
        nameLbl.setColor(Color.WHITE);
        detailPanel.add(nameLbl).expandX().fillX().padBottom(4).row();

        // Sun cost row
        detailPanel.add(buildCostRow(p)).padBottom(10).row();

        Table statsTable = new Table();
        statsTable.defaults().left().pad(4f);

        statsTable.add(new Label("HP:", skin)).width(150f);
        statsTable.add(new Label(formatStat(p.baseHp), skin)).row();

        statsTable.add(new Label("Damage:", skin)).width(150f);
        statsTable.add(new Label(formatStat(p.damage), skin)).row();

        statsTable.add(new Label("Recharge:", skin)).width(150f);
        statsTable.add(new Label(formatStat(p.recharge) + "s", skin)).row();

        if (p.actionInterval > 0) {
            statsTable.add(new Label("Action interval:", skin)).width(150f);
            statsTable.add(
                    new Label(formatStat(p.actionInterval) + "s", skin)
            ).row();
        }

        detailPanel.add(statsTable).growX().pad(12f).row();

        // Upgrade button
        detailPanel.add(buildUpgradeButton(p)).expandX().fillX().height(50).padBottom(6).row();

        // Boost button
        detailPanel.add(buildBoostButton(p)).expandX().fillX().height(50);
    }

    private String formatStat(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001) {
            return String.valueOf((int) Math.rint(value));
        }

        return String.format(java.util.Locale.US, "%.2f", value)
                .replaceAll("0+$", "")
                .replaceAll("\\.$", "");
    }

    private Actor buildUpgradeButton(PlantData p) {
        boolean canUpgrade = p.level > 0 && p.level < 4 && p.ownedPackets > 0;
        int cost = p.level * 1000;

        TextButton btn = new TextButton("UPGRADE  " + cost, skin, "green");
        btn.getLabel().setFontScale(0.8f);
        btn.setColor(canUpgrade ? Color.WHITE : Color.GRAY);

        if (canUpgrade) {
            btn.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    String res = new com.ussr.pvz.service.CollectionService()
                            .upgradePlant(new com.ussr.pvz.model.dto.PlantTypeRequest(p.id));
                    if (res.contains("Upgraded")) {
                        NotificationCenter.success(p.name + " upgraded!");
                    } else {
                        NotificationCenter.error(res);
                    }
                    service.getSelectablePlants().stream()
                            .filter(pl -> pl.id.equals(p.id))
                            .findFirst().ifPresent(updated -> focusedPlant = updated);
                    refresh();
                }
            });
        }
        return btn;
    }

    private Actor buildBoostButton(PlantData p) {
        boolean isSelected = service.isSelected(p.id);
        boolean isBoosted  = service.isBoosted(p.id);

        int seedPackets = p.ownedPackets;
        int savedBoosts = 0;
        com.ussr.pvz.model.account.SavedBoosts sb = App.getAccount() != null
                ? App.getAccount().getSavedBoosts() : null;
        if (sb != null) {
            savedBoosts = (int) sb.getBoosts().stream()
                    .filter(b -> b.equalsIgnoreCase(p.id)).count();
        }
        int totalBoosts = seedPackets + savedBoosts;
        boolean canBoost = isSelected && totalBoosts > 0;

        TextButton btn = new TextButton(
                (isBoosted ? "✓ " : "") + "BOOST  x" + totalBoosts,
                skin, "green");
        btn.getLabel().setFontScale(0.8f);
        btn.getLabel().setColor(isBoosted ? Color.GREEN : Color.WHITE);
        btn.setColor(canBoost ? Color.WHITE : Color.GRAY);

        if (canBoost) {
            btn.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    if (!isBoosted) {
                        boolean usedSaved = sb != null && sb.useBoost(p.id);
                        if (!usedSaved) {
                            service.toggleBoost(p.id);
                        } else {
                            service.applyBoost(p.id);
                        }
                        NotificationCenter.success(p.name + " boosted!");
                    } else {
                        service.toggleBoost(p.id);
                        NotificationCenter.info(p.name + " boost removed.");
                    }
                    service.getSelectablePlants().stream()
                            .filter(pl -> pl.id.equals(p.id))
                            .findFirst().ifPresent(updated -> focusedPlant = updated);
                    refresh();
                }
            });
        }
        return btn;
    }

    // ── Let's Rock button ─────────────────────────────────────────────────────

    private Actor buildLetsRockButton() {
        TextButton btn = new TextButton("LET'S ROCK!", skin, "green");
        btn.getLabel().setFontScale(1.1f);
        btn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                String result = service.startGame();
                if (result.contains("started")) {
                    NotificationCenter.success("Game started!");
                } else {
                    NotificationCenter.warning(result);
                }
            }
        });
        return btn;
    }

    private Actor buildCostRow(PlantData p) {
        Table row = new Table();
        TextureRegion sunReg = textures.region("image_ui_almanac_almanac_stat_icon_suncost_layer_1");
        if (sunReg != null)
            row.add(new Image(new TextureRegionDrawable(sunReg))).size(22, 22).padRight(4);
        Label cost = new Label(String.valueOf(p.cost), skin, "default");
        cost.setFontScale(0.9f);
        cost.setColor(Color.YELLOW);
        row.add(cost);
        return row;
    }

    // ── Refresh everything ────────────────────────────────────────────────────

    private void refresh() {
        refreshSelectedSlots();
        refreshGrid();
        refreshDetailPanel();
    }
}
