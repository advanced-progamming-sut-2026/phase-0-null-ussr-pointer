package com.ussr.pvz.view.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.ussr.pvz.controller.maincontroller.ShopController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.dto.ShopBuyRequest;
import com.ussr.pvz.model.shop.ShopItem;
import com.ussr.pvz.notification.NotificationCenter;
import com.ussr.pvz.view.FadingMenu;
import pvz.libpvz.textures.TextureBank;

import java.util.ArrayList;
import java.util.List;

public class ShopMenu extends FadingMenu {
    // Atlas Region Name Constants
    private static final String REGION_SEED_PACKET = "IMAGE_UI_STOREMULTI_SEEDPACKETICON";
    private static final String REGION_POT = "IMAGE_ZEN_GARDEN_GROWING_PLANT_SLOT_GROWING_PLANT_SLOT_184X161_2";
    private static final String REGION_COIN = "IMAGE_EFFECTS_COIN_GOLD_COIN_GOLD_98X95";
    private static final String REGION_GEM = "IMAGE_EFFECTS_COIN_DIAMOND_COIN_DIAMOND_141X146";
    private static final String REGION_BTN_BACK = "IMAGE_UI_ALMANAC_BUTTONS_HUD_BACK_SELECTED";
    private static final String REGION_BTN_CANCEL = "IMAGE_UI_ALMANAC_TABS_CLOSE_TAB";
    private static final String REGION_BTN_CONFIRM = "IMAGE_UI_ALMANAC_CHECKBOX_ENABLED_SHARP";

    private final Skin skin;
    private final ShopController shopController;
    private final TextureBank textures;

    private Label coinBalanceLabel;
    private Label gemBalanceLabel;
    private final List<DailyOfferCountdown> dailyOfferCountdowns = new ArrayList<>();

    private static final class DailyOfferCountdown {
        final ShopItem item;
        final Label label;

        DailyOfferCountdown(ShopItem item, Label label) {
            this.item = item;
            this.label = label;
        }
    }

    public ShopMenu(Skin skin) {
        this(skin, new ShopController());
    }

    public ShopMenu(Skin skin, ShopController shopController) {
        this.skin = skin;
        this.shopController = shopController;

        // TextureBank setup
        FileHandle assetsFolder = Gdx.files.local("pvz-assets");
        this.textures = new TextureBank("ATLASES", assetsFolder);

        buildShopUI();
    }

    private void buildShopUI() {
        clearChildren();
        setFillParent(true);
        dailyOfferCountdowns.clear();

        Table dialogTable = new Table();
        dialogTable.setBackground(skin.getDrawable("image_ui_dialog_asset_dialogborder_10"));
        dialogTable.pad(20f, 30f, 20f, 30f);

        // --- Header ---
        Label titleLabel = new Label("SHOP", skin, "big_outline");
        dialogTable.add(titleLabel).colspan(2).padBottom(6f).row();

        // Currency Header Display
        Table currencyHud = createCurrencyHud();
        dialogTable.add(currencyHud).colspan(2).padBottom(16f).row();

        // --- Wide Items Scroll Panel ---
        Table itemsTable = new Table();
        itemsTable.top().pad(6f);
        itemsTable.defaults().growX().padBottom(12f);

        List<ShopItem> items = shopController.getShopItems();
        for (ShopItem item : items) {
            Table itemCard = createWideItemCard(item);
            itemsTable.add(itemCard).row();
        }

        ScrollPane scrollPane = new ScrollPane(itemsTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        dialogTable.add(scrollPane).growX().growY().padBottom(14f).row();

        // --- Back Button ---
        Drawable backDrawable = getAtlasDrawable(REGION_BTN_BACK);
        Actor backButton;
        if (backDrawable != null) {
            ImageButton imgBtn = new ImageButton(backDrawable);
            imgBtn.getImage().setScaling(Scaling.fit);
            imgBtn.addListener(listener(App::goBackMenuState));
            backButton = imgBtn;
        } else {
            TextButton txtBtn = new TextButton("Back", skin, "brown");
            txtBtn.addListener(listener(App::goBackMenuState));
            backButton = txtBtn;
        }

        dialogTable.add(backButton).width(160f).height(50f).colspan(2).row();

        // .grow() fills the screen, .pad() leaves standard ~5% top/bottom and ~4% left/right borders
        add(dialogTable)
                .grow()
                .pad(36f, 48f, 36f, 48f);
    }

    private Table createCurrencyHud() {
        Table hud = new Table();
        hud.setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));
        hud.pad(6f, 24f, 6f, 24f);

