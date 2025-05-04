package metu.ceng.ceng453_20242_group3_frontend.features.auth.service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import metu.ceng.ceng453_20242_group3_frontend.config.AppConfig;
import metu.ceng.ceng453_20242_group3_frontend.features.auth.model.User;
import metu.ceng.ceng453_20242_group3_frontend.features.common.util.SessionManager;

/**
 * Service class for handling authentication operations.
 */
public class AuthService {
    
    /**
     * Attempts to log in a user with the provided credentials.
     * 
     * @param username The username
     * @param password The password
     * @param onSuccess Callback for successful login
     * @param onError Callback for login failure
     */
    public void login(String username, String password, Consumer<User> onSuccess, Consumer<String> onError) {
        CompletableFuture.runAsync(() -> {
            try {
                String jsonBody = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
                URL url = new URL(AppConfig.LOGIN_ENDPOINT);
                
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                
                int responseCode = connection.getResponseCode();
                
                if (responseCode == 200) {
                    // Parse the response and create a User object
                    String responseBody = readResponse(connection);
                    
                    // Parse the JSON response
                    JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                    
                    // Extract tokens
                    String accessToken = jsonResponse.get("accessToken").getAsString();
                    String refreshToken = jsonResponse.get("refreshToken").getAsString();
                    
                    // Create user object
                    User user = new User();
                    user.setUsername(username);
                    
                    // Try to get user ID if available
                    if (jsonResponse.has("userId")) {
                        user.setId(jsonResponse.get("userId").getAsLong());
                    }
                    
                    // Try to get email if available
                    if (jsonResponse.has("email")) {
                        user.setEmail(jsonResponse.get("email").getAsString());
                    }
                    
                    // Start a new session with tokens
                    SessionManager.getInstance().startSession(user, accessToken, refreshToken);
                    
                    // Debug output to verify tokens
                    System.out.println("===== Login successful =====");
                    System.out.println("Username: " + username);
                    System.out.println("Access Token: " + accessToken);
                    System.out.println("Refresh Token: " + refreshToken);
                    System.out.println("===========================");
                    
                    // Call the success callback
                    onSuccess.accept(user);
                } else {
                    // Call the error callback
                    String errorResponse = readErrorResponse(connection);
                    onError.accept("Login failed: " + errorResponse);
                }
                
                connection.disconnect();
            } catch (IOException e) {
                onError.accept("Network error: " + e.getMessage());
            } catch (Exception e) {
                onError.accept("Error processing login response: " + e.getMessage());
            }
        });
    }
    
