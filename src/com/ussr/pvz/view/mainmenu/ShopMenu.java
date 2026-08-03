package com.ussr.pvz.view.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.ussr.pvz.controller.maincontroller.ShopController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.account.AdventureProgress;
import com.ussr.pvz.model.dto.ShopBuyRequest;
import com.ussr.pvz.model.shop.ShopItem;
import com.ussr.pvz.notification.NotificationCenter;
import com.ussr.pvz.view.FadingMenu;

import java.util.List;

public class ShopMenu extends FadingMenu {
    private final Skin skin;
    private final ShopController shopController;
    private Label balanceLabel;

    public ShopMenu(Skin skin) {
        this.skin = skin;
        this.shopController = new ShopController();
        buildShopUI();
    }

    public ShopMenu(Skin skin, ShopController shopController) {
        this.skin = skin;
        this.shopController = shopController;
        buildShopUI();
    }

    private void buildShopUI() {
        clearChildren();
        setFillParent(true);

        Table mainContainer = new Table();
        mainContainer.setFillParent(true);
        mainContainer.center();

        // Screen responsiveness: fill ~90% of screen width
        float targetDialogWidth = Gdx.graphics.getWidth() * 0.90f;
        float targetDialogHeight = Gdx.graphics.getHeight() * 0.88f;

        Table dialogTable = new Table();
        dialogTable.setBackground(skin.getDrawable("image_ui_dialog_asset_dialogborder_10"));
        dialogTable.pad(25f, 35f, 25f, 35f);

        // --- Header ---
        Label titleLabel = new Label("SHOP", skin, "big_outline");
        balanceLabel = new Label(getBalanceText(), skin, "medium");

        dialogTable.add(titleLabel).colspan(2).padBottom(4f).row();
        dialogTable.add(balanceLabel).colspan(2).padBottom(16f).row();

        // --- Wide Items Container ---
        Table itemsTable = new Table();
        itemsTable.top().pad(8f);
        itemsTable.defaults().growX().padBottom(12f); // Ensures item cards take full inner width

        List<ShopItem> items = shopController.getShopItems();
        for (ShopItem item : items) {
            Table itemCard = createWideItemCard(item);
            itemsTable.add(itemCard).row();
        }

        ScrollPane scrollPane = new ScrollPane(itemsTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        dialogTable.add(scrollPane).growX().growY().padBottom(16f).row();

        // --- Footer Navigation ---
        TextButton backButton = new TextButton("Back", skin, "brown");
        backButton.addListener(listener(() -> App.setMenuState(MenuState.GAME)));
        dialogTable.add(backButton).width(220f).height(50f).colspan(2).row();

        mainContainer.add(dialogTable).width(targetDialogWidth).height(targetDialogHeight);
        add(mainContainer);
    }

    private Table createWideItemCard(ShopItem item) {
        Table card = new Table();
        card.setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));
        card.pad(14f, 20f, 14f, 20f);

        // Details Column
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
        infoTable.add(descLabel).growX().left().padTop(6f).row();

        // Buy Action Button
        boolean isExpired = item.isExpired();
        TextButton buyBtn = new TextButton(isExpired ? "Expired" : "Buy", skin, isExpired ? "brown" : "green");
        if (!isExpired) {
            buyBtn.addListener(listener(() -> showBuyConfirmationOverlay(item)));
        } else {
            buyBtn.setDisabled(true);
        }

        card.add(infoTable).expandX().fillX().left();
        card.add(buyBtn).width(120f).height(48f).right().padLeft(15f);

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
        popup.add(countField).width(220f).height(45f).padBottom(10f).row();

        final TextField plantTypeField;
        if (item.requiresPlantType()) {
            plantTypeField = new TextField("", skin);
            popup.add(new Label("Plant Type:", skin)).left().padRight(10f);
            popup.add(plantTypeField).width(220f).height(45f).padBottom(15f).row();
        } else {
            plantTypeField = null;
        }

        // Action Buttons
        TextButton cancelBtn = new TextButton("Cancel", skin, "brown");
        TextButton confirmBtn = new TextButton("Confirm", skin, "green");

        cancelBtn.addListener(listener(overlay::remove));
        confirmBtn.addListener(listener(() -> {
            String count = countField.getText().trim();
            String plantType = plantTypeField != null ? plantTypeField.getText().trim() : "";

            ShopBuyRequest request = new ShopBuyRequest(item.getId(), count, plantType);

            // Delegate purchase execution directly through controller
            String result = shopController.buy(request);

            if (isErrorMessage(result)) {
                NotificationCenter.error(result);
            } else {
                NotificationCenter.success(result);
                overlay.remove();
                refreshUI();
            }
        }));

        Table btnTable = new Table();
        btnTable.add(cancelBtn).width(130f).height(48f).padRight(10f);
        btnTable.add(confirmBtn).width(130f).height(48f);

        popup.add(btnTable).colspan(2).padTop(10f).row();

        overlay.add(popup);
        addActor(overlay);
    }

    private boolean isErrorMessage(String response) {
        if (response == null) return true;
        String lower = response.toLowerCase();
        return lower.contains("insufficient") || lower.contains("invalid")
                || lower.contains("expired") || lower.contains("must be")
                || lower.contains("not found") || lower.contains("unknown")
                || lower.contains("failed");
    }

    private String getBalanceText() {
        if (App.getAccount() != null && App.getAccount().getAdventureProgress() != null) {
            AdventureProgress progress = App.getAccount().getAdventureProgress();
            return "Coins: " + progress.getCoin() + "  |  Gems: " + progress.getGem();
        }
        return "Coins: 0  |  Gems: 0";
    }

    private void refreshUI() {
        if (balanceLabel != null) {
            balanceLabel.setText(getBalanceText());
        }
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