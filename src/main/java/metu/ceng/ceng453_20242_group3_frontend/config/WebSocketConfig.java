package metu.ceng.ceng453_20242_group3_frontend.config;

/**
 * Configuration class for WebSocket connections and endpoints.
 * Contains WebSocket URLs, endpoints, and message types.
 */
public class WebSocketConfig {
    
    // Base WebSocket URL
    public static final String WEBSOCKET_BASE_URL = "ws://localhost:8080/ws";
    
    // WebSocket endpoint patterns
    public static final String GAME_JOIN_ENDPOINT = "game/%d/join";
    public static final String GAME_LEAVE_ENDPOINT = "game/%d/leave";
    public static final String GAME_STATE_ENDPOINT = "game/%d/state";
    public static final String GAME_MOVE_ENDPOINT = "game/%d/move";
    
    // Message types
    public static final String MSG_TYPE_JOIN = "JOIN";
    public static final String MSG_TYPE_LEAVE = "LEAVE";
    public static final String MSG_TYPE_GAME_STATE = "GAME_STATE";
    public static final String MSG_TYPE_PLAYER_MOVE = "PLAYER_MOVE";
    public static final String MSG_TYPE_GAME_END = "GAME_END";
    public static final String MSG_TYPE_ERROR = "ERROR";
    
    // Connection settings
    public static final int CONNECTION_TIMEOUT_MS = 5000;
    public static final int RECONNECT_DELAY_MS = 2000;
    public static final int MAX_RECONNECT_ATTEMPTS = 3;
    
    /**
     * Builds a complete WebSocket URL with the base URL.
     * 
     * @param endpoint The endpoint to append to the base URL
     * @return Complete WebSocket URL
     */
    public static String buildWebSocketUrl(String endpoint) {
        return WEBSOCKET_BASE_URL + "/" + endpoint;
    }
    
    /**
     * Formats the game join endpoint with the specific game ID.
     * 
     * @param gameId The game ID to join
     * @return Formatted game join endpoint
     */
    public static String formatGameJoinEndpoint(int gameId) {
        return String.format(GAME_JOIN_ENDPOINT, gameId);
    }
    
    /**
     * Formats the game leave endpoint with the specific game ID.
     * 
     * @param gameId The game ID to leave
     * @return Formatted game leave endpoint
     */
    public static String formatGameLeaveEndpoint(int gameId) {
        return String.format(GAME_LEAVE_ENDPOINT, gameId);
    }
    
    /**
     * Formats the game state endpoint with the specific game ID.
     * 
     * @param gameId The game ID to get state for
     * @return Formatted game state endpoint
     */
    public static String formatGameStateEndpoint(int gameId) {
        return String.format(GAME_STATE_ENDPOINT, gameId);
    }
    
    /**
     * Formats the game move endpoint with the specific game ID.
     * 
     * @param gameId The game ID to send move to
     * @return Formatted game move endpoint
     */
    public static String formatGameMoveEndpoint(int gameId) {
        return String.format(GAME_MOVE_ENDPOINT, gameId);
    }
} 