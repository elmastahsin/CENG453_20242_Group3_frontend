package metu.ceng.ceng453_20242_group3_frontend.features.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import metu.ceng.ceng453_20242_group3_frontend.config.WebSocketConfig;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket manager for handling real-time multiplayer game communication.
 */
public class WebSocketManager {

    private WebSocketClient webSocketClient;
    private final ObjectMapper objectMapper;
    private boolean isConnected = false;
    private int currentGameId = -1;
    private String currentUsername;

    // Callback interface for handling WebSocket events
    public interface WebSocketCallback {
        void onConnected();
        void onDisconnected();
        void onGameStateReceived(String gameState);
        void onPlayerJoined(String playerName);
        void onPlayerLeft(String playerName);
        void onGameMove(String moveData);
        void onError(String error);
    }

    private WebSocketCallback callback;

    /**
     * Creates a new WebSocket manager.
     */
    public WebSocketManager() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Sets the callback for WebSocket events.
     *
     * @param callback The callback to handle WebSocket events
     */
    public void setCallback(WebSocketCallback callback) {
        this.callback = callback;
    }

    /**
     * Connects to the WebSocket server and joins a specific game.
     *
     * @param gameId The ID of the game to join
     * @param username The username of the player joining
     * @return true if connection was successful, false otherwise
     */
    public boolean connectAndJoinGame(int gameId, String username) {
        if (isConnected && currentGameId == gameId) {
            System.out.println("Already connected to game " + gameId);
            return true;
        }

        this.currentGameId = gameId;
        this.currentUsername = username;

        // Try different WebSocket URLs in order
        String[] wsUrls = {
            WebSocketConfig.WEBSOCKET_BASE_URL,  // ws://localhost:8080/ws/websocket
            WebSocketConfig.SOCKJS_BASE_URL,     // ws://localhost:8080/ws
            "ws://localhost:8080/websocket",     // Direct websocket
            "ws://localhost:8080/ws/info"        // SockJS info endpoint
        };

        for (String wsUrl : wsUrls) {
            System.out.println("🔄 Trying WebSocket URL: " + wsUrl);
            if (tryConnectToUrl(wsUrl, gameId, username)) {
                return true;
            }
        }

        System.err.println("✗ Failed to connect to any WebSocket endpoint");
        return false;
    }

    /**
     * Attempts to connect to a specific WebSocket URL.
     *
     * @param wsUrl The WebSocket URL to try
     * @param gameId The game ID
     * @param username The username
     * @return true if connection successful, false otherwise
     */
    private boolean tryConnectToUrl(String wsUrl, int gameId, String username) {
        try {
            // Disconnect any existing connection
            disconnect();

            // Create WebSocket connection to the specified URL
            URI serverUri = new URI(wsUrl);

            // Get authorization token from SessionManager
            String authToken = SessionManager.getInstance().getAuthToken();
            if (authToken == null || authToken.isEmpty()) {
                System.err.println("✗ No authentication token available");
                return false;
            }

            // Create headers map with authorization
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + authToken);

            System.out.println("=== WEBSOCKET CONNECTION ATTEMPT ===");
            System.out.println("URL: " + wsUrl);
            System.out.println("Game ID: " + gameId);
            System.out.println("Username: " + username);
            System.out.println("Auth Token: " + authToken.substring(0, 20) + "...");
            System.out.println("===================================");

            CountDownLatch connectionLatch = new CountDownLatch(1);

            webSocketClient = new WebSocketClient(serverUri, headers) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("=== WEBSOCKET CONNECTED ===");
                    System.out.println("Status: " + handshake.getHttpStatus());
                    System.out.println("Status Message: " + handshake.getHttpStatusMessage());
                    System.out.println("URL: " + wsUrl);

                    isConnected = true;
                    connectionLatch.countDown();

                    // Send join game message immediately after connection
                    joinGame(gameId, username);

                    // Notify callback
                    if (callback != null) {
                        Platform.runLater(() -> callback.onConnected());
                    }
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("=== WEBSOCKET MESSAGE RECEIVED ===");
                    System.out.println("Raw message: " + message);
                    
                    // 🔍 ENHANCED DEBUGGING: Log ALL message characteristics
                    System.out.println("🔍 MESSAGE ANALYSIS:");
                    System.out.println("   Length: " + message.length());
                    System.out.println("   Type: " + (message.startsWith("CONNECTED") ? "CONNECTED" : 
                                                   message.startsWith("MESSAGE") ? "MESSAGE" : 
                                                   message.startsWith("ERROR") ? "ERROR" : 
                                                   message.startsWith("RECEIPT") ? "RECEIPT" : "UNKNOWN"));
                    System.out.println("   Contains 'lobby': " + message.contains("lobby"));
                    System.out.println("   Contains 'start': " + message.contains("start"));
                    System.out.println("   Contains 'move': " + message.contains("move"));
                    System.out.println("   Contains 'firstPlayer': " + message.contains("firstPlayer"));
                    System.out.println("   Contains 'players': " + message.contains("players"));
                    System.out.println("   Contains 'ready': " + message.contains("ready"));
                    System.out.println("================================");
                    
                    handleIncomingMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("=== WEBSOCKET DISCONNECTED ===");
                    System.out.println("Code: " + code);
                    System.out.println("Reason: " + reason);
                    System.out.println("Remote: " + remote);
                    System.out.println("URL: " + wsUrl);

                    isConnected = false;

                    // Notify callback
                    if (callback != null) {
                        Platform.runLater(() -> callback.onDisconnected());
                    }
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("=== WEBSOCKET ERROR ===");
                    System.err.println("URL: " + wsUrl);
                    System.err.println("Error: " + ex.getMessage());
                    ex.printStackTrace();

                    isConnected = false;

                    // Notify callback
                    if (callback != null) {
                        Platform.runLater(() -> callback.onError("WebSocket error: " + ex.getMessage()));
                    }
                }
            };

            // Connect to WebSocket
            System.out.println("Connecting to WebSocket: " + serverUri);
            webSocketClient.connect();

            // Wait for connection with timeout
            boolean connected = connectionLatch.await(WebSocketConfig.CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            if (connected && isConnected) {
                System.out.println("✓ WebSocket connected successfully to: " + wsUrl);
                return true;
            } else {
                System.err.println("✗ WebSocket connection timeout for: " + wsUrl);
                disconnect();
                return false;
            }

        } catch (Exception e) {
            System.err.println("✗ Failed to connect to WebSocket URL: " + wsUrl);
            System.err.println("Error: " + e.getMessage());
            disconnect();
            return false;
        }
    }

    /**
     * Joins a game by sending the join message and subscribing to game topics.
     * 
     * @param gameId The game ID to join
     * @param username The username of the player joining
     */
    private void joinGame(int gameId, String username) {
        try {
            // Store current game info
            currentGameId = gameId;
            currentUsername = username;
            
            // Send STOMP CONNECT frame first
            String connectFrame = "CONNECT\n" +
                                "accept-version:1.1,1.0\n" +
                                "heart-beat:10000,10000\n" +
                                "\n" +
                                "\0";
            
            System.out.println("=== SENDING STOMP CONNECT ===");
            System.out.println(connectFrame);
            System.out.println("============================");

            webSocketClient.send(connectFrame);

            // Wait longer for STOMP connection to be established
            Thread.sleep(2000);

            // 🔧 CRITICAL FIX: Subscribe BEFORE joining to avoid missing broadcasts
            System.out.println("🔧 === SUBSCRIBING BEFORE JOINING ===");
            System.out.println("This ensures we don't miss any broadcasts");
            System.out.println("====================================");
            
            // Send SUBSCRIBE frame to receive game state updates (lobby) FIRST
            String lobbyDestination = "/topic/game/" + gameId + "/lobby";
            String lobbySubscribeId = "sub-lobby-" + gameId;
            String lobbySubscribeFrame = "SUBSCRIBE\n" +
                                        "id:" + lobbySubscribeId + "\n" +
                                        "destination:" + lobbyDestination + "\n" +
                                        "\n" +
                                        "\0";
            System.out.println("=== SENDING STOMP SUBSCRIBE (LOBBY) ===");
            System.out.println("Game ID: " + gameId);
            System.out.println("Subscribe ID: " + lobbySubscribeId);
            System.out.println("Subscribe Destination: " + lobbyDestination);
            System.out.println("STOMP Frame:");
            System.out.println(lobbySubscribeFrame);
            System.out.println("===============================");
            webSocketClient.send(lobbySubscribeFrame);
            
            // Send SUBSCRIBE frame to receive game start event
            String startDestination = "/topic/game/" + gameId + "/start";
            String startSubscribeId = "sub-start-" + gameId;
            String startSubscribeFrame = "SUBSCRIBE\n" +
                                        "id:" + startSubscribeId + "\n" +
                                        "destination:" + startDestination + "\n" + 
                                        "\n" +
                                        "\0";
            System.out.println("=== SENDING STOMP SUBSCRIBE (START) ===");
            System.out.println("Game ID: " + gameId);
            System.out.println("Subscribe ID: " + startSubscribeId);
            System.out.println("Subscribe Destination: " + startDestination);
            System.out.println("STOMP Frame:");
            System.out.println(startSubscribeFrame);
            System.out.println("===============================");
            webSocketClient.send(startSubscribeFrame);
            
            // Subscribe to game moves
            String moveDestination = "/topic/game/" + gameId + "/move";
            String moveSubscribeId = "sub-move-" + gameId;
            String moveSubscribeFrame = "SUBSCRIBE\n" +
                                       "id:" + moveSubscribeId + "\n" +
                                       "destination:" + moveDestination + "\n" +
                                       "\n" +
                                       "\0";
            System.out.println("=== SENDING STOMP SUBSCRIBE (MOVES) ===");
            System.out.println("Game ID: " + gameId);
            System.out.println("Subscribe ID: " + moveSubscribeId);
            System.out.println("Subscribe Destination: " + moveDestination);
            System.out.println("STOMP Frame:");
            System.out.println(moveSubscribeFrame);
            System.out.println("===============================");
            webSocketClient.send(moveSubscribeFrame);
            
            // Subscribe to game state updates (where backend broadcasts moves)
            String stateDestination = "/topic/game/" + gameId + "/state";
            String stateSubscribeId = "sub-state-" + gameId;
            String stateSubscribeFrame = "SUBSCRIBE\n" +
                                        "id:" + stateSubscribeId + "\n" +
                                        "destination:" + stateDestination + "\n" +
                                        "\n" +
                                        "\0";
            System.out.println("=== SENDING STOMP SUBSCRIBE (STATE) ===");
            System.out.println("Game ID: " + gameId);
            System.out.println("Subscribe ID: " + stateSubscribeId);
            System.out.println("Subscribe Destination: " + stateDestination);
            System.out.println("STOMP Frame:");
            System.out.println(stateSubscribeFrame);
            System.out.println("===============================");
            webSocketClient.send(stateSubscribeFrame);
            
            // Wait for subscriptions to be registered on server
            Thread.sleep(1000);

            // NOW send join message AFTER subscriptions are active
            System.out.println("📡 === NOW JOINING AFTER SUBSCRIPTIONS ===");

            // Send STOMP SEND frame to join game
            String destination = "/app/game/" + gameId + "/join";
            String sendFrame = "SEND\n" +
                              "destination:" + destination + "\n" +
                              "content-type:application/json\n" +
                              "\n" +
                              "{\"username\":\"" + username + "\"}\n" +
                              "\0";
            
            System.out.println("=== SENDING STOMP JOIN GAME MESSAGE ===");
            System.out.println("Game ID: " + gameId);
            System.out.println("Username: " + username);
            System.out.println("Destination: " + destination);
            System.out.println("STOMP Frame:");
            System.out.println(sendFrame);
            System.out.println("======================================");

            webSocketClient.send(sendFrame);

            System.out.println("✅ ALL STOMP FRAMES SENT SUCCESSFULLY");
            System.out.println("- CONNECT frame sent");
            System.out.println("- SEND (join game) frame sent");
            System.out.println("- SUBSCRIBE (lobby state) frame sent");
            System.out.println("- SUBSCRIBE (game start) frame sent");
            System.out.println("- SUBSCRIBE (game moves) frame sent");
            System.out.println("- SUBSCRIBE (game state) frame sent");
            System.out.println("Now waiting for lobby updates and game start events...");

            // Start a timeout timer to check for game state updates
            new Thread(() -> {
                try {
                    Thread.sleep(10000); // Wait 10 seconds
                    if (isConnected) {
                        System.out.println("⚠️ === GAME STATE TIMEOUT WARNING ===");
                        System.out.println("No game state received after 10 seconds");
                        System.out.println("This might indicate:");
                        System.out.println("1. Game hasn't started yet (waiting for more players)");
                        System.out.println("2. Server subscription issue");
                        System.out.println("3. Game state is empty/pending");
                        System.out.println("Connection is still active - will continue listening...");
                        System.out.println("=====================================");

                        // Try multiple approaches to get game state
                        System.out.println("🔧 === TRYING ALTERNATIVE APPROACHES ===");
                        
                        // 1. Manual request for game state
                        requestGameState(gameId);
                        
                        // 2. Try requesting lobby state specifically
                        requestLobbyState(gameId);
                        
                        // 3. Try re-subscribing in case first subscription failed
                        resubscribeToGameTopics(gameId);
                        
                        System.out.println("========================================");
                    }
                } catch (InterruptedException e) {
                    // Thread interrupted, ignore
                }
            }).start();
            
        } catch (Exception e) {
            System.err.println("❌ Error joining game via WebSocket: " + e.getMessage());
            e.printStackTrace();
            if (callback != null) {
                callback.onError("Failed to join game: " + e.getMessage());
            }
        }
    }

    /**
     * Disconnects the WebSocket client if connected.
     */
    public void disconnect() {
        if (webSocketClient != null && isConnected) {
            try {
                webSocketClient.close();
            } catch (Exception e) {
                System.err.println("Error while closing WebSocket: " + e.getMessage());
            }
            isConnected = false;
            webSocketClient = null;
        }
    }

    /**
     * Returns whether the WebSocket is currently connected.
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Handles incoming WebSocket messages (STOMP protocol).
     * 
     * @param message The raw message received from the server
     */
    private void handleIncomingMessage(String message) {
        try {
            System.out.println("=== PROCESSING STOMP MESSAGE ===");
            System.out.println("Content: " + message);
            System.out.println("===============================");
            
            // Parse STOMP frame
            if (message.startsWith("CONNECTED")) {
                System.out.println("✅ STOMP Connected successfully");
                System.out.println("Server acknowledged CONNECT frame");
                // Extract connection details
                String[] lines = message.split("\n");
                for (String line : lines) {
                    if (line.startsWith("version:")) {
                        System.out.println("📋 STOMP Version: " + line.substring(8));
                    } else if (line.startsWith("heart-beat:")) {
                        System.out.println("💓 Heart-beat: " + line.substring(11));
                    } else if (line.startsWith("user-name:")) {
                        System.out.println("👤 Authenticated as: " + line.substring(10));
                    }
                }
                return;
            }
            
            if (message.startsWith("ERROR")) {
                System.err.println("❌ STOMP ERROR received:");
                System.err.println(message);
                String[] lines = message.split("\n");
                for (String line : lines) {
                    if (line.startsWith("message:")) {
                        System.err.println("Error message: " + line.substring(8));
                    }
                }
                if (callback != null) {
                    Platform.runLater(() -> callback.onError("STOMP Error: " + message));
                }
                return;
            }
            
            if (message.startsWith("MESSAGE")) {
                System.out.println("📨 STOMP MESSAGE received:");
                handleStompMessage(message);
                return;
            }
            
            if (message.startsWith("RECEIPT")) {
                System.out.println("✅ STOMP RECEIPT received - operation confirmed");
                String[] lines = message.split("\n");
                for (String line : lines) {
                    if (line.startsWith("receipt-id:")) {
                        System.out.println("📧 Receipt ID: " + line.substring(11));
                    }
                }
                return;
            }
            
            if (message.trim().isEmpty() || message.equals("\n") || message.equals("\0")) {
                System.out.println("💓 Heartbeat or empty frame received");
                return;
            }
            
            System.out.println("❓ UNKNOWN STOMP FRAME TYPE:");
            System.out.println("First line: " + (message.contains("\n") ? message.split("\n")[0] : message));
            System.out.println("Full content: " + message);
            
        } catch (Exception e) {
            System.err.println("❌ CRITICAL ERROR parsing STOMP message: " + e.getMessage());
            System.err.println("Raw message that caused error:");
            System.err.println(message);
            e.printStackTrace();
        }
    }
    
    /**
     * Handles STOMP MESSAGE frames by parsing the destination and routing appropriately.
     * 
     * @param message The STOMP MESSAGE frame
     */
    private void handleStompMessage(String message) {
        try {
            // Parse STOMP MESSAGE frame headers and body
            String[] lines = message.split("\n");
            String destination = null;
            String subscriptionId = null;
            boolean inBody = false;
            StringBuilder jsonPayload = new StringBuilder();
            
            for (String line : lines) {
                if (inBody) {
                    if (!line.equals("\0")) {
                        jsonPayload.append(line).append("\n");
                    }
                } else if (line.startsWith("destination:")) {
                    destination = line.substring(12);
                    System.out.println("📍 Message destination: " + destination);
                } else if (line.startsWith("subscription:")) {
                    subscriptionId = line.substring(13);
                    System.out.println("🆔 Subscription ID: " + subscriptionId);
                } else if (line.startsWith("content-type:")) {
                    System.out.println("📄 Content-Type: " + line.substring(13));
                } else if (line.trim().isEmpty()) {
                    inBody = true; // Empty line indicates start of body
                    System.out.println("📄 Starting to parse message body...");
                }
            }
            
            String jsonContent = jsonPayload.toString().trim();
            System.out.println("📋 JSON Content Length: " + jsonContent.length());
            
            if (!jsonContent.isEmpty() && !jsonContent.equals("\0")) {
                System.out.println("🔍 Raw JSON Content:");
                System.out.println(jsonContent);
                System.out.println("========================");
                
                // Handle /start topic: transition UI to game state
                if (destination != null && destination.endsWith("/start")) {
                    System.out.println("🚦 Game start event received! Transitioning UI to game state.");
                    if (callback != null) {
                        Platform.runLater(() -> callback.onGameStateReceived(jsonContent));
                    }
                    return;
                }
                
                // Handle /lobby topic: update lobby state
                if (destination != null && destination.endsWith("/lobby")) {
                    System.out.println("👥 Lobby update received.");
                    if (callback != null) {
                        Platform.runLater(() -> callback.onGameStateReceived(jsonContent));
                    }
                    return;
                }
                
                // Handle /move topic: process game moves
                if (destination != null && destination.endsWith("/move")) {
                    System.out.println("🎯 Game move received! Processing opponent move.");
                    if (callback != null) {
                        Platform.runLater(() -> callback.onGameMove(jsonContent));
                    }
                    return;
                }
                
                // Handle /state topic: also process as game moves (backend broadcasts moves here)
                if (destination != null && destination.endsWith("/state")) {
                    System.out.println("🎯 Game state update received! Processing as potential move.");
                    if (callback != null) {
                        Platform.runLater(() -> callback.onGameMove(jsonContent));
                    }
                    return;
                }
                
            } else {
                System.out.println("⚠️ Empty or null message body received");
                System.out.println("This might be a heartbeat or acknowledgment message");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error parsing STOMP message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Manually requests the current game state.
     *
     * @param gameId The game ID to request state for
     */
    private void requestGameState(int gameId) {
        if (!isConnected) {
            System.err.println("Cannot request game state - not connected");
            return;
        }
        try {
            System.out.println("🔄 === MANUALLY REQUESTING GAME STATE ===");
            String destination = "/app/game/" + gameId + "/state";
            String sendFrame = "SEND\n" +
                              "destination:" + destination + "\n" +
                              "content-type:application/json\n" +
                              "\n" +
                              "{}\n" +
                              "\0";
            System.out.println("Destination: " + destination);
            System.out.println("STOMP Frame:");
            System.out.println(sendFrame);
            System.out.println("========================================");
            webSocketClient.send(sendFrame);
        } catch (Exception e) {
            System.err.println("✗ Failed to request game state: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void requestLobbyState(int gameId) {
        if (!isConnected) {
            System.err.println("Cannot request lobby state - not connected");
            return;
        }
        
        try {
            System.out.println("🏛️ === MANUALLY REQUESTING LOBBY STATE ===");
            
            String destination = "/app/game/" + gameId + "/lobby";
            String sendFrame = "SEND\n" +
                              "destination:" + destination + "\n" +
                              "content-type:application/json\n" +
                              "\n" +
                              "{}\n" +
                              "\0";
            
            System.out.println("Destination: " + destination);
            System.out.println("STOMP Frame:");
            System.out.println(sendFrame);
            System.out.println("==========================================");
            
            webSocketClient.send(sendFrame);
            
        } catch (Exception e) {
            System.err.println("✗ Failed to request lobby state: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Re-subscribes to all game-related topics after connection issues.
     * 
     * @param gameId The game ID to subscribe to
     */
    private void resubscribeToGameTopics(int gameId) {
        try {
            System.out.println("🔄 === RE-SUBSCRIBING TO GAME TOPICS ===");
            
            // Re-subscribe to lobby updates
            String lobbyDestination = "/topic/game/" + gameId + "/lobby";
            String lobbySubscribeId = "resub-lobby-" + gameId + "-" + System.currentTimeMillis();
            String lobbySubscribeFrame = "SUBSCRIBE\n" +
                                        "destination:" + lobbyDestination + "\n" +
                                        "id:" + lobbySubscribeId + "\n" +
                                        "\n" +
                                        "\0";
            
            System.out.println("Lobby destination: " + lobbyDestination);
            System.out.println("Lobby subscribe ID: " + lobbySubscribeId);
            webSocketClient.send(lobbySubscribeFrame);
            
            // Re-subscribe to game start events
            String startDestination = "/topic/game/" + gameId + "/start";
            String startSubscribeId = "resub-start-" + gameId + "-" + System.currentTimeMillis();
            String startSubscribeFrame = "SUBSCRIBE\n" +
                                        "destination:" + startDestination + "\n" +
                                        "id:" + startSubscribeId + "\n" +
                                        "\n" +
                                        "\0";
            
            System.out.println("Start destination: " + startDestination);
            System.out.println("Start subscribe ID: " + startSubscribeId);
            webSocketClient.send(startSubscribeFrame);
            
            // Re-subscribe to game moves
            String moveDestination = "/topic/game/" + gameId + "/move";
            String moveSubscribeId = "resub-move-" + gameId + "-" + System.currentTimeMillis();
            String moveSubscribeFrame = "SUBSCRIBE\n" +
                                       "destination:" + moveDestination + "\n" +
                                       "id:" + moveSubscribeId + "\n" +
                                       "\n" +
                                       "\0";
            
            System.out.println("Move destination: " + moveDestination);
            System.out.println("Move subscribe ID: " + moveSubscribeId);
            webSocketClient.send(moveSubscribeFrame);
            
            // Re-subscribe to game state updates (where backend broadcasts moves)
            String stateDestination = "/topic/game/" + gameId + "/state";
            String stateSubscribeId = "resub-state-" + gameId + "-" + System.currentTimeMillis();
            String stateSubscribeFrame = "SUBSCRIBE\n" +
                                        "destination:" + stateDestination + "\n" +
                                        "id:" + stateSubscribeId + "\n" +
                                        "\n" +
                                        "\0";
            
            System.out.println("State destination: " + stateDestination);
            System.out.println("State subscribe ID: " + stateSubscribeId);
            webSocketClient.send(stateSubscribeFrame);
            
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("❌ Error re-subscribing to game topics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sends a card play message to the server.
     * 
     * @param destination The destination endpoint 
     * @param jsonPayload The JSON payload to send
     */
    public void sendCardPlay(String destination, String jsonPayload) {
        try {
            if (!isConnected()) {
                System.err.println("❌ Cannot send card play: WebSocket not connected");
                return;
            }
            
            // Create STOMP SEND frame
            String sendFrame = "SEND\n" +
                              "destination:" + destination + "\n" +
                              "content-type:application/json\n" +
                              "\n" +
                              jsonPayload + "\n" +
                              "\0";
            
            System.out.println("📤 Sending card play message...");
            System.out.println("STOMP Frame:\n" + sendFrame);
            
            webSocketClient.send(sendFrame);
            System.out.println("✅ Card play message sent successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Error sending card play message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
