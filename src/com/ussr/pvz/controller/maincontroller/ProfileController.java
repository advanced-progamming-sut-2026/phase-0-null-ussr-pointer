package com.ussr.pvz.controller.maincontroller;

import com.ussr.pvz.service.ProfileService;
import com.ussr.pvz.shared.dto.ChangeEmailRequest;
import com.ussr.pvz.shared.dto.ChangeNicknameRequest;
import com.ussr.pvz.shared.dto.ChangePasswordRequest;
import com.ussr.pvz.shared.dto.ChangeUsernameRequest;


public class ProfileController {

    private final ProfileService profileService =
            new ProfileService();

    public ProfileController() {
    }

    public String changeUsername(
            String username
    ) {

        ChangeUsernameRequest request =
                new ChangeUsernameRequest(
                        username.trim()
                );

        return profileService
                .changeUsername(
                        request
                );
    }


    public String changeNickname(
            String nickname
    ) {

        ChangeNicknameRequest request =
                new ChangeNicknameRequest(
                        nickname.trim()
                );

        return profileService
                .changeNickname(
                        request
                );
    }


    public String changeEmail(
            String email
    ) {

        ChangeEmailRequest request =
                new ChangeEmailRequest(
                        email.trim()
                );

        return profileService
                .changeEmail(
                        request
                );
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

        return profileService
                .changePassword(
                        request
                );
    }
}