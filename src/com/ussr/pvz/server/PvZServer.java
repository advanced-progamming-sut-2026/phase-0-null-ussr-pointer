package com.ussr.pvz.server;

import com.ussr.pvz.server.account.AccountRepository;
import com.ussr.pvz.server.account.AuthService;
import com.ussr.pvz.server.account.ServerSessionManager;
import com.ussr.pvz.server.match.MatchManager;
import com.ussr.pvz.server.match.MatchPeer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PvZServer {

    private final int port;

    private final AccountRepository accountRepository;
    private final ServerSessionManager sessionManager;
    private final AuthService authService;

    /**
     * token → ClientHandler for every authenticated, connected client.
     * Populated on login/auth-token, removed on disconnect.
     * Shared with MatchManager so it can look up both peers by token
     * when creating a room.
     */
    final Map<String, ClientHandler> connectedPeers =
            new ConcurrentHashMap<>();

    private final MatchManager matchManager;

    public PvZServer(int port) {
        this.port = port;

        this.accountRepository = new AccountRepository();
        this.sessionManager    = new ServerSessionManager();
        this.authService       = new AuthService(accountRepository, sessionManager);
        this.matchManager      = new MatchManager(connectedPeers);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Loaded accounts: " + accountRepository.size());
            System.out.println("PVZ Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();

                System.out.println(
                        "Client connected: " + clientSocket.getInetAddress()
                );

                ClientHandler handler = new ClientHandler(
                        clientSocket,
                        authService,
                        matchManager,
                        connectedPeers
                );

                new Thread(handler).start();
            }

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}