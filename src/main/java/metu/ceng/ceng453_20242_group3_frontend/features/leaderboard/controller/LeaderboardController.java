package metu.ceng.ceng453_20242_group3_frontend.features.leaderboard.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import metu.ceng.ceng453_20242_group3_frontend.features.leaderboard.model.LeaderboardEntry;
import metu.ceng.ceng453_20242_group3_frontend.features.leaderboard.service.LeaderboardService;
import metu.ceng.ceng453_20242_group3_frontend.features.common.util.SessionManager;

/**
 * Controller for the leaderboard view.
 */
public class LeaderboardController implements Initializable {
    
    @FXML
    private StackPane leaderboardPane;
    
    @FXML
    private TabPane leaderboardTabs;
    
    // Weekly leaderboard
    @FXML
    private TableView<LeaderboardEntry> weeklyTable;
    
    @FXML
    private TableColumn<LeaderboardEntry, Integer> weeklyRankColumn;
    
    @FXML
    private TableColumn<LeaderboardEntry, String> weeklyUsernameColumn;
    
    @FXML
    private TableColumn<LeaderboardEntry, Integer> weeklyScoreColumn;
    
    // Monthly leaderboard
    @FXML
    private TableView<LeaderboardEntry> monthlyTable;
    
    @FXML
    private TableColumn<LeaderboardEntry, Integer> monthlyRankColumn;
    
    @FXML
    private TableColumn<LeaderboardEntry, String> monthlyUsernameColumn;
    
    @FXML
    private TableColumn<LeaderboardEntry, Integer> monthlyScoreColumn;
    
    // All-time leaderboard
    @FXML
    private TableView<LeaderboardEntry> allTimeTable;
    
    @FXML
    private TableColumn<LeaderboardEntry, Integer> allTimeRankColumn;
    
    @FXML
    private TableColumn<LeaderboardEntry, String> allTimeUsernameColumn;
    
    @FXML
    private TableColumn<LeaderboardEntry, Integer> allTimeScoreColumn;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Button backButton;
    
    private LeaderboardService leaderboardService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        leaderboardService = new LeaderboardService();
        
        // Initialize table column cell factories
        initializeTableColumns();
        
        // Set up button actions
        refreshButton.setOnAction(event -> loadLeaderboardData());
        backButton.setOnAction(event -> navigateToMainMenu());
        
        // Load initial data
        loadLeaderboardData();
    }
    
    /**
     * Initializes the table columns for all leaderboard views.
     */
    private void initializeTableColumns() {
        // Weekly table
        weeklyRankColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getRank()).asObject());
        weeklyUsernameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getUsername()));
        weeklyScoreColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getScore()).asObject());
            
        // Monthly table
        monthlyRankColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getRank()).asObject());
        monthlyUsernameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getUsername()));
        monthlyScoreColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getScore()).asObject());
            
        // All-time table
        allTimeRankColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getRank()).asObject());
        allTimeUsernameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getUsername()));
        allTimeScoreColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getScore()).asObject());
    }
    
    /**
     * Loads leaderboard data from the service.
     */
    private void loadLeaderboardData() {
        // We'll implement these API calls later. For now, populate with sample data
        loadSampleData();
        
        // In the real implementation, we would call API endpoints:
        /*
        leaderboardService.getWeeklyLeaderboard(
            weeklyData -> {
                Platform.runLater(() -> {
                    weeklyTable.setItems(FXCollections.observableArrayList(weeklyData));
                });
            },
            error -> {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to load weekly leaderboard: " + error);
                });
            }
        );
        
        leaderboardService.getMonthlyLeaderboard(...);
        leaderboardService.getAllTimeLeaderboard(...);
        */
    }
    
    /**
     * Populates tables with sample data for demonstration purposes.
     */
    private void loadSampleData() {
        // Create sample data for weekly leaderboard
        List<LeaderboardEntry> weeklyData = new ArrayList<>();
        weeklyData.add(new LeaderboardEntry(1, "user1", 1250));
        weeklyData.add(new LeaderboardEntry(2, "user2", 980));
        weeklyData.add(new LeaderboardEntry(3, "user3", 870));
        weeklyData.add(new LeaderboardEntry(4, "user4", 750));
        weeklyData.add(new LeaderboardEntry(5, "user5", 650));
        
        // Sample data for monthly leaderboard
        List<LeaderboardEntry> monthlyData = new ArrayList<>();
        monthlyData.add(new LeaderboardEntry(1, "player1", 2500));
        monthlyData.add(new LeaderboardEntry(2, "player2", 2200));
        monthlyData.add(new LeaderboardEntry(3, "player3", 1900));
        monthlyData.add(new LeaderboardEntry(4, "player4", 1600));
        monthlyData.add(new LeaderboardEntry(5, "player5", 1400));
        
        // Sample data for all-time leaderboard
        List<LeaderboardEntry> allTimeData = new ArrayList<>();
        allTimeData.add(new LeaderboardEntry(1, "champion1", 15000));
        allTimeData.add(new LeaderboardEntry(2, "champion2", 12500));
        allTimeData.add(new LeaderboardEntry(3, "champion3", 10000));
        allTimeData.add(new LeaderboardEntry(4, "champion4", 8500));
        allTimeData.add(new LeaderboardEntry(5, "champion5", 7000));
        
        // Load data into tables
        weeklyTable.setItems(FXCollections.observableArrayList(weeklyData));
        monthlyTable.setItems(FXCollections.observableArrayList(monthlyData));
        allTimeTable.setItems(FXCollections.observableArrayList(allTimeData));
        
        // Find current user and highlight
        String currentUsername = SessionManager.getInstance().getCurrentUser() != null ? 
                SessionManager.getInstance().getCurrentUser().getUsername() : null;
        
        if (currentUsername != null) {
            for (TableView<LeaderboardEntry> table : new TableView[] {weeklyTable, monthlyTable, allTimeTable}) {
                for (LeaderboardEntry entry : table.getItems()) {
                    if (entry.getUsername().equals(currentUsername)) {
                        table.getSelectionModel().select(entry);
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Navigates back to the main menu.
     */
    private void navigateToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/main-menu-view.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            // Apply CSS styling
            URL cssUrl = getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/css/imports.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            // Add fade transition
            root.getStyleClass().add("fade-transition");
            
            Stage stage = (Stage) leaderboardPane.getScene().getWindow();
            
            // Add keyboard shortcuts for full screen mode
            scene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.F11) {
                    stage.setFullScreen(!stage.isFullScreen());
                } else if (e.getCode() == KeyCode.ENTER && e.isAltDown()) {
                    stage.setFullScreen(!stage.isFullScreen());
                }
            });
            
            stage.setScene(scene);
            
            // Start the fade-in transition after scene is set
            Platform.runLater(() -> {
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                      "Could not navigate to main menu: " + e.getMessage());
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