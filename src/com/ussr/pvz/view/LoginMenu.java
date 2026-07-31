package com.ussr.pvz.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.ussr.pvz.controller.LoginController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.dto.AnswerRequest;
import com.ussr.pvz.model.dto.ForgetPasswordRequest;
import com.ussr.pvz.model.dto.LoginRequest;
import com.ussr.pvz.model.dto.LoginResult;
import com.ussr.pvz.model.dto.enums.LoginStatus;
import com.ussr.pvz.notification.NotificationCenter;

public final class LoginMenu extends FadingMenu {
    private final Skin skin;
    private final LoginController controller = new LoginController();
    private TextField usernameField;
    private TextField passwordField;
    private boolean stayLoggedIn;

    public LoginMenu(Skin skin) {
        this.skin = skin;
        buildLoginForm();
    }

    private void buildLoginForm() {
        clearChildren();
        usernameField = new TextField("", skin);
        passwordField = createPasswordField();
        stayLoggedIn = false;

        Table form = createPanel("Login");
        addWideField(form, "Username", usernameField);
        addWideField(form, "Password", passwordField);
        addLoginButtons(form);
        add(form);
    }

    private TextField createPasswordField() {
        TextField field = new TextField("", skin);
        field.setPasswordMode(true);
        field.setPasswordCharacter('*');
        return field;
    }

    private Table createPanel(String title) {
        Table form = new Table();
        form.setBackground(skin.getDrawable("image_ui_dialog_asset_dialogborder_10"));
        form.pad(30f);
        form.add(new Label(title, skin, "big_outline"))
                .colspan(2).padBottom(20f).row();
        return form;
    }

    private void addWideField(Table form, String title, Actor field) {
        form.add(new Label(title, skin)).colspan(2).left().row();
        form.add(field).colspan(2).width(420f).height(48f)
                .padBottom(12f).row();
    }

    private void addLoginButtons(Table form) {
        TextButton stayButton = new TextButton("Stay logged in: No", skin, "brown");
        TextButton loginButton = new TextButton("Login", skin, "green");
        TextButton forgotButton = new TextButton("Forgot password", skin, "brown");
        TextButton registerButton = new TextButton("Create an account", skin, "brown");

        form.add(stayButton).width(205f).height(52f).padRight(8f);
        form.add(loginButton).width(205f).height(52f).row();
        form.add(forgotButton).width(205f).height(52f).padTop(8f).padRight(8f);
        form.add(registerButton).width(205f).height(52f).padTop(8f).row();
        attachLoginListeners(stayButton, loginButton, forgotButton, registerButton);
    }

    private void attachLoginListeners(TextButton stayButton, TextButton loginButton,
                                      TextButton forgotButton, TextButton registerButton) {
        stayButton.addListener(listener(() -> toggleStayLoggedIn(stayButton)));
        loginButton.addListener(listener(this::submitLogin));
        forgotButton.addListener(listener(
                () -> transitionContent(this::buildForgotPasswordForm)
        ));
        registerButton.addListener(listener(() -> App.setMenuState(MenuState.REGISTER)));
    }

    private ChangeListener listener(Runnable action) {
        return new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        };
    }

    private void toggleStayLoggedIn(TextButton button) {
        stayLoggedIn = !stayLoggedIn;
        button.setText("Stay logged in: " + (stayLoggedIn ? "Yes" : "No"));
    }

    private void submitLogin() {
        LoginRequest request = new LoginRequest(
                usernameField.getText().trim(), passwordField.getText(), stayLoggedIn);
        LoginResult result = controller.login(request);
        if (result.status() == LoginStatus.LOGIN_SUCCESS) {
            NotificationCenter.success(result.message());
        } else {
            NotificationCenter.error(result.message());
        }
    }

    private void buildForgotPasswordForm() {
        clearChildren();
        TextField resetUsername = new TextField("", skin);
        TextField resetEmail = new TextField("", skin);
        Table form = createPanel("Reset Password");
        addWideField(form, "Username", resetUsername);
        addWideField(form, "Email", resetEmail);

        TextButton continueButton = new TextButton("Continue", skin, "green");
        TextButton backButton = new TextButton("Back", skin, "brown");
        addButtonPair(form, backButton, continueButton);
        backButton.addListener(listener(
                () -> transitionContent(this::buildLoginForm)
        ));
        continueButton.addListener(listener(() -> submitIdentity(resetUsername, resetEmail)));
        add(form);
    }

    private void addButtonPair(Table form, TextButton left, TextButton right) {
        form.add(left).width(190f).height(52f).padRight(8f);
        form.add(right).width(220f).height(52f).row();
    }

    private void submitIdentity(TextField username, TextField email) {
        ForgetPasswordRequest request = new ForgetPasswordRequest(
                username.getText().trim(), email.getText().trim());
        LoginResult result = controller.forgetPassword(request);
        if (result.status() == LoginStatus.SECURITY_QUESTION) {
            transitionContent(() -> buildAnswerForm(result.message()));
        } else {
            NotificationCenter.error(result.message());
        }
    }

    private void buildAnswerForm(String question) {
        clearChildren();
        TextField answerField = new TextField("", skin);
        Table form = createPanel("Security Question");
        form.add(new Label(question, skin)).colspan(2).width(420f).padBottom(12f).row();
        addWideField(form, "Answer", answerField);

        TextButton backButton = new TextButton("Back", skin, "brown");
        TextButton continueButton = new TextButton("Continue", skin, "green");
        addButtonPair(form, backButton, continueButton);
        backButton.addListener(listener(
                () -> transitionContent(this::buildLoginForm)
        ));
        continueButton.addListener(listener(() -> submitAnswer(answerField)));
        add(form);
    }

    private void submitAnswer(TextField answerField) {
        LoginResult result = controller.answer(new AnswerRequest(answerField.getText().trim()));
        if (result.status() == LoginStatus.ANSWER_ACCEPTED) {
            NotificationCenter.success(result.message());
            transitionContent(this::buildNewPasswordForm);
        } else {
            NotificationCenter.error(result.message());
        }
    }

    private void buildNewPasswordForm() {
        clearChildren();
        TextField newPassword = createPasswordField();
        TextField confirmPassword = createPasswordField();
        Table form = createPanel("New Password");
        addWideField(form, "New Password", newPassword);
        addWideField(form, "Confirm Password", confirmPassword);

        TextButton saveButton = new TextButton("Save Password", skin, "green");
        form.add(saveButton).colspan(2).width(240f).height(52f).row();
        saveButton.addListener(listener(() -> submitNewPassword(newPassword, confirmPassword)));
        add(form);
    }

    private void submitNewPassword(TextField password, TextField confirmation) {
        if (!password.getText().equals(confirmation.getText())) {
            NotificationCenter.error("Passwords do not match.");
            return;
        }
        LoginResult result = controller.resetPassword(password.getText());
        if (result.status() == LoginStatus.PASSWORD_RESET) {
            NotificationCenter.success(result.message());
            transitionContent(this::buildLoginForm);
        } else {
            NotificationCenter.error(result.message());
        }
    }
}
