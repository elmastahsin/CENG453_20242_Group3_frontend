package metu.ceng.ceng453_20242_group3_frontend.features.common.util;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
        
        try {
            // Disconnect any existing connection
            disconnect();
            
            // Create WebSocket connection
            URI serverUri = new URI(WebSocketConfig.WEBSOCKET_BASE_URL);
            
            CountDownLatch connectionLatch = new CountDownLatch(1);
            
            webSocketClient = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("=== WEBSOCKET CONNECTED ===");
                    System.out.println("Status: " + handshake.getHttpStatus());
                    System.out.println("Status Message: " + handshake.getHttpStatusMessage());
                    
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
                    
                    isConnected = false;
                    
                    // Notify callback
                    if (callback != null) {
                        Platform.runLater(() -> callback.onDisconnected());
                    }
                }
                
                @Override
                public void onError(Exception ex) {
                    System.err.println("=== WEBSOCKET ERROR ===");
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
                System.out.println("✓ WebSocket connected successfully");
                return true;
            } else {
                System.err.println("✗ WebSocket connection timeout");
                disconnect();
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("✗ Failed to connect to WebSocket: " + e.getMessage());
            e.printStackTrace();
            disconnect();
            return false;
        }
    }
    
    /**
     * Sends a join game message to the WebSocket server.
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
            String endpoint = WebSocketConfig.formatGameJoinEndpoint(gameId);
            String joinMessage = String.format("""
                {
                  "type": "%s",
                  "endpoint": "%s",
                  "gameId": %d,
                  "username": "%s",
                  "timestamp": "%s"
                }
                """, 
                WebSocketConfig.MSG_TYPE_JOIN,
                endpoint,
                gameId,
                username,
                java.time.Instant.now().toString()
            );
            
            System.out.println("=== SENDING JOIN GAME MESSAGE ===");
            System.out.println("Game ID: " + gameId);
            System.out.println("Username: " + username);
            System.out.println("Endpoint: " + endpoint);
            System.out.println("Message: " + joinMessage);
            System.out.println("===============================");
            
            webSocketClient.send(joinMessage);
            
        } catch (Exception e) {
            System.err.println("✗ Failed to send join game message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handles incoming WebSocket messages.
     * 
     * @param message The raw message received from the server
     */
    private void handleIncomingMessage(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            
            // Extract message type
            String messageType = root.has("type") ? root.get("type").asText() : "UNKNOWN";
            
            System.out.println("=== PROCESSING MESSAGE ===");
            System.out.println("Type: " + messageType);
            System.out.println("Content: " + message);
            System.out.println("========================");
            
            switch (messageType) {
                case "GAME_STATE":
                    // Handle game state updates
                    if (root.has("gameState")) {
                        String gameState = root.get("gameState").toString();
                        System.out.println("📊 GAME STATE RECEIVED:");
                        System.out.println(gameState);
                        
                        if (callback != null) {
                            Platform.runLater(() -> callback.onGameStateReceived(gameState));
                        }
                    }
                    break;
                    
                case "PLAYER_JOINED":
                    // Handle player joining
                    if (root.has("playerName")) {
                        String playerName = root.get("playerName").asText();
                        System.out.println("👤 PLAYER JOINED: " + playerName);
                        
                        if (callback != null) {
                            Platform.runLater(() -> callback.onPlayerJoined(playerName));
                        }
                    }
                    break;
                    
                case "PLAYER_LEFT":
                    // Handle player leaving
                    if (root.has("playerName")) {
                        String playerName = root.get("playerName").asText();
                        System.out.println("👤 PLAYER LEFT: " + playerName);
                        
                        if (callback != null) {
                            Platform.runLater(() -> callback.onPlayerLeft(playerName));
                        }
                    }
                    break;
                    
                case "PLAYER_MOVE":
                    // Handle player moves
                    if (root.has("moveData")) {
                        String moveData = root.get("moveData").toString();
                        System.out.println("🎯 PLAYER MOVE RECEIVED:");
                        System.out.println(moveData);
                        
                        if (callback != null) {
                            Platform.runLater(() -> callback.onGameMove(moveData));
                        }
                    }
                    break;
                    
                case "ERROR":
                    // Handle errors
                    String errorMessage = root.has("message") ? root.get("message").asText() : "Unknown error";
                    System.err.println("❌ SERVER ERROR: " + errorMessage);
                    
                    if (callback != null) {
                        Platform.runLater(() -> callback.onError(errorMessage));
                    }
                    break;
                    
                default:
                    System.out.println("❓ UNKNOWN MESSAGE TYPE: " + messageType);
                    System.out.println("Full message: " + message);
                    break;
            }
            
        } catch (Exception e) {
            System.err.println("✗ Failed to parse WebSocket message: " + e.getMessage());
            System.err.println("Raw message: " + message);
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