package com.ussr.pvz.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.dto.AnswerRequest;
import com.ussr.pvz.model.dto.ForgetPasswordRequest;
import com.ussr.pvz.model.dto.LoginRequest;

public class LoginService {

    private Account pendingPasswordReset;

    public String login(LoginRequest request) {
        Account account = findAccountByUsername(request.username());

        if (account == null) {
            return "username not found";
        }

        if (!account.getPassword().equals(request.password())) {
            return "invalid password";
        }

        App.login(account);
        return "logged in successfully";
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

        String newPassword = pendingPasswordReset.getPassword();
        pendingPasswordReset = null;
        return "your password is: " + newPassword;
    }

    private Account findAccountByUsername(String username) {
        return App.getAccounts().stream()
                .filter(a -> a.getName().equals(username))
                .findFirst()
                .orElse(null);
    }
}