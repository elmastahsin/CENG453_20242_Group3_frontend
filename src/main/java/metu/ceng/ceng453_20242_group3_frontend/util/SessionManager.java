package metu.ceng.ceng453_20242_group3_frontend.util;

import metu.ceng.ceng453_20242_group3_frontend.model.User;

/**
 * Singleton class for managing user session data throughout the application.
 */
public class SessionManager {
    
    private static SessionManager instance;
    private User currentUser;
    private String authToken;
    private String refreshToken;
    
    // Private constructor for singleton pattern
    private SessionManager() {
    }
    
    // Get singleton instance
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Start a new user session after successful login
     * @param user The logged-in user
     * @param token Authentication token from the server
     * @param refreshToken Refresh token from the server
     */
    public void startSession(User user, String token, String refreshToken) {
        try {
            this.currentUser = user;
            this.authToken = token;
            this.refreshToken = refreshToken;
            
            if (user != null) {
                user.setToken(token);
                user.setRefreshToken(refreshToken);
            }
            
            System.out.println("Session started successfully");
        } catch (Exception e) {
            System.err.println("Error starting session: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * End the current user session (logout)
     */
    public void endSession() {
        this.currentUser = null;
        this.authToken = null;
        this.refreshToken = null;
    }
    
    /**
     * Check if a user is currently logged in
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return this.currentUser != null && this.authToken != null;
    }
    
    // Getters
    public User getCurrentUser() {
        return currentUser;
    }
    
    public String getAuthToken() {
        return authToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
} 