        // Gold Coin Icon
        Drawable coinDrawable = getAtlasDrawable(REGION_COIN);
        if (coinDrawable != null) {
            Image coinImg = new Image(coinDrawable);
            coinImg.setScaling(Scaling.fit);
            hud.add(coinImg).size(36f, 36f).padRight(8f);
        }
        coinBalanceLabel = new Label(getCoinText(), skin, "medium");
        hud.add(coinBalanceLabel).padRight(50f);

        // Gem Icon
        Drawable gemDrawable = getAtlasDrawable(REGION_GEM);
        if (gemDrawable != null) {
            Image gemImg = new Image(gemDrawable);
            gemImg.setScaling(Scaling.fit);
            hud.add(gemImg).size(36f, 36f).padRight(8f);
        }
        gemBalanceLabel = new Label(getGemText(), skin, "medium");
        hud.add(gemBalanceLabel);

        return hud;
    }

    private Table createWideItemCard(ShopItem item) {
        Table card = new Table();
        card.setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));
        card.pad(10f, 16f, 10f, 16f);

        // Item Icon (Pot or Seed Packet)
        Drawable itemIconDrawable = getItemDrawable(item);
        if (itemIconDrawable != null) {
            Image itemIcon = new Image(itemIconDrawable);
            itemIcon.setScaling(Scaling.fit);
            card.add(itemIcon).size(60f, 60f).padRight(16f);
        }

        // Details
        Table infoTable = new Table();
        infoTable.left();

        Label nameLabel = new Label(item.getName() + " (ID: " + item.getId() + ")", skin, "medium_outline");

        int finalCost = item.getCost();
        if (item.getDiscountPercent() != null && item.getDiscountPercent() > 0) {
            finalCost = (int) (finalCost * (1f - item.getDiscountPercent() / 100f));
        }

        String costText = "Cost: " + finalCost + " " + item.getType().getCostType();
        if (item.getDiscountPercent() != null && item.getDiscountPercent() > 0) {
            costText += " (" + item.getDiscountPercent().intValue() + "% OFF)";
        }

        Label costLabel = new Label(costText, skin, "secondary");
        Label descLabel = new Label(item.getDescription(), skin, "default");
        descLabel.setWrap(true);

        infoTable.add(nameLabel).left().row();
        infoTable.add(costLabel).left().padTop(2f).row();
        infoTable.add(descLabel).growX().left().padTop(4f).row();

        if (item.isDailyOffer()) {
            Label countdownLabel = new Label(formatRemainingTime(item), skin, "secondary");
            infoTable.add(countdownLabel).left().padTop(4f).row();
            dailyOfferCountdowns.add(new DailyOfferCountdown(item, countdownLabel));
        }

        card.add(infoTable).expandX().fillX().left();

        // Buy Action
        boolean isExpired = item.isExpired();
        TextButton buyBtn = new TextButton(isExpired ? "Expired" : "Buy", skin, isExpired ? "brown" : "green");
        if (!isExpired) {
            buyBtn.addListener(listener(() -> showBuyConfirmationOverlay(item)));
        } else {
            buyBtn.setDisabled(true);
        }

        card.add(buyBtn).width(110f).height(46f).right().padLeft(14f);

        return card;
    }

    private void showBuyConfirmationOverlay(ShopItem item) {
        Table overlay = new Table();
        overlay.setFillParent(true);
        overlay.setBackground(skin.newDrawable("white-pixel", 0f, 0f, 0f, 0.75f));

        Table popup = new Table();
        popup.setBackground(skin.getDrawable("image_ui_dialog_asset_dialogborder_10"));
        popup.pad(25f);

        Label title = new Label("Confirm Purchase", skin, "big_outline");
        Label itemTitle = new Label(item.getName(), skin, "medium");
        popup.add(title).colspan(2).padBottom(4f).row();
        popup.add(itemTitle).colspan(2).padBottom(15f).row();

        // Inputs
        TextField countField = new TextField("1", skin);
        popup.add(new Label("Count:", skin)).left().padRight(10f);
        popup.add(countField).width(200f).height(45f).padBottom(10f).row();

        final TextField plantTypeField;
        if (item.requiresPlantType()) {
            plantTypeField = new TextField("", skin);
            popup.add(new Label("Plant Type:", skin)).left().padRight(10f);
            popup.add(plantTypeField).width(200f).height(45f).padBottom(15f).row();
        } else {
            plantTypeField = null;
        }

        // Cancel & Confirm atlas buttons
        Drawable cancelDraw = getAtlasDrawable(REGION_BTN_CANCEL);
        Drawable confirmDraw = getAtlasDrawable(REGION_BTN_CONFIRM);

        Actor cancelBtn = cancelDraw != null
                ? createGraphicButton(cancelDraw, overlay::remove)
                : createTextButton("Cancel", "brown", overlay::remove);

        Actor confirmBtn = confirmDraw != null
                ? createGraphicButton(confirmDraw, () -> executePurchase(item, countField, plantTypeField, overlay))
                : createTextButton("Confirm", "green", () -> executePurchase(item, countField, plantTypeField, overlay));

        Table btnTable = new Table();
        btnTable.add(cancelBtn).size(64f, 64f).padRight(20f);
        btnTable.add(confirmBtn).size(64f, 64f);

        popup.add(btnTable).colspan(2).padTop(12f).row();

        overlay.add(popup);
        addActor(overlay);
    }

    private void executePurchase(ShopItem item, TextField countField, TextField plantTypeField, Table overlay) {
        String count = countField.getText().trim();
        String plantType = plantTypeField != null ? plantTypeField.getText().trim() : "";

        ShopBuyRequest request = new ShopBuyRequest(item.getId(), count, plantType);
        String result = shopController.buy(request);

        if (isErrorMessage(result)) {
            NotificationCenter.error(result);
        } else {
            NotificationCenter.success(result);
            overlay.remove();
            refreshUI();
        }
    }

    private String formatRemainingTime(ShopItem item) {
        long remaining = item.getLastRefreshedAt() + ShopItem.DAILY_ROTATION_INTERVAL_MILLIS
                - System.currentTimeMillis();
        if (remaining < 0) remaining = 0;

        long totalSeconds = remaining / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return String.format("Resets in: %02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        updateDailyOfferCountdowns();
    }

    private void updateDailyOfferCountdowns() {
        if (dailyOfferCountdowns.isEmpty()) return;

        boolean anyRotationDue = false;
        for (DailyOfferCountdown countdown : dailyOfferCountdowns) {
            countdown.label.setText(formatRemainingTime(countdown.item));
            if (countdown.item.isDailyRotationDue()) {
                anyRotationDue = true;
            }
        }

        if (anyRotationDue) {
            refreshUI();
        }
    }

    private Actor createGraphicButton(Drawable drawable, Runnable action) {
        ImageButton button = new ImageButton(drawable);
        button.getImage().setScaling(Scaling.fit);
        button.addListener(listener(action));
        return button;
    }

    private Actor createTextButton(String text, String style, Runnable action) {
        TextButton button = new TextButton(text, skin, style);
        button.addListener(listener(action));
        return button;
    }

    private Drawable getItemDrawable(ShopItem item) {
        String name = item.getName() != null ? item.getName().toLowerCase() : "";
        if (name.contains("pot")) {
            return getAtlasDrawable(REGION_POT);
        }
        return getAtlasDrawable(REGION_SEED_PACKET);
    }

    private Drawable getAtlasDrawable(String regionName) {
        if (textures != null) {
            TextureRegion region = textures.region(regionName);
            if (region != null) {
                return new TextureRegionDrawable(region);
            }
        }
        return null;
    }

    private boolean isErrorMessage(String response) {
        if (response == null) return true;
        String lower = response.toLowerCase();
        return lower.contains("insufficient") || lower.contains("invalid")
                || lower.contains("expired") || lower.contains("must be")
                || lower.contains("not found") || lower.contains("unknown")
                || lower.contains("failed");
    }

    private String getCoinText() {
        if (App.getAccount() != null && App.getAccount().getAdventureProgress() != null) {
            return String.valueOf(App.getAccount().getAdventureProgress().getCoin());
        }
        return "0";
    }

    private String getGemText() {
        if (App.getAccount() != null && App.getAccount().getAdventureProgress() != null) {
            return String.valueOf(App.getAccount().getAdventureProgress().getGem());
        }
        return "0";
    }

    private void refreshUI() {
        if (coinBalanceLabel != null) coinBalanceLabel.setText(getCoinText());
        if (gemBalanceLabel != null) gemBalanceLabel.setText(getGemText());
        buildShopUI();
    }

    private ChangeListener listener(Runnable action) {
        return new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        };
    }
}