package com.ussr.pvz.view.mainmenu.profile;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.ussr.pvz.model.account.Account;

public final class ProfileInformationTab extends Table {
    private final Skin skin;

    private final Label usernameValue;
    private final Label nicknameValue;
    private final Label emailValue;
    private final Label genderValue;
    private final Label completedLevelsValue;
    private final Label scoreValue;

    public ProfileInformationTab(Skin skin) {
        this.skin = skin;

        usernameValue = createValueLabel();
        nicknameValue = createValueLabel();
        emailValue = createValueLabel();
        genderValue = createValueLabel();
        completedLevelsValue = createValueLabel();
        scoreValue = createValueLabel();

        emailValue.setWrap(true);

        buildUi();
    }

    private Label createValueLabel() {
        return ProfileUiFactory.cardText(skin, "");
    }

    private void buildUi() {
        Table identityCard = createIdentityCard();
        Table progressCard = createProgressCard();

        add(identityCard)
                .width(360f)
                .top()
                .padRight(15f);

        add(progressCard)
                .width(300f)
                .top();
    }

    private Table createIdentityCard() {
        Table card = ProfileUiFactory.card(
                skin,
                "Identity"
        );

        addInformationRow(card, "Username", usernameValue);
        addInformationRow(card, "Nickname", nicknameValue);
        addInformationRow(card, "Email", emailValue);
        addInformationRow(card, "Gender", genderValue);

        return card;
    }

    private Table createProgressCard() {
        Table card = ProfileUiFactory.card(
                skin,
                "Progress"
        );

        addInformationRow(
                card,
                "Completed Levels",
                completedLevelsValue
        );

        addInformationRow(
                card,
                "Meow Points",
                scoreValue
        );

        return card;
    }

    private void addInformationRow(
            Table table,
            String title,
            Label value
    ) {
        table.add(ProfileUiFactory.cardText(
                        skin,
                        title + ":"
                )).width(145f)
                .left()
                .padBottom(12f);

        table.add(value)
                .minWidth(130f)
                .left()
                .padBottom(12f)
                .row();
    }

    public void refresh(Account account) {
        if (account == null) {
            return;
        }

        usernameValue.setText(safeText(account.getName()));
        nicknameValue.setText(safeText(account.getNickname()));
        emailValue.setText(safeText(account.getEmail()));

        genderValue.setText(formatName(
                account.getGender().name()
        ));

        completedLevelsValue.setText(
                account.getAdventureProgress()
                        .getCompletedLevels()
                        .size()
                        + " levels"
        );

        scoreValue.setText(String.format(
                "%,d",
                account.getScoreRecord().getScore()
        ));
    }

    private String safeText(String value) {
        return value == null || value.isBlank()
                ? "Not set"
                : value;
    }

    private String formatName(String value) {
        if (value == null || value.isBlank()) {
            return "Not set";
        }

        String lower = value.toLowerCase();

        return Character.toUpperCase(lower.charAt(0))
                + lower.substring(1);
    }
}