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
import metu.ceng.ceng453_20242_group3_frontend.features.common.util.SessionManager;

/**
 * Controller for the game mode selection view.
 * This controller handles the selection between single player and multiplayer modes.
 */
public class GameModeController {
    
    @FXML
    private StackPane gameModePane;
    
    @FXML
    private VBox twoPlayerBox;
    
    @FXML
    private VBox threePlayerBox;
    
    @FXML
    private VBox fourPlayerBox;
    
    @FXML
    private Button twoPlayerButton;
    
    @FXML
    private Button threePlayerButton;
    
    @FXML
    private Button fourPlayerButton;
    
    @FXML
    private Button backButton;
    
    @FXML
    private void initialize() {
        try {
            // Set up event handlers
            twoPlayerButton.setOnAction(event -> startGame(2));
            threePlayerButton.setOnAction(event -> startGame(3));
            fourPlayerButton.setOnAction(event -> startGame(4));
            backButton.setOnAction(event -> backToMainMenu());
            
            // Add click handler for boxes too
            twoPlayerBox.setOnMouseClicked(event -> startGame(2));
            threePlayerBox.setOnMouseClicked(event -> startGame(3));
            fourPlayerBox.setOnMouseClicked(event -> startGame(4));
            
        } catch (Exception e) {
            System.err.println("Error initializing game mode controller: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Starts the game with the specified number of players.
     * 
     * @param playerCount The number of players
     */
    private void startGame(int playerCount) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/game-view.fxml"));
            Parent root = loader.load();
            
            GameController controller = loader.getController();
            
            // Initialize the game with the selected player count
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