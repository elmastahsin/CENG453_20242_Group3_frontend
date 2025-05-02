package metu.ceng.ceng453_20242_group3_frontend.controller;

import java.io.IOException;
import java.net.URL;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import metu.ceng.ceng453_20242_group3_frontend.service.AuthService;

/**
 * Controller for the reset password view that appears after clicking the reset link.
 */
public class ResetPasswordController {
    
    @FXML
    private ImageView logoImageView;
    
    @FXML
    private TextField tokenField;
    
    @FXML
    private PasswordField newPasswordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private Button submitButton;
    
    @FXML
    private Button backToLoginButton;
    
    @FXML
    private StackPane resetPasswordPane;
    
    @FXML
    private ProgressIndicator progressIndicator;
    
    private final AuthService authService;
    
    // The reset token from the URL
    private String resetToken;
    
    public ResetPasswordController() {
        authService = new AuthService();
    }
    
    /**
     * Sets the reset token extracted from the URL.
     * This should be called before showing this view.
     * 
     * @param token The password reset token
     */
    public void setResetToken(String token) {
        this.resetToken = token;
        
        // If the token field is available, update it
        if (tokenField != null) {
            tokenField.setText(token);
        }
    }
    
    @FXML
    private void initialize() {
        // Set up event handlers
        submitButton.setOnAction(event -> resetPassword());
        backToLoginButton.setOnAction(event -> navigateToLogin());
        
        // If we already have a reset token (from URL or direct launch), 
        // update the token field
        if (resetToken != null && tokenField != null) {
            tokenField.setText(resetToken);
        }
        
        // Apply fade-in transition on load
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), resetPasswordPane);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
    
    /**
     * Resets the user's password with the new one provided.
     */
    private void resetPassword() {
        // Get the token from the field if not already set
        if (resetToken == null || resetToken.isEmpty()) {
            resetToken = tokenField.getText();
        }
        
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Validate token
        if (resetToken == null || resetToken.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Reset Error", "Please enter your reset token.");
            return;
        }
        
        // Validate fields
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Reset Error", "Please fill in all fields.");
            return;
        }
        
        // Validate password length
        if (newPassword.length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Reset Error", "Password must be at least 6 characters.");
            return;
        }
        
        // Check if passwords match
        if (!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Reset Error", "Passwords do not match.");
            return;
        }
        
        // Show progress indicator and disable button
        progressIndicator.setVisible(true);
        submitButton.setDisable(true);
        
        // Call the auth service to complete password reset
        authService.completePasswordReset(resetToken, newPassword,
            () -> {
                // On successful password reset
                progressIndicator.setVisible(false);
                
                showAlert(Alert.AlertType.INFORMATION, "Password Reset Successful", 
                          "Your password has been successfully reset. Please log in with your new password.");
                
                try {
                    navigateToLogin();
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                              "Could not navigate to login page: " + e.getMessage());
                }
                
                submitButton.setDisable(false);
            },
            errorMessage -> {
                // On password reset failure
                progressIndicator.setVisible(false);
                showAlert(Alert.AlertType.ERROR, "Password Reset Failed", errorMessage);
                submitButton.setDisable(false);
            }
        );
    }
    
    /**
     * Navigates back to the login screen.
     */
    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/login-view.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            // Apply CSS styling
            URL cssUrl = getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            Stage stage = (Stage) resetPasswordPane.getScene().getWindow();
            
            // Create fade-out transition
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), resetPasswordPane);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                // Add keyboard shortcuts for full screen mode
                scene.setOnKeyPressed(ke -> {
                    if (ke.getCode() == KeyCode.F11) {
                        stage.setFullScreen(!stage.isFullScreen());
                    } else if (ke.getCode() == KeyCode.ENTER && ke.isAltDown()) {
                        stage.setFullScreen(!stage.isFullScreen());
                    }
                });
                
                stage.setScene(scene);
            });
            fadeOut.play();
            
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