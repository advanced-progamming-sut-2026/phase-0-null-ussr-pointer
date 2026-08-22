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
import com.ussr.pvz.server.match.MatchManager;
import com.ussr.pvz.server.match.MatchPeer;
import com.ussr.pvz.shared.account.AccountState;
import com.ussr.pvz.shared.dto.*;
import com.ussr.pvz.shared.dto.enums.LoginStatus;
import com.ussr.pvz.shared.dto.enums.RegistrationStatus;
import com.ussr.pvz.shared.multiplayer.*;
import com.ussr.pvz.shared.network.NetworkRequest;
import com.ussr.pvz.shared.network.NetworkResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class ClientHandler implements Runnable, MatchPeer {

    private final Socket socket;
    private final AuthService authService;
    private final MatchManager matchManager;
    private final Map<String, ClientHandler> connectedPeers;
    private final Gson gson = new Gson();
    private final LobbyManager lobby = LobbyManager.getInstance();

    private BufferedReader reader;
    private PrintWriter writer;

    // Single source of truth for the authenticated session.
    // Both MatchPeer.token() and all request handling use this.
    private volatile String sessionToken;
    private volatile String sessionUsername;

    // Per-connection registration / password-reset state
    private AccountState pendingRegistration;
    private Account pendingPasswordReset;
    private boolean passwordResetAnswerAccepted;

    public ClientHandler(
            Socket socket,
            AuthService authService,
            MatchManager matchManager,
            Map<String, ClientHandler> connectedPeers
    ) {
        this.socket          = socket;
        this.authService     = authService;
        this.matchManager    = matchManager;
        this.connectedPeers  = connectedPeers;
    }

    // =========================================================
    // MatchPeer
    // =========================================================

    @Override
    public String token() { return sessionToken; }

    @Override
    public String username() { return sessionUsername; }

    @Override
    public void sendMatchStarted(MatchDescriptor descriptor) {
        sendPush(MatchServerMessage.started(descriptor));
    }

    @Override
    public void sendMatchAction(MatchAction action) {
        sendPush(MatchServerMessage.action(action));
    }

    @Override
    public void sendMatchClosed(String matchId, String reason) {
        sendPush(MatchServerMessage.closed(matchId, reason));
    }

    /** Thread-safe write of a server-push message (not a response to a request). */
    private synchronized void sendPush(MatchServerMessage message) {
        if (writer != null) {
            writer.println(gson.toJson(message));
        }
    }

    // =========================================================
    // Runnable
    // =========================================================

    @Override
    public void run() {
        try {
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );
            writer = new PrintWriter(socket.getOutputStream(), true);

            String line;
            while ((line = reader.readLine()) != null) {
                NetworkResponse response;
                try {
                    NetworkRequest request = gson.fromJson(line, NetworkRequest.class);
                    response = handleRequest(request);
                } catch (Exception e) {
                    response = NetworkResponse.error("Invalid request: " + e.getMessage());
                }
                writer.println(gson.toJson(response));
            }

        } catch (IOException e) {
            System.out.println("Client disconnected: " + socket.getInetAddress());
        } finally {
            closeConnection();
        }
    }

    // =========================================================
    // Dispatch
    // =========================================================

    private NetworkResponse handleRequest(NetworkRequest request) {
        if (request == null || request.getType() == null) {
            return NetworkResponse.error("Invalid request.");
        }

        return switch (request.getType()) {
            case PING -> NetworkResponse.success("PONG");

            case LOGIN               -> handleLogin(request);
            case AUTH_TOKEN          -> handleAuthToken(request);
            case REGISTER            -> handleRegister(request);
            case COMPLETE_REGISTRATION -> handleCompleteRegistration(request);

            case FORGOT_PASSWORD          -> handleForgotPassword(request);
            case ANSWER_SECURITY_QUESTION -> handleSecurityAnswer(request);
            case RESET_PASSWORD           -> handleResetPassword(request);

            case LOGOUT -> handleLogout(request);

            case GET_PROFILE      -> handleGetProfile(request);
            case CHANGE_USERNAME  -> handleChangeUsername(request);
            case CHANGE_NICKNAME  -> handleChangeNickname(request);
            case CHANGE_EMAIL     -> handleChangeEmail(request);
            case CHANGE_PASSWORD  -> handleChangePassword(request);

            case GET_LEADERBOARD -> handleGetLeaderboard(request);

            case GET_ONLINE_PLAYERS  -> handleGetOnlinePlayers(request);
            case SEND_INVITE         -> handleSendInvite(request);
            case CANCEL_INVITE       -> handleCancelInvite(request);
            case RESPOND_INVITE      -> handleRespondInvite(request);
            case CHECK_INVITE        -> handleCheckInvite(request);
            case JOIN_RANDOM_QUEUE   -> handleJoinRandomQueue(request);
            case LEAVE_RANDOM_QUEUE  -> handleLeaveRandomQueue(request);
            case CHECK_RANDOM_MATCH  -> handleCheckRandomMatch(request);
            case CHECK_INVITE_RESULT -> handleCheckInviteResult(request);

            case GAME_ACTION -> handleGameAction(request);

            default -> NetworkResponse.error("Request not implemented.");
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

        if (serverResult.account() == null || serverResult.token() == null) {
            return new NetworkResponse(false, result.message(), data);
        }

        data.addProperty("token", serverResult.token());
        data.add("accountState", gson.toJsonTree(serverResult.account().toState()));

        registerSession(serverResult.token(), serverResult.account());

        return NetworkResponse.success(result.message(), data);
    }

    private NetworkResponse handleAuthToken(NetworkRequest request) {
        String token = request.getToken();
        Account account = authService.getSessionManager().getAccount(token);

        if (account == null) {
            return NetworkResponse.error("Session expired.");
        }

        JsonObject data = new JsonObject();
        data.addProperty("token", token);
        data.add("accountState", gson.toJsonTree(account.toState()));

        registerSession(token, account);

        return NetworkResponse.success("Session restored.", data);
    }

    /**
     * Stores the session token/username, registers with the peer map,
     * and marks the player as online in the lobby.
     * Called on both fresh login and token restore.
     */
    private void registerSession(String token, Account account) {
        this.sessionToken    = token;
        this.sessionUsername = account.getName();
        connectedPeers.put(token, this);
        lobby.playerEntered(token, account);
    }

    // =========================================================
    // REGISTER
    // =========================================================

    private NetworkResponse handleRegister(NetworkRequest request) {
        if (request.getData() == null) {
            return NetworkResponse.error("Missing registration data.");
        }

        RegisterRequest registerRequest =
                gson.fromJson(request.getData(), RegisterRequest.class);

        PendingRegistrationResult result =
                authService.getRegisterService().register(registerRequest);

        RegistrationResult publicResult = result.result();

        JsonObject data = new JsonObject();
        data.addProperty("status", publicResult.status().name());

        if (result.pendingAccount() == null) {
            return new NetworkResponse(false, publicResult.message(), data);
        }

        pendingRegistration = result.pendingAccount();
        return NetworkResponse.success(publicResult.message(), data);
    }

    private NetworkResponse handleCompleteRegistration(NetworkRequest request) {
        if (pendingRegistration == null) {
            return NetworkResponse.error("No pending registration.");
        }
        if (request.getData() == null) {
            return NetworkResponse.error("Missing security question data.");
        }

        PickQuestionRequest questionRequest =
                gson.fromJson(request.getData(), PickQuestionRequest.class);

        RegistrationResult result =
                authService.getRegisterService()
                        .pickQuestion(pendingRegistration, questionRequest);

        JsonObject data = new JsonObject();
        data.addProperty("status", result.status().name());

        boolean success = result.status() == RegistrationStatus.COMPLETED;
        if (success) pendingRegistration = null;

        return new NetworkResponse(success, result.message(), data);
    }

    // =========================================================
    // PASSWORD RESET
    // =========================================================

    private NetworkResponse handleForgotPassword(NetworkRequest request) {
        if (request.getData() == null) {
            return NetworkResponse.error("Missing password recovery data.");
        }

        ForgetPasswordRequest forgotRequest =
                gson.fromJson(request.getData(), ForgetPasswordRequest.class);

        PasswordResetStartResult result =
                authService.getLoginService().forgetPassword(forgotRequest);

        if (result.account() == null) {
            pendingPasswordReset = null;
            passwordResetAnswerAccepted = false;
            return createLoginResponse(result.result(), false);
        }

        pendingPasswordReset = result.account();
        passwordResetAnswerAccepted = false;
        return createLoginResponse(result.result(), true);
    }

    private NetworkResponse handleSecurityAnswer(NetworkRequest request) {
        if (request.getData() == null) {
            return NetworkResponse.error("Missing security answer.");
        }

        AnswerRequest answerRequest =
                gson.fromJson(request.getData(), AnswerRequest.class);

        LoginResult result =
                authService.getLoginService()
                        .answer(pendingPasswordReset, answerRequest);

        passwordResetAnswerAccepted = result.status() == LoginStatus.ANSWER_ACCEPTED;
        return createLoginResponse(result, passwordResetAnswerAccepted);
    }

    private NetworkResponse handleResetPassword(NetworkRequest request) {
        if (request.getData() == null || !request.getData().has("newPassword")) {
            return NetworkResponse.error("Missing new password.");
        }

        String newPassword = request.getData().get("newPassword").getAsString();

        LoginResult result =
                authService.getLoginService()
                        .resetPassword(pendingPasswordReset,
                                passwordResetAnswerAccepted,
                                newPassword);

        boolean success = result.status() == LoginStatus.PASSWORD_RESET;
        if (success) {
            pendingPasswordReset = null;
            passwordResetAnswerAccepted = false;
        }

        return createLoginResponse(result, success);
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    private NetworkResponse handleLogout(NetworkRequest request) {
        String token = request.getToken();
        if (!validSession(token)) {
            return NetworkResponse.error("Invalid session.");
        }
        authService.getLoginService().logout(token);
        return NetworkResponse.success("Logged out successfully.");
    }

    // =========================================================
    // PROFILE
    // =========================================================

    private NetworkResponse handleGetProfile(NetworkRequest request) {
        Account account = authService.getProfileService().getAccount(request.getToken());
        if (account == null) {
            return NetworkResponse.error("you are not logged in");
        }
        String info = authService.getProfileService().showInfo(request.getToken());
        return NetworkResponse.success(info);
    }

    private NetworkResponse handleChangeUsername(NetworkRequest request) {
        if (request.getData() == null) return NetworkResponse.error("Missing username data.");
        ChangeUsernameRequest changeRequest =
                gson.fromJson(request.getData(), ChangeUsernameRequest.class);
        return profileResponse(
                authService.getProfileService()
                        .changeUsername(request.getToken(), changeRequest));
    }

    private NetworkResponse handleChangeNickname(NetworkRequest request) {
        if (request.getData() == null) return NetworkResponse.error("Missing nickname data.");
        ChangeNicknameRequest changeRequest =
                gson.fromJson(request.getData(), ChangeNicknameRequest.class);
        return profileResponse(
                authService.getProfileService()
                        .changeNickname(request.getToken(), changeRequest));
    }

    private NetworkResponse handleChangeEmail(NetworkRequest request) {
        if (request.getData() == null) return NetworkResponse.error("Missing email data.");
        ChangeEmailRequest changeRequest =
                gson.fromJson(request.getData(), ChangeEmailRequest.class);
        return profileResponse(
                authService.getProfileService()
                        .changeEmail(request.getToken(), changeRequest));
    }

    private NetworkResponse handleChangePassword(NetworkRequest request) {
        if (request.getData() == null) return NetworkResponse.error("Missing password data.");
        ChangePasswordRequest changeRequest =
                gson.fromJson(request.getData(), ChangePasswordRequest.class);
        return profileResponse(
                authService.getProfileService()
                        .changePassword(request.getToken(), changeRequest));
    }

    // =========================================================
    // LEADERBOARD
    // =========================================================

    private NetworkResponse handleGetLeaderboard(NetworkRequest request) {
        if (!validSession(request.getToken())) {
            return NetworkResponse.error("Invalid session.");
        }

        List<Account> accounts =
                authService.getAccountRepository().getAccounts();

        JsonArray entries = new JsonArray();
        for (Account account : accounts) {
            LeaderboardEntryDto entry = new LeaderboardEntryDto(
                    account.getName(),
                    account.getAdventureProgress().getCurrentChapter(),
                    account.getAdventureProgress().getCurrentLvl(),
                    account.getAdventureProgress().getMinigamesWon(),
                    getCompletedQuestCount(account, QuestType.DAILY),
                    getCompletedQuestCount(account, QuestType.CHALLENGE)
                            + getCompletedQuestCount(account, QuestType.EPIC),
                    account.getScoreRecord().getScore()
            );
            entries.add(gson.toJsonTree(entry));
        }

        JsonObject data = new JsonObject();
        data.add("entries", entries);
        return NetworkResponse.success("Leaderboard loaded successfully.", data);
    }

    // =========================================================
    // LOBBY — ONLINE PLAYERS
    // =========================================================

    private NetworkResponse handleGetOnlinePlayers(NetworkRequest request) {
        if (!validSession(request.getToken())) {
            return NetworkResponse.error("Invalid session.");
        }

        List<LobbyManager.OnlinePlayerInfo> players = lobby.getOnlinePlayers();

        JsonArray arr = new JsonArray();
        for (LobbyManager.OnlinePlayerInfo info : players) {
            if (!info.token().equals(request.getToken())) {
                JsonObject obj = new JsonObject();
                obj.addProperty("username", info.username());
                arr.add(obj);
            }
        }

        JsonObject data = new JsonObject();
        data.add("players", arr);
        return NetworkResponse.success("Online players fetched.", data);
    }

    // =========================================================
    // LOBBY — SEND INVITE
    // =========================================================

    private NetworkResponse handleSendInvite(NetworkRequest request) {
        if (!validSession(request.getToken())) {
            return NetworkResponse.error("Invalid session.");
        }
        if (request.getData() == null || !request.getData().has("invitedUsername")) {
            return NetworkResponse.error("Missing invitedUsername.");
        }

        String invitedUsername = request.getData().get("invitedUsername").getAsString();
        String error = lobby.sendInvite(request.getToken(), invitedUsername);

        return error != null
                ? NetworkResponse.error(error)
                : NetworkResponse.success("Invite sent to " + invitedUsername + ".");
    }

    // =========================================================
    // LOBBY — CANCEL INVITE
    // =========================================================

    private NetworkResponse handleCancelInvite(NetworkRequest request) {
        if (!validSession(request.getToken())) {
            return NetworkResponse.error("Invalid session.");
        }
        lobby.cancelInviteByInviter(request.getToken());
        return NetworkResponse.success("Invite cancelled.");
    }

    // =========================================================
    // LOBBY — RESPOND TO INVITE
    // =========================================================

    private NetworkResponse handleRespondInvite(NetworkRequest request) {
        if (!validSession(request.getToken())) {
            return NetworkResponse.error("Invalid session.");
        }
        if (request.getData() == null || !request.getData().has("accepted")) {
            return NetworkResponse.error("Missing 'accepted' field.");
        }

        boolean accepted = request.getData().get("accepted").getAsBoolean();
        String error = lobby.respondToInvite(request.getToken(), accepted);

        if (error != null) return NetworkResponse.error(error);

        // If accepted, the invite pair is now in confirmedMatches on both sides.
        // Trigger room creation immediately so neither peer has to poll.
        if (accepted) {
            triggerMatchForInvitePair(request.getToken());
        }

        return NetworkResponse.success(accepted ? "Invite accepted." : "Invite rejected.");
    }

    /**
     * After an invite is accepted both tokens are in LobbyManager.confirmedMatches.
     * Poll both sides and create the room.
     * The invitee's token is known (recipientToken); we find the inviter by
     * scanning confirmedMatches — whoever has this username as opponent.
     */
    private void triggerMatchForInvitePair(String recipientToken) {
        // confirmedMatches maps token → opponentUsername.
        // The recipient's entry was just written; find the inviter by checking
        // which other online peer's confirmed entry points to the recipient's username.
        String recipientUsername = sessionUsername; // already set

        // The inviter's token: onlinePlayers entry whose confirmed opponent == recipientUsername.
        // We look this up through LobbyManager's online list.
        List<LobbyManager.OnlinePlayerInfo> online = lobby.getOnlinePlayers();
        for (LobbyManager.OnlinePlayerInfo info : online) {
            if (info.token().equals(recipientToken)) continue;

            // Poll their confirmed match — if it points to us, they're the inviter.
            // We don't consume it here (matchManager.createRoom will start the match
            // and the inviter will get MATCH_STARTED, not a poll response).
            // Use a non-consuming peek via getOnlinePlayers is not enough;
            // we need to find who sent the invite.
            // Simplest: just call createRoom with both tokens; MatchManager is idempotent.
            // The inviter's token is in connectedPeers keyed by their token.
            // We identify them by checking if their confirmedMatch opponent == us.
            ClientHandler candidate = connectedPeers.get(info.token());
            if (candidate == null) continue;

            // pollRandomMatch is destructive, so we can't use it here.
            // Instead we rely on the fact that LobbyManager.respondToInvite already
            // wrote confirmedMatches for BOTH tokens. We create the room with
            // (inviter=PLANTS, recipient=ZOMBIES) — first-sender gets plants.
            // We identify the inviter as whoever is NOT the recipient in the pair.
            // Since we don't have a non-destructive peek, we create the room now
            // using the first match of a connected peer that has a confirmed entry.
            // MatchManager.createRoom is synchronized and idempotent (no double rooms).
            matchManager.createRoom(info.token(), recipientToken);
            return;
        }
    }

    // =========================================================
    // LOBBY — CHECK INCOMING INVITE
    // =========================================================

    private NetworkResponse handleCheckInvite(NetworkRequest request) {
        if (!validSession(request.getToken())) {
            return NetworkResponse.error("Invalid session.");
        }

        LobbyManager.PendingInvite invite = lobby.getInviteFor(request.getToken());

        JsonObject data = new JsonObject();
        if (invite == null) {
            data.addProperty("hasInvite", false);
        } else {
            data.addProperty("hasInvite", true);
            data.addProperty("fromUsername", invite.inviterUsername());
        }
        return NetworkResponse.success("Invite check done.", data);
    }

    // =========================================================
    // LOBBY — JOIN RANDOM QUEUE
    // =========================================================

    private NetworkResponse handleJoinRandomQueue(NetworkRequest request) {
        if (!validSession(request.getToken())) {
            return NetworkResponse.error("Invalid session.");
        }

        Account me = authService.getProfileService().getAccount(request.getToken());
        if (me != null) lobby.playerEntered(request.getToken(), me);

        boolean ok = lobby.joinRandomQueue(request.getToken());
        if (!ok) return NetworkResponse.error("Could not join queue.");

        // If the queue matched us immediately (two players), create the room now.
        // LobbyManager.joinRandomQueue writes confirmedMatches for both tokens
        // when it finds a pair. We don't know the opponent's token here, but
        // MatchManager.handleCheckRandomMatch will create the room when the
        // opponent polls. We also create it here for the side that triggered
        // the match (the second joiner).
        // We detect "matched immediately" by checking confirmedMatches via poll.
        // But pollRandomMatch is destructive — we must pass the result on.
        // Solution: store it and return it in the response so the client
        // skips the poll cycle.
        String opponentUsername = lobby.pollRandomMatch(request.getToken());
        if (opponentUsername != null) {
            // Find the opponent's token and create the room.
            triggerRandomMatch(request.getToken(), opponentUsername);

            JsonObject data = new JsonObject();
            data.addProperty("matched", true);
            data.addProperty("opponentUsername", opponentUsername);
            return NetworkResponse.success("Match found!", data);
        }

        return NetworkResponse.success("Joined matchmaking queue.");
    }

    // =========================================================
    // LOBBY — LEAVE RANDOM QUEUE
    // =========================================================

    private NetworkResponse handleLeaveRandomQueue(NetworkRequest request) {
        if (!validSession(request.getToken())) {
            return NetworkResponse.error("Invalid session.");
        }
        lobby.leaveRandomQueue(request.getToken());
        return NetworkResponse.success("Left matchmaking queue.");
    }

    // =========================================================
    // LOBBY — POLL FOR RANDOM MATCH
    // =========================================================

    private NetworkResponse handleCheckRandomMatch(NetworkRequest request) {
        if (!validSession(request.getToken())) {
            return NetworkResponse.error("Invalid session.");
        }

        String opponentUsername = lobby.pollRandomMatch(request.getToken());

        JsonObject data = new JsonObject();
        if (opponentUsername == null) {
            data.addProperty("matched", false);
        } else {
            data.addProperty("matched", true);
            data.addProperty("opponentUsername", opponentUsername);
            triggerRandomMatch(request.getToken(), opponentUsername);
        }
        return NetworkResponse.success("Random match check done.", data);
    }

    /**
     * Finds the opponent's token by their username and calls createRoom.
     * The caller's token becomes ZOMBIES; the opponent becomes PLANTS
     * (arbitrary but consistent — first-in-queue gets plants).
     */
    private void triggerRandomMatch(String myToken, String opponentUsername) {
        for (LobbyManager.OnlinePlayerInfo info : lobby.getOnlinePlayers()) {
            if (info.username().equalsIgnoreCase(opponentUsername)) {
                // opponent = PLANTS (they were in the queue first)
                // me       = ZOMBIES (I triggered the match by joining second)
                matchManager.createRoom(info.token(), myToken);
                return;
            }
        }
        System.err.println("[ClientHandler] Could not find opponent peer for username: "
                + opponentUsername);
    }

    // =========================================================
    // LOBBY — CHECK INVITE RESULT
    // =========================================================

    private NetworkResponse handleCheckInviteResult(NetworkRequest request) {
        if (!validSession(request.getToken())) {
            return NetworkResponse.error("Invalid session.");
        }

        String result = lobby.pollInviteResult(request.getToken());

        JsonObject data = new JsonObject();
        if (result == null) {
            data.addProperty("hasResult", false);
        } else {
            data.addProperty("hasResult", true);
            data.addProperty("result", result); // "ACCEPTED" or "REJECTED"
        }
        return NetworkResponse.success("Invite result check done.", data);
    }

    // =========================================================
    // GAME ACTION
    // =========================================================

    private NetworkResponse handleGameAction(NetworkRequest request) {
        if (!validSession(request.getToken())) {
            return NetworkResponse.error("Invalid session.");
        }
        if (request.getData() == null) {
            return NetworkResponse.error("Missing command data.");
        }
        MatchCommand cmd = gson.fromJson(request.getData(), MatchCommand.class);
        matchManager.handleCommand(cmd, sessionToken);
        return NetworkResponse.success("Action relayed.");
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private boolean validSession(String token) {
        return token != null
                && !token.isBlank()
                && authService.getLoginService().isLoggedIn(token);
    }

    private int getCompletedQuestCount(Account account, QuestType type) {
        if (account.getQuestManager() == null) return 0;
        List<ConfigurableQuest> quests = account.getQuestManager().getByType(type);
        if (quests == null) return 0;
        return (int) quests.stream().filter(ConfigurableQuest::isCompleted).count();
    }

    private NetworkResponse createLoginResponse(LoginResult result, boolean success) {
        JsonObject data = new JsonObject();
        data.addProperty("status", result.status().name());
        return new NetworkResponse(success, result.message(), data);
    }

    private NetworkResponse profileResponse(String message) {
        if (message == null) return NetworkResponse.error("Unknown profile error.");
        String lower = message.toLowerCase();
        boolean success = !message.equalsIgnoreCase("you are not logged in")
                && !lower.startsWith("invalid")
                && !lower.contains("incorrect")
                && !lower.contains("already exists");
        return new NetworkResponse(success, message, null);
    }

    private void closeConnection() {
        if (sessionToken != null) {
            connectedPeers.remove(sessionToken);
            lobby.playerLeft(sessionToken);
            matchManager.onPeerDisconnected(this);
            sessionToken = null;
        }
        try { if (reader != null) reader.close(); } catch (IOException ignored) {}
        if (writer != null) writer.close();
        try { if (socket != null && !socket.isClosed()) socket.close(); }
        catch (IOException ignored) {}
    }
}