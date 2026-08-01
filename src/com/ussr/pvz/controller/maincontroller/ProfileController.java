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
        return changeUsername(matcher.group("username"));
    }

    private String handleChangeNickname(Matcher matcher) {
        return changeNickname(matcher.group("nickname"));
    }

    private String handleChangeEmail(Matcher matcher) {
        return changeEmail(matcher.group("email"));
    }

    private String handleChangePassword(Matcher matcher) {
        return changePassword(
                matcher.group("oldPassword"),
                matcher.group("newPassword")
        );
    }

    private String handleShowInfo() {
        return profileService.showInfo();
    }

    public String changeUsername(String username) {
        ChangeUsernameRequest request =
                new ChangeUsernameRequest(username.trim());

        return profileService.changeUsername(request);
    }

    public String changeNickname(String nickname) {
        ChangeNicknameRequest request =
                new ChangeNicknameRequest(nickname.trim());

        return profileService.changeNickname(request);
    }

    public String changeEmail(String email) {
        ChangeEmailRequest request =
                new ChangeEmailRequest(email.trim());

        return profileService.changeEmail(request);
    }

    public String changePassword(
            String oldPassword,
            String newPassword
    ) {
        ChangePasswordRequest request =
                new ChangePasswordRequest(
                        newPassword,
                        oldPassword
                );

        return profileService.changePassword(request);
    }
}