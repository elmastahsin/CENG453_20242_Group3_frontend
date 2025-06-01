package metu.ceng.ceng453_20242_group3_frontend.features.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import metu.ceng.ceng453_20242_group3_frontend.features.auth.model.User;

/**
 * Singleton class to manage user session data.
 */
public class SessionManager {

    private static SessionManager instance;

    private String authToken;
    private String username;
    private Long userId;
    private boolean loggedIn;
    private final ObjectMapper objectMapper;
    private User currentUser;
    private String refreshToken;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private SessionManager() {
        this.authToken = null;
        this.username = null;
        this.userId = null;
        this.loggedIn = false;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get the singleton instance of SessionManager.
     *
     * @return The SessionManager instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Login a user with authentication token.
     *
     * @param username The username
     * @param userId The user ID
     * @param authToken The authentication token
     */
    public void login(String username, Long userId, String authToken) {
        this.username = username;
        this.userId = userId;
        this.authToken = authToken;
        this.loggedIn = true;
    }

    /**
     * Logout the current user.
     */
    public void logout() {
        this.username = null;
        this.userId = null;
        this.authToken = null;
        this.loggedIn = false;
    }

    /**
     * Check if a user is logged in.
     *
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * Get the authentication token.
     *
     * @return The authentication token
     */
    public String getAuthToken() {
        return authToken;
    }

    /**
     * Get the username of the logged-in user.
     *
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get the user ID of the logged-in user.
     *
     * @return The user ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Start a session with the given user, token, and refresh token.
     *
     * @param user The User object
     * @param token The authentication token
     * @param refreshToken The refresh token
     */
    public void startSession(User user, String token, String refreshToken) {
        this.currentUser = user;
        this.authToken = token;
        this.refreshToken = refreshToken;
        this.username = user != null ? user.getUsername() : null;
        this.userId = user != null ? user.getId() : null;
        this.loggedIn = true;
    }

    /**
     * End the current session.
     */
    public void endSession() {
        this.currentUser = null;
        this.authToken = null;
        this.refreshToken = null;
        this.username = null;
        this.userId = null;
        this.loggedIn = false;
    }

    /**
     * Get the current logged-in User object.
     *
     * @return The User object
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Get the refresh token.
     *
     * @return The refresh token
     */
    public String getRefreshToken() {
        return refreshToken;
    }
}
