package com.ussr.pvz.controller.maincontroller;

import com.ussr.pvz.controller.command.maincommand.ProfileCommand;
import com.ussr.pvz.model.dto.ChangeUsernameRequest;
import com.ussr.pvz.model.dto.ChangeNicknameRequest;
import com.ussr.pvz.model.dto.ChangeEmailRequest;
import com.ussr.pvz.model.dto.ChangePasswordRequest;

import java.util.regex.Matcher;

public class ProfileController {

    public ProfileController() {
    }

    public String handleCommand(String command) {
        for (ProfileCommand cmd : ProfileCommand.values()) {
            Matcher matcher = cmd.getMatcher(command);
            if (matcher.matches()) {
                return switch (cmd) {
                    case CHANGE_USERNAME -> handleChangeUsername(matcher);
                    case CHANGE_NICKNAME -> handleChangeNickname(matcher);
                    case CHANGE_EMAIL -> handleChangeEmail(matcher);
                    case CHANGE_PASSWORD -> handleChangePassword(matcher);
                    case SHOW_INFO -> handleShowInfo();
                };
            }
        }
        return "";
    }

    private String handleChangeUsername(Matcher matcher) {
        ChangeUsernameRequest request = new ChangeUsernameRequest(matcher.group("username"));
        // TODO: call profileService.changeUsername(request) and return its message
        return "";
    }

    private String handleChangeNickname(Matcher matcher) {
        ChangeNicknameRequest request = new ChangeNicknameRequest(matcher.group("nickname"));
        // TODO: call profileService.changeNickname(request) and return its message
        return "";
    }

    private String handleChangeEmail(Matcher matcher) {
        ChangeEmailRequest request = new ChangeEmailRequest(matcher.group("email"));
        // TODO: call profileService.changeEmail(request) and return its message
        return "";
    }

    private String handleChangePassword(Matcher matcher) {
        ChangePasswordRequest request = new ChangePasswordRequest(
                matcher.group("newPassword"),
                matcher.group("oldPassword")
        );
        // TODO: call profileService.changePassword(request) and return its message
        return "";
    }

    private String handleShowInfo() {
        // TODO: call profileService.showInfo() and return its message
        return "";
    }
}