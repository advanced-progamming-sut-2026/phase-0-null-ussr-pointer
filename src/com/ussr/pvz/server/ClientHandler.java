package com.ussr.pvz.server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.quest.ConfigurableQuest;
import com.ussr.pvz.model.quest.QuestType;
import com.ussr.pvz.server.account.AuthService;
import com.ussr.pvz.server.account.PasswordResetStartResult;
import com.ussr.pvz.server.account.PendingRegistrationResult;
import com.ussr.pvz.server.account.ServerLoginResult;
import com.ussr.pvz.shared.account.AccountState;
import com.ussr.pvz.shared.dto.*;
import com.ussr.pvz.shared.dto.enums.LoginStatus;
import com.ussr.pvz.shared.dto.enums.RegistrationStatus;
import com.ussr.pvz.shared.network.NetworkRequest;
import com.ussr.pvz.shared.network.NetworkResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final AuthService authService;
    private final Gson gson = new Gson();

    private BufferedReader reader;
    private PrintWriter writer;

    private AccountState pendingRegistration;
    private Account pendingPasswordReset;
    private boolean passwordResetAnswerAccepted;

    public ClientHandler(Socket socket, AuthService authService) {
        this.socket = socket;
        this.authService = authService;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
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
                            gson.fromJson(line, NetworkRequest.class);

                    response = handleRequest(request);

                } catch (Exception e) {
                    response = NetworkResponse.error(
                            "Invalid request: " + e.getMessage()
                    );
                }

                writer.println(gson.toJson(response));
            }

        } catch (IOException e) {
            System.out.println(
                    "Client disconnected: " + socket.getInetAddress()
            );

        } finally {
            closeConnection();
        }
    }

    private NetworkResponse handleRequest(NetworkRequest request) {
        if (request == null || request.getType() == null) {
            return NetworkResponse.error("Invalid request.");
        }

        return switch (request.getType()) {
            case PING -> NetworkResponse.success("PONG");

            case LOGIN -> handleLogin(request);
            case REGISTER -> handleRegister(request);
            case COMPLETE_REGISTRATION -> handleCompleteRegistration(request);

            case FORGOT_PASSWORD -> handleForgotPassword(request);
            case ANSWER_SECURITY_QUESTION -> handleSecurityAnswer(request);
            case RESET_PASSWORD -> handleResetPassword(request);

            case LOGOUT -> handleLogout(request);

            case GET_PROFILE -> handleGetProfile(request);
            case CHANGE_USERNAME -> handleChangeUsername(request);
            case CHANGE_NICKNAME -> handleChangeNickname(request);
            case CHANGE_EMAIL -> handleChangeEmail(request);
            case CHANGE_PASSWORD -> handleChangePassword(request);

            case GET_LEADERBOARD -> handleGetLeaderboard(request);

            default -> NetworkResponse.error(
                    "Request not implemented."
            );
        };
    }

    // =========================================================
    // LOGIN
    // =========================================================

    private NetworkResponse handleLogin(NetworkRequest request) {
        if (request.getData() == null) {
            return NetworkResponse.error("Missing login data.");
        }

        LoginRequest loginRequest =
                gson.fromJson(request.getData(), LoginRequest.class);

        ServerLoginResult serverResult =
                authService.getLoginService().login(loginRequest);

        LoginResult result = serverResult.result();

        JsonObject data = new JsonObject();
        data.addProperty("status", result.status().name());

        if (serverResult.account() == null ||
                serverResult.token() == null) {

            return new NetworkResponse(
                    false,
                    result.message(),
                    data
            );
        }

        data.addProperty(
                "token",
                serverResult.token()
        );

        data.add(
                "accountState",
                gson.toJsonTree(
                        serverResult.account().toState()
                )
        );

        return NetworkResponse.success(
                result.message(),
                data
        );
    }

    // =========================================================
    // REGISTER
    // =========================================================

    private NetworkResponse handleRegister(NetworkRequest request) {
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

        JsonObject data = new JsonObject();
        data.addProperty(
                "status",
                publicResult.status().name()
        );

        if (result.pendingAccount() == null) {
            return new NetworkResponse(
                    false,
                    publicResult.message(),
                    data
            );
        }

        pendingRegistration =
                result.pendingAccount();

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

        JsonObject data = new JsonObject();
        data.addProperty(
                "status",
                result.status().name()
        );

        boolean success =
                result.status() ==
                        RegistrationStatus.COMPLETED;

        if (success) {
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
    // PASSWORD RESET
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
                        .forgetPassword(forgotRequest);

        if (result.account() == null) {
            pendingPasswordReset = null;
            passwordResetAnswerAccepted = false;

            return createLoginResponse(
                    result.result(),
                    false
            );
        }

        pendingPasswordReset =
                result.account();

        passwordResetAnswerAccepted =
                false;

        return createLoginResponse(
                result.result(),
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

        passwordResetAnswerAccepted =
                result.status() ==
                        LoginStatus.ANSWER_ACCEPTED;

        return createLoginResponse(
                result,
                passwordResetAnswerAccepted
        );
    }

    private NetworkResponse handleResetPassword(
            NetworkRequest request
    ) {
        if (request.getData() == null ||
                !request.getData().has("newPassword")) {

            return NetworkResponse.error(
                    "Missing new password."
            );
        }

        String newPassword =
                request
                        .getData()
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

        boolean success =
                result.status() ==
                        LoginStatus.PASSWORD_RESET;

        if (success) {
            pendingPasswordReset = null;
            passwordResetAnswerAccepted = false;
        }

        return createLoginResponse(
                result,
                success
        );
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    private NetworkResponse handleLogout(
            NetworkRequest request
    ) {
        String token = request.getToken();

        if (!validSession(token)) {
            return NetworkResponse.error(
                    "Invalid session."
            );
        }

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
        String token = request.getToken();

        Account account =
                authService
                        .getProfileService()
                        .getAccount(token);

        if (account == null) {
            return NetworkResponse.error(
                    "you are not logged in"
            );
        }

        String info =
                authService
                        .getProfileService()
                        .showInfo(token);

        return NetworkResponse.success(info);
    }

    private NetworkResponse handleChangeUsername(
            NetworkRequest request
    ) {
        if (request.getData() == null) {
            return NetworkResponse.error(
                    "Missing username data."
            );
        }

        ChangeUsernameRequest changeRequest =
                gson.fromJson(
                        request.getData(),
                        ChangeUsernameRequest.class
                );

        return profileResponse(
                authService
                        .getProfileService()
                        .changeUsername(
                                request.getToken(),
                                changeRequest
                        )
        );
    }

    private NetworkResponse handleChangeNickname(
            NetworkRequest request
    ) {
        if (request.getData() == null) {
            return NetworkResponse.error(
                    "Missing nickname data."
            );
        }

        ChangeNicknameRequest changeRequest =
                gson.fromJson(
                        request.getData(),
                        ChangeNicknameRequest.class
                );

        return profileResponse(
                authService
                        .getProfileService()
                        .changeNickname(
                                request.getToken(),
                                changeRequest
                        )
        );
    }

    private NetworkResponse handleChangeEmail(
            NetworkRequest request
    ) {
        if (request.getData() == null) {
            return NetworkResponse.error(
                    "Missing email data."
            );
        }

        ChangeEmailRequest changeRequest =
                gson.fromJson(
                        request.getData(),
                        ChangeEmailRequest.class
                );

        return profileResponse(
                authService
                        .getProfileService()
                        .changeEmail(
                                request.getToken(),
                                changeRequest
                        )
        );
    }

    private NetworkResponse handleChangePassword(
            NetworkRequest request
    ) {
        if (request.getData() == null) {
            return NetworkResponse.error(
                    "Missing password data."
            );
        }

        ChangePasswordRequest changeRequest =
                gson.fromJson(
                        request.getData(),
                        ChangePasswordRequest.class
                );

        return profileResponse(
                authService
                        .getProfileService()
                        .changePassword(
                                request.getToken(),
                                changeRequest
                        )
        );
    }

    // =========================================================
    // LEADERBOARD
    // =========================================================

    private NetworkResponse handleGetLeaderboard(
            NetworkRequest request
    ) {
        if (!validSession(request.getToken())) {
            return NetworkResponse.error(
                    "Invalid session."
            );
        }

        List<Account> accounts =
                authService
                        .getAccountRepository()
                        .getAccounts();

        JsonArray entries =
                new JsonArray();

        for (Account account : accounts) {
            LeaderboardEntryDto entry =
                    new LeaderboardEntryDto(
                            account.getName(),
                            account.getAdventureProgress()
                                    .getCurrentChapter(),
                            account.getAdventureProgress()
                                    .getCurrentLvl(),
                            account.getAdventureProgress()
                                    .getMinigamesWon(),
                            getCompletedQuestCount(
                                    account,
                                    QuestType.DAILY
                            ),
                            getCompletedQuestCount(
                                    account,
                                    QuestType.CHALLENGE
                            ) + getCompletedQuestCount(
                                    account,
                                    QuestType.EPIC
                            ),
                            account.getScoreRecord()
                                    .getScore()
                    );

            entries.add(
                    gson.toJsonTree(entry)
            );
        }

        JsonObject data =
                new JsonObject();

        data.add(
                "entries",
                entries
        );

        return NetworkResponse.success(
                "Leaderboard loaded successfully.",
                data
        );
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private boolean validSession(String token) {
        return token != null
                && !token.isBlank()
                && authService
                .getLoginService()
                .isLoggedIn(token);
    }

    private int getCompletedQuestCount(
            Account account,
            QuestType type
    ) {
        if (account.getQuestManager() == null) {
            return 0;
        }

        List<ConfigurableQuest> quests =
                account
                        .getQuestManager()
                        .getByType(type);

        if (quests == null) {
            return 0;
        }

        return (int) quests
                .stream()
                .filter(
                        ConfigurableQuest::isCompleted
                )
                .count();
    }

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
        if (message == null) {
            return NetworkResponse.error(
                    "Unknown profile error."
            );
        }

        String lower =
                message.toLowerCase();

        boolean success =
                !message.equalsIgnoreCase(
                        "you are not logged in"
                )
                        && !lower.startsWith("invalid")
                        && !lower.contains("incorrect")
                        && !lower.contains("already exists");

        return new NetworkResponse(
                success,
                message,
                null
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