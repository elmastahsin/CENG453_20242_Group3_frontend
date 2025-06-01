package metu.ceng.ceng453_20242_group3_frontend.features.game.controller;

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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import metu.ceng.ceng453_20242_group3_frontend.config.AppConfig;

/**
 * Controller for the game mode selection view.
 * This controller handles the selection between single player and multiplayer modes.
 */
public class GameModeController {
    
    @FXML
    private StackPane gameModePane;
    
    @FXML
    private VBox singleplayerBox;
    
    @FXML
    private VBox multiplayerBox;
    
    @FXML
    private Button singleplayerButton;
    
    @FXML
    private Button multiplayerButton;
    
    @FXML
    private Button backButton;
    
    @FXML
    private void initialize() {
        try {
            // Set up event handlers
            singleplayerButton.setOnAction(event -> startSingleplayer());
            multiplayerButton.setOnAction(event -> startMultiplayer());
            backButton.setOnAction(event -> backToMainMenu());
            
            // Add click handler for boxes too
            singleplayerBox.setOnMouseClicked(event -> startSingleplayer());
            multiplayerBox.setOnMouseClicked(event -> startMultiplayer());
            
        } catch (Exception e) {
            System.err.println("Error initializing game mode controller: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Starts a singleplayer game by navigating to the player count selection.
     */
    private void startSingleplayer() {
        try {
            // Navigate to the singleplayer game selection (keeping the current flow)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/singleplayer-mode-view.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            URL cssUrl = getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/css/imports.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            // Add keyboard shortcuts for full screen
            Stage stage = (Stage) gameModePane.getScene().getWindow();
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            
            scene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.F11) {
                    stage.setFullScreen(!stage.isFullScreen());
                } else if (e.getCode() == KeyCode.ENTER && e.isAltDown()) {
                    stage.setFullScreen(!stage.isFullScreen());
                }
            });
            
            stage.setScene(scene);
        } catch (IOException e) {
            // If singleplayer-mode-view.fxml doesn't exist, create a temporary solution
            // by redirecting to a default 3-player game
            System.out.println("Singleplayer mode view not found, starting default 3-player game...");
            startGameWithPlayerCount(3);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Game Error", 
                    "Could not start singleplayer mode: " + e.getMessage());
            System.err.println("Error starting singleplayer: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Starts multiplayer mode by navigating to the lobby.
     */
    private void startMultiplayer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/lobby-view.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            URL cssUrl = getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/css/imports.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            // Add keyboard shortcuts for full screen
            Stage stage = (Stage) gameModePane.getScene().getWindow();
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            
            scene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.F11) {
                    stage.setFullScreen(!stage.isFullScreen());
                } else if (e.getCode() == KeyCode.ENTER && e.isAltDown()) {
                    stage.setFullScreen(!stage.isFullScreen());
                }
            });
            
            stage.setScene(scene);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Multiplayer Error", 
                    "Could not start multiplayer mode: " + e.getMessage());
            System.err.println("Error starting multiplayer: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Fallback method to start a game with a specific player count (for singleplayer).
     * 
     * @param playerCount The total number of players (including AI)
     */
    private void startGameWithPlayerCount(int playerCount) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/game-view.fxml"));
            Parent root = loader.load();
            
            GameController controller = loader.getController();
            
            // Initialize the game with the selected player count (playerCount - 1 AI players)
            controller.initializeGame("Normal", playerCount - 1, 7);
            
            Scene scene = new Scene(root);
            URL cssUrl = getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/css/imports.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            // Add keyboard shortcuts for full screen
            Stage stage = (Stage) gameModePane.getScene().getWindow();
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            
            scene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.F11) {
                    stage.setFullScreen(!stage.isFullScreen());
                } else if (e.getCode() == KeyCode.ENTER && e.isAltDown()) {
                    stage.setFullScreen(!stage.isFullScreen());
                }
            });
            
            stage.setScene(scene);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Game Error", 
                    "Could not start the game: " + e.getMessage());
            System.err.println("Error starting game: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Navigates back to the main menu.
     */
    private void backToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/main-menu-view.fxml"));
            Scene scene = new Scene(loader.load(), AppConfig.WINDOW_WIDTH, AppConfig.WINDOW_HEIGHT);
            
            // Apply CSS styling
            URL cssUrl = getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/css/imports.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            // Set up scene and keyboard shortcuts
            Stage stage = (Stage) gameModePane.getScene().getWindow();
            scene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.F11) {
                    stage.setFullScreen(!stage.isFullScreen());
                } else if (e.getCode() == KeyCode.ENTER && e.isAltDown()) {
                    stage.setFullScreen(!stage.isFullScreen());
                }
            });
            
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                      "Failed to navigate to the main menu. Please try again.");
        }
    }
    
    /**
     * Shows a standard alert dialog.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        if (gameModePane != null && gameModePane.getScene() != null) {
            alert.initOwner(gameModePane.getScene().getWindow());
        }
        
        alert.showAndWait();
    }
} 