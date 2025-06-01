package metu.ceng.ceng453_20242_group3_frontend.features.common.util;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.HashMap;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.application.Platform;
import metu.ceng.ceng453_20242_group3_frontend.config.WebSocketConfig;

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
     * Sends a join game message to the WebSocket server using STOMP protocol.
     * 
     * @param gameId The game ID to join
     * @param username The username of the player
     */
    private void joinGame(int gameId, String username) {
        if (!isConnected) {
            System.err.println("Cannot send join message - not connected");
            return;
        }
        
        try {
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
            
            // Wait longer for join to be processed by server
            Thread.sleep(1500);
            
            // Send SUBSCRIBE frame to receive game state updates
            String subscribeDestination = "/topic/game/" + gameId + "/lobby";
            String subscribeId = "sub-game-" + gameId;
            String subscribeFrame = "SUBSCRIBE\n" +
                                   "id:" + subscribeId + "\n" +
                                   "destination:" + subscribeDestination + "\n" +
                                   "\n" +
                                   "\0";
            
            System.out.println("=== SENDING STOMP SUBSCRIBE ===");
            System.out.println("Game ID: " + gameId);
            System.out.println("Subscribe ID: " + subscribeId);
            System.out.println("Subscribe Destination: " + subscribeDestination);
            System.out.println("STOMP Frame:");
            System.out.println(subscribeFrame);
            System.out.println("===============================");
            
            webSocketClient.send(subscribeFrame);
            
            System.out.println("✅ ALL STOMP FRAMES SENT SUCCESSFULLY");
            System.out.println("- CONNECT frame sent");
            System.out.println("- SEND (join game) frame sent");
            System.out.println("- SUBSCRIBE (game state) frame sent");
            System.out.println("Now waiting for game state updates...");
            
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
                        
                        // Try to manually request game state
                        requestGameState(gameId);
                    }
                } catch (InterruptedException e) {
                    // Thread interrupted, ignore
                }
            }).start();
            
        } catch (Exception e) {
            System.err.println("✗ Failed to send STOMP join game message: " + e.getMessage());
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
                
                // Don't call callback here - wait for actual game state
                return;
            }
            
            if (message.startsWith("ERROR")) {
                System.err.println("❌ STOMP ERROR received:");
                System.err.println(message);
                
                // Parse error details
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
                System.out.println("🎉 === GAME STATE UPDATE INCOMING ===");
                
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
                    
                    try {
                        // Parse JSON game state
                        JsonNode gameState = objectMapper.readTree(jsonContent);
                        handleGameStateReceived(gameState, destination, subscriptionId);
                    } catch (Exception je) {
                        System.err.println("❌ Failed to parse JSON content: " + je.getMessage());
                        System.err.println("Raw content that failed: " + jsonContent);
                        je.printStackTrace();
                    }
                } else {
                    System.out.println("⚠️ Empty or null message body received");
                    System.out.println("This might be a heartbeat or acknowledgment message");
                }
                return;
            }
            
            if (message.startsWith("RECEIPT")) {
                System.out.println("✅ STOMP RECEIPT received - operation confirmed");
                
                // Parse receipt details
                String[] lines = message.split("\n");
                for (String line : lines) {
                    if (line.startsWith("receipt-id:")) {
                        System.out.println("📧 Receipt ID: " + line.substring(11));
                    }
                }
                return;
            }
            
            // Handle heartbeat or other simple frames
            if (message.trim().isEmpty() || message.equals("\n") || message.equals("\0")) {
                System.out.println("💓 Heartbeat or empty frame received");
                return;
            }
            
            // Handle other STOMP frames
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
     * Handles received game state updates with comprehensive logging.
     * 
     * @param gameState The parsed game state JSON
     * @param destination The STOMP destination
     * @param subscriptionId The subscription ID
     */
    private void handleGameStateReceived(JsonNode gameState, String destination, String subscriptionId) {
        try {
            System.out.println("🎮 === GAME STATE UPDATE RECEIVED ===");
            System.out.println("📍 From destination: " + destination);
            System.out.println("🆔 Subscription ID: " + subscriptionId);
            
            // Extract key game information
            if (gameState.has("gameId")) {
                int gameId = gameState.get("gameId").asInt();
                System.out.println("🎯 Game ID: " + gameId);
            }
            
            if (gameState.has("status")) {
                String status = gameState.get("status").asText();
                System.out.println("📊 Game Status: " + status);
            }
            
            if (gameState.has("currentPlayer")) {
                String currentPlayer = gameState.get("currentPlayer").asText();
                System.out.println("🎲 Current Player: " + currentPlayer);
            }
            
            if (gameState.has("isClockwise")) {
                boolean isClockwise = gameState.get("isClockwise").asBoolean();
                System.out.println("🔄 Direction: " + (isClockwise ? "Clockwise" : "Counter-clockwise"));
            }
            
            // Log top card information
            if (gameState.has("topCard") && !gameState.get("topCard").isNull()) {
                JsonNode topCard = gameState.get("topCard");
                System.out.println("🃏 Top Card: " + 
                    topCard.get("color").asText() + " " + 
                    (topCard.has("number") && !topCard.get("number").isNull() ? 
                        topCard.get("number").asText() : 
                        topCard.get("action").asText()));
            }
            
            // Log players information
            if (gameState.has("players") && gameState.get("players").isArray()) {
                JsonNode players = gameState.get("players");
                System.out.println("👥 Players (" + players.size() + "):");
                for (int i = 0; i < players.size(); i++) {
                    JsonNode player = players.get(i);
                    String username = player.get("username").asText();
                    int cardCount = player.get("cardCount").asInt();
                    boolean isHost = player.has("isHost") ? player.get("isHost").asBoolean() : false;
                    System.out.println("  " + (i + 1) + ". " + username + 
                        " (" + cardCount + " cards)" + 
                        (isHost ? " [HOST]" : ""));
                }
            }
            
            // Log player hand information
            if (gameState.has("playerHand") && gameState.get("playerHand").isArray()) {
                JsonNode playerHand = gameState.get("playerHand");
                System.out.println("🎴 Your Hand (" + playerHand.size() + " cards):");
                for (int i = 0; i < Math.min(playerHand.size(), 5); i++) { // Show first 5 cards
                    JsonNode card = playerHand.get(i);
                    System.out.println("  " + (i + 1) + ". " + 
                        card.get("color").asText() + " " +
                        (card.has("number") && !card.get("number").isNull() ? 
                            card.get("number").asText() : 
                            card.get("action").asText()));
                }
                if (playerHand.size() > 5) {
                    System.out.println("  ... and " + (playerHand.size() - 5) + " more cards");
                }
            }
            
            // Log deck information
            if (gameState.has("deckCardsRemaining")) {
                int deckCards = gameState.get("deckCardsRemaining").asInt();
                System.out.println("🎰 Deck Cards Remaining: " + deckCards);
            }
            
            // Log last action
            if (gameState.has("lastAction")) {
                String lastAction = gameState.get("lastAction").asText();
                System.out.println("📝 Last Action: " + lastAction);
            }
            
            System.out.println("🎮 === END GAME STATE UPDATE ===");
            
            // Notify callback with full game state
            if (callback != null) {
                Platform.runLater(() -> {
                    callback.onGameStateReceived(gameState.toString());
                    System.out.println("✅ Game state forwarded to UI callback");
                });
            } else {
                System.out.println("⚠️ No callback registered for game state updates");
            }
            
        } catch (Exception e) {
            System.err.println("❌ ERROR processing game state: " + e.getMessage());
            System.err.println("Game state that caused error: " + gameState.toString());
            e.printStackTrace();
        }
    }
    
    /**
     * Disconnects from the WebSocket server.
     */
    public void disconnect() {
        if (webSocketClient != null) {
            try {
                System.out.println("Disconnecting from WebSocket...");
                webSocketClient.close();
                webSocketClient = null;
            } catch (Exception e) {
                System.err.println("Error disconnecting WebSocket: " + e.getMessage());
            }
        }
        isConnected = false;
        currentGameId = -1;
        currentUsername = null;
    }
    
    /**
     * Checks if the WebSocket is currently connected.
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return isConnected && webSocketClient != null && webSocketClient.isOpen();
    }
    
    /**
     * Gets the current game ID.
     * 
     * @return The current game ID or -1 if not connected to any game
     */
    public int getCurrentGameId() {
        return currentGameId;
    }
    
    /**
     * Gets the current username.
     * 
     * @return The current username or null if not connected
     */
    public String getCurrentUsername() {
        return currentUsername;
    }
} 