package metu.ceng.ceng453_20242_group3_frontend.features.game.controller;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import metu.ceng.ceng453_20242_group3_frontend.features.common.util.ApiClient;
import metu.ceng.ceng453_20242_group3_frontend.features.common.util.SessionManager;
import metu.ceng.ceng453_20242_group3_frontend.features.game.model.GameLobby;

/**
 * Controller for the multiplayer lobby view.
 * Handles listing available games, creating new games, and joining games.
 */
public class LobbyController {
    
    @FXML
    private StackPane lobbyPane;
    
    @FXML
    private ScrollPane gamesScrollPane;
    
    @FXML
    private VBox gamesContainer;
    
    @FXML
    private Button createGameButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Button backButton;
    
    @FXML
    private Label playerNameLabel;
    
    @FXML
    private Label gamesCountLabel;
    
    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;
    private List<GameLobby> availableGames;
    
    // Store joined game information for future use
    private Integer joinedGameId;
    private String joinedGameType;
    
    public LobbyController() {
        apiClient = new ApiClient();
        objectMapper = new ObjectMapper();
        availableGames = new ArrayList<>();
    }
    
    @FXML
    private void initialize() {
        try {
            // Set player name from session
            if (SessionManager.getInstance().isLoggedIn()) {
                String username = SessionManager.getInstance().getCurrentUser().getUsername();
                playerNameLabel.setText("Welcome, " + username + "!");
            }
            
            // Set up event handlers
            createGameButton.setOnAction(event -> showCreateGameModal());
            refreshButton.setOnAction(event -> loadAvailableGames());
            backButton.setOnAction(event -> backToGameMode());
            
            // Initial load of games
            loadAvailableGames();
            
        } catch (Exception e) {
            System.err.println("Error initializing lobby controller: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Loads available games from the server.
     */
    private void loadAvailableGames() {
        // Show loading state
        gamesContainer.getChildren().clear();
        Label loadingLabel = new Label("Loading games...");
        loadingLabel.getStyleClass().add("loading-label");
        gamesContainer.getChildren().add(loadingLabel);
        
        // Make API request
        apiClient.get("/game/lobby", 
            response -> {
                Platform.runLater(() -> {
                    try {
                        parseAndDisplayGames(response);
                    } catch (Exception e) {
                        System.err.println("Error parsing games response: " + e.getMessage());
                        showErrorMessage("Failed to load games: " + e.getMessage());
                    }
                });
            },
            error -> {
                Platform.runLater(() -> {
                    System.err.println("Error loading games: " + error);
                    showErrorMessage("Failed to connect to server: " + error);
                });
            }
        );
    }
    
    /**
     * Parses the API response and displays the games.
     * 
     * @param response The JSON response from the server
     */
    private void parseAndDisplayGames(String response) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        availableGames.clear();
        
        if (root.has("games") && root.get("games").isArray()) {
            JsonNode gamesArray = root.get("games");
            
            for (JsonNode gameNode : gamesArray) {
                GameLobby gameLobby = new GameLobby();
                gameLobby.setGameId(gameNode.get("game_id").asInt());
                gameLobby.setPlayerCount(gameNode.get("player_count").asInt());
                gameLobby.setGametype(gameNode.get("gametype").asText());
                
                // Parse players array
                List<String> players = new ArrayList<>();
                if (gameNode.has("players") && gameNode.get("players").isArray()) {
                    for (JsonNode playerNode : gameNode.get("players")) {
                        players.add(playerNode.asText());
                    }
                }
                gameLobby.setPlayers(players);
                
                // Only show multiplayer games
                if (!"SINGLE_PLAYER".equals(gameLobby.getGametype())) {
                    availableGames.add(gameLobby);
                }
            }
        }
        
        displayGames();
    }
    
    /**
     * Displays the games in the UI.
     */
    private void displayGames() {
        gamesContainer.getChildren().clear();
        
        if (availableGames.isEmpty()) {
            Label noGamesLabel = new Label("No games available. Create one to get started!");
            noGamesLabel.getStyleClass().add("no-games-label");
            gamesContainer.getChildren().add(noGamesLabel);
        } else {
            for (GameLobby game : availableGames) {
                VBox gameCard = createGameCard(game);
                gamesContainer.getChildren().add(gameCard);
            }
        }
        
        // Update games count
        gamesCountLabel.setText(availableGames.size() + " games available");
    }
    
    /**
     * Creates a visual card for a game.
     * 
     * @param game The game to create a card for
     * @return The game card VBox
     */
    private VBox createGameCard(GameLobby game) {
        VBox gameCard = new VBox(10);
        gameCard.getStyleClass().add("game-card");
        gameCard.setPadding(new Insets(15));
        
        // Game header with ID and type
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label gameIdLabel = new Label("Game #" + game.getGameId());
        gameIdLabel.getStyleClass().add("game-id-label");
        
        Label gameTypeLabel = new Label(game.getDisplayGametype());
        gameTypeLabel.getStyleClass().add("game-type-label");
        
        header.getChildren().addAll(gameIdLabel, gameTypeLabel);
        
        // Player info
        HBox playerInfo = new HBox(10);
        playerInfo.setAlignment(Pos.CENTER_LEFT);
        
        Label playersLabel = new Label("Players: " + game.getPlayerCount() + "/" + game.getMaxPlayers());
        playersLabel.getStyleClass().add("players-label");
        
        playerInfo.getChildren().add(playersLabel);
        
        // Player names (if any)
        if (!game.getPlayers().isEmpty()) {
            VBox playersContainer = new VBox(5);
            playersContainer.getStyleClass().add("players-container");
            
            for (String playerName : game.getPlayers()) {
                Label playerNameLabel = new Label("• " + playerName);
                playerNameLabel.getStyleClass().add("player-name-label");
                playersContainer.getChildren().add(playerNameLabel);
            }
            gameCard.getChildren().add(playersContainer);
        }
        
        // Join button
        HBox buttonContainer = new HBox();
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        
        Button joinButton = new Button();
        if (game.isFull()) {
            joinButton.setText("FULL");
            joinButton.setDisable(true);
            joinButton.getStyleClass().add("join-button-disabled");
        } else if (game.getPlayerCount() == 0) {
            joinButton.setText("START GAME");
            joinButton.getStyleClass().add("join-button");
            joinButton.setOnAction(e -> joinGame(game));
        } else {
            joinButton.setText("JOIN GAME");
            joinButton.getStyleClass().add("join-button");
            joinButton.setOnAction(e -> joinGame(game));
        }
        
        buttonContainer.getChildren().add(joinButton);
        
        gameCard.getChildren().addAll(header, playerInfo, buttonContainer);
        
        return gameCard;
    }
    
    /**
     * Shows the create game modal.
     */
    private void showCreateGameModal() {
        try {
            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initStyle(StageStyle.DECORATED);
            modal.setTitle("Create New Game");
            modal.setResizable(false);
            
            VBox content = new VBox(20);
            content.setPadding(new Insets(30));
            content.setAlignment(Pos.CENTER);
            content.getStyleClass().add("create-game-modal");
            
            Label titleLabel = new Label("Select Game Type");
            titleLabel.getStyleClass().add("modal-title");
            
            VBox buttonsContainer = new VBox(15);
            buttonsContainer.setAlignment(Pos.CENTER);
            
            // Create buttons for each game type
            Button twoPlayerButton = createGameTypeButton("2 Players", "TWO_PLAYER", modal);
            Button threePlayerButton = createGameTypeButton("3 Players", "THREE_PLAYER", modal);
            Button fourPlayerButton = createGameTypeButton("4 Players", "FOUR_PLAYER", modal);
            
            // Close button
            Button cancelButton = new Button("Cancel");
            cancelButton.getStyleClass().add("cancel-button");
            cancelButton.setOnAction(e -> modal.close());
            
            buttonsContainer.getChildren().addAll(twoPlayerButton, threePlayerButton, fourPlayerButton, cancelButton);
            content.getChildren().addAll(titleLabel, buttonsContainer);
            
            Scene modalScene = new Scene(content, 300, 400);
            
            // Apply CSS styling
            URL cssUrl = getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/css/imports.css");
            if (cssUrl != null) {
                modalScene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            modal.setScene(modalScene);
            modal.showAndWait();
            
        } catch (Exception e) {
            System.err.println("Error showing create game modal: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Creates a button for a specific game type.
     * 
     * @param displayText The text to display on the button
     * @param gameType The game type identifier
     * @param modal The modal stage to close after creation
     * @return The configured button
     */
    private Button createGameTypeButton(String displayText, String gameType, Stage modal) {
        Button button = new Button(displayText);
        button.getStyleClass().add("game-type-button");
        button.setPrefWidth(200);
        button.setOnAction(e -> createGame(gameType, modal));
        return button;
    }
    
    /**
     * Creates a new game via API call and automatically joins it.
     * 
     * @param gameType The type of game to create (TWO_PLAYER, THREE_PLAYER, FOUR_PLAYER)
     * @param modal The modal to close after successful creation
     */
    private void createGame(String gameType, Stage modal) {
        try {
            // Prepare request body
            String currentTime = Instant.now().toString();
            String requestBody = String.format("""
                {
                  "id": 0,
                  "status": "PENDING",
                  "startDate": "%s",
                  "endDate": "%s",
                  "gameType": "%s",
                  "topCardId": 0,
                  "multiplayer": true,
                  "winnerUsername": ""
                }
                """, currentTime, currentTime, gameType);
            
            System.out.println("Creating game with request: " + requestBody);
            
            // Disable button during API call
            setButtonLoadingState(modal, true, "Creating...");
            
            // Make API call
            apiClient.post("/game/create", requestBody,
                response -> Platform.runLater(() -> {
                    try {
                        handleCreateGameResponse(response, gameType, modal);
                    } catch (Exception ex) {
                        System.err.println("Error handling create game response: " + ex.getMessage());
                        ex.printStackTrace();
                        showCreateGameError("Failed to parse server response", modal);
                    }
                }),
                error -> Platform.runLater(() -> {
                    System.err.println("Error creating game: " + error);
                    showCreateGameError("Failed to create game: " + error, modal);
                })
            );
            
        } catch (Exception e) {
            System.err.println("Error preparing create game request: " + e.getMessage());
            e.printStackTrace();
            showCreateGameError("Failed to create game: " + e.getMessage(), modal);
        }
    }
    
    /**
     * Handles the response from create game API call and auto-joins the created game.
     * 
     * @param response The JSON response from the server
     * @param gameType The game type that was created
     * @param modal The modal to close
     */
    private void handleCreateGameResponse(String response, String gameType, Stage modal) throws Exception {
        System.out.println("Create game response: " + response);
        
        JsonNode root = objectMapper.readTree(response);
        
        // Check if the request was successful
        if (root.has("status") && root.get("status").has("code")) {
            String statusCode = root.get("status").get("code").asText();
            
            if ("OK".equals(statusCode) && root.has("data")) {
                JsonNode data = root.get("data");
                
                // Extract game ID
                if (data.has("gameId")) {
                    int createdGameId = data.get("gameId").asInt();
                    
                    System.out.println("=== GAME CREATED SUCCESSFULLY ===");
                    System.out.println("Game ID: " + createdGameId);
                    System.out.println("Game Type: " + gameType);
                    System.out.println("Auto-joining the created game...");
                    System.out.println("=================================");
                    
                    // Close modal
                    modal.close();
                    
                    // Auto-join the created game
                    autoJoinCreatedGame(createdGameId, gameType);
                } else {
                    showCreateGameError("Server response missing game ID", modal);
                }
            } else {
                String errorMessage = "Unknown error";
                if (root.has("status") && root.get("status").has("description")) {
                    errorMessage = root.get("status").get("description").asText();
                }
                showCreateGameError("Failed to create game: " + errorMessage, modal);
            }
        } else {
            showCreateGameError("Invalid server response format", modal);
        }
    }
    
    /**
     * Automatically joins a created game.
     * 
     * @param gameId The ID of the created game
     * @param gameType The type of the created game
     */
    private void autoJoinCreatedGame(int gameId, String gameType) {
        String username = SessionManager.getInstance().getCurrentUser().getUsername();
        
        String requestBody = String.format("""
            {
              "gameId": %d,
              "username": "%s"
            }
            """, gameId, username);
        
        System.out.println("Auto-joining created game with request: " + requestBody);
        
        apiClient.post("/game/join", requestBody,
            response -> Platform.runLater(() -> {
                try {
                    handleJoinGameResponse(response, gameId, gameType, true);
                } catch (Exception ex) {
                    System.err.println("Error handling auto-join response: " + ex.getMessage());
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Join Error", 
                            "Created game successfully but failed to join automatically");
                }
            }),
            error -> Platform.runLater(() -> {
                System.err.println("Error auto-joining created game: " + error);
                showAlert(Alert.AlertType.ERROR, "Join Error", 
                        "Created game successfully but failed to join: " + error);
            })
        );
    }
    
    /**
     * Handles joining a game.
     * 
     * @param game The game to join
     */
    private void joinGame(GameLobby game) {
        String username = SessionManager.getInstance().getCurrentUser().getUsername();
        
        String requestBody = String.format("""
            {
              "gameId": %d,
              "username": "%s"
            }
            """, game.getGameId(), username);
        
        System.out.println("Joining game with request: " + requestBody);
        
        apiClient.post("/game/join", requestBody,
            response -> Platform.runLater(() -> {
                try {
                    handleJoinGameResponse(response, game.getGameId(), game.getGametype(), false);
                } catch (Exception ex) {
                    System.err.println("Error handling join game response: " + ex.getMessage());
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Join Error", 
                            "Failed to parse server response");
                }
            }),
            error -> Platform.runLater(() -> {
                System.err.println("Error joining game: " + error);
                showAlert(Alert.AlertType.ERROR, "Join Error", 
                        "Failed to join game: " + error);
            })
        );
    }
    
    /**
     * Handles the response from join game API call.
     * 
     * @param response The JSON response from the server
     * @param gameId The game ID that was joined
     * @param gameType The game type
     * @param isAutoJoin Whether this is an auto-join from game creation
     */
    private void handleJoinGameResponse(String response, int gameId, String gameType, boolean isAutoJoin) throws Exception {
        System.out.println("Join game response: " + response);
        
        JsonNode root = objectMapper.readTree(response);
        
        // Check if the request was successful
        if (root.has("status") && root.get("status").has("code")) {
            String statusCode = root.get("status").get("code").asText();
            
            if ("OK".equals(statusCode)) {
                // Store joined game information
                joinedGameId = gameId;
                joinedGameType = gameType;
                
                System.out.println("=== SUCCESSFULLY JOINED GAME ===");
                System.out.println("Game ID: " + joinedGameId);
                System.out.println("Game Type: " + joinedGameType);
                System.out.println("Auto-join: " + isAutoJoin);
                System.out.println("================================");
                
                // Navigate to game table
                navigateToGameTable();
            } else {
                String errorMessage = "Unknown error";
                if (root.has("status") && root.get("status").has("description")) {
                    errorMessage = root.get("status").get("description").asText();
                }
                showAlert(Alert.AlertType.ERROR, "Join Error", 
                        "Failed to join game: " + errorMessage);
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Join Error", 
                    "Invalid server response format");
        }
    }
    
    /**
     * Navigates to the game table after successfully joining a game.
     */
    private void navigateToGameTable() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/game-view.fxml"));
            Parent root = loader.load();
            
            // Initialize multiplayer game with joined game data
            GameController controller = loader.getController();
            if (controller != null && joinedGameId != null && joinedGameType != null) {
                controller.initializeMultiplayerGame(joinedGameId, joinedGameType);
                System.out.println("Initialized GameController with multiplayer game data");
            } else {
                System.err.println("Failed to initialize GameController: controller=" + controller + 
                                 ", gameId=" + joinedGameId + ", gameType=" + joinedGameType);
            }
            
            Scene scene = new Scene(root);
            URL cssUrl = getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/css/imports.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            Stage stage = (Stage) lobbyPane.getScene().getWindow();
            scene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.F11) {
                    stage.setFullScreen(!stage.isFullScreen());
                } else if (e.getCode() == KeyCode.ENTER && e.isAltDown()) {
                    stage.setFullScreen(!stage.isFullScreen());
                }
            });
            
            stage.setScene(scene);
            
            System.out.println("Navigated to game table - ready for multiplayer game!");
            
        } catch (IOException e) {
            System.err.println("Error navigating to game table: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                    "Failed to open game table: " + e.getMessage());
        }
    }
    
    /**
     * Sets the loading state for modal buttons.
     * 
     * @param modal The modal dialog
     * @param loading Whether buttons should be in loading state
     * @param loadingText The text to show when loading
     */
    private void setButtonLoadingState(Stage modal, boolean loading, String loadingText) {
        modal.getScene().getRoot().lookupAll(".game-type-button").forEach(node -> {
            if (node instanceof Button) {
                Button btn = (Button) node;
                btn.setDisable(loading);
                if (loading) {
                    btn.setText(loadingText);
                } else {
                    btn.setText(btn.getText().replace(loadingText, "").trim());
                }
            }
        });
    }
    
    /**
     * Shows an error message for game creation failures.
     * 
     * @param message The error message
     * @param modal The modal dialog
     */
    private void showCreateGameError(String message, Stage modal) {
        setButtonLoadingState(modal, false, "Creating...");
        showAlert(Alert.AlertType.ERROR, "Create Game Error", message);
    }
    
    /**
     * Shows an error message to the user.
     * 
     * @param message The error message to display
     */
    private void showErrorMessage(String message) {
        gamesContainer.getChildren().clear();
        
        VBox errorContainer = new VBox(10);
        errorContainer.setAlignment(Pos.CENTER);
        errorContainer.getStyleClass().add("error-container");
        
        Label errorLabel = new Label("Error loading games");
        errorLabel.getStyleClass().add("error-title");
        
        Label errorMessage = new Label(message);
        errorMessage.getStyleClass().add("error-message");
        
        Button retryButton = new Button("Retry");
        retryButton.getStyleClass().add("retry-button");
        retryButton.setOnAction(e -> loadAvailableGames());
        
        errorContainer.getChildren().addAll(errorLabel, errorMessage, retryButton);
        gamesContainer.getChildren().add(errorContainer);
    }
    
    /**
     * Navigates back to the game mode selection.
     */
    private void backToGameMode() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/game-mode-view.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            URL cssUrl = getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/css/imports.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            Stage stage = (Stage) lobbyPane.getScene().getWindow();
            scene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.F11) {
                    stage.setFullScreen(!stage.isFullScreen());
                } else if (e.getCode() == KeyCode.ENTER && e.isAltDown()) {
                    stage.setFullScreen(!stage.isFullScreen());
                }
            });
            
            stage.setScene(scene);
        } catch (IOException e) {
            System.err.println("Error navigating back to game mode: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Shows a standard alert dialog.
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
        
        if (lobbyPane != null && lobbyPane.getScene() != null) {
            alert.initOwner(lobbyPane.getScene().getWindow());
        }
        
        alert.showAndWait();
    }
    
    /**
     * Gets the joined game ID.
     * 
     * @return The joined game ID or null if no game was joined
     */
    public Integer getJoinedGameId() {
        return joinedGameId;
    }
    
    /**
     * Gets the joined game type.
     * 
     * @return The joined game type or null if no game was joined
     */
    public String getJoinedGameType() {
        return joinedGameType;
    }
    
    /**
     * Refreshes the lobby by reloading available games.
     * This method can be called externally to update the lobby display.
     */
    public void refreshLobby() {
        loadAvailableGames();
    }
} 