package com.ussr.pvz.view.mainmenu.profile;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.ussr.pvz.controller.GlobalController;
import com.ussr.pvz.controller.maincontroller.ProfileController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

public final class ProfileMenu extends Table {
    private enum ProfileTab {
        INFORMATION,
        EDIT
    }

    private final Skin skin;
    private final ProfileController controller;
    private final GlobalController globalController;

    private Table contentRoot;
    private Table tabBar;
    private TextButton informationTab;
    private TextButton editTab;

    private ProfileInformationTab informationContent;
    private ProfileEditTab editContent;
    private ProfileTab selectedTab;

    public ProfileMenu(Skin skin) {
        this.skin = skin;
        this.controller = new ProfileController();
        this.globalController = new GlobalController();

        setFillParent(true);
        buildUi();
    }

    private void buildUi() {
        clearChildren();

        Account account = App.getAccount();

        if (account == null) {
            add(new Label(
                    "No user is logged in.",
                    skin
            ));
            return;
        }

        initializeTabContent(account);

        Table panel = createPanel();
        addTabs(panel);

        contentRoot = new Table();

        panel.add(contentRoot)
                .colspan(2)
                .grow()
                .padTop(15f)
                .row();

        add(panel);

        showTabImmediately(ProfileTab.INFORMATION);
    }

    private void initializeTabContent(Account account) {
        informationContent = new ProfileInformationTab(
                skin,
                this::logout
        );

        informationContent.refresh(account);

        editContent = new ProfileEditTab(
                skin,
                controller,
                this::profileChanged,
                this::showPasswordPanel
        );

        editContent.refresh(account);
    }

    private Table createPanel() {
        Table panel = new Table();

        panel.setBackground(skin.getDrawable(
                "image_ui_dialog_asset_dialogborder_10"
        ));

        panel.pad(30f);

        panel.add(new Label(
                        "Profile",
                        skin,
                        "big_outline"
                )).colspan(2)
                .padBottom(20f)
                .row();

        return panel;
    }

    private void addTabs(Table panel) {
        tabBar = new Table();

        informationTab = new TextButton(
                "Information",
                skin,
                "green"
        );

        editTab = new TextButton(
                "Edit Profile",
                skin,
                "brown"
        );

        tabBar.add(informationTab)
                .width(220f)
                .height(52f)
                .padRight(8f);

        tabBar.add(editTab)
                .width(220f)
                .height(52f);

        panel.add(tabBar)
                .colspan(2)
                .row();

        informationTab.addListener(
                ProfileUiFactory.listener(
                        () -> switchTab(
                                ProfileTab.INFORMATION
                        )
                )
        );

        editTab.addListener(
                ProfileUiFactory.listener(
                        () -> switchTab(ProfileTab.EDIT)
                )
        );
    }

    private void switchTab(ProfileTab target) {
        if (target == selectedTab) {
            return;
        }

        contentRoot.clearActions();

        contentRoot.addAction(sequence(
                fadeOut(0.12f, Interpolation.fade),
                run(() -> replaceTabContent(target)),
                fadeIn(0.18f, Interpolation.fade)
        ));
    }

    private void showTabImmediately(ProfileTab target) {
        replaceTabContent(target);
        contentRoot.getColor().a = 1f;
    }

    private void replaceTabContent(ProfileTab target) {
        selectedTab = target;
        contentRoot.clearChildren();

        Actor content = contentFor(target);
        contentRoot.add(content).grow();

        updateTabStyles();
    }

    private Actor contentFor(ProfileTab target) {
        if (target == ProfileTab.EDIT) {
            editContent.refresh(App.getAccount());
            return editContent;
        }

        informationContent.refresh(App.getAccount());
        return informationContent;
    }

    private void updateTabStyles() {
        boolean informationSelected =
                selectedTab == ProfileTab.INFORMATION;

        informationTab.setStyle(skin.get(
                informationSelected ? "green" : "brown",
                TextButton.TextButtonStyle.class
        ));

        editTab.setStyle(skin.get(
                informationSelected ? "brown" : "green",
                TextButton.TextButtonStyle.class
        ));
    }

    private void profileChanged() {
        informationContent.refresh(App.getAccount());
        switchTab(ProfileTab.INFORMATION);
    }

    private void logout() {
        ProfileUiFactory.showResult(
                globalController.logout()
        );
    }

    private void showPasswordPanel() {
        setTabsVisible(false);
        replaceContentWithAnimation(new ChangePasswordPanel(
                skin,
                controller,
                this::closePasswordPanel
        ));
    }

    private void closePasswordPanel() {
        contentRoot.clearActions();
        contentRoot.addAction(sequence(
                fadeOut(0.12f, Interpolation.fade),
                run(() -> {
                    setTabsVisible(true);
                    replaceTabContent(ProfileTab.EDIT);
                }),
                fadeIn(0.18f, Interpolation.fade)
        ));
    }

    private void replaceContentWithAnimation(Actor content) {
        contentRoot.clearActions();
        contentRoot.addAction(sequence(
                fadeOut(0.12f, Interpolation.fade),
                run(() -> {
                    contentRoot.clearChildren();
                    contentRoot.add(content).grow();
                }),
                fadeIn(0.18f, Interpolation.fade)
        ));
    }

    private void setTabsVisible(boolean visible) {
        tabBar.setVisible(visible);
        tabBar.setTouchable(
                visible ? Touchable.childrenOnly : Touchable.disabled
        );
    }
}