    /**
     * Registers a new user.
     * 
     * @param username The username
     * @param email The email
     * @param password The password
     * @param onSuccess Callback for successful registration
     * @param onError Callback for registration failure
     */
    public void register(String username, String email, String password, 
                         Runnable onSuccess, Consumer<String> onError) {
        try {
            System.out.println("Sending registration request for username: " + username);
            
            String jsonBody = String.format("{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}", 
                                           username, email, password);
            URL url = new URL(AppConfig.REGISTER_ENDPOINT);
            
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = connection.getResponseCode();
            System.out.println("Registration response code: " + responseCode);
            
            // Accept both 200 and 201 as successful registration
            if (responseCode == 200 || responseCode == 201) {
                System.out.println("Registration successful with status code: " + responseCode);
                
                try {
                    // Try to read the response body to get tokens
                    String responseBody = readResponse(connection);
                    System.out.println("Registration response: " + responseBody);
                    
                    // Try to extract tokens from the response
                    try {
                        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                        if (jsonResponse.has("body") && jsonResponse.getAsJsonObject("body").has("data")) {
                            JsonObject data = jsonResponse.getAsJsonObject("body").getAsJsonObject("data");
                            
                            if (data.has("accessToken") && data.has("refreshToken")) {
                                // Create user object and start session
                                String accessToken = data.get("accessToken").getAsString();
                                String refreshToken = data.get("refreshToken").getAsString();
                                
                                User user = new User();
                                user.setUsername(username);
                                user.setEmail(email);
                                
                                // Start session directly without a separate login call
                                SessionManager.getInstance().startSession(user, accessToken, refreshToken);
                                
                                System.out.println("===== Auto-login successful after registration =====");
                                System.out.println("Username: " + username);
                                System.out.println("Access Token: " + accessToken);
                                System.out.println("Refresh Token: " + refreshToken);
                                System.out.println("================================================");
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Could not parse tokens from registration response: " + e.getMessage());
                        // Continue anyway, we'll just use the regular login method in RegisterController
                    }
                } catch (Exception e) {
                    System.err.println("Error reading registration response: " + e.getMessage());
                    // Continue anyway, since registration was successful
                }
                
                // Notify caller of success
                onSuccess.run();
            } else if (responseCode == 409) {
                // Handle conflict (e.g., username already exists)
                onError.accept("Username or email already exists. Please choose a different one.");
            } else {
                // Try to read error response safely
                try {
                    String errorResponse = readErrorResponse(connection);
                    System.err.println("Registration error response: " + errorResponse);
                    onError.accept("Registration failed: " + errorResponse);
                } catch (Exception e) {
                    System.err.println("Error reading registration error response: " + e.getMessage());
                    onError.accept("Registration failed with status code: " + responseCode);
                }
            }
            
            connection.disconnect();
        } catch (IOException e) {
            System.err.println("Network error in registration: " + e.getMessage());
            e.printStackTrace();
            onError.accept("Network error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error in registration: " + e.getMessage());
            e.printStackTrace();
            onError.accept("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Logs out the current user.
     */
    public void logout() {
        // End the current session
        SessionManager.getInstance().endSession();
    }
    
    /**
     * Initiates a password reset process.
     * 
     * The backend should send an email with a link in the format:
     * uno-reset://TOKEN_HERE
     * 
     * This link, when clicked, will open the application with the token as a parameter,
     * allowing the user to reset their password.
     * 
     * @param email The user's email
     * @param onSuccess Callback for successful password reset request
     * @param onError Callback for password reset request failure
     */
    public void resetPassword(String email, Runnable onSuccess, Consumer<String> onError) {
        try {
            System.out.println("Sending password reset request for email: " + email);
            
            // Prepare the request with email as query parameter
            String encodedEmail = java.net.URLEncoder.encode(email, StandardCharsets.UTF_8.name());
            URL url = new URL(AppConfig.FORGOT_PASSWORD_ENDPOINT + "?email=" + encodedEmail);
            System.out.println("Request URL: " + url);
            
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            // No need to set DoOutput to true since we're not sending a request body
            
            int responseCode = connection.getResponseCode();
            System.out.println("Password reset response code: " + responseCode);
            
            // Check the response
            if (responseCode == 200) {
                System.out.println("Password reset request successful");
                onSuccess.run();
            } else if (responseCode == 401) {
                // Handle unauthorized error
                onError.accept("Unauthorized request. Please log in again and try.");
            } else if (responseCode == 404) {
                // Handle not found error, but maintain user privacy
                onError.accept("If an account exists with this email, a password reset link has been sent.");
            } else {
                // Handle other errors
                String errorResponse = readErrorResponse(connection);
                System.err.println("Error response: " + errorResponse);
                onError.accept("Password reset request failed: " + errorResponse);
            }
            
            connection.disconnect();
        } catch (IOException e) {
            System.err.println("Network error in password reset: " + e.getMessage());
            e.printStackTrace();
            onError.accept("Network error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error in password reset: " + e.getMessage());
            e.printStackTrace();
            onError.accept("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Completes the password reset process by sending the new password and token.
     * 
     * @param token The password reset token from the email link
     * @param newPassword The new password for the user
     * @param onSuccess Callback for successful password reset
     * @param onError Callback for password reset failure
     */
    public void completePasswordReset(String token, String newPassword, Runnable onSuccess, Consumer<String> onError) {
        try {
            System.out.println("Sending password reset completion request with token");
            
            // Prepare the request with parameters in the query string
            String encodedToken = java.net.URLEncoder.encode(token, StandardCharsets.UTF_8.name());
            String encodedPassword = java.net.URLEncoder.encode(newPassword, StandardCharsets.UTF_8.name());
            URL url = new URL(AppConfig.SET_NEW_PASSWORD_ENDPOINT + "?token=" + encodedToken + "&newPassword=" + encodedPassword + "&confirmPassword=" + encodedPassword);
            System.out.println("Request URL: " + url);
            
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            // No need to set DoOutput to true since we're not sending a request body
            
            int responseCode = connection.getResponseCode();
            System.out.println("Complete password reset response code: " + responseCode);
            
            if (responseCode == 200) {
                System.out.println("Password reset successful");
                onSuccess.run();
            } else if (responseCode == 400) {
                // Bad request
                String errorResponse = readErrorResponse(connection);
                onError.accept("Invalid token or password: " + errorResponse);
            } else if (responseCode == 404) {
                // Token not found
                onError.accept("Reset token not found or expired. Please request a new password reset.");
            } else {
                String errorResponse = readErrorResponse(connection);
                System.err.println("Error response: " + errorResponse);
                onError.accept("Password reset failed: " + errorResponse);
            }
            
            connection.disconnect();
        } catch (IOException e) {
            System.err.println("Network error in password reset completion: " + e.getMessage());
            e.printStackTrace();
            onError.accept("Network error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error in password reset completion: " + e.getMessage());
            e.printStackTrace();
            onError.accept("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Reads the response body from a successful HTTP connection.
     * 
     * @param connection The HTTP connection
     * @return The response body as a string
     * @throws IOException If an I/O error occurs
     */
    private String readResponse(HttpURLConnection connection) throws IOException {
        try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name())) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }
    
    /**
     * Reads the error response body from a failed HTTP connection.
     * 
     * @param connection The HTTP connection
     * @return The error response body as a string
     * @throws IOException If an I/O error occurs
     */
    private String readErrorResponse(HttpURLConnection connection) throws IOException {
        // Check if error stream is null
        if (connection.getErrorStream() == null) {
            // Try to get response code and message
            int responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();
            return String.format("{\"status\":{\"code\":\"%d\",\"description\":\"%s\"}}", 
                                responseCode, responseMessage);
        }
        
        try (Scanner scanner = new Scanner(connection.getErrorStream(), StandardCharsets.UTF_8.name())) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }
} 