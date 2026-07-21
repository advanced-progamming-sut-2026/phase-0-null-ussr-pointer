package com.ussr.pvz.service;

import com.ussr.pvz.controller.command.ValidationRegex;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.dto.ChangeEmailRequest;
import com.ussr.pvz.model.dto.ChangeNicknameRequest;
import com.ussr.pvz.model.dto.ChangePasswordRequest;
import com.ussr.pvz.model.dto.ChangeUsernameRequest;
import com.ussr.pvz.model.util.SecurityUtil;
import com.ussr.pvz.model.util.SessionManager;

import java.util.Objects;

public class ProfileService {

    public String changeUsername(ChangeUsernameRequest request) {
        Account account = App.getAccount();

        if (!ValidationRegex.VALID_USERNAME.matchToRegex(request.username()).matches())
            return "invalid username";

        if (App.getAccounts().stream().anyMatch(a -> a.getName().equals(request.username())))
            return "username already exists";
        if(Objects.equals(SessionManager.getAutoLoginUsername(), account.getName())) {
            SessionManager.saveSession(request.username());
        }
        account.setName(request.username());

        return "username changed successfully";
    }

    public String changeNickname(ChangeNicknameRequest request) {
        Account account = App.getAccount();

        if (!ValidationRegex.VALID_NICKNAME.matchToRegex(request.nickname()).matches())
            return "invalid nickname length";

        account.setNickname(request.nickname());
        return "nickname changed successfully";
    }

    public String changeEmail(ChangeEmailRequest request) {
        Account account = App.getAccount();

        if (!ValidationRegex.VALID_EMAIL.matchToRegex(request.email()).matches())
            return "invalid email format";

        account.setEmail(request.email());
        return "email changed successfully";
    }

    public String changePassword(ChangePasswordRequest request) {
        Account account = App.getAccount();

        if (!account.getPassword().equals(SecurityUtil.hashPassword(request.oldPassword())))
            return "old password is incorrect";

        if (!ValidationRegex.VALID_PASSWORD_LENGTH.matchToRegex(request.newPassword()).matches())
            return "invalid password length";
        if (!ValidationRegex.VALID_PASSWORD_LOWER.matchToRegex(request.newPassword()).matches())
            return "password must contain a lowercase character";
        if (!ValidationRegex.VALID_PASSWORD_UPPER.matchToRegex(request.newPassword()).matches())
            return "password must contain an uppercase character";
        if (!ValidationRegex.VALID_PASSWORD_NUMBER.matchToRegex(request.newPassword()).matches())
            return "password must contain a number";
        if (!ValidationRegex.VALID_PASSWORD_SPECIFIC_CHARACTER.matchToRegex(request.newPassword()).matches())
            return "password must contain a specific character";

        account.setPassword(SecurityUtil.hashPassword(
                request.newPassword()));
        return "password changed successfully";
    }

    public String showInfo() {
        Account account = App.getAccount();
        return "username: " + account.getName() + "\n" +
                "nickname: " + account.getNickname() + "\n" +
                "email: " + account.getEmail() + "\n" +
                "gender: " + account.getGender().name().toLowerCase() + "\n" +
                "coin: " + account.getAdventureProgress().getCoin() + "\n" +
                "gems: " + account.getAdventureProgress().getGem() + "\n" +
                "mewo points: " + account.getScoreRecord().getScore() + "\n" +
                "current chapter: " + account.getAdventureProgress().getCurrentChapter() + "\n" +
                "current level: " + account.getAdventureProgress().getCurrentLvl();
    }
}