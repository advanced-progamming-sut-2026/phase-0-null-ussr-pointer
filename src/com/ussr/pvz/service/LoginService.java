package com.ussr.pvz.service;

import com.ussr.pvz.controller.command.ValidationRegex;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.AccountState;
import com.ussr.pvz.model.dto.AnswerRequest;
import com.ussr.pvz.model.dto.ForgetPasswordRequest;
import com.ussr.pvz.model.dto.LoginRequest;
import com.ussr.pvz.model.util.SecurityUtil;

import java.util.List;

public class LoginService {

    private Account pendingPasswordReset;
    private boolean waitingForNewPass = false;

    public String login(LoginRequest request) {
        Account account = findAccountByUsername(request.username());

        if (account == null) {
            return "username not found";
        }

        if (!account.getPassword().equals(SecurityUtil.hashPassword(request.password()))) {
            return "invalid password";
        }

        App.login(account);
        return "logged in successfully";
        //check if user wants to stay logged in
    }

    public String forgetPassword(ForgetPasswordRequest request) {
        Account account = findAccountByUsername(request.username());

        if (account == null) {
            return "username not found";
        }

        if (!account.getEmail().equals(request.email())) {
            return "invalid email";
        }

        pendingPasswordReset = account;
        return "security question: " + account.getSecurityQuestion();
    }

    public String answer(AnswerRequest request) {
        if (pendingPasswordReset == null) {
            return "no active password reset";
        }

        if (!pendingPasswordReset.getSecurityAnswer().equals(request.answer())) {
            pendingPasswordReset = null;
            return "wrong answer";
        }

        waitingForNewPass = true;
        return "Enter your new password:";
    }

    public String resetPassword(String newPass) {
        if(!waitingForNewPass)
            return "Invalid Command";

        if (!validPasswordLength(newPass))
            return "invalid password length";
        else if (!validPasswordLower(newPass))
            return "password must contain a lowercase character";
        else if (!validPasswordUpper(newPass))
            return "password must contain an uppercase character";
        else if (!validPasswordNumber(newPass))
            return "password must contain a number";
        else if (!validPasswordSpecific(newPass))
            return "password must contain a specific character";

        String hashedPass = SecurityUtil.hashPassword(newPass);
        pendingPasswordReset.setPassword(hashedPass);

        List<AccountState> updatedStates = App.getAccounts().stream()
                .map(Account::toState)
                .toList();
        SaveService.saveAccounts(updatedStates);

        waitingForNewPass = false;
        pendingPasswordReset = null;

        return "Your password updated successfully now you can login to the game with your fresh password!";
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