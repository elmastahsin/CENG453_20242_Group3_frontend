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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import metu.ceng.ceng453_20242_group3_frontend.service.AuthService;

/**
 * Controller for the login view.
 */
public class LoginController {
    
    @FXML
    private ImageView logoImageView;
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Button registerButton;
    
    @FXML
    private Button forgotPasswordButton;
    
    @FXML
    private StackPane loginPane;
    
    private final AuthService authService;
    
    public LoginController() {
        authService = new AuthService();
    }
    
    @FXML
    private void initialize() {
        // Set up event handlers
        loginButton.setOnAction(event -> login());
        registerButton.setOnAction(event -> navigateToRegister());
        forgotPasswordButton.setOnAction(event -> navigateToForgotPassword());
        
        // Add right-click context menu to forgot password button to enter reset token directly
        forgotPasswordButton.setOnContextMenuRequested(event -> showEnterResetTokenDialog());
    }
    
    /**
     * Shows a dialog to enter a reset token directly.
     */
    private void showEnterResetTokenDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enter Reset Token");
        dialog.setHeaderText("Direct Password Reset");
        dialog.setContentText("Enter the reset token from your email:");
        
        dialog.showAndWait().ifPresent(token -> {
            if (!token.isEmpty()) {
                try {
                    navigateToResetPassword(token);
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                          "Could not navigate to reset password page: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Navigates to the reset password screen with the provided token.
     */
    private void navigateToResetPassword(String token) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/reset-password-view.fxml"));
            Parent root = loader.load();
            
            // Set the token in the controller
            ResetPasswordController controller = loader.getController();
            controller.setResetToken(token);
            
            Scene scene = new Scene(root);
            // Apply CSS styling
            URL cssUrl = getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            Stage stage = (Stage) loginPane.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            System.err.println("Error navigating to reset password view: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Attempts to log in with the provided credentials.
     */
    private void login() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Please enter both username and password.");
            return;
        }
        
        loginButton.setDisable(true);
        
        authService.login(username, password, 
            user -> {
                // On successful login
                try {
                    // Use Platform.runLater to ensure UI updates happen on the JavaFX Application Thread
                    Platform.runLater(() -> {
                        try {
                            navigateToMainMenu();
                        } catch (IOException e) {
                            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                                "Could not navigate to main menu: " + e.getMessage());
                        }
                        loginButton.setDisable(false);
                    });
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                          "Could not navigate to main menu: " + e.getMessage());
                    loginButton.setDisable(false);
                }
            },
            errorMessage -> {
                // On login failure
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Login Failed", errorMessage);
                    loginButton.setDisable(false);
                });
            }
        );
    }
    
    /**
     * Navigates to the registration screen.
     */
    private void navigateToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/register-view.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            // Apply CSS styling
            URL cssUrl = getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            // Add the fade transition class
            root.getStyleClass().add("fade-transition");
            
            Stage stage = (Stage) loginPane.getScene().getWindow();
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
                      "Could not navigate to registration page: " + e.getMessage());
        }
    }
    
    /**
     * Navigates to the forgot password screen.
     */
    private void navigateToForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/forgot-password-view.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            // Apply CSS styling
            URL cssUrl = getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            // Add the fade transition class
            root.getStyleClass().add("fade-transition");
            
            Stage stage = (Stage) loginPane.getScene().getWindow();
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
                      "Could not navigate to forgot password page: " + e.getMessage());
        }
    }
    
    /**
     * Navigates to the main menu.
     */
    private void navigateToMainMenu() throws IOException {
        try {
            System.out.println("Attempting to navigate to main menu...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/main-menu-view.fxml"));
            
            // Load the FXML first
            Parent root = loader.load();
            System.out.println("Main menu FXML loaded successfully");
            
            // Get the controller (optional, for passing data)
            MainMenuController controller = loader.getController();
            System.out.println("Main menu controller obtained");
            
            // Create the new scene
            Scene scene = new Scene(root);
            
            // Apply CSS styling
            URL cssUrl = getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("CSS styling applied successfully");
            } else {
                System.out.println("WARNING: CSS styling not found");
            }
            
            // Add the fade transition class
            root.getStyleClass().add("fade-transition");
            
            // Get the current stage from any element in the current scene
            Stage stage = (Stage) loginPane.getScene().getWindow();
            
            // Set the new scene
            stage.setScene(scene);
            System.out.println("Main menu scene set successfully");
            
            // Animate the fade in
            Platform.runLater(() -> {
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            
        } catch (Exception e) {
            System.err.println("Error navigating to main menu: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to navigate to main menu", e);
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