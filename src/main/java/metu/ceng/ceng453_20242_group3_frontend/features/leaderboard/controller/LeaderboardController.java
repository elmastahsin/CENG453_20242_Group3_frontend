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
        
        // Set up button actions
        refreshButton.setOnAction(event -> loadLeaderboardData());
        backButton.setOnAction(event -> navigateToMainMenu());
        
        // Apply consistent styling to tables
        applyTableStyling();
        
        // Set tab selection listener to highlight current user when tab changes
        leaderboardTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            int selectedIndex = leaderboardTabs.getSelectionModel().getSelectedIndex();
            switch (selectedIndex) {
                case 0:
                    highlightCurrentUser(weeklyTable);
                    break;
                case 1:
                    highlightCurrentUser(monthlyTable);
                    break;
                case 2:
                    highlightCurrentUser(allTimeTable);
                    break;
            }
        });
        
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
     * Applies consistent styling to all tables.
     */
    private void applyTableStyling() {
        // Common table styles
        String tableStyle = "-fx-background-color: white; -fx-border-color: #ccc;";
        weeklyTable.setStyle(tableStyle);
        monthlyTable.setStyle(tableStyle);
        allTimeTable.setStyle(tableStyle);
        
        // Add CSS class for column alignment
        weeklyRankColumn.getStyleClass().add("rank-column");
        weeklyScoreColumn.getStyleClass().add("score-column");
        
        monthlyRankColumn.getStyleClass().add("rank-column");
        monthlyScoreColumn.getStyleClass().add("score-column");
        
        allTimeRankColumn.getStyleClass().add("rank-column");
        allTimeScoreColumn.getStyleClass().add("score-column");
        
        // Set center alignment for rank columns
        weeklyRankColumn.setStyle("-fx-alignment: CENTER;");
        monthlyRankColumn.setStyle("-fx-alignment: CENTER;");
        allTimeRankColumn.setStyle("-fx-alignment: CENTER;");
        
        // Set right alignment for score columns
        weeklyScoreColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        monthlyScoreColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        allTimeScoreColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        
        // Set row factory for zebra striping
        weeklyTable.setRowFactory(tv -> createStyledRow());
        monthlyTable.setRowFactory(tv -> createStyledRow());
        allTimeTable.setRowFactory(tv -> createStyledRow());
    }
    
    /**
     * Creates a styled table row with consistent styling.
     * 
     * @return A styled table row
     */
    private javafx.scene.control.TableRow<LeaderboardEntry> createStyledRow() {
        javafx.scene.control.TableRow<LeaderboardEntry> row = new javafx.scene.control.TableRow<>();
        row.itemProperty().addListener((obs, oldItem, newItem) -> {
            // Simple border styling
            row.setStyle("-fx-border-color: #eee; -fx-border-width: 0 0 1 0;");
            
            // If this row contains the current user, highlight it
            if (newItem != null) {
                String currentUsername = SessionManager.getInstance().getCurrentUser() != null ? 
                    SessionManager.getInstance().getCurrentUser().getUsername() : null;
                
                if (currentUsername != null && currentUsername.equals(newItem.getUsername())) {
                    row.setStyle(row.getStyle() + "-fx-background-color: #e2f0ff;");
                }
            }
        });
        return row;
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
                            highlightCurrentUser(weeklyTable);
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
                            highlightCurrentUser(monthlyTable);
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
                            highlightCurrentUser(allTimeTable);
                            
                            // Hide loading indicator once all data is loaded
                            if (loadingIndicator != null) {
                                loadingIndicator.setVisible(false);
                            }
                            isDataLoading = false;
                        });
                    },
                    error -> {
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load all-time leaderboard: " + error);
                            
                            // Hide loading indicator on error
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
     * Highlights the current user in the leaderboard.
     * 
     * @param tableView The table to highlight the user in
     */
    private void highlightCurrentUser(TableView<LeaderboardEntry> tableView) {
        String currentUsername = SessionManager.getInstance().getCurrentUser() != null ? 
                SessionManager.getInstance().getCurrentUser().getUsername() : null;
        
        if (currentUsername != null) {
            for (LeaderboardEntry entry : tableView.getItems()) {
                if (entry.getUsername().equals(currentUsername)) {
                    tableView.getSelectionModel().select(entry);
                    tableView.scrollTo(entry);
                    break;
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
            
            Stage stage = (Stage) leaderboardPane.getScene().getWindow();
            
            // Add keyboard shortcuts for full screen mode
            scene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.F11) {
                    stage.setFullScreen(!stage.isFullScreen());
                } else if (e.getCode() == KeyCode.ENTER && e.isAltDown()) {
                    stage.setFullScreen(!stage.isFullScreen());
                }
            });
            
            // Set the new scene with a fade transition
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), leaderboardPane);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                stage.setScene(scene);
                
                // Start the fade-in transition after scene is set
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), root);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to return to main menu: " + e.getMessage());
        }
    }
    
    /**
     * Shows an alert dialog.
     * 
     * @param type The type of alert
     * @param title The title of the alert
     * @param message The message to display
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 