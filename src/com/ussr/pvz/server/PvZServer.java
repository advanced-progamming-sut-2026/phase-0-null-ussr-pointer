package com.ussr.pvz.server;

import com.ussr.pvz.server.account.AccountRepository;
import com.ussr.pvz.server.account.AuthService;
import com.ussr.pvz.server.account.ServerSessionManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PvZServer {

    private final int port;

    private final AccountRepository accountRepository;
    private final ServerSessionManager sessionManager;
    private final AuthService authService;

    public PvZServer(int port) {

        this.port = port;

        this.accountRepository =
                new AccountRepository();

        this.sessionManager =
                new ServerSessionManager();

        this.authService =
                new AuthService(
                        accountRepository,
                        sessionManager
                );
    }

    public void start() {

        try (ServerSocket serverSocket =
                     new ServerSocket(port)) {

            System.out.println(
                    "Loaded accounts: "
                            + accountRepository.size()
            );

            System.out.println(
                    "PVZ Server started on port "
                            + port
            );

            while (true) {

                Socket clientSocket =
                        serverSocket.accept();

                System.out.println(
                        "Client connected: "
                                + clientSocket
                                .getInetAddress()
                );

                ClientHandler handler =
                        new ClientHandler(
                                clientSocket,
                                authService
                        );

                Thread clientThread =
                        new Thread(handler);

                clientThread.start();
            }

        } catch (IOException e) {

            System.err.println(
                    "Server error: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}