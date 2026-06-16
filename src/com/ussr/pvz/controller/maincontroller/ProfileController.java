package com.ussr.pvz.controller.maincontroller;

import com.ussr.pvz.controller.command.maincommand.ProfileCommand;
import com.ussr.pvz.model.dto.ChangeEmailRequest;
import com.ussr.pvz.model.dto.ChangeNicknameRequest;
import com.ussr.pvz.model.dto.ChangePasswordRequest;
import com.ussr.pvz.model.dto.ChangeUsernameRequest;
import com.ussr.pvz.service.ProfileService;

import java.util.regex.Matcher;

public class ProfileController {

    private final ProfileService profileService = new ProfileService();

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
        return profileService.changeUsername(request);
    }

    private String handleChangeNickname(Matcher matcher) {
        ChangeNicknameRequest request = new ChangeNicknameRequest(matcher.group("nickname"));
        return profileService.changeNickname(request);
    }

    private String handleChangeEmail(Matcher matcher) {
        ChangeEmailRequest request = new ChangeEmailRequest(matcher.group("email"));
        return profileService.changeEmail(request);
    }

    private String handleChangePassword(Matcher matcher) {
        ChangePasswordRequest request = new ChangePasswordRequest(
                matcher.group("newPassword"),
                matcher.group("oldPassword")
        );
        return profileService.changePassword(request);
    }

    private String handleShowInfo() {
        return profileService.showInfo();
    }
}