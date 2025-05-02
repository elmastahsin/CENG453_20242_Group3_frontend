package metu.ceng.ceng453_20242_group3_frontend;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import metu.ceng.ceng453_20242_group3_frontend.config.AppConfig;
import metu.ceng.ceng453_20242_group3_frontend.controller.ResetPasswordController;
import metu.ceng.ceng453_20242_group3_frontend.util.GameModeIconGenerator;
import metu.ceng.ceng453_20242_group3_frontend.util.IconGenerator;
import metu.ceng.ceng453_20242_group3_frontend.util.LogoGenerator;
import metu.ceng.ceng453_20242_group3_frontend.util.ProtocolHandler;

/**
 * Main application class for the UNO game.
 * This class is responsible for starting the application and loading the initial view.
 */
public class UnoApplication extends Application {
    
    // Singleton instance for accessing the application
    private static UnoApplication instance;
    private Stage primaryStage;
    
    @Override
    public void init() {
        // Generate the logo and icon on startup
        try {
            LogoGenerator.generateLogo();
            IconGenerator.generateIcon();
            
            // Generate game mode icons
            GameModeIconGenerator.generateIcons();
        } catch (Exception e) {
            System.err.println("Failed to generate logo or icons: " + e.getMessage());
        }
        
        // Register the protocol handler for handling password reset links
        try {
            ProtocolHandler.registerProtocol();
            
            // Show special instructions for macOS users
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("mac")) {
                System.out.println("======== IMPORTANT NOTE FOR MACOS USERS ========");
                System.out.println("To ensure password reset links work properly:");
                System.out.println("1. When you receive a password reset email");
                System.out.println("2. Copy the entire 'uno-reset://TOKEN' URL");
                System.out.println("3. Start this application manually");
                System.out.println("4. Go to your terminal and run:");
                System.out.println("   open \"uno-reset://TOKEN\"");
                System.out.println("   (replacing TOKEN with your actual token)");
                System.out.println("=================================================");
            }
        } catch (Exception e) {
            System.err.println("Failed to register protocol handler: " + e.getMessage());
        }
    }
    
    @Override
    public void start(Stage stage) throws IOException {
        this.primaryStage = stage;
        instance = this;
        
        // Set this system property to help with some macOS rendering issues
        System.setProperty("prism.order", "sw");
        
        // Set application icon
        try {
            URL iconUrl = getClass().getResource("/images/app_icon.png");
            if (iconUrl != null) {
                stage.getIcons().add(new Image(iconUrl.toExternalForm()));
            } else {
                System.err.println("Could not find application icon");
            }
        } catch (Exception e) {
            System.err.println("Error setting application icon: " + e.getMessage());
        }
        
        // Configure stage for resizable and full screen capability
        stage.setTitle(AppConfig.GAME_TITLE);
        stage.setResizable(true); // Make window resizable
        
        // Set up full screen shortcut (F11 or Alt+Enter)
        stage.setFullScreenExitKeyCombination(KeyCombination.valueOf("F11"));
        stage.setFullScreenExitHint("Press F11 to exit full screen mode");
        
        // Get parameters to check if app was launched with a reset token
        Parameters params = getParameters();
        List<String> rawParams = params.getRaw();
        
        if (!rawParams.isEmpty()) {
            try {
                // Check if it's a URI
                if (rawParams.get(0).startsWith("uno-reset://")) {
                    URI uri = new URI(rawParams.get(0));
                    String resetToken = ProtocolHandler.handleUri(uri);
                    if (resetToken != null) {
                        showResetPasswordView(resetToken);
                    } else {
                        showLoginView();
                    }
                } else if (rawParams.get(0).startsWith("reset-password:")) {
                    // Legacy format
                    String resetToken = rawParams.get(0).substring("reset-password:".length());
                    showResetPasswordView(resetToken);
                } else {
                    showLoginView();
                }
            } catch (URISyntaxException e) {
                System.err.println("Invalid URI: " + e.getMessage());
                showLoginView();
            }
        } else {
            // Show the default login view
            showLoginView();
        }
        
        // Add a graceful shutdown hook
        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        
        // Show the stage
        stage.show();
    }
    
    /**
     * Shows the login view.
     */
    private void showLoginView() throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), AppConfig.WINDOW_WIDTH, AppConfig.WINDOW_HEIGHT);
            
            // Apply CSS styling if available
            URL cssUrl = getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/css/imports.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            // Set up keyboard shortcuts for this scene
            scene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.F11) {
                    primaryStage.setFullScreen(!primaryStage.isFullScreen());
                } else if (e.getCode() == KeyCode.ENTER && e.isAltDown()) {
                    primaryStage.setFullScreen(!primaryStage.isFullScreen());
                }
            });
            
            primaryStage.setScene(scene);
        } catch (Exception e) {
            System.err.println("Error loading login view: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Shows the reset password view with the provided token.
     * 
     * @param token The reset token from the URL
     */
    private void showResetPasswordView(String token) throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/reset-password-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), AppConfig.WINDOW_WIDTH, AppConfig.WINDOW_HEIGHT);
            
            // Set the token in the controller
            ResetPasswordController controller = fxmlLoader.getController();
            controller.setResetToken(token);
            
            // Apply CSS styling if available
            URL cssUrl = getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/css/imports.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            // Set up keyboard shortcuts for this scene
            scene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.F11) {
                    primaryStage.setFullScreen(!primaryStage.isFullScreen());
                } else if (e.getCode() == KeyCode.ENTER && e.isAltDown()) {
                    primaryStage.setFullScreen(!primaryStage.isFullScreen());
                }
            });
            
            primaryStage.setScene(scene);
        } catch (Exception e) {
            System.err.println("Error loading reset password view: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Gets the singleton instance of the application.
     * 
     * @return The application instance
     */
    public static UnoApplication getInstance() {
        return instance;
    }
    
    /**
     * Gets the primary stage of the application.
     * 
     * @return The primary stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    public static void main(String[] args) {
        // Set these properties before launching the app
        System.setProperty("glass.disableGrab", "true");
        System.setProperty("javafx.macosx.embedded", "true");
        launch(args);
    }
} 