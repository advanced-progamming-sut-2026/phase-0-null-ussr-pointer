package com.ussr.pvz.service;

import com.ussr.pvz.controller.command.ValidationRegex;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.AccountState;
import com.ussr.pvz.model.dto.AnswerRequest;
import com.ussr.pvz.model.dto.ForgetPasswordRequest;
import com.ussr.pvz.model.dto.LoginRequest;
import com.ussr.pvz.model.dto.LoginResult;
import com.ussr.pvz.model.dto.enums.LoginStatus;
import com.ussr.pvz.model.util.SecurityUtil;
import com.ussr.pvz.model.util.SessionManager;

import java.util.List;

public class LoginService {

    private Account pendingPasswordReset;
    private boolean waitingForNewPass = false;

    public LoginResult login(LoginRequest request) {
        Account account = findAccountByUsername(request.username());

        if (account == null) {
            return LoginResult.error("Username not found.");
        }

        if (!account.getPassword().equals(SecurityUtil.hashPassword(request.password()))) {
            return LoginResult.error("Invalid password.");
        }

        if (request.stayLoggedIn()) {
            SessionManager.saveSession(account.getName());
        }

        // Update login time and check for daily resets
        account.updateLoginTime();

        App.login(account);

        App.setMenuState(MenuState.MAIN);
        return LoginResult.of(LoginStatus.LOGIN_SUCCESS, "Logged in successfully.");
    }


    public LoginResult forgetPassword(ForgetPasswordRequest request) {
        Account account = findAccountByUsername(request.username());

        if (account == null) {
            return LoginResult.error("Username not found.");
        }

        if (!account.getEmail().equals(request.email())) {
            return LoginResult.error("Invalid email.");
        }

        if (account.getSecurityQuestion() == null) {
            return LoginResult.error("This account has no security question.");
        }

        pendingPasswordReset = account;
        String question = account.getSecurityQuestion().getText();
        return LoginResult.of(LoginStatus.SECURITY_QUESTION, question);
    }

    public LoginResult answer(AnswerRequest request) {
        if (pendingPasswordReset == null) {
            return LoginResult.error("No active password reset.");
        }

        if (!pendingPasswordReset.getSecurityAnswer().equals(request.answer())) {
            return LoginResult.error("Wrong security answer.");
        }

        waitingForNewPass = true;
        return LoginResult.of(LoginStatus.ANSWER_ACCEPTED, "Answer accepted.");
    }

    public LoginResult resetPassword(String newPass) {
        if(!waitingForNewPass)
            return LoginResult.error("No active password reset.");

        String validationError = validatePassword(newPass);
        if (validationError != null) return LoginResult.error(validationError);

        String hashedPass = SecurityUtil.hashPassword(newPass);
        pendingPasswordReset.setPassword(hashedPass);

        List<AccountState> updatedStates = App.getAccounts().stream()
                .map(Account::toState)
                .toList();
        SaveService.saveAccounts(updatedStates);

        waitingForNewPass = false;
        pendingPasswordReset = null;

        return LoginResult.of(LoginStatus.PASSWORD_RESET, "Password updated successfully.");
    }

    private String validatePassword(String password) {
        if (!validPasswordLength(password)) return "Invalid password length.";
        if (!validPasswordLower(password)) return "Password must contain a lowercase character.";
        if (!validPasswordUpper(password)) return "Password must contain an uppercase character.";
        if (!validPasswordNumber(password)) return "Password must contain a number.";
        if (!validPasswordSpecific(password)) return "Password must contain a special character.";
        return null;
    }

    private Account findAccountByUsername(String username) {
        return App.getAccounts().stream()
                .filter(a -> a.getName().equals(username))
                .findFirst()
                .orElse(null);
    }

    private boolean validPasswordLength(String password) {
        return ValidationRegex.VALID_PASSWORD_LENGTH.matchToRegex(password).matches();
    }

    private boolean validPasswordLower(String password) {
        return ValidationRegex.VALID_PASSWORD_LOWER.matchToRegex(password).matches();
    }

    private boolean validPasswordUpper(String password) {
        return ValidationRegex.VALID_PASSWORD_UPPER.matchToRegex(password).matches();
    }

    private boolean validPasswordSpecific(String password) {
        return ValidationRegex.VALID_PASSWORD_SPECIFIC_CHARACTER.matchToRegex(password).matches();
    }

    private boolean validPasswordNumber(String password) {
        return ValidationRegex.VALID_PASSWORD_NUMBER.matchToRegex(password).matches();
    }

}
