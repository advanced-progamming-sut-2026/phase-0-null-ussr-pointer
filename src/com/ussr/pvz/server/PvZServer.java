package com.ussr.pvz.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PvZServer {

    private final int port;

    public PvZServer(int port) {
        this.port = port;
    }

    public void start() {

        try (ServerSocket serverSocket =
                     new ServerSocket(port)) {

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
                                clientSocket
                        );

                Thread thread =
                        new Thread(handler);

                thread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}