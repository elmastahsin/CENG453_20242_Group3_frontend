package metu.ceng.ceng453_20242_group3_frontend.controller;

import java.io.IOException;
import java.net.URL;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import metu.ceng.ceng453_20242_group3_frontend.service.AuthService;
import metu.ceng.ceng453_20242_group3_frontend.util.SessionManager;

/**
 * Controller for the main menu view.
 */
public class MainMenuController {
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Button playButton;
    
    @FXML
    private Button leaderboardButton;
    
    @FXML
    private Button settingsButton;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private StackPane mainMenuPane;
    
    // Debug elements
    @FXML
    private Label tokenDebugLabel;
    
    @FXML
    private Button toggleDebugButton;
    
    @FXML
    private VBox debugSection;
    
    private final AuthService authService;
    private boolean debugVisible = true;
    
    public MainMenuController() {
        authService = new AuthService();
    }
    
    @FXML
    private void initialize() {
        try {
            // Update welcome message with the username
            if (SessionManager.getInstance().isLoggedIn()) {
                // Debug output to verify user session
                System.out.println("===== Main Menu Loaded =====");
                System.out.println("User: " + SessionManager.getInstance().getCurrentUser().getUsername());
                System.out.println("Token: " + SessionManager.getInstance().getAuthToken());
                System.out.println("===========================");
                
                String username = SessionManager.getInstance().getCurrentUser().getUsername();
                welcomeLabel.setText("Welcome, " + username + "!");
                
                // Update debug label
                if (tokenDebugLabel != null) {
                    String token = SessionManager.getInstance().getAuthToken();
                    String refreshToken = SessionManager.getInstance().getRefreshToken();
                    
                    StringBuilder sb = new StringBuilder();
                    if (token != null) {
                        sb.append("Token: ").append(token.substring(0, 20)).append("...\n\n");
                    }
                    
                    if (refreshToken != null) {
                        sb.append("Refresh: ").append(refreshToken.substring(0, 20)).append("...");
                    }
                    
                    tokenDebugLabel.setText(sb.toString());
                }
            } else {
                System.out.println("Warning: Reached main menu without logging in!");
                welcomeLabel.setText("Not logged in!");
            }
            
            // Set up event handlers
            playButton.setOnAction(event -> startGame());
            leaderboardButton.setOnAction(event -> showLeaderboard());
            settingsButton.setOnAction(event -> showSettings());
            logoutButton.setOnAction(event -> logout());
            
            // Initialize the debug section visibility
            if (debugSection != null) {
                debugSection.setVisible(debugVisible);
            }
        } catch (Exception e) {
            System.err.println("Error initializing main menu: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Toggles the visibility of the debug section.
     */
    @FXML
    private void toggleDebugInfo() {
        if (debugSection != null) {
            debugVisible = !debugVisible;
            debugSection.setVisible(debugVisible);
            toggleDebugButton.setText(debugVisible ? "Hide Debug Info" : "Show Debug Info");
        }
    }
    
    /**
     * Starts a new game.
     */
    private void startGame() {
        try {
            // Load the game view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/game-view.fxml"));
            Parent root = loader.load();
            
            // Get the current stage
            Stage stage = (Stage) mainMenuPane.getScene().getWindow();
            
            // Set the new scene
            Scene gameScene = new Scene(root);
            
            // Apply CSS styling
            URL cssUrl = getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/styles.css");
            if (cssUrl != null) {
                gameScene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            // Add keyboard shortcuts for full screen in game scene
            gameScene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.F11) {
                    stage.setFullScreen(!stage.isFullScreen());
                } else if (e.getCode() == KeyCode.ENTER && e.isAltDown()) {
                    stage.setFullScreen(!stage.isFullScreen());
                }
            });
            
            stage.setScene(gameScene);
            
            System.out.println("Starting new game...");
        } catch (IOException e) {
            System.err.println("Error navigating to game view: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Game Error", 
                      "Could not start the game: " + e.getMessage());
        }
    }
    
    /**
     * Shows the leaderboard.
     */
    private void showLeaderboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/leaderboard-view.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            URL cssUrl = getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            // Add the fade transition class
            root.getStyleClass().add("fade-transition");
            
            Stage stage = (Stage) mainMenuPane.getScene().getWindow();
            stage.setScene(scene);
            
            // Animate the fade in
            Platform.runLater(() -> {
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                      "Could not open leaderboard: " + e.getMessage());
        }
    }
    
    /**
     * Shows the settings screen.
     */
    private void showSettings() {
        // This would navigate to the settings screen
        showAlert(Alert.AlertType.INFORMATION, "Settings", "Opening settings...");
        // Implementation for showing settings will be added later
    }
    
    /**
     * Logs out the current user and returns to the login screen.
     */
    private void logout() {
        authService.logout();
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/login-view.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            // Apply CSS styling
            URL cssUrl = getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            // Add keyboard shortcuts for full screen in login scene
            Stage stage = (Stage) mainMenuPane.getScene().getWindow();
            scene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.F11) {
                    stage.setFullScreen(!stage.isFullScreen());
                } else if (e.getCode() == KeyCode.ENTER && e.isAltDown()) {
                    stage.setFullScreen(!stage.isFullScreen());
                }
            });
            
            stage.setScene(scene);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                      "Could not navigate to login page: " + e.getMessage());
        }
    }
    
    /**
     * Shows an alert dialog.
     * 
     * @param type The alert type
     * @param title The alert title
     * @param message The alert message
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 