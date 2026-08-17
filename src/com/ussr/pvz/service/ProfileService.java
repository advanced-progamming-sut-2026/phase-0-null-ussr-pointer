package com.ussr.pvz.service;

import com.google.gson.JsonObject;
import com.ussr.pvz.model.util.SessionManager;
import com.ussr.pvz.network.NetworkClient;
import com.ussr.pvz.shared.dto.ChangeEmailRequest;
import com.ussr.pvz.shared.dto.ChangeNicknameRequest;
import com.ussr.pvz.shared.dto.ChangePasswordRequest;
import com.ussr.pvz.shared.dto.ChangeUsernameRequest;
import com.ussr.pvz.shared.network.NetworkRequest;
import com.ussr.pvz.shared.network.NetworkResponse;
import com.ussr.pvz.shared.network.RequestType;

public class ProfileService {

    private final NetworkClient networkClient;

    public ProfileService() {

        this.networkClient =
                NetworkClient.getInstance();
    }


    // =========================================================
    // SHOW PROFILE
    // =========================================================

    public String showInfo() {

        String token =
                SessionManager.getToken();

        if (token == null ||
                token.isBlank()) {

            return "you are not logged in";
        }

        NetworkRequest request =
                new NetworkRequest(
                        RequestType.GET_PROFILE,
                        token,
                        null
                );

        NetworkResponse response =
                send(request);

        if (response == null) {

            return "Could not connect to server.";
        }

        return response.getMessage();
    }


    // =========================================================
    // CHANGE USERNAME
    // =========================================================

    public String changeUsername(
            ChangeUsernameRequest changeRequest
    ) {

        String token =
                SessionManager.getToken();

        if (token == null ||
                token.isBlank()) {

            return "you are not logged in";
        }

        JsonObject data =
                new JsonObject();

        data.addProperty(
                "username",
                changeRequest.username()
        );

        NetworkRequest request =
                new NetworkRequest(
                        RequestType.CHANGE_USERNAME,
                        token,
                        data
                );

        NetworkResponse response =
                send(request);

        return getMessage(response);
    }


    // =========================================================
    // CHANGE NICKNAME
    // =========================================================

    public String changeNickname(
            ChangeNicknameRequest changeRequest
    ) {

        String token =
                SessionManager.getToken();

        if (token == null ||
                token.isBlank()) {

            return "you are not logged in";
        }

        JsonObject data =
                new JsonObject();

        data.addProperty(
                "nickname",
                changeRequest.nickname()
        );

        NetworkRequest request =
                new NetworkRequest(
                        RequestType.CHANGE_NICKNAME,
                        token,
                        data
                );

        NetworkResponse response =
                send(request);

        return getMessage(response);
    }


    // =========================================================
    // CHANGE EMAIL
    // =========================================================

    public String changeEmail(
            ChangeEmailRequest changeRequest
    ) {

        String token =
                SessionManager.getToken();

        if (token == null ||
                token.isBlank()) {

            return "you are not logged in";
        }

        JsonObject data =
                new JsonObject();

        data.addProperty(
                "email",
                changeRequest.email()
        );

        NetworkRequest request =
                new NetworkRequest(
                        RequestType.CHANGE_EMAIL,
                        token,
                        data
                );

        NetworkResponse response =
                send(request);

        return getMessage(response);
    }


    // =========================================================
    // CHANGE PASSWORD
    // =========================================================

    public String changePassword(
            ChangePasswordRequest changeRequest
    ) {

        String token =
                SessionManager.getToken();

        if (token == null ||
                token.isBlank()) {

            return "you are not logged in";
        }

        JsonObject data =
                new JsonObject();

        data.addProperty(
                "newPassword",
                changeRequest.newPassword()
        );

        data.addProperty(
                "oldPassword",
                changeRequest.oldPassword()
        );

        NetworkRequest request =
                new NetworkRequest(
                        RequestType.CHANGE_PASSWORD,
                        token,
                        data
                );

        NetworkResponse response =
                send(request);

        return getMessage(response);
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private NetworkResponse send(
            NetworkRequest request
    ) {

        try {

            return networkClient.send(
                    request
            );

        } catch (Exception e) {

            System.err.println(
                    "Network error: "
                            + e.getMessage()
            );

            return null;
        }
    }


    private String getMessage(
            NetworkResponse response
    ) {

        if (response == null) {

            return "Could not connect to server.";
        }

        return response.getMessage();
    }
}