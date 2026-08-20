package com.ussr.pvz.network;

import com.google.gson.Gson;
import com.ussr.pvz.shared.network.NetworkRequest;
import com.ussr.pvz.shared.network.NetworkResponse;
import com.ussr.pvz.shared.network.RequestType;
import com.ussr.pvz.shared.multiplayer.MatchCommand;
import com.ussr.pvz.shared.multiplayer.MatchServerMessage;

import java.io.*;
import java.net.Socket;
import java.util.Objects;
import java.util.function.Consumer;

public class NetworkClient {

    private static final NetworkClient instance =
            new NetworkClient();

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    private final Gson gson = new Gson();
    private volatile Consumer<MatchServerMessage> matchMessageHandler = message -> { };

    private NetworkClient() {
    }

    public static NetworkClient getInstance() {
        return instance;
    }

    public synchronized void connect(
            String host,
            int port
    ) throws IOException {

        if (isConnected()) {
            return;
        }

        socket = new Socket(host, port);

        reader = new BufferedReader(
                new InputStreamReader(
                        socket.getInputStream()
                )
        );

        writer = new PrintWriter(
                socket.getOutputStream(),
                true
        );
    }

    public synchronized NetworkResponse send(
            NetworkRequest request
    ) throws IOException {

        if (!isConnected() ||
                reader == null ||
                writer == null) {

            throw new IOException(
                    "Not connected to server."
            );
        }

        String json =
                gson.toJson(request);

        writer.println(json);

        if (writer.checkError()) {
            throw new IOException(
                    "Failed to send request to server."
            );
        }

        String responseJson =
                reader.readLine();

        if (responseJson == null) {
            disconnect();

            throw new IOException(
                    "Server disconnected."
            );
        }

        return gson.fromJson(
                responseJson,
                NetworkResponse.class
        );
    }

    /**
     * Sends a match command through the existing request protocol.
     * The server's GAME_ACTION handler is responsible for relaying it.
     */
    public NetworkResponse sendMatchCommand(MatchCommand command) throws IOException {
        Objects.requireNonNull(command, "command");
        return send(new NetworkRequest(
                RequestType.GAME_ACTION,
                gson.toJsonTree(command).getAsJsonObject()
        ));
    }

    public void setMatchMessageHandler(Consumer<MatchServerMessage> handler) {
        matchMessageHandler = handler == null ? message -> { } : handler;
    }

    /**
     * Entry point for a polling service or future single socket-reader dispatcher.
     */
    public void dispatchMatchMessage(MatchServerMessage message) {
        matchMessageHandler.accept(Objects.requireNonNull(message, "message"));
    }

    public boolean isConnected() {

        return socket != null
                && socket.isConnected()
                && !socket.isClosed();
    }

    public synchronized void disconnect() {

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

        reader = null;
        writer = null;
        socket = null;
        matchMessageHandler = message -> { };
    }
}
