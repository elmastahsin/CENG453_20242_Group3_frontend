package metu.ceng.ceng453_20242_group3_frontend.controller;

import java.io.IOException;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import metu.ceng.ceng453_20242_group3_frontend.service.AuthService;

/**
 * Controller for the forgot password view.
 */
public class ForgotPasswordController {
    
    @FXML
    private ImageView logoImageView;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private Button resetPasswordButton;
    
    @FXML
    private Button backToLoginButton;
    
    @FXML
    private VBox forgotPasswordPane;
    
    private final AuthService authService;
    
    // Email validation regex pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    public ForgotPasswordController() {
        authService = new AuthService();
    }
    
    @FXML
    private void initialize() {
        // Set up event handlers
        resetPasswordButton.setOnAction(event -> resetPassword());
        backToLoginButton.setOnAction(event -> navigateToLogin());
    }
    
    /**
     * Initiates the password reset process.
     */
    private void resetPassword() {
        String email = emailField.getText();
        
        // Validate email
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Reset Error", "Please enter your email address.");
            return;
        }
        
        // Validate email format
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showAlert(Alert.AlertType.ERROR, "Reset Error", "Please enter a valid email address.");
            return;
        }
        
        resetPasswordButton.setDisable(true);
        
        // Call the auth service to reset password
        authService.resetPassword(email,
            () -> {
                // On successful reset request
                Platform.runLater(() -> {
                    try {
                        // Instead of showing instructions and going back to login,
                        // direct the user to the reset password form
                        showResetPasswordForm();
                    } catch (Exception e) {
                        showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                                  "Could not navigate to reset password form: " + e.getMessage());
                        resetPasswordButton.setDisable(false);
                    }
                });
            },
            errorMessage -> {
                // On reset request failure
                Platform.runLater(() -> {
                    try {
                        showAlert(Alert.AlertType.ERROR, "Reset Request Failed", errorMessage);
                    } catch (Exception e) {
                        System.err.println("Error showing alert: " + e.getMessage());
                    } finally {
                        resetPasswordButton.setDisable(false);
                    }
                });
            }
        );
    }
    
    /**
     * Navigate to the reset password form.
     */
    private void showResetPasswordForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/reset-password-view.fxml"));
            Parent root = loader.load();
            
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Reset Email Sent");
            info.setHeaderText(null);
            info.setContentText("A reset link has been sent to your email. Please check your email and enter the reset token below.");
            info.showAndWait();
            
            Stage stage = (Stage) forgotPasswordPane.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                      "Could not navigate to reset password form: " + e.getMessage());
        }
    }
    
    /**
     * Navigates back to the login screen.
     */
    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/login-view.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) forgotPasswordPane.getScene().getWindow();
            stage.setScene(new Scene(root));
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