package com.ussr.pvz.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.ussr.pvz.model.account.Account;

import com.ussr.pvz.server.account.AuthService;
import com.ussr.pvz.server.account.PasswordResetStartResult;
import com.ussr.pvz.server.account.PendingRegistrationResult;
import com.ussr.pvz.server.account.ServerLoginResult;

import com.ussr.pvz.shared.account.AccountState;

import com.ussr.pvz.shared.dto.AnswerRequest;
import com.ussr.pvz.shared.dto.ChangeEmailRequest;
import com.ussr.pvz.shared.dto.ChangeNicknameRequest;
import com.ussr.pvz.shared.dto.ChangePasswordRequest;
import com.ussr.pvz.shared.dto.ChangeUsernameRequest;
import com.ussr.pvz.shared.dto.ForgetPasswordRequest;
import com.ussr.pvz.shared.dto.LoginRequest;
import com.ussr.pvz.shared.dto.LoginResult;
import com.ussr.pvz.shared.dto.PickQuestionRequest;
import com.ussr.pvz.shared.dto.RegisterRequest;
import com.ussr.pvz.shared.dto.RegistrationResult;
import com.ussr.pvz.shared.dto.UserInfo;

import com.ussr.pvz.shared.dto.enums.LoginStatus;
import com.ussr.pvz.shared.dto.enums.RegistrationStatus;

import com.ussr.pvz.shared.network.NetworkRequest;
import com.ussr.pvz.shared.network.NetworkResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final AuthService authService;

    private BufferedReader reader;
    private PrintWriter writer;

    private final Gson gson = new Gson();


    // -------------------------
    // Per-client temporary state
    // -------------------------

    private AccountState pendingRegistration;

    private Account pendingPasswordReset;

    private boolean passwordResetAnswerAccepted = false;


    public ClientHandler(
            Socket socket,
            AuthService authService
    ) {

        this.socket = socket;
        this.authService = authService;
    }


    @Override
    public void run() {

        try {

            reader = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()
                    )
            );

            writer = new PrintWriter(
                    socket.getOutputStream(),
                    true
            );

            String line;

            while ((line = reader.readLine()) != null) {

                NetworkResponse response;

                try {

                    NetworkRequest request =
                            gson.fromJson(
                                    line,
                                    NetworkRequest.class
                            );

                    response =
                            handleRequest(request);

                } catch (Exception e) {

                    response =
                            NetworkResponse.error(
                                    "Invalid request: "
                                            + e.getMessage()
                            );
                }

                writer.println(
                        gson.toJson(response)
                );
            }

        } catch (IOException e) {

            System.out.println(
                    "Client disconnected: "
                            + socket.getInetAddress()
            );

        } finally {

            closeConnection();
        }
    }


    private NetworkResponse handleRequest(
            NetworkRequest request
    ) {

        if (request == null ||
                request.getType() == null) {

            return NetworkResponse.error(
                    "Invalid request."
            );
        }

        return switch (request.getType()) {

            case PING ->
                    NetworkResponse.success(
                            "PONG"
                    );


            case LOGIN ->
                    handleLogin(request);


            case REGISTER ->
                    handleRegister(request);


            case COMPLETE_REGISTRATION ->
                    handleCompleteRegistration(
                            request
                    );


            case FORGOT_PASSWORD ->
                    handleForgotPassword(
                            request
                    );


            case ANSWER_SECURITY_QUESTION ->
                    handleSecurityAnswer(
                            request
                    );


            case RESET_PASSWORD ->
                    handleResetPassword(
                            request
                    );


            case LOGOUT ->
                    handleLogout(
                            request
                    );


            case GET_PROFILE ->
                    handleGetProfile(
                            request
                    );


            case CHANGE_USERNAME ->
                    handleChangeUsername(
                            request
                    );


            case CHANGE_NICKNAME ->
                    handleChangeNickname(
                            request
                    );


            case CHANGE_EMAIL ->
                    handleChangeEmail(
                            request
                    );


            case CHANGE_PASSWORD ->
                    handleChangePassword(
                            request
                    );


            default ->
                    NetworkResponse.error(
                            "Request not implemented."
                    );
        };
    }


    // =========================================================
    // LOGIN
    // =========================================================

    private NetworkResponse handleLogin(
            NetworkRequest request
    ) {

        if (request.getData() == null) {

            return NetworkResponse.error(
                    "Missing login data."
            );
        }

        LoginRequest loginRequest =
                gson.fromJson(
                        request.getData(),
                        LoginRequest.class
                );

        ServerLoginResult serverResult =
                authService
                        .getLoginService()
                        .login(loginRequest);

        LoginResult loginResult =
                serverResult.result();

        if (serverResult.account() == null ||
                serverResult.token() == null) {

            JsonObject data =
                    new JsonObject();

            data.addProperty(
                    "status",
                    loginResult.status().name()
            );

            return new NetworkResponse(
                    false,
                    loginResult.message(),
                    data
            );
        }

        Account account =
                serverResult.account();

        UserInfo userInfo =
                createUserInfo(account);

        JsonObject data =
                new JsonObject();

        data.addProperty(
                "status",
                loginResult.status().name()
        );

        data.addProperty(
                "token",
                serverResult.token()
        );

        data.add(
                "user",
                gson.toJsonTree(userInfo)
        );

        return NetworkResponse.success(
                loginResult.message(),
                data
        );
    }


    // =========================================================
    // REGISTER
    // =========================================================

    private NetworkResponse handleRegister(
            NetworkRequest request
    ) {

        if (request.getData() == null) {

            return NetworkResponse.error(
                    "Missing registration data."
            );
        }

        RegisterRequest registerRequest =
                gson.fromJson(
                        request.getData(),
                        RegisterRequest.class
                );

        PendingRegistrationResult result =
                authService
                        .getRegisterService()
                        .register(registerRequest);

        RegistrationResult publicResult =
                result.result();

        if (result.pendingAccount() == null) {

            JsonObject data =
                    new JsonObject();

            data.addProperty(
                    "status",
                    publicResult.status().name()
            );

            return new NetworkResponse(
                    false,
                    publicResult.message(),
                    data
            );
        }

        // This remains only on the server.
        pendingRegistration =
                result.pendingAccount();

        JsonObject data =
                new JsonObject();

        data.addProperty(
                "status",
                publicResult.status().name()
        );

        return NetworkResponse.success(
                publicResult.message(),
                data
        );
    }


    private NetworkResponse handleCompleteRegistration(
            NetworkRequest request
    ) {

        if (pendingRegistration == null) {

            return NetworkResponse.error(
                    "No pending registration."
            );
        }

        if (request.getData() == null) {

            return NetworkResponse.error(
                    "Missing security question data."
            );
        }

        PickQuestionRequest questionRequest =
                gson.fromJson(
                        request.getData(),
                        PickQuestionRequest.class
                );

        RegistrationResult result =
                authService
                        .getRegisterService()
                        .pickQuestion(
                                pendingRegistration,
                                questionRequest
                        );

        JsonObject data =
                new JsonObject();

        data.addProperty(
                "status",
                result.status().name()
        );

        if (result.status()
                == RegistrationStatus.COMPLETED) {

            pendingRegistration = null;

            return NetworkResponse.success(
                    result.message(),
                    data
            );
        }

        return new NetworkResponse(
                false,
                result.message(),
                data
        );
    }


    // =========================================================
    // PASSWORD RECOVERY
    // =========================================================

    private NetworkResponse handleForgotPassword(
            NetworkRequest request
    ) {

        if (request.getData() == null) {

            return NetworkResponse.error(
                    "Missing password recovery data."
            );
        }

        ForgetPasswordRequest forgotRequest =
                gson.fromJson(
                        request.getData(),
                        ForgetPasswordRequest.class
                );

        PasswordResetStartResult result =
                authService
                        .getLoginService()
                        .forgetPassword(
                                forgotRequest
                        );

        LoginResult publicResult =
                result.result();

        if (result.account() == null) {

            return createLoginResponse(
                    publicResult,
                    false
            );
        }

        // Stored per connection only.
        pendingPasswordReset =
                result.account();

        passwordResetAnswerAccepted =
                false;

        return createLoginResponse(
                publicResult,
                true
        );
    }


    private NetworkResponse handleSecurityAnswer(
            NetworkRequest request
    ) {

        if (request.getData() == null) {

            return NetworkResponse.error(
                    "Missing security answer."
            );
        }

        AnswerRequest answerRequest =
                gson.fromJson(
                        request.getData(),
                        AnswerRequest.class
                );

        LoginResult result =
                authService
                        .getLoginService()
                        .answer(
                                pendingPasswordReset,
                                answerRequest
                        );

        if (result.status()
                == LoginStatus.ANSWER_ACCEPTED) {

            passwordResetAnswerAccepted =
                    true;

            return createLoginResponse(
                    result,
                    true
            );
        }

        passwordResetAnswerAccepted =
                false;

        return createLoginResponse(
                result,
                false
        );
    }


    private NetworkResponse handleResetPassword(
            NetworkRequest request
    ) {

        if (request.getData() == null ||
                !request.getData()
                        .has("newPassword")) {

            return NetworkResponse.error(
                    "Missing new password."
            );
        }

        String newPassword =
                request.getData()
                        .get("newPassword")
                        .getAsString();

        LoginResult result =
                authService
                        .getLoginService()
                        .resetPassword(
                                pendingPasswordReset,
                                passwordResetAnswerAccepted,
                                newPassword
                        );

        if (result.status()
                == LoginStatus.PASSWORD_RESET) {

            pendingPasswordReset = null;

            passwordResetAnswerAccepted =
                    false;

            return createLoginResponse(
                    result,
                    true
            );
        }

        return createLoginResponse(
                result,
                false
        );
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    private NetworkResponse handleLogout(
            NetworkRequest request
    ) {

        String token =
                request.getToken();

        if (token == null ||
                token.isBlank()) {

            return NetworkResponse.error(
                    "Invalid session."
            );
        }

        // We will expose logout through AuthService/session manager
        // rather than letting the client manipulate sessions.
        //
        // Add logout() to LoginService:
        //
        // public void logout(String token) {
        //     sessionManager.removeSession(token);
        // }

        authService
                .getLoginService()
                .logout(token);

        return NetworkResponse.success(
                "Logged out successfully."
        );
    }


    // =========================================================
    // PROFILE
    // =========================================================

    private NetworkResponse handleGetProfile(
            NetworkRequest request
    ) {

        String token =
                request.getToken();

        Account account =
                authService
                        .getProfileService()
                        .getAccount(token);

        if (account == null) {

            return NetworkResponse.error(
                    "you are not logged in"
            );
        }

        UserInfo userInfo =
                createUserInfo(account);

        JsonObject data =
                new JsonObject();

        data.add(
                "user",
                gson.toJsonTree(userInfo)
        );

        return NetworkResponse.success(
                "Profile loaded.",
                data
        );
    }


    private NetworkResponse handleChangeUsername(
            NetworkRequest request
    ) {

        ChangeUsernameRequest changeRequest =
                gson.fromJson(
                        request.getData(),
                        ChangeUsernameRequest.class
                );

        String message =
                authService
                        .getProfileService()
                        .changeUsername(
                                request.getToken(),
                                changeRequest
                        );

        return profileResponse(message);
    }


    private NetworkResponse handleChangeNickname(
            NetworkRequest request
    ) {

        ChangeNicknameRequest changeRequest =
                gson.fromJson(
                        request.getData(),
                        ChangeNicknameRequest.class
                );

        String message =
                authService
                        .getProfileService()
                        .changeNickname(
                                request.getToken(),
                                changeRequest
                        );

        return profileResponse(message);
    }


    private NetworkResponse handleChangeEmail(
            NetworkRequest request
    ) {

        ChangeEmailRequest changeRequest =
                gson.fromJson(
                        request.getData(),
                        ChangeEmailRequest.class
                );

        String message =
                authService
                        .getProfileService()
                        .changeEmail(
                                request.getToken(),
                                changeRequest
                        );

        return profileResponse(message);
    }


    private NetworkResponse handleChangePassword(
            NetworkRequest request
    ) {

        ChangePasswordRequest changeRequest =
                gson.fromJson(
                        request.getData(),
                        ChangePasswordRequest.class
                );

        String message =
                authService
                        .getProfileService()
                        .changePassword(
                                request.getToken(),
                                changeRequest
                        );

        return profileResponse(message);
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private NetworkResponse createLoginResponse(
            LoginResult result,
            boolean success
    ) {

        JsonObject data =
                new JsonObject();

        data.addProperty(
                "status",
                result.status().name()
        );

        return new NetworkResponse(
                success,
                result.message(),
                data
        );
    }


    private NetworkResponse profileResponse(
            String message
    ) {

        boolean success =
                !message.equals(
                        "you are not logged in"
                )
                        && !message.startsWith(
                        "invalid"
                )
                        && !message.contains(
                        "incorrect"
                )
                        && !message.contains(
                        "already exists"
                );

        return new NetworkResponse(
                success,
                message,
                null
        );
    }


    private UserInfo createUserInfo(
            Account account
    ) {

        return new UserInfo(
                account.getName(),
                account.getNickname(),
                account.getEmail(),
                account.getGender()
                        .name()
                        .toLowerCase(),
                account.getAdventureProgress()
                        .getCoin(),
                account.getAdventureProgress()
                        .getGem(),
                account.getScoreRecord()
                        .getScore(),
                account.getAdventureProgress()
                        .getCurrentChapter(),
                account.getAdventureProgress()
                        .getCurrentLvl()
        );
    }


    private void closeConnection() {

        try {

            if (reader != null) {
                reader.close();
            }

        } catch (IOException ignored) {
        }

        if (writer != null) {
            writer.close();
        }

        try {

            if (socket != null &&
                    !socket.isClosed()) {

                socket.close();
            }

        } catch (IOException ignored) {
        }
    }
}