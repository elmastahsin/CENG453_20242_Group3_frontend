package metu.ceng.ceng453_20242_group3_frontend.features.leaderboard.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import metu.ceng.ceng453_20242_group3_frontend.features.common.util.SessionManager;
import metu.ceng.ceng453_20242_group3_frontend.features.leaderboard.model.LeaderboardEntry;
import metu.ceng.ceng453_20242_group3_frontend.features.leaderboard.service.LeaderboardService;

/**
 * Controller for the leaderboard view.
 */
public class LeaderboardController implements Initializable {
    
    @FXML
    private StackPane leaderboardPane;
    
    @FXML
    private TabPane leaderboardTabs;
    
    @FXML
    private ProgressIndicator loadingIndicator;
    
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
    private boolean isDataLoading = false;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        leaderboardService = new LeaderboardService();
        
        // Initialize table column cell factories
        initializeTableColumns();
        
        // Apply fade transition class
        leaderboardPane.getStyleClass().add("fade-transition");
        
        // Set up button actions
        refreshButton.setOnAction(event -> loadLeaderboardData());
        backButton.setOnAction(event -> navigateToMainMenu());
        
        // Set keyboard event handler for ESC key
        leaderboardPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                navigateToMainMenu();
            }
        });
        
        // Make sure the pane can receive focus
        leaderboardPane.setFocusTraversable(true);
        
        // Set tab selection listener to highlight current user when tab changes
        leaderboardTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            int selectedIndex = leaderboardTabs.getSelectionModel().getSelectedIndex();
            switch (selectedIndex) {
                case 0:
                    highlightCurrentUserAndTopRanks(weeklyTable);
                    break;
                case 1:
                    highlightCurrentUserAndTopRanks(monthlyTable);
                    break;
                case 2:
                    highlightCurrentUserAndTopRanks(allTimeTable);
                    break;
            }
        });
        
        // Create fade-in animation for initial load
        Platform.runLater(() -> {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), leaderboardPane);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
            
            // Load initial data
            loadLeaderboardData();
            
            // Request focus to enable keyboard navigation
            leaderboardPane.requestFocus();
        });
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
            
        // Apply CSS classes for styling
        weeklyRankColumn.getStyleClass().add("rank-column");
        weeklyScoreColumn.getStyleClass().add("score-column");
        
        monthlyRankColumn.getStyleClass().add("rank-column");
        monthlyScoreColumn.getStyleClass().add("score-column");
        
        allTimeRankColumn.getStyleClass().add("rank-column");
        allTimeScoreColumn.getStyleClass().add("score-column");
        
        // Set custom cell factories for rank styling
        weeklyRankColumn.setCellFactory(column -> createRankCell());
        monthlyRankColumn.setCellFactory(column -> createRankCell());
        allTimeRankColumn.setCellFactory(column -> createRankCell());
    }
    
    /**
     * Creates a custom table cell for rank columns to apply special styling to top 3 ranks.
     */
    private javafx.scene.control.TableCell<LeaderboardEntry, Integer> createRankCell() {
        return new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Integer rank, boolean empty) {
                super.updateItem(rank, empty);
                
                if (empty || rank == null) {
                    setText(null);
                    getStyleClass().removeAll("rank-1", "rank-2", "rank-3");
                } else {
                    setText(rank.toString());
                    getStyleClass().removeAll("rank-1", "rank-2", "rank-3");
                    
                    // Apply special styling for top 3 ranks
                    if (rank == 1) {
                        getStyleClass().add("rank-1");
                    } else if (rank == 2) {
                        getStyleClass().add("rank-2");
                    } else if (rank == 3) {
                        getStyleClass().add("rank-3");
                    }
                }
            }
        };
    }
    
    /**
     * Highlights the current user and applies special styling to top ranks in the given table.
     */
    private void highlightCurrentUserAndTopRanks(TableView<LeaderboardEntry> tableView) {
        String currentUsername = SessionManager.getInstance().getCurrentUser() != null ? 
            SessionManager.getInstance().getCurrentUser().getUsername() : null;
            
        if (currentUsername == null) {
            return;
        }
        
        for (int i = 0; i < tableView.getItems().size(); i++) {
            LeaderboardEntry entry = tableView.getItems().get(i);
            
            // Check if this is the current user's row
            if (entry.getUsername().equals(currentUsername)) {
                tableView.getSelectionModel().select(i);
                tableView.scrollTo(i);
                break;
            }
        }
    }
    
    /**
     * Loads leaderboard data from the service.
     */
    private void loadLeaderboardData() {
        if (isDataLoading) {
            return; // Prevent multiple simultaneous loading operations
        }
        
        isDataLoading = true;
        
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(true);
        }
        
        // Create a task to load all leaderboard data
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() {
                // Load weekly leaderboard
                leaderboardService.getWeeklyLeaderboard(
                    weeklyData -> {
                        Platform.runLater(() -> {
                            weeklyTable.setItems(FXCollections.observableArrayList(weeklyData));
                            highlightCurrentUserAndTopRanks(weeklyTable);
                        });
                    },
                    error -> {
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load weekly leaderboard: " + error);
                        });
                    }
                );
                
                // Load monthly leaderboard
                leaderboardService.getMonthlyLeaderboard(
                    monthlyData -> {
                        Platform.runLater(() -> {
                            monthlyTable.setItems(FXCollections.observableArrayList(monthlyData));
                            highlightCurrentUserAndTopRanks(monthlyTable);
                        });
                    },
                    error -> {
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load monthly leaderboard: " + error);
                        });
                    }
                );
                
                // Load all-time leaderboard
                leaderboardService.getAllTimeLeaderboard(
                    allTimeData -> {
                        Platform.runLater(() -> {
                            allTimeTable.setItems(FXCollections.observableArrayList(allTimeData));
                            highlightCurrentUserAndTopRanks(allTimeTable);
                            
                            // Hide loading indicator when all data is loaded
                            if (loadingIndicator != null) {
                                loadingIndicator.setVisible(false);
                            }
                            isDataLoading = false;
                        });
                    },
                    error -> {
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load all-time leaderboard: " + error);
                            if (loadingIndicator != null) {
                                loadingIndicator.setVisible(false);
                            }
                            isDataLoading = false;
                        });
                    }
                );
                
                return null;
            }
        };
        
        // Start the loading task
        new Thread(loadTask).start();
    }
    
    /**
     * Navigates to the main menu.
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
            
            // Add the fade transition class
            root.getStyleClass().add("fade-transition");
            
            Stage stage = (Stage) leaderboardPane.getScene().getWindow();
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
                      "Could not navigate to main menu: " + e.getMessage());
        }
    }
    
    /**
     * Shows an alert dialog with the given type, title, and message.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header text
        alert.setContentText(message);
        
        // Apply CSS to dialog
        alert.getDialogPane().getStyleClass().add("modern-alert-pane");
        
        alert.showAndWait();
    }
} 