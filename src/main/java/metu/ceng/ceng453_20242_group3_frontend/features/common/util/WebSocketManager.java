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
            
            // Wait a moment for connection to be established
            Thread.sleep(1000);
            
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
            
        } catch (Exception e) {
            System.err.println("✗ Failed to send STOMP join game message: " + e.getMessage());
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
                System.out.println("✓ STOMP Connected successfully");
                if (callback != null) {
                    Platform.runLater(() -> callback.onConnected());
                }
                return;
            }
            
            if (message.startsWith("ERROR")) {
                System.err.println("❌ STOMP ERROR received:");
                System.err.println(message);
                if (callback != null) {
                    Platform.runLater(() -> callback.onError("STOMP Error: " + message));
                }
                return;
            }
            
            if (message.startsWith("MESSAGE")) {
                System.out.println("📊 STOMP MESSAGE received:");
                
                // Extract JSON payload from STOMP MESSAGE frame
                String[] lines = message.split("\n");
                boolean inBody = false;
                StringBuilder jsonPayload = new StringBuilder();
                
                for (String line : lines) {
                    if (inBody) {
                        jsonPayload.append(line).append("\n");
                    } else if (line.trim().isEmpty()) {
                        inBody = true; // Empty line indicates start of body
                    }
                }
                
                String jsonContent = jsonPayload.toString().trim();
                if (!jsonContent.isEmpty() && !jsonContent.equals("\0")) {
                    try {
                        // Try to parse as JSON
                        JsonNode root = objectMapper.readTree(jsonContent);
                        handleGameStateUpdate(root);
                    } catch (Exception je) {
                        System.out.println("Non-JSON message content: " + jsonContent);
                    }
                }
                return;
            }
            
            // Handle other STOMP frames
            System.out.println("❓ UNKNOWN STOMP FRAME: " + message);
            
        } catch (Exception e) {
            System.err.println("✗ Failed to parse STOMP message: " + e.getMessage());
            System.err.println("Raw message: " + message);
            e.printStackTrace();
        }
    }
    
    /**
     * Handles parsed game state updates from STOMP messages.
     * 
     * @param root The parsed JSON node
     */
    private void handleGameStateUpdate(JsonNode root) {
        try {
            // Extract message type
            String messageType = root.has("type") ? root.get("type").asText() : "GAME_STATE";
            
            System.out.println("=== PROCESSING GAME UPDATE ===");
            System.out.println("Type: " + messageType);
            System.out.println("Content: " + root.toString());
            System.out.println("=============================");
            
            switch (messageType) {
                case "GAME_STATE":
                    // Handle game state updates
                    System.out.println("📊 GAME STATE RECEIVED:");
                    System.out.println(root.toString());
                    
                    if (callback != null) {
                        Platform.runLater(() -> callback.onGameStateReceived(root.toString()));
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
                    
                default:
                    System.out.println("📋 GAME UPDATE: " + messageType);
                    if (callback != null) {
                        Platform.runLater(() -> callback.onGameStateReceived(root.toString()));
                    }
                    break;
            }
            
        } catch (Exception e) {
            System.err.println("✗ Failed to handle game state update: " + e.getMessage());
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