package com.ussr.pvz.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.Collection;
import com.ussr.pvz.model.util.SessionManager;

import com.ussr.pvz.network.NetworkClient;

import com.ussr.pvz.shared.account.AccountState;
import com.ussr.pvz.shared.dto.AnswerRequest;
import com.ussr.pvz.shared.dto.ForgetPasswordRequest;
import com.ussr.pvz.shared.dto.LoginRequest;
import com.ussr.pvz.shared.dto.LoginResult;
import com.ussr.pvz.shared.dto.enums.LoginStatus;

import com.ussr.pvz.shared.network.NetworkRequest;
import com.ussr.pvz.shared.network.NetworkResponse;
import com.ussr.pvz.shared.network.RequestType;

import java.util.ArrayList;

public class LoginService {

    private final NetworkClient networkClient;

    private final Gson gson =
            new Gson();


    public LoginService() {

        this.networkClient =
                NetworkClient.getInstance();
    }


    // =========================================================
    // LOGIN
    // =========================================================

    public LoginResult login(
            LoginRequest loginRequest
    ) {

        JsonObject data =
                new JsonObject();

        data.addProperty(
                "username",
                loginRequest.username()
        );

        data.addProperty(
                "password",
                loginRequest.password()
        );

        data.addProperty("stayLoggedIn", loginRequest.stayLoggedIn());

        NetworkRequest request =
                new NetworkRequest(
                        RequestType.LOGIN,
                        data
                );

        NetworkResponse response =
                send(request);

        if (response == null) {

            return LoginResult.error(
                    "Could not connect to server."
            );
        }

        if (response.getData() == null ||
                !response.getData()
                        .has("status")) {

            return LoginResult.error(
                    response.getMessage()
            );
        }

        LoginStatus status;

        try {

            status =
                    LoginStatus.valueOf(
                            response
                                    .getData()
                                    .get("status")
                                    .getAsString()
                    );

        } catch (IllegalArgumentException e) {

            return LoginResult.error(
                    response.getMessage()
            );
        }


        // -----------------------------------------
        // Successful login
        // -----------------------------------------

        if (status ==
                LoginStatus.LOGIN_SUCCESS) {

            if (!response
                    .getData()
                    .has("token")) {

                return LoginResult.error(
                        "Server did not return a session token."
                );
            }

            String token =
                    response
                            .getData()
                            .get("token")
                            .getAsString();

            /*
             * Save the SERVER session token.
             *
             * LoginService, ProfileService,
             * GlobalService, etc. will all use
             * SessionManager.getToken().
             */
            SessionManager.saveSession(token, loginRequest.stayLoggedIn());


            /*
             * The server should also return the
             * currently logged-in account state.
             *
             * This keeps all of your existing
             * App.getAccount() game logic working.
             */
            if (response
                    .getData()
                    .has("accountState")) {

                try {

                    AccountState state =
                            gson.fromJson(
                                    response
                                            .getData()
                                            .get(
                                                    "accountState"
                                            ),
                                    AccountState.class
                            );

                    Account account =
                            new Account(
                                    state,
                                    new Collection(
                                            new ArrayList<>(),
                                            new ArrayList<>()
                                    )
                            );

                    App.login(
                            account
                    );

                } catch (Exception e) {

                    /*
                     * The authentication itself already
                     * succeeded, but without account data
                     * the rest of the game cannot work
                     * correctly.
                     */

                    SessionManager.clearSession();

                    return LoginResult.error(
                            "Could not load account data."
                    );
                }

            } else {

                SessionManager.clearSession();

                return LoginResult.error(
                        "Server did not return account data."
                );
            }
        }


        return LoginResult.of(
                status,
                response.getMessage()
        );
    }

    public LoginResult restoreSession() {
        String token = SessionManager.getToken();
        if (token == null || token.isBlank()) {
            return LoginResult.error("No saved session.");
        }

        NetworkRequest request = new NetworkRequest(RequestType.AUTH_TOKEN, token, null);
        NetworkResponse response = send(request);

        if (response == null || !response.isSuccess()) {
            SessionManager.clearSession(); // stale/expired token — don't keep retrying it
            return LoginResult.error(response != null ? response.getMessage() : "Could not connect to server.");
        }

        if (response.getData() == null || !response.getData().has("accountState")) {
            SessionManager.clearSession();
            return LoginResult.error("Could not load account data.");
        }

        try {
            AccountState state = gson.fromJson(response.getData().get("accountState"), AccountState.class);
            Account account = new Account(state, new Collection(new ArrayList<>(), new ArrayList<>()));
            App.login(account);
            return LoginResult.of(LoginStatus.LOGIN_SUCCESS, "Session restored.");
        } catch (Exception e) {
            SessionManager.clearSession();
            return LoginResult.error("Could not load account data.");
        }
    }


    // =========================================================
    // FORGOT PASSWORD
    // =========================================================

    public LoginResult forgetPassword(
            ForgetPasswordRequest forgotRequest
    ) {

        JsonObject data =
                new JsonObject();

        data.addProperty(
                "username",
                forgotRequest.username()
        );

        data.addProperty(
                "email",
                forgotRequest.email()
        );

        NetworkRequest request =
                new NetworkRequest(
                        RequestType.FORGOT_PASSWORD,
                        data
                );

        NetworkResponse response =
                send(request);

        return convertToLoginResult(
                response
        );
    }


    // =========================================================
    // SECURITY ANSWER
    // =========================================================

    public LoginResult answer(
            AnswerRequest answerRequest
    ) {

        JsonObject data =
                new JsonObject();

        data.addProperty(
                "answer",
                answerRequest.answer()
        );

        NetworkRequest request =
                new NetworkRequest(
                        RequestType.ANSWER_SECURITY_QUESTION,
                        data
                );

        NetworkResponse response =
                send(request);

        return convertToLoginResult(
                response
        );
    }


    // =========================================================
    // RESET PASSWORD
    // =========================================================

    public LoginResult resetPassword(
            String newPassword
    ) {

        JsonObject data =
                new JsonObject();

        data.addProperty(
                "newPassword",
                newPassword
        );

        NetworkRequest request =
                new NetworkRequest(
                        RequestType.RESET_PASSWORD,
                        data
                );

        NetworkResponse response =
                send(request);

        return convertToLoginResult(
                response
        );
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    public boolean logout() {

        String token =
                SessionManager.getToken();

        if (token == null ||
                token.isBlank()) {

            return false;
        }

        NetworkRequest request =
                new NetworkRequest(
                        RequestType.LOGOUT,
                        token,
                        null
                );

        NetworkResponse response =
                send(request);

        if (response == null ||
                !response.isSuccess()) {

            return false;
        }

        /*
         * Remove local authentication information
         * only after server logout succeeds.
         */
        SessionManager.clearSession();

        App.logout();

        return true;
    }


    // =========================================================
    // SESSION
    // =========================================================

    public String getToken() {

        return SessionManager.getToken();
    }


    public boolean isLoggedIn() {

        return SessionManager.isLoggedIn();
    }


    // =========================================================
    // NETWORK
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


    // =========================================================
    // RESULT CONVERSION
    // =========================================================

    private LoginResult convertToLoginResult(
            NetworkResponse response
    ) {

        if (response == null) {

            return LoginResult.error(
                    "Could not connect to server."
            );
        }

        if (response.getData() == null ||
                !response
                        .getData()
                        .has("status")) {

            return LoginResult.error(
                    response.getMessage()
            );
        }

        try {

            LoginStatus status =
                    LoginStatus.valueOf(
                            response
                                    .getData()
                                    .get("status")
                                    .getAsString()
                    );

            return LoginResult.of(
                    status,
                    response.getMessage()
            );

        } catch (IllegalArgumentException e) {

            return LoginResult.error(
                    response.getMessage()
            );
        }
    }
}