package com.ussr.pvz.view.mainmenu;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.ussr.pvz.controller.maincontroller.SettingController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.notification.NotificationCenter;
import com.ussr.pvz.view.mainmenu.profile.ProfileUiFactory;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

public final class SettingMenu extends Table {
    private static final String[] DIFFICULTIES = {
            "1 - Easy",
            "2 - Normal",
            "3 - Medium",
            "4 - Hard",
            "5 - Very Hard"
    };

    private final Skin skin;
    private final SettingController controller;

    public SettingMenu(Skin skin) {
        this.skin = skin;
        this.controller = new SettingController();

        setFillParent(true);
        buildUi();
    }

    private void buildUi() {
        Table panel = createPanel();
        Table settingsCard = createSettingsCard();

        panel.add(settingsCard)
                .width(620f)
                .growY()
                .row();

        add(panel);
    }

    private Table createPanel() {
        Table panel = new Table();

        panel.setBackground(skin.getDrawable(
                "image_ui_dialog_asset_dialogborder_10"
        ));

        panel.pad(30f);
        panel.add(new Label(
                        "Settings",
                        skin,
                        "big_outline"
                ))
                .padBottom(20f)
                .row();

        return panel;
    }

    private Table createSettingsCard() {
        Table card = ProfileUiFactory.card(
                skin,
                "Gameplay"
        );

        addDifficultySetting(card);
        addSpeedSetting(card);
        addToggleSetting(
                card,
                "Show lawn grid",
                "Display red grid lines during gameplay."
        );
        addToggleSetting(
                card,
                "Debug mode",
                "Show currency and gameplay resource controls."
        );

        return card;
    }

    private void addDifficultySetting(Table card) {
        SelectBox<String> selector = new SelectBox<>(skin);
        selector.setItems(DIFFICULTIES);
        selector.setSelectedIndex(currentDifficulty() - 1);

        addSettingDescription(
                card,
                "Difficulty",
                "Controls zombie strength and game challenge."
        );

        card.add(selector)
                .colspan(2)
                .width(300f)
                .height(52f)
                .left()
                .padBottom(20f)
                .row();

        selector.addListener(difficultyListener(selector));
    }

    private int currentDifficulty() {
        if (App.getAccount() == null) {
            return 3;
        }

        return Math.max(
                1,
                Math.min(5, App.getAccount().getDifficultyLvl())
        );
    }

    private ChangeListener difficultyListener(
            SelectBox<String> selector
    ) {
        return ProfileUiFactory.listener(() -> {
            int difficulty = selector.getSelectedIndex() + 1;
            String result = controller.changeDifficulty(difficulty);
            showResult(result);
        });
    }

    private void addSpeedSetting(Table card) {
        addSettingDescription(
                card,
                "Game progression speed",
                "Choose a speed between 1 and 3. Logic will be added later."
        );

        Slider speedSlider = createSpeedSlider();
        Label valueLabel = createSpeedValueLabel(speedSlider);

        Table sliderRow = new Table();
        Label minimumLabel =
                ProfileUiFactory.cardText(skin, "1");

        Label maximumLabel =
                ProfileUiFactory.cardText(skin, "3");

        sliderRow.add(minimumLabel)
                .width(25f)
                .left();

        sliderRow.add(speedSlider)
                .width(440f)
                .height(42f)
                .growX();

        sliderRow.add(maximumLabel)
                .width(25f)
                .right();

        card.add(sliderRow)
                .colspan(2)
                .width(520f)
                .left()
                .row();

        card.add(valueLabel)
                .colspan(2)
                .center()
                .padTop(5f)
                .padBottom(20f)
                .row();

        speedSlider.addListener(
                createSpeedListener(
                        speedSlider,
                        valueLabel
                )
        );
    }

    private Slider createSpeedSlider() {
        Slider slider = new Slider(
                1f,
                3f,
                0.05f,
                false,
                skin,
                "default-horizontal"
        );

        slider.setValue(controller.getGameSpeed());
        slider.setAnimateDuration(0.08f);
        slider.setVisualInterpolation(
                Interpolation.smooth
        );

        return slider;
    }

    private void addToggleSetting(
            Table card,
            String title,
            String description
    ) {
        addSettingDescription(card, title, description);

        CheckBox checkBox = createLargeCheckBox();

        card.add(checkBox)
                .colspan(2)
                .left()
                .height(52f)
                .padBottom(16f)
                .row();

        checkBox.addListener(
                createCheckBoxListener(checkBox)
        );
    }

    private CheckBox createLargeCheckBox() {
        CheckBox checkBox = new CheckBox(
                " Disabled",
                skin
        );

        checkBox.setChecked(false);
        checkBox.getImageCell()
                .size(32f, 32f)
                .padRight(8f);
        checkBox.getImage().setScaling(Scaling.fit);

        return checkBox;
    }

    private ChangeListener createCheckBoxListener(
            CheckBox checkBox
    ) {
        return ProfileUiFactory.listener(
                () -> updateCheckBoxText(checkBox)
        );
    }

    private void updateCheckBoxText(CheckBox checkBox) {
        checkBox.setText(
                checkBox.isChecked()
                        ? " Enabled"
                        : " Disabled"
        );
    }

    private void addSettingDescription(
            Table card,
            String title,
            String description
    ) {
        card.add(ProfileUiFactory.sectionTitle(skin, title))
                .colspan(2)
                .growX()
                .left()
                .row();

        Label details = ProfileUiFactory.cardText(
                skin,
                description
        );

        details.setWrap(true);

        card.add(details)
                .colspan(2)
                .width(540f)
                .left()
                .padTop(4f)
                .padBottom(10f)
                .row();
    }

    private void showResult(String result) {
        if (result != null && result.contains("successfully")) {
            NotificationCenter.success(result);
            return;
        }

        NotificationCenter.error(result);
    }

    private Label createSpeedValueLabel(
            Slider slider
    ) {
        Label label = ProfileUiFactory.cardText(
                skin,
                formatSpeed(slider.getValue())
        );

        label.setAlignment(Align.center);
        return label;
    }

    private ChangeListener createSpeedListener(
            Slider slider,
            Label valueLabel
    ) {
        return ProfileUiFactory.listener(() -> {
            float speed = slider.getValue();

            valueLabel.setText(formatSpeed(speed));
            controller.changeGameSpeed(speed);
        });
    }

    private String formatSpeed(float value) {
        return String.format("Speed: %.2f", value);
    }
}
