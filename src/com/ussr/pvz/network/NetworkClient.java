package com.ussr.pvz.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ussr.pvz.model.util.SessionManager;
import com.ussr.pvz.shared.multiplayer.MatchCommand;
import com.ussr.pvz.shared.multiplayer.MatchServerMessage;
import com.ussr.pvz.shared.multiplayer.MatchServerMessageType;
import com.ussr.pvz.shared.network.NetworkRequest;
import com.ussr.pvz.shared.network.NetworkResponse;
import com.ussr.pvz.shared.network.RequestType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public final class NetworkClient {

    private static final long RESPONSE_TIMEOUT_SECONDS = 15;

    private static final NetworkClient INSTANCE =
            new NetworkClient();

    private final Gson gson = new Gson();

    /*
     * Normal requests are serialized by send(), so responses are
     * completed in the same order as their requests.
     */
    private final BlockingQueue<
            CompletableFuture<NetworkResponse>
            > pendingResponses = new LinkedBlockingQueue<>();

    private volatile Consumer<MatchServerMessage>
            matchMessageHandler = message -> {
    };

    private volatile boolean readerRunning;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Thread readerThread;

    private NetworkClient() {
    }

    public static NetworkClient getInstance() {
        return INSTANCE;
    }

    public synchronized void connect(
            String host,
            int port
    ) throws IOException {
        if (isConnected()) {
            return;
        }

        requireNonBlank(host, "host");

        if (port <= 0 || port > 65_535) {
            throw new IllegalArgumentException(
                    "port must be between 1 and 65535"
            );
        }

        Socket newSocket = new Socket(host, port);

        try {
            BufferedReader newReader =
                    new BufferedReader(
                            new InputStreamReader(
                                    newSocket.getInputStream()
                            )
                    );

            PrintWriter newWriter =
                    new PrintWriter(
                            newSocket.getOutputStream(),
                            true
                    );

            socket = newSocket;
            reader = newReader;
            writer = newWriter;

            startReaderThread();

        } catch (IOException exception) {
            try {
                newSocket.close();
            } catch (IOException ignored) {
            }

            throw exception;
        }
    }

    /**
     * Sends an ordinary request and waits for its corresponding
     * NetworkResponse.
     *
     * Only the reader thread reads from the socket.
     */
    public synchronized NetworkResponse send(
            NetworkRequest request
    ) throws IOException {
        Objects.requireNonNull(request, "request");

        if (!isConnected()
                || reader == null
                || writer == null
                || !readerRunning) {
            throw new IOException(
                    "Not connected to server."
            );
        }

        CompletableFuture<NetworkResponse> pending =
                new CompletableFuture<>();

        /*
         * Add the pending response before writing. This prevents a
         * very fast response from arriving before it is registered.
         */
        pendingResponses.add(pending);

        writer.println(gson.toJson(request));

        if (writer.checkError()) {
            pendingResponses.remove(pending);

            throw new IOException(
                    "Failed to send request to server."
            );
        }

        try {
            return pending.get(
                    RESPONSE_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS
            );

        } catch (TimeoutException exception) {
            pendingResponses.remove(pending);

            throw new IOException(
                    "Server response timed out.",
                    exception
            );

        } catch (InterruptedException exception) {
            pendingResponses.remove(pending);
            Thread.currentThread().interrupt();

            throw new IOException(
                    "Interrupted while waiting for server response.",
                    exception
            );

        } catch (ExecutionException exception) {
            Throwable cause = exception.getCause();

            if (cause instanceof IOException ioException) {
                throw ioException;
            }

            throw new IOException(
                    "Failed to receive server response.",
                    cause
            );
        }
    }

    public NetworkResponse sendMatchCommand(
            MatchCommand command
    ) throws IOException {
        Objects.requireNonNull(command, "command");

        String token = SessionManager.getToken();

        if (token == null || token.isBlank()) {
            throw new IOException(
                    "Cannot send a match command without a session token."
            );
        }

        JsonObject commandData =
                gson.toJsonTree(command).getAsJsonObject();

        return send(
                new NetworkRequest(
                        RequestType.GAME_ACTION,
                        token,
                        commandData
                )
        );
    }

    public void setMatchMessageHandler(
            Consumer<MatchServerMessage> handler
    ) {
        matchMessageHandler =
                handler == null
                        ? message -> {
                }
                        : handler;
    }

    /**
     * Useful for tests or a manually supplied match message.
     */
    public void dispatchMatchMessage(
            MatchServerMessage message
    ) {
        Objects.requireNonNull(message, "message");
        deliverMatchMessage(message);
    }

    private synchronized void startReaderThread() {
        if (readerRunning) {
            return;
        }

        readerRunning = true;

        readerThread = new Thread(
                this::readLoop,
                "pvz-network-reader"
        );

        readerThread.setDaemon(true);
        readerThread.start();
    }

    /**
     * This is the only method allowed to call reader.readLine().
     */
    private void readLoop() {
        IOException failure = null;

        try {
            while (readerRunning) {
                BufferedReader activeReader = reader;

                if (activeReader == null) {
                    break;
                }

                String json = activeReader.readLine();

                if (json == null) {
                    failure = new IOException(
                            "Server disconnected."
                    );
                    break;
                }

                handleIncomingJson(json);
            }

        } catch (IOException exception) {
            if (readerRunning) {
                failure = exception;
            }

        } catch (RuntimeException exception) {
            if (readerRunning) {
                failure = new IOException(
                        "Invalid message received from server.",
                        exception
                );
            }

        } finally {
            readerRunning = false;

            IOException finalFailure =
                    failure != null
                            ? failure
                            : new IOException(
                            "Network connection closed."
                    );

            failPendingResponses(finalFailure);
            closeResources();
        }
    }

    private void handleIncomingJson(String json) {
        JsonObject object =
                gson.fromJson(json, JsonObject.class);

        if (object == null) {
            throw new IllegalArgumentException(
                    "Server sent an empty JSON message."
            );
        }

        if (isMatchServerMessage(object)) {
            MatchServerMessage message =
                    gson.fromJson(
                            object,
                            MatchServerMessage.class
                    );

            deliverMatchMessage(message);
            return;
        }

        NetworkResponse response =
                gson.fromJson(
                        object,
                        NetworkResponse.class
                );

        CompletableFuture<NetworkResponse> pending =
                pendingResponses.poll();

        if (pending == null) {
            /*
             * The server sent an ordinary response when no request
             * was waiting. This indicates a protocol error, but it
             * should not kill match-message processing.
             */
            System.err.println(
                    "[NetworkClient] Unexpected server response: "
                            + json
            );
            return;
        }

        pending.complete(response);
    }

    private boolean isMatchServerMessage(
            JsonObject object
    ) {
        if (!object.has("type")
                || object.get("type").isJsonNull()) {
            return false;
        }

        String typeName;

        try {
            typeName = object
                    .get("type")
                    .getAsString();

        } catch (RuntimeException exception) {
            return false;
        }

        try {
            MatchServerMessageType.valueOf(typeName);
            return true;

        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private void deliverMatchMessage(
            MatchServerMessage message
    ) {
        try {
            matchMessageHandler.accept(message);

        } catch (RuntimeException exception) {
            /*
             * A UI or gameplay handler failure must not terminate
             * the socket reader.
             */
            System.err.println(
                    "[NetworkClient] Match-message handler failed: "
                            + exception.getMessage()
            );

            exception.printStackTrace();
        }
    }

    public boolean isConnected() {
        Socket activeSocket = socket;

        return activeSocket != null
                && activeSocket.isConnected()
                && !activeSocket.isClosed();
    }

    public synchronized void disconnect() {
        readerRunning = false;

        failPendingResponses(
                new IOException(
                        "Network client disconnected."
                )
        );

        closeResources();

        Thread activeReaderThread = readerThread;

        if (activeReaderThread != null
                && activeReaderThread != Thread.currentThread()) {
            activeReaderThread.interrupt();
        }

        readerThread = null;
        matchMessageHandler = message -> {
        };
    }

    private void failPendingResponses(
            IOException exception
    ) {
        CompletableFuture<NetworkResponse> pending;

        while ((pending = pendingResponses.poll()) != null) {
            pending.completeExceptionally(exception);
        }
    }

    private synchronized void closeResources() {
        BufferedReader activeReader = reader;
        PrintWriter activeWriter = writer;
        Socket activeSocket = socket;

        reader = null;
        writer = null;
        socket = null;

        if (activeWriter != null) {
            activeWriter.close();
        }

        try {
            if (activeReader != null) {
                activeReader.close();
            }
        } catch (IOException ignored) {
        }

        try {
            if (activeSocket != null
                    && !activeSocket.isClosed()) {
                activeSocket.close();
            }
        } catch (IOException ignored) {
        }
    }

    private static void requireNonBlank(
            String value,
            String name
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    name + " must not be blank"
            );
        }
    }
}