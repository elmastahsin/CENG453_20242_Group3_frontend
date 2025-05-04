package metu.ceng.ceng453_20242_group3_frontend.features.auth.controller;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import metu.ceng.ceng453_20242_group3_frontend.features.auth.service.AuthService;
import metu.ceng.ceng453_20242_group3_frontend.config.AppConfig;
import metu.ceng.ceng453_20242_group3_frontend.features.game.controller.MainMenuController;

/**
 * Controller for the registration view.
 */
public class RegisterController {
    
    @FXML
    private ImageView logoImageView;
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private Button registerButton;
    
    @FXML
    private Button backToLoginButton;
    
    @FXML
    private StackPane registerPane;
    
    private final AuthService authService;
    
    // Email validation regex pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    public RegisterController() {
        authService = new AuthService();
    }
    
    @FXML
    private void initialize() {
        // Set up event handlers
        registerButton.setOnAction(event -> register());
        backToLoginButton.setOnAction(event -> navigateToLogin());
    }
    
    /**
     * Attempts to register a new user with the provided information.
     */
    private void register() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Validate all fields
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Please fill in all fields.");
            return;
        }
        
        // Validate username length
        if (username.length() < 3 || username.length() > 20) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Username must be between 3 and 20 characters.");
            return;
        }
        
        // Validate email format
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Please enter a valid email address.");
            return;
        }
        
        // Validate password length
        if (password.length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Password must be at least 6 characters.");
            return;
        }
        
        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Passwords do not match.");
            return;
        }
        
        registerButton.setDisable(true);
        System.out.println("Attempting to register user: " + username);
        
        // Call the auth service to register
        authService.register(username, email, password, 
            () -> {
                // On successful registration
                Platform.runLater(() -> {
                    try {
                        showAlert(Alert.AlertType.INFORMATION, "Registration Successful", 
                                "You have successfully registered. You will be automatically logged in.");
                        
                        // Auto login after successful registration
                        autoLoginAfterRegistration(username, password);
                    } catch (Exception e) {
                        System.err.println("Error after successful registration: " + e.getMessage());
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                                "Could not navigate to main menu: " + e.getMessage());
                        navigateToLogin(); // Fallback to login page
                    } finally {
                        registerButton.setDisable(false);
                    }
                });
            },
            errorMessage -> {
                // On registration failure
                Platform.runLater(() -> {
                    try {
                        showAlert(Alert.AlertType.ERROR, "Registration Failed", errorMessage);
                    } catch (Exception e) {
                        System.err.println("Error showing alert: " + e.getMessage());
                    } finally {
                        registerButton.setDisable(false);
                    }
                });
            }
        );
    }
    
    /**
     * Automatically logs in the user after successful registration.
     * 
     * @param username The username
     * @param password The password
     */
    private void autoLoginAfterRegistration(String username, String password) {
        authService.login(username, password,
            user -> {
                // On successful login
                Platform.runLater(() -> {
                    try {
                        navigateToMainMenu();
                    } catch (IOException e) {
                        System.err.println("Error navigating to main menu: " + e.getMessage());
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                                "Could not navigate to main menu: " + e.getMessage());
                        navigateToLogin(); // Fallback to login page
                    }
                });
            },
            errorMessage -> {
                // On login failure
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Auto-Login Failed", 
                            "Registration was successful, but auto-login failed: " + errorMessage + 
                            "\nPlease try logging in manually.");
                    navigateToLogin();
                });
            }
        );
    }
    
    /**
     * Navigates to the main menu.
     */
    private void navigateToMainMenu() throws IOException {
        try {
            System.out.println("Attempting to navigate to main menu after registration...");
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
            URL cssUrl = getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/css/imports.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("CSS styling applied successfully");
            } else {
                System.out.println("WARNING: CSS styling not found");
            }
            
            // Get the current stage from any element in the current scene
            Stage stage = (Stage) registerPane.getScene().getWindow();
            
            // Add keyboard shortcuts for full screen mode
            scene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.F11) {
                    stage.setFullScreen(!stage.isFullScreen());
                } else if (e.getCode() == KeyCode.ENTER && e.isAltDown()) {
                    stage.setFullScreen(!stage.isFullScreen());
                }
            });
            
            // Set and show the new scene
            stage.setScene(scene);
            System.out.println("Main menu scene set successfully");
        } catch (Exception e) {
            System.err.println("Error navigating to main menu: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to navigate to main menu", e);
        }
    }
    
    /**
     * Navigates back to the login screen.
     */
    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/login-view.fxml"));
            Scene scene = new Scene(loader.load(), AppConfig.WINDOW_WIDTH, AppConfig.WINDOW_HEIGHT);
            
            // Apply CSS styling
            URL cssUrl = getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/css/imports.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            // Set up keyboard shortcuts for full screen mode
            setupFullScreenShortcuts(scene);
            
            // Set the scene on the stage
            Stage stage = (Stage) registerPane.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Could not navigate to login page: " + e.getMessage());
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
    
    /**
     * Sets up keyboard shortcuts for full screen mode on the given scene.
     * 
     * @param scene The scene to set up shortcuts for
     */
    private void setupFullScreenShortcuts(Scene scene) {
        Stage stage = (Stage) registerPane.getScene().getWindow();
        scene.setOnKeyPressed(ke -> {
            if (ke.getCode() == KeyCode.F11) {
                stage.setFullScreen(!stage.isFullScreen());
            } else if (ke.getCode() == KeyCode.ENTER && ke.isAltDown()) {
                stage.setFullScreen(!stage.isFullScreen());
            }
        });
    }
    
    private void showErrorAlert(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }
} 