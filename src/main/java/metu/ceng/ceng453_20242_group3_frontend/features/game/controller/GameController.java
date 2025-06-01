package metu.ceng.ceng453_20242_group3_frontend.features.game.controller;

import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import metu.ceng.ceng453_20242_group3_frontend.features.common.util.SessionManager;
import metu.ceng.ceng453_20242_group3_frontend.features.game.model.*;
import metu.ceng.ceng453_20242_group3_frontend.features.game.view.CardRenderer;
import metu.ceng.ceng453_20242_group3_frontend.features.game.view.ColorSelectionDialog;
import metu.ceng.ceng453_20242_group3_frontend.features.game.view.NotificationManager;
import metu.ceng.ceng453_20242_group3_frontend.features.game.view.UnoIndicatorManager;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import metu.ceng.ceng453_20242_group3_frontend.features.common.util.ApiClient;
import metu.ceng.ceng453_20242_group3_frontend.features.game.controller.LobbyController;
import metu.ceng.ceng453_20242_group3_frontend.features.common.util.WebSocketManager;

/**
 * Controller for the game view.
 */
public class GameController {
    
    @FXML
    private AnchorPane gamePane;
    
    // Player areas
    @FXML
    private HBox leftPlayerArea;
    
    @FXML
    private HBox rightPlayerArea;
    
    @FXML
    private VBox topPlayerArea;
    
    @FXML
    private VBox bottomPlayerArea;
    
    // Player name labels
    @FXML
    private Label topPlayerNameLabel;
    
    @FXML
    private Label leftPlayerNameLabel;
    
    @FXML
    private Label rightPlayerNameLabel;
    
    @FXML
    private Label bottomPlayerNameLabel;
    
    // Card containers
    @FXML
    private HBox topPlayerCardsContainer;
    
    @FXML
    private VBox leftPlayerCardsContainer;
    
    @FXML
    private VBox rightPlayerCardsContainer;
    
    @FXML
    private HBox bottomPlayerCardsContainer;
    
    // Game elements
    @FXML
    private StackPane drawPileContainer;
    
    @FXML
    private StackPane discardPileContainer;
    
    @FXML
    private ImageView directionIndicator;
    
    @FXML
    private Button exitGameButton;
    
    @FXML
    private Label currentTurnLabel;
    
    // Cheat buttons
    @FXML
    private Button skipButton;
    
    @FXML
    private Button reverseButton;
    
    @FXML
    private Button drawTwoButton;
    
    @FXML
    private Button wildButton;
    
    @FXML
    private Button wildDrawFourButton;
    
    // Game state using our Game model
    private Game game;
    private boolean isGameRunning = true;
    private boolean firstCardPlayed = false;
    
    // Store game information for API calls
    private Integer gameId;
    private String gameType;
    private String gameStartTime;
    private boolean isMultiplayerGame = false;
    
    // API client for backend communication
    private ApiClient apiClient;
    
    // WebSocket manager for multiplayer communication
    private WebSocketManager webSocketManager;
    
    // Legacy fields to be removed after full refactoring
    private List<ComputerAIPlayer> aiPlayers = new ArrayList<>();
    
    // Store player area animations for control
    private javafx.animation.Timeline[] playerAnimations;
    
    // Add a field to store the game table animation
    private javafx.animation.Timeline gameTableAnimation;
    
    // Sub-controllers
    private NotificationManager notificationManager;
    private UnoIndicatorManager unoIndicatorManager;
    private GameTableController gameTableController;
    private AIPlayerController aiPlayerController;
    private CardAnimationController cardAnimationController;
    
    @FXML
    private void initialize() {
        // Initialize notification manager
        notificationManager = new NotificationManager(gamePane);
        
        // Initialize UNO indicator manager
        unoIndicatorManager = new UnoIndicatorManager(
            topPlayerNameLabel,
            leftPlayerNameLabel,
            rightPlayerNameLabel,
            bottomPlayerNameLabel
        );
        
        // Initialize API client
        apiClient = new ApiClient();
        
        // Initialize WebSocket manager
        webSocketManager = new WebSocketManager();
        
        // Initialize cheat buttons
        initializeCheatButtons();
        
        // Initialize our exit button
        exitGameButton.setOnAction(event -> exitGame());
        
        // Set local player name from session
        if (SessionManager.getInstance().isLoggedIn()) {
            String username = SessionManager.getInstance().getCurrentUser().getUsername();
            bottomPlayerNameLabel.setText(username);
        }
        
        // Initialize game table controller
        gameTableController = new GameTableController(
            gamePane,
            topPlayerArea,
            leftPlayerArea,
            rightPlayerArea,
            bottomPlayerArea
        );
        
        // Card animation controller will be initialized when the game starts
        // since it needs the discard pile container
    }
    
    /**
     * Initializes the game with the specified parameters.
     * 
     * @param gameMode The game mode (singleplayer/multiplayer)
     * @param aiPlayerCount Number of AI players
     * @param initialCardCount Number of initial cards for each player
     */
    public void initializeGame(String gameMode, int aiPlayerCount, int initialCardCount) {
        // Create PlayerCount enum
        PlayerCount playerCount = switch (aiPlayerCount + 1) { // +1 for human player
            case 2 -> PlayerCount.TWO;
            case 3 -> PlayerCount.THREE;
            case 4 -> PlayerCount.FOUR;
            default -> PlayerCount.TWO;
        };

        // Set game metadata
        isMultiplayerGame = "multiplayer".equalsIgnoreCase(gameMode);
        gameStartTime = java.time.Instant.now().toString();
        
        // Set game type based on player count
        gameType = switch (aiPlayerCount + 1) {
            case 2 -> "TWO_PLAYER";
            case 3 -> "THREE_PLAYER";
            case 4 -> "FOUR_PLAYER";
            default -> "TWO_PLAYER";
        };

        // Create a new game instance
        this.game = new Game(
            isMultiplayerGame ? GameMode.MULTIPLAYER : GameMode.SINGLEPLAYER,
            playerCount
        );
        
        // Reset game ID
        this.gameId = null;
        
        // Call the game start API
        String requestBody = String.format("{\"gameType\": \"%s\", \"multiplayer\": %b}", 
                                           isMultiplayerGame ? gameType : "SINGLE_PLAYER", 
                                           isMultiplayerGame);
        
        System.out.println("=== STARTING GAME ===");
        System.out.println("Game Mode: " + gameMode);
        System.out.println("Game Type: " + gameType);
        System.out.println("Multiplayer: " + isMultiplayerGame);
        System.out.println("Start Time: " + gameStartTime);
        System.out.println("API Request: " + requestBody);
        System.out.println("====================");
        
        apiClient.post("/game/start", requestBody, 
            response -> {
                try {
                    // Parse the response and extract game ID
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode root = objectMapper.readTree(response);
                    if (root.has("data")) {
                        this.gameId = root.get("data").asInt();
                        System.out.println("Game started with ID: " + this.gameId);
                    } else {
                        System.err.println("Game ID not found in response: " + response);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to parse game start response: " + e.getMessage());
                }
            },
            error -> System.err.println("Failed to call game start API: " + error)
        );
        
        // Debug output to confirm game direction
        System.out.println("GAME INITIALIZATION - DIRECTION IS: " + game.getDirection());
        
        // Reset first card flag
        firstCardPlayed = false;
        
        // Clear any existing players
        this.aiPlayers.clear();
        
        // Add human player (current user)
        String username = SessionManager.getInstance().isLoggedIn() ? 
                          SessionManager.getInstance().getCurrentUser().getUsername() : 
                          "Player";
        Player humanPlayer = new Player(username);
        game.addPlayer(humanPlayer);
        
        // Create AI player names in the correct order for the layout
        List<String> aiNames = new ArrayList<>();
        
        // First add right opponent (Opponent 1)
        if (aiPlayerCount >= 2) {
            String aiName = "Opponent 1";
            aiNames.add(aiName);
            Player aiPlayer = new Player(aiName, true);
            game.addPlayer(aiPlayer);
            aiPlayers.add(new ComputerAIPlayer(aiName));
            System.out.println("Added " + aiName + " (right position)");
        }
        
        // Next add top opponent (Opponent 2)
        String topName = (aiPlayerCount == 1) ? "Opponent 1" : "Opponent 2";
        aiNames.add(topName);
        Player topPlayer = new Player(topName, true);
        game.addPlayer(topPlayer);
        aiPlayers.add(new ComputerAIPlayer(topName));
        System.out.println("Added " + topName + " (top position)");
        
        // Finally add left opponent (Opponent 3) if needed
        if (aiPlayerCount >= 3) {
            String aiName = "Opponent 3";
            aiNames.add(aiName);
            Player aiPlayer = new Player(aiName, true);
            game.addPlayer(aiPlayer);
            aiPlayers.add(new ComputerAIPlayer(aiName));
            System.out.println("Added " + aiName + " (left position)");
        }
        
        // Print the final player list for debugging
        System.out.println("Final player order for COUNTER_CLOCKWISE movement:");
        for (int i = 0; i < game.getPlayers().size(); i++) {
            System.out.println("Index " + i + ": " + game.getPlayers().get(i).getName());
        }
        
        // Initialize sub-controllers that need the game model
        cardAnimationController = new CardAnimationController(gamePane, discardPileContainer);
        
        // Initialize AI player controller with a callback for card plays
        aiPlayerController = new AIPlayerController(
            game, 
            notificationManager, 
            gameTableController,
            new AIPlayerController.CardPlayCallback() {
                @Override
                public void onCardPlayed(int aiIndex, Card card) {
                    // Update the UI after AI plays a card
                    updateUI();
                    updateDirectionIndicator();
                    updateTurnLabel();
                }
                
                @Override
                public void onCardDrawn() {
                    // Update the UI after AI draws a card
                    updateUI();
                    updateTurnLabel();
                }
                
                @Override
                public void onGameEnd(boolean isAIWinner) {
                    handleGameEnd(false);
                }
            }
        );
        
        // Configure player areas based on player count
        gameTableController.setupPlayerAreas(game, aiNames);
        
        // Start the game (this deals cards and initializes the discard pile)
        game.startGame();
        
        // Initialize direction indicator
        updateDirectionIndicator();
        
        // When a new game starts, mark all cards as playable for the initial play
        for (Player player : game.getPlayers()) {
            for (Card card : player.getHand()) {
                card.setPlayable(true);
            }
        }
        
        // Update the UI with initial game state
        updateUI();
        
        // Update UNO indicators based on card counts
        updateUnoIndicators();
        
        updateTurnLabel();
    }
    
    /**
     * Updates the UI to match the current game state
     */
    private void updateUI() {
        // Clear any existing cards
        bottomPlayerCardsContainer.getChildren().clear();
        topPlayerCardsContainer.getChildren().clear();
        leftPlayerCardsContainer.getChildren().clear();
        rightPlayerCardsContainer.getChildren().clear();
        
        // Clear draw and discard piles
        drawPileContainer.getChildren().clear();
        discardPileContainer.getChildren().clear();
        
        // First make sure playable cards are up-to-date
        if (game != null) {
            game.updatePlayableCards();
        }
        
        // Display player cards
        List<Player> players = game.getPlayers();
        if (!players.isEmpty()) {
            // Human player's cards (always the first player in our design)
            Player humanPlayer = players.get(0);
            
            // Instead of adding cards directly, use updatePlayerHandVisuals
            // which handles playability and visual effects
            updatePlayerHandVisuals();
            
            // Display AI cards based on total players and new counterclockwise layout
            if (players.size() > 1) {
                int totalPlayers = players.size();
                
                if (totalPlayers == 2) {
                    // 2 players: Display Opponent 1 at top (player 1)
                    displayOpponentCards(1, topPlayerCardsContainer, false, 180);
                } else if (totalPlayers == 3) {
                    // 3 players: Display Opponent 1 at right (player 1) and Opponent 2 at top (player 2)
                    displayOpponentCards(1, rightPlayerCardsContainer, true, -90);
                    displayOpponentCards(2, topPlayerCardsContainer, false, 180);
                } else if (totalPlayers == 4) {
                    // 4 players: Opponent 1 at right, Opponent 2 at top, Opponent 3 at left
                    displayOpponentCards(1, rightPlayerCardsContainer, true, -90);
                    displayOpponentCards(2, topPlayerCardsContainer, false, 180);
                    displayOpponentCards(3, leftPlayerCardsContainer, true, 90);
                }
            }
        }
        
        // Set up draw pile
        setupDrawPile();
        
        // Always set up discard pile - will show empty placeholder if no card has been played
        setupDiscardPile();
        
        // Update UNO indicators for all players
        updateUnoIndicators();

        // Always update the direction indicator
        updateDirectionIndicator();

        updateTurnLabel();
    }
    
    /**
     * Displays opponent cards as card backs
     * 
     * @param playerIndex The index of the player in the game's player list
     * @param container The container to add the cards to
     * @param vertical Whether to display cards vertically (for side opponents)
     * @param rotation Rotation angle for the cards
     */
    private void displayOpponentCards(int playerIndex, javafx.scene.layout.Pane container, boolean vertical, double rotation) {
        if (playerIndex < 0 || playerIndex >= game.getPlayers().size()) {
            return;
        }
        
        Player opponent = game.getPlayers().get(playerIndex);
        
        // Debug mode: Show cards face up instead of card backs
        for (Card card : opponent.getHand()) {
            StackPane cardView = createCardView(card);
            
            if (rotation != 0) {
                cardView.setRotate(rotation);
            }
            
            container.getChildren().add(cardView);
        }
    }
    
    /**
     * Sets up the draw pile.
     */
    private void setupDrawPile() {
        drawPileContainer.getChildren().clear();
        
        // Create a card back view for the draw pile
        StackPane cardBackView = CardRenderer.createCardBackView();
        
        // Add click event to draw a card
        cardBackView.setOnMouseClicked(event -> {
                drawCard();
        });
        
        drawPileContainer.getChildren().add(cardBackView);
        
        // Add draw pile label
        Label pileLabel = new Label("DRAW PILE");
        pileLabel.getStyleClass().add("card-pile-label");
        drawPileContainer.getChildren().add(pileLabel);
    }
    
    /**
     * Updates which cards are playable based on the top discard card
     */
    private void updatePlayableCards() {
        if (game == null) return;
        
        // Have the game model update which cards are playable
        game.updatePlayableCards();
        
        // Refresh the card visuals for human player to show which ones are playable
        updatePlayerHandVisuals();
    }
    
    /**
     * Updates the visual appearance of cards in the player's hand
     */
    private void updatePlayerHandVisuals() {
        bottomPlayerCardsContainer.getChildren().clear();
        
        if (game == null || game.getPlayers().isEmpty()) return;
        
        // First, make sure the playable status of cards is up-to-date
        game.updatePlayableCards();
        
        Player humanPlayer = game.getPlayers().get(0);
        for (Card card : humanPlayer.getHand()) {
            StackPane cardView = createCardView(card);
            
            // Check if the card is playable according to the game rules
            boolean isPlayable = card.isPlayable();
            
            // Only enable click for playable cards and when it's player's turn
            if (isPlayable && game.getCurrentPlayerIndex() == 0) {
                // Add glow effect to playable cards
                cardView.setEffect(new javafx.scene.effect.DropShadow(15, Color.GOLD));
                cardView.setStyle("-fx-cursor: hand;");
                cardView.setOnMouseClicked(event -> playCard(cardView, card));
            } else {
                // For unplayable cards, set a dimmed appearance
                cardView.setOpacity(0.8);
                // Remove click handler for unplayable cards to prevent attempts
                // that would fail the validation checks
                if (!isPlayable) {
                    // Add a different click handler that explains why the card can't be played
                    cardView.setOnMouseClicked(event -> {
                        if (card.getAction() == CardAction.WILD_DRAW_FOUR) {
                            showCardUnplayableMessage("You can't play a Wild Draw Four when you have cards matching the current color.");
                        } else {
                            showCardUnplayableMessage("This card doesn't match the color or value of the top card.");
                        }
                    });
                    cardView.setStyle("-fx-cursor: not-allowed;");
                } else {
                    // Card is playable but it's not player's turn
                    cardView.setOnMouseClicked(event -> {
                        showCardUnplayableMessage("It's not your turn.");
                    });
                    cardView.setStyle("-fx-cursor: wait;");
                }
            }
            
            bottomPlayerCardsContainer.getChildren().add(cardView);
        }
    }
    
    /**
     * Shows a message explaining why a card can't be played
     * 
     * @param message The explanation message
     */
    private void showCardUnplayableMessage(String message) {
        notificationManager.showCardUnplayableNotification(message);
    }
    
    /**
     * Plays a card from the player's hand.
     * 
     * @param cardView The card view to animate
     * @param card The card model to play
     */
    private void playCard(StackPane cardView, Card card) {
        // Check if the game is running
        if (!isGameRunning || game == null) {
            return;
        }
        
        Player currentPlayer = game.getCurrentPlayer();
        
        // Make sure it's the player's turn
        if (currentPlayer.isAI() || game.getCurrentPlayerIndex() != 0) {
            showCardUnplayableMessage("It's not your turn.");
            return;
        }
        
        // In the initial state (discard pile empty), all cards are playable
        if (game.isDiscardPileEmpty()) {
            System.out.println("Playing first card in the game");
            // Set the first card played flag (for initial card)
            firstCardPlayed = true;
            
            // For wild cards, handle color selection
            if (card.isWildCard()) {
                // For wild cards, use the color selection dialog
                handleWildCardColorSelection(card, () -> {
                    finishCardPlay(cardView, card);
                });
            } else {
                // For regular cards, the game will set the color to the card's color
                finishCardPlay(cardView, card);
            }
            return;
        }
        
        // Make sure the playable status is up-to-date
        game.updatePlayableCards();
        
        // Check if the card is playable according to UNO rules
        if (!card.isPlayable()) {
            showCardUnplayableMessage("This card doesn't match the color or value of the top card.");
            return;
        }
        
        // Extra validation for WILD_DRAW_FOUR
        if (card.getAction() == CardAction.WILD_DRAW_FOUR) {
            // Let the Game class handle the validation, we just need to select color
            handleWildCardColorSelection(card, () -> {
                finishCardPlay(cardView, card);
            });
        } else if (card.isWildCard()) {
            // For other wild cards, handle color selection
            handleWildCardColorSelection(card, () -> {
                finishCardPlay(cardView, card);
            });
        } else {
            // For regular cards, game will set the color to the card's color
            finishCardPlay(cardView, card);
        }
    }
    
    /**
     * Handles color selection for wild cards played by the human player.
     * 
     * @param card The wild card being played
     * @param onColorSelected Callback for when color selection is complete
     */
    private void handleWildCardColorSelection(Card card, Runnable onColorSelected) {
        Stage stage = (Stage) gamePane.getScene().getWindow();
        
        ColorSelectionDialog dialog = new ColorSelectionDialog(stage, selectedColor -> {
            // Set the current color in the game
            game.setCurrentColor(selectedColor);
            
            // Execute the callback
            onColorSelected.run();
        });
        
        // Show the dialog
        dialog.show();
    }
    
    /**
     * Displays a notification that the player has automatically declared UNO.
     */
    private void declareUno() {
        Player player = game.getPlayers().get(0); // Human player
        
        // Automatically declare UNO for the player
        if (player.declareUno()) {
            // Show UNO call notification
            notificationManager.showUnoCallNotification(player.getName());
        }
    }
    
    /**
     * Gets an action message based on the card type.
     * 
     * @param card The card that was played
     * @param targetPlayerName The name of the player affected by the action
     * @return A message describing the card's action or null if no notification should be shown
     */
    private String getActionMessage(Card card, String targetPlayerName) {
        // Delegate to notification manager
        Player currentPlayer = game.getCurrentPlayer();
        return notificationManager.getActionMessage(card, currentPlayer != null ? currentPlayer.getName() : "", targetPlayerName);
    }
    
    /**
     * Completes playing a card after any required user input (like wild card color selection).
     * 
     * @param cardView The card view to animate
     * @param card The card model to play
     */
    private void finishCardPlay(StackPane cardView, Card card) {
        // Store the original position of the card before removing it from the container
        double originalX = 0;
        double originalY = 0;
        
        try {
            // Convert the card's position to scene coordinates
            Bounds cardBounds = cardView.localToScene(cardView.getBoundsInLocal());
            originalX = cardBounds.getMinX();
            originalY = cardBounds.getMinY();
        } catch (Exception e) {
            System.out.println("Warning: Could not get original card position");
        }
        
        // Remove card from player's hand visually
        bottomPlayerCardsContainer.getChildren().remove(cardView);
        
        Player currentPlayer = game.getCurrentPlayer();
        int originalCardCount = currentPlayer.getCardCount();
        
        // Get the next player who will be affected by the action
        Player nextPlayer = game.getNextPlayer();
        String nextPlayerName = nextPlayer != null ? nextPlayer.getName() : "Unknown";
        
        // First update the game model - this needs to happen before animation
        boolean success = game.playCard(card);
        
        if (success) {
            // Show wild card color notification separately to ensure it's always displayed
            if (card.isWildCard() && game.getCurrentColor() != CardColor.MULTI) {
                notificationManager.showColorSelectionNotification(currentPlayer.getName(), game.getCurrentColor());
            }
            
            // ONLY show notifications for special actions
            String actionMessage = notificationManager.getActionMessage(card, currentPlayer.getName(), nextPlayerName);
            if (actionMessage != null) {
                notificationManager.showActionNotification(currentPlayer.getName(), actionMessage);
            }
            
            // Check for UNO declaration when player will have 1 card left
            if (originalCardCount == 2 && currentPlayer.getCardCount() == 1) {
                // For human players, automatically declare UNO
                if (!currentPlayer.isAI()) {
                    declareUno();
                } else {
                    // AI players also automatically declare UNO
                    currentPlayer.declareUno();
                    notificationManager.showUnoCallNotification(currentPlayer.getName());
                }
            }
            
            // Use the CardAnimationController to animate the card to the discard pile
            cardAnimationController.animateCardFromPosition(card, originalX, originalY, new CardAnimationController.AnimationCallback() {
                @Override
                public void onAnimationComplete() {
                    // Update the discard pile
                    setupDiscardPile();
                    
                    // Update UNO indicators
                    updateUnoIndicators();
                    
                    // For DRAW_TWO and WILD_DRAW_FOUR cards, force an immediate UI update 
                    // to show the drawn cards in opponent hands
                    if (card.getAction() == CardAction.DRAW_TWO || card.getAction() == CardAction.WILD_DRAW_FOUR) {
            updateUI();
                    }
                }
            });
            
            // Update direction indicator
            updateDirectionIndicator();
            
            // Update turn label
            updateTurnLabel();
            
            // Check for game over
            if (game.isGameEnded()) {
                handleGameEnd(true);
                return;
            }
            
            // Update playable cards
                updatePlayableCards();
                
            // Handle AI turns
            aiPlayerController.handleAITurns();
        }
    }
    
    /**
     * Updates the direction indicator based on the game's direction
     */
    private void updateDirectionIndicator() {
        if (game == null) {
            return;
        }
        
        Direction gameDirection = game.getDirection();
        System.out.println("### UPDATING DIRECTION INDICATOR - CURRENT DIRECTION: " + gameDirection + " ###");
        
        // Simple direct PNG loading
        boolean isClockwise = gameDirection == Direction.CLOCKWISE;
        String imagePath = isClockwise 
            ? "/images/arrow-clockwise.png" 
            : "/images/arrow-counterclockwise.png";
        
        // Load the image directly
        Image directionImage = new Image(getClass().getResourceAsStream(imagePath));
        directionIndicator.setImage(directionImage);
        
        // Animate it
        ScaleTransition pulse = new ScaleTransition(Duration.millis(1000), directionIndicator);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.2);
        pulse.setToY(1.2);
        pulse.setCycleCount(2);
        pulse.setAutoReverse(true);
        pulse.play();
    }
    
    /**
     * Updates the current turn label based on the active player
     */
    private void updateTurnLabel() {
        if (game == null) {
            return;
        }
        
        Player currentPlayer = game.getCurrentPlayer();
        if (currentPlayer == null) {
            return;
        }
        
        // Current player is the human player (always at index 0)
        if (game.getCurrentPlayerIndex() == 0) {
            currentTurnLabel.setText("YOUR TURN");
            currentTurnLabel.setStyle("-fx-background-color: rgba(0, 153, 51, 0.9); -fx-font-size: 20px; -fx-font-weight: bold; -fx-border-color: white; -fx-border-width: 2px; -fx-border-radius: 20px;"); // Green for player's turn
            
            // Enable cheat buttons when it's the player's turn
            updateCheatButtonState(true);
        } else {
            // Display the appropriate opponent name based on new counterclockwise layout
            String playerName = currentPlayer.getName();
            currentTurnLabel.setText(playerName + "'S TURN");
            currentTurnLabel.setStyle("-fx-background-color: rgba(217, 83, 79, 0.9); -fx-font-size: 20px; -fx-font-weight: bold; -fx-border-color: white; -fx-border-width: 2px; -fx-border-radius: 20px;"); // Red for opponent's turn
            
            // Disable cheat buttons when it's not the player's turn
            updateCheatButtonState(false);
        }
        
        // Update which player area is pulsing
        gameTableController.updatePlayerAreaAnimations(game);
    }
    
    /**
     * Updates the discard pile with the most recently played card.
     */
    private void setupDiscardPile() {
        // Clear the discard pile
        discardPileContainer.getChildren().clear();
        
        Card topCard = game.getDiscardPile().peekCard();
        
        if (topCard == null) {
            // If there's no card yet, show empty placeholder
            discardPileContainer.getChildren().add(CardRenderer.createEmptyCardPlaceholder());
        } else {
            // Create a view for the top card
            StackPane cardView;
            
            // For wild cards, use the special card renderer with color indicator
            if (topCard.isWildCard()) {
                    cardView = CardRenderer.createWildCardWithSelectedColor(topCard, game.getCurrentColor());
                
                // Add a slight rotation for visual interest
                cardView.setRotate(-5 + (Math.random() * 10));
            } else {
                // Regular cards
                cardView = CardRenderer.createCardView(topCard);
                
                // Add a slight rotation for visual interest
                cardView.setRotate(-5 + (Math.random() * 10));
            }
            
            // Add shadow for emphasis
            javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
            shadow.setColor(javafx.scene.paint.Color.BLACK);
            shadow.setRadius(10);
            cardView.setEffect(shadow);
            
            discardPileContainer.getChildren().add(cardView);
        }
        
        // Add a label to show what pile this is
        Label pileLabel = new Label("DISCARD PILE");
        pileLabel.getStyleClass().add("card-pile-label");
        discardPileContainer.getChildren().add(pileLabel);
        
        // Add style class to the container
        discardPileContainer.getStyleClass().add("discard-pile");
    }
    
    /**
     * Handles the action of drawing a card from the draw pile
     */
    private void drawCard() {
        if (!isGameRunning || game == null) {
            return;
        }
        
        // Only allow drawing when it's the player's turn
        if (game.getCurrentPlayerIndex() != 0) {
            return;
        }
        
        // Draw a card without advancing the turn
        Card drawnCard = game.drawCardWithoutAdvancingTurn();
        
        if (drawnCard != null) {
            // Create animation for card being added to hand
            StackPane cardView = createCardView(drawnCard);
            cardView.setOpacity(0);
            cardView.setTranslateY(-20);
            
            // Add click event to the new card
            cardView.setOnMouseClicked(event -> playCard(cardView, drawnCard));
            
            // Add the card to the player's hand UI
            bottomPlayerCardsContainer.getChildren().add(cardView);
            
            // Animate the card appearing
            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(Duration.millis(300), cardView);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            
            javafx.animation.TranslateTransition moveIn = new javafx.animation.TranslateTransition(Duration.millis(300), cardView);
            moveIn.setFromY(-20);
            moveIn.setToY(0);
            
            javafx.animation.ParallelTransition animation = new javafx.animation.ParallelTransition(fadeIn, moveIn);
            animation.setOnFinished(e -> {
                // Update the UI after animation completes
                updateUI();
                
                // Update direction indicator
                updateDirectionIndicator();
                
                // Check if the drawn card is playable
                boolean isPlayable = drawnCard.isPlayable();
                
                if (isPlayable) {
                    // If the card is playable, show a notification and don't advance the turn
                    notificationManager.showActionNotification("", "The drawn card is playable. You may play it now.");
                    
                    // Highlight the drawn card more prominently
                    cardView.setEffect(new javafx.scene.effect.DropShadow(20, Color.GOLD));
                    ScaleTransition pulse = new ScaleTransition(Duration.millis(500), cardView);
                    pulse.setFromX(1.0);
                    pulse.setFromY(1.0);
                    pulse.setToX(1.2);
                    pulse.setToY(1.2);
                    pulse.setCycleCount(3);
                    pulse.setAutoReverse(true);
                    pulse.play();
                } else {
                    // If the card is not playable, advance to the next player's turn
                    game.advanceTurnAfterDraw();
                    
                    // Show a notification that the card is not playable
                    notificationManager.showActionNotification("", "The drawn card cannot be played. Turn passed.");
                
                // Update turn label
                updateTurnLabel();
                
                // Check if an automatic move is needed for AI
                    aiPlayerController.handleAITurns();
                }
            });
            
            animation.play();
        } else {
            // If no card was drawn (e.g., draw pile is empty), still update the UI
            updateUI();
            updateDirectionIndicator();
            
            // Since no card was drawn, advance to the next player's turn
            game.advanceTurnAfterDraw();
            
            updateTurnLabel();
            aiPlayerController.handleAITurns();
        }
    }
    
    /**
     * Creates a visual representation of a card.
     *
     * @param card The card model to display
     * @return A StackPane containing the card visualization
     */
    private StackPane createCardView(Card card) {
        return CardRenderer.createCardView(card);
    }
    
    /**
     * Handles the end of the game
     * 
     * @param isPlayerWinner Whether the local player won the game
     */
    private void handleGameEnd(boolean isPlayerWinner) {
        isGameRunning = false;
        
        // Get the name of the winner
        String winnerName;
        if (isPlayerWinner) {
            winnerName = game.getPlayers().get(0).getName(); // Human player
        } else {
            winnerName = game.getWinner() != null ? game.getWinner().getName() : "AI Player";
        }
        
        // Call the game end API with proper format
        callGameEndAPI(winnerName);
        
        // For multiplayer games in waiting state, just navigate back to lobby
        if (isMultiplayerGame && game.getPlayers().size() <= 1) {
            System.out.println("Multiplayer game ending - returning to lobby");
            navigateAfterGameEnd();
            return;
        }
        
        // For normal games, show the game over overlay
        showGameOverOverlay(isPlayerWinner, winnerName);
    }
    
    /**
     * Shows the game over overlay with appropriate options.
     * 
     * @param isPlayerWinner Whether the player won
     * @param winnerName The name of the winner
     */
    private void showGameOverOverlay(boolean isPlayerWinner, String winnerName) {
        // Use Platform.runLater to ensure UI updates happen on the JavaFX thread
        Platform.runLater(() -> {
            // Find the StackPane in the center of the grid (game table)
            final StackPane gameTableStack = findGameTable();
            
            if (gameTableStack != null) {
                // Create a game over overlay
                StackPane gameOverPane = new StackPane();
                gameOverPane.setPrefWidth(500);
                gameOverPane.setPrefHeight(400);
                gameOverPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-background-radius: 20;");
                gameOverPane.setEffect(new javafx.scene.effect.DropShadow(20, Color.BLACK));
                
                // Create content for the game over screen
                VBox content = new VBox(20);
                content.setAlignment(javafx.geometry.Pos.CENTER);
                content.setPadding(new javafx.geometry.Insets(30));
                
                // Game over header
                Label gameOverLabel = new Label("GAME OVER");
                gameOverLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: white;");
                
                // Winner text with animation
                Label winnerLabel = new Label(isPlayerWinner ? "YOU WIN!" : "YOU LOSE!");
                winnerLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: " + 
                        (isPlayerWinner ? "gold" : "crimson") + ";");
                
                // Winner name
                Label winnerNameLabel = new Label("Winner: " + winnerName);
                winnerNameLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");
                
                // Buttons container
                HBox buttonsBox = new HBox(30);
                buttonsBox.setAlignment(javafx.geometry.Pos.CENTER);
                buttonsBox.setPadding(new javafx.geometry.Insets(20, 0, 0, 0));
                
                // Navigation buttons based on game type
                if (isMultiplayerGame) {
                    // For multiplayer games, go back to lobby
                    Button backToLobbyButton = new Button("BACK TO LOBBY");
                    backToLobbyButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; " +
                            "-fx-font-size: 16px; -fx-padding: 15 30; -fx-background-radius: 10;");
                    backToLobbyButton.setOnAction(e -> navigateToLobby());
                    
                    Button exitButton = new Button("EXIT TO MENU");
                    exitButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold; " +
                            "-fx-font-size: 16px; -fx-padding: 15 30; -fx-background-radius: 10;");
                    exitButton.setOnAction(e -> navigateToMainMenu());
                    
                    // Add hover effects
                    addButtonHoverEffects(backToLobbyButton, "#66BB6A", "#4CAF50");
                    addButtonHoverEffects(exitButton, "#EF5350", "#F44336");
                    
                    buttonsBox.getChildren().addAll(backToLobbyButton, exitButton);
                } else {
                    // For singleplayer games, go back to game mode selection
                    Button playAgainButton = new Button("PLAY AGAIN");
                    playAgainButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; " +
                            "-fx-font-size: 16px; -fx-padding: 15 30; -fx-background-radius: 10;");
                    playAgainButton.setOnAction(e -> navigateToGameMode());
                    
                    Button exitButton = new Button("EXIT TO MENU");
                    exitButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold; " +
                            "-fx-font-size: 16px; -fx-padding: 15 30; -fx-background-radius: 10;");
                    exitButton.setOnAction(e -> navigateToMainMenu());
                    
                    // Add hover effects
                    addButtonHoverEffects(playAgainButton, "#66BB6A", "#4CAF50");
                    addButtonHoverEffects(exitButton, "#EF5350", "#F44336");
                    
                    buttonsBox.getChildren().addAll(playAgainButton, exitButton);
                }
                
                // Add all components to content
                content.getChildren().addAll(gameOverLabel, winnerLabel, winnerNameLabel, buttonsBox);
                
                // Add content to game over pane
                gameOverPane.getChildren().add(content);
                
                // Add animations
                // 1. Pulsing animation for the winner text
                ScaleTransition pulse = new ScaleTransition(Duration.seconds(1), winnerLabel);
                pulse.setFromX(1.0);
                pulse.setFromY(1.0);
                pulse.setToX(1.1);
                pulse.setToY(1.1);
                pulse.setCycleCount(javafx.animation.Animation.INDEFINITE);
                pulse.setAutoReverse(true);
                pulse.play();
                
                // Add the game over screen to the game table
                gameTableStack.getChildren().add(gameOverPane);
                
                // Fade in animation for the overlay
                javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(Duration.millis(500), gameOverPane);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            } else {
                // Fallback to simple navigation if we can't find the game table
                navigateAfterGameEnd();
            }
        });
    }
    
    /**
     * Calls the game end API with the proper format.
     * 
     * @param winnerUsername The username of the winner
     */
    private void callGameEndAPI(String winnerUsername) {
        if (gameId == null) {
            System.err.println("Cannot call game end API: No game ID available");
            return;
        }
        
        try {
            // Use current time for end date
            String currentTime = java.time.Instant.now().toString();
            
            // Sanitize winner username (remove spaces and special characters)
            String sanitizedWinnerName = winnerUsername.replaceAll("[^a-zA-Z0-9]", "");
            if (sanitizedWinnerName.isEmpty()) {
                sanitizedWinnerName = "UnknownWinner";
            }
            
            // Prepare request body according to API specification
            // Make sure we're ending the existing game, not creating a new one
            String requestBody = String.format("""
                {
                  "id": %d,
                  "status": "COMPLETED",
                  "startDate": "%s",
                  "endDate": "%s",
                  "gameType": "%s",
                  "topCardId": 0,
                  "multiplayer": %b,
                  "winnerUsername": "%s"
                }
                """, 
                gameId, 
                gameStartTime != null ? gameStartTime : currentTime, 
                currentTime, 
                gameType != null ? gameType : "SINGLE_PLAYER", 
                isMultiplayerGame, 
                sanitizedWinnerName
            );
            
            System.out.println("=== ENDING GAME ===");
            System.out.println("Game ID: " + gameId);
            System.out.println("Winner: " + sanitizedWinnerName);
            System.out.println("Multiplayer: " + isMultiplayerGame);
            System.out.println("Status: COMPLETED (ending existing game)");
            System.out.println("Request: " + requestBody);
            System.out.println("==================");
            
            apiClient.post("/game/end", requestBody, 
                response -> {
                    System.out.println("Game end API response: " + response);
                    // Parse response to ensure game was ended properly
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode root = objectMapper.readTree(response);
                        if (root.has("status") && root.get("status").has("code")) {
                            String statusCode = root.get("status").get("code").asText();
                            if ("OK".equals(statusCode)) {
                                System.out.println("✓ Game ended successfully on server");
                                // Clear the game ID to prevent further API calls
                                gameId = null;
                            } else {
                                System.err.println("✗ Game end API returned error: " + response);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("✗ Error parsing game end response: " + e.getMessage());
                    }
                },
                error -> {
                    System.err.println("✗ Failed to call game end API: " + error);
                    // Clear the game ID even on error to prevent retries
                    gameId = null;
                }
            );
        } catch (Exception e) {
            System.err.println("✗ Error preparing game end request: " + e.getMessage());
            e.printStackTrace();
            // Clear the game ID on error
            gameId = null;
        }
    }
    
    /**
     * Adds hover effects to a button.
     * 
     * @param button The button to add effects to
     * @param hoverColor The color when hovering
     * @param normalColor The normal color
     */
    private void addButtonHoverEffects(Button button, String hoverColor, String normalColor) {
        button.setOnMouseEntered(e -> 
            button.setStyle(button.getStyle().replace(normalColor, hoverColor)));
        button.setOnMouseExited(e -> 
            button.setStyle(button.getStyle().replace(hoverColor, normalColor)));
    }
    
    /**
     * Navigates to the multiplayer lobby.
     */
    private void navigateToLobby() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/lobby-view.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            URL cssUrl = getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/css/imports.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            Stage stage = (Stage) gamePane.getScene().getWindow();
            stage.setScene(scene);
            
            // Get the lobby controller and refresh AFTER scene is set
            LobbyController lobbyController = loader.getController();
            if (lobbyController != null) {
                // Use Platform.runLater to ensure UI is fully loaded before refresh
                Platform.runLater(() -> {
                    lobbyController.refreshLobby();
                    System.out.println("✓ Lobby refreshed after navigation from multiplayer game");
                });
            } else {
                System.err.println("✗ Failed to get lobby controller for refresh");
            }
            
            System.out.println("Navigated back to multiplayer lobby");
        } catch (IOException e) {
            System.err.println("Error navigating to lobby: " + e.getMessage());
            e.printStackTrace();
            // Fallback to main menu
            navigateToMainMenu();
        }
    }
    
    /**
     * Navigates to the game mode selection.
     */
    private void navigateToGameMode() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/game-mode-view.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            URL cssUrl = getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/css/imports.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            Stage stage = (Stage) gamePane.getScene().getWindow();
            stage.setScene(scene);
            
            System.out.println("Navigated back to game mode selection");
        } catch (IOException e) {
            System.err.println("Error navigating to game mode: " + e.getMessage());
            e.printStackTrace();
            // Fallback to main menu
            navigateToMainMenu();
        }
    }
    
    /**
     * Updates UNO indicators for all players based on their card count.
     */
    private void updateUnoIndicators() {
        // Use the UNO indicator manager to update indicators
        unoIndicatorManager.updateUnoIndicators(game.getPlayers());
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
            
            Stage stage = (Stage) gamePane.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Exits the game and returns to main menu with confirmation
     */
    private void exitGame() {
        try {
            // Show confirmation dialog
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION,
                "Are you sure you want to exit the game?",
                javafx.scene.control.ButtonType.YES,
                javafx.scene.control.ButtonType.NO
            );
            alert.setTitle("Exit Game");
            alert.setHeaderText(null);
            
            // Handle the user's response
            alert.showAndWait().ifPresent(response -> {
                if (response == javafx.scene.control.ButtonType.YES) {
                    // Force end the game properly
                    forceEndGame();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Forces the game to end (used for manual exits).
     */
    private void forceEndGame() {
        // Stop the game immediately
        isGameRunning = false;
        
        // Disconnect WebSocket if connected
        if (webSocketManager != null && webSocketManager.isConnected()) {
            System.out.println("Disconnecting WebSocket...");
            webSocketManager.disconnect();
        }
        
        // End the game via API if we have a game ID
        if (gameId != null) {
            // For manual exit, determine winner based on game type
            String winnerUsername;
            
            if (isMultiplayerGame) {
                // For multiplayer games, use the current user as winner (forfeit by others)
                winnerUsername = SessionManager.getInstance().getCurrentUser().getUsername();
            } else {
                // For singleplayer games, pick an AI winner to indicate player forfeit
                winnerUsername = "SystemWin"; // Neutral system win for forfeit
            }
            
            System.out.println("=== FORCE ENDING GAME ===");
            System.out.println("Game ID: " + gameId);
            System.out.println("Multiplayer: " + isMultiplayerGame);
            System.out.println("Force Winner: " + winnerUsername);
            System.out.println("========================");
            
            // Call the end game API
            callGameEndAPI(winnerUsername);
        }
        
        // Navigate immediately without waiting for API response
        navigateAfterGameEnd();
    }
    
    /**
     * Handles navigation after game ends (either natural or forced).
     */
    private void navigateAfterGameEnd() {
        Platform.runLater(() -> {
            if (isMultiplayerGame) {
                navigateToLobby();
            } else {
                navigateToMainMenu();
            }
        });
    }
    
    /**
     * Finds the game table StackPane in the center of the grid.
     * 
     * @return The game table StackPane or null if not found
     */
    private StackPane findGameTable() {
        for (javafx.scene.Node node : gamePane.getChildren()) {
            if (node instanceof GridPane) {
                GridPane gridPane = (GridPane) node;
                for (javafx.scene.Node child : gridPane.getChildren()) {
                    // Find the center stack pane
                    Integer colIndex = GridPane.getColumnIndex(child);
                    Integer rowIndex = GridPane.getRowIndex(child);
                    
                    if (colIndex != null && rowIndex != null && colIndex == 1 && rowIndex == 1) {
                        if (child instanceof StackPane) {
                            return (StackPane) child;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Initializes the cheat buttons for the human player
     */
    private void initializeCheatButtons() {
        // Skip button event handler
        skipButton.setOnAction(event -> playCheatCard(CardAction.SKIP));
        
        // Reverse button event handler
        reverseButton.setOnAction(event -> playCheatCard(CardAction.REVERSE));
        
        // Draw Two button event handler
        drawTwoButton.setOnAction(event -> playCheatCard(CardAction.DRAW_TWO));
        
        // Wild button event handler
        wildButton.setOnAction(event -> playCheatCard(CardAction.WILD));
        
        // Wild Draw Four button event handler
        wildDrawFourButton.setOnAction(event -> playCheatCard(CardAction.WILD_DRAW_FOUR));
        
        // Initially disable all cheat buttons - they'll be enabled during player's turn
        updateCheatButtonState(false);
    }
    
    /**
     * Updates the enabled/disabled state of the cheat buttons.
     * 
     * @param isPlayerTurn Whether it's the human player's turn
     */
    private void updateCheatButtonState(boolean isPlayerTurn) {
        skipButton.setDisable(!isPlayerTurn);
        reverseButton.setDisable(!isPlayerTurn);
        drawTwoButton.setDisable(!isPlayerTurn);
        wildButton.setDisable(!isPlayerTurn);
        wildDrawFourButton.setDisable(!isPlayerTurn);
    }
    
    /**
     * Plays a "cheat" card with the specified action.
     * This creates a virtual card and plays it as if it was in the player's hand.
     * 
     * @param action The card action to play
     */
    private void playCheatCard(CardAction action) {
        if (!isGameRunning || game == null) {
            return;
        }
        
        // Make sure it's the player's turn
        if (game.getCurrentPlayerIndex() != 0) {
            showCardUnplayableMessage("It's not your turn.");
            return;
        }
        
        System.out.println("=== CHEAT BUTTON PRESSED: " + action + " ===");
        
        // Check if this is the first play (no cards on discard pile yet)
        boolean isFirstPlay = game.isDiscardPileEmpty();
        System.out.println("Is first play: " + isFirstPlay);
        
        // Get the current color from the game - use RED as fallback if it's null
        CardColor currentColor = game.getCurrentColor();
        System.out.println("Current color before: " + currentColor);
        
        if (currentColor == null || isFirstPlay) {
            currentColor = CardColor.RED; // Default to RED if no color is set
            System.out.println("Using default RED color");
        }
        
        System.out.println("Final color for card: " + currentColor);
        
        // Create a card of the appropriate type
        Card card;
        
        // Special handling for Wild Draw Four - check if player has matching color cards
        if (action == CardAction.WILD_DRAW_FOUR) {
            Player player = game.getCurrentPlayer();
            boolean hasMatchingColorCard = false;

            if (isFirstPlay) {
                showCardUnplayableMessage("You can't play a Wild Draw Four on the first turn.");
                return;
            }
            
            // Only check if there's a discard pile with a color
            if (currentColor != null && currentColor != CardColor.MULTI) {
                // Check player's hand for matching color cards
                for (Card handCard : player.getHand()) {
                    if (handCard.getColor() == currentColor) {
                        hasMatchingColorCard = true;
                        break;
                    }
                }
            }
            
            // If player has matching color cards, they can't play Wild Draw Four
            if (hasMatchingColorCard) {
                System.out.println("Player has matching color cards - Wild Draw Four not allowed");
                showCardUnplayableMessage("You can't play a Wild Draw Four when you have cards matching the current color.");
                return;
            }
        }
        
        if (action == CardAction.WILD || action == CardAction.WILD_DRAW_FOUR) {
            // Wild cards always use MULTI color initially
            card = new Card(CardColor.MULTI, action);
            System.out.println("Created wild card: " + card);
            
            // For wild cards, handle color selection
            handleWildCardColorSelection(card, () -> {
                System.out.println("Color selected for wild card: " + game.getCurrentColor());
                
                // Set the first card played flag for initial card
                if (isFirstPlay) {
                    firstCardPlayed = true;
                    System.out.println("Setting firstCardPlayed = true");
                }
                
                // Add the card to player's hand temporarily (so that game logic works)
                Player player = game.getCurrentPlayer();
                player.addCard(card);
                System.out.println("Added card to player's hand: " + player.getHand());
                
                // Mark the card as playable to bypass the rule check
                card.setPlayable(true);
                System.out.println("Marked card as playable: " + card.isPlayable());
                
                // Play the card using the main finishCardPlay function
                StackPane cardView = CardRenderer.createCardView(card);
                System.out.println("Created card view for: " + card);
                finishCardPlay(cardView, card);
            });
        } else {
            // Non-wild cards use the current color
            card = new Card(currentColor, action);
            System.out.println("Created regular card: " + card);
            
            // Set the first card played flag for initial card
            if (isFirstPlay) {
                firstCardPlayed = true;
                System.out.println("Setting firstCardPlayed = true");
            }
            
            // Add the card to player's hand temporarily (so that game logic works)
            Player player = game.getCurrentPlayer();
            player.addCard(card);
            System.out.println("Added card to player's hand: " + player.getHand());
            
            // Mark the card as playable to bypass the rule check
            card.setPlayable(true);
            System.out.println("Marked card as playable: " + card.isPlayable());
            
            // Create a visual representation of the card
            StackPane cardView = CardRenderer.createCardView(card);
            System.out.println("Created card view for: " + card);
            
            // Play the card using the main card play functionality
            finishCardPlay(cardView, card);
        }
    }
    
    /**
     * Initializes a multiplayer game with existing game ID and type.
     * This is called when joining a game from the lobby.
     * 
     * @param joinedGameId The ID of the game that was joined
     * @param joinedGameType The type of the game (TWO_PLAYER, THREE_PLAYER, FOUR_PLAYER)
     */
    public void initializeMultiplayerGame(Integer joinedGameId, String joinedGameType) {
        // Set multiplayer game metadata
        this.gameId = joinedGameId;
        this.gameType = joinedGameType;
        this.isMultiplayerGame = true;
        this.gameStartTime = java.time.Instant.now().toString();
        
        System.out.println("=== INITIALIZING MULTIPLAYER GAME ===");
        System.out.println("Joined Game ID: " + joinedGameId);
        System.out.println("Game Type: " + joinedGameType);
        System.out.println("=====================================");
        
        // For multiplayer games, we DON'T create AI players
        // Instead, we wait for real players to join via WebSocket
        // For now, just show a waiting screen or lobby-like interface
        
        // Initialize basic game state without AI players
        initializeMultiplayerGameState(joinedGameType);
        
        System.out.println("Multiplayer game initialized - waiting for other players to join!");
    }
    
    /**
     * Initializes the basic multiplayer game state without AI players.
     * 
     * @param gameType The type of game (TWO_PLAYER, THREE_PLAYER, FOUR_PLAYER)
     */
    private void initializeMultiplayerGameState(String gameType) {
        // Create PlayerCount enum
        PlayerCount playerCount = switch (gameType) {
            case "TWO_PLAYER" -> PlayerCount.TWO;
            case "THREE_PLAYER" -> PlayerCount.THREE;
            case "FOUR_PLAYER" -> PlayerCount.FOUR;
            default -> PlayerCount.TWO;
        };

        // Create a new game instance for multiplayer (no AI players)
        this.game = new Game(GameMode.MULTIPLAYER, playerCount);
        
        // Add only the human player
        String username = SessionManager.getInstance().isLoggedIn() ? 
                          SessionManager.getInstance().getCurrentUser().getUsername() : 
                          "Player";
        Player humanPlayer = new Player(username);
        game.addPlayer(humanPlayer);
        
        // Set player name in UI
        bottomPlayerNameLabel.setText(username);
        
        // Initialize sub-controllers
        notificationManager = new NotificationManager(gamePane);
        unoIndicatorManager = new UnoIndicatorManager(
            topPlayerNameLabel,
            leftPlayerNameLabel,
            rightPlayerNameLabel,
            bottomPlayerNameLabel
        );
        
        gameTableController = new GameTableController(
            gamePane,
            topPlayerArea,
            leftPlayerArea,
            rightPlayerArea,
            bottomPlayerArea
        );
        
        cardAnimationController = new CardAnimationController(gamePane, discardPileContainer);
        
        // Set up multiplayer waiting state
        setupMultiplayerWaitingState();
    }
    
    /**
     * Sets up the UI for waiting for other players in multiplayer.
     */
    private void setupMultiplayerWaitingState() {
        // Hide all opponent areas initially
        topPlayerArea.setVisible(false);
        leftPlayerArea.setVisible(false);
        rightPlayerArea.setVisible(false);
        
        // Show waiting message
        currentTurnLabel.setText("WAITING FOR PLAYERS...");
        currentTurnLabel.setStyle("-fx-background-color: rgba(255, 193, 7, 0.9); -fx-font-size: 20px; -fx-font-weight: bold; -fx-border-color: white; -fx-border-width: 2px; -fx-border-radius: 20px;");
        
        // Clear game areas
        bottomPlayerCardsContainer.getChildren().clear();
        drawPileContainer.getChildren().clear();
        discardPileContainer.getChildren().clear();
        
        // Set up WebSocket callback for multiplayer events
        webSocketManager.setCallback(new WebSocketManager.WebSocketCallback() {
            @Override
            public void onConnected() {
                System.out.println("✓ WebSocket connected for multiplayer game");
                Platform.runLater(() -> {
                    currentTurnLabel.setText("CONNECTED - WAITING FOR PLAYERS...");
                    notificationManager.showActionNotification("", "Connected to multiplayer server!");
                });
            }
            
            @Override
            public void onDisconnected() {
                System.out.println("✗ WebSocket disconnected from multiplayer game");
                Platform.runLater(() -> {
                    currentTurnLabel.setText("DISCONNECTED - RECONNECTING...");
                    notificationManager.showActionNotification("", "Disconnected from server. Trying to reconnect...");
                });
            }
            
            @Override
            public void onGameStateReceived(String gameState) {
                System.out.println("📊 GAME STATE UPDATE:");
                System.out.println(gameState);
                
                // For now, just show the received game state in notifications
                Platform.runLater(() -> {
                    notificationManager.showActionNotification("", "Game state updated (see console)");
                });
                
                // TODO: Parse game state and update UI accordingly
            }
            
            @Override
            public void onPlayerJoined(String playerName) {
                System.out.println("👤 Player joined: " + playerName);
                Platform.runLater(() -> {
                    notificationManager.showActionNotification("", playerName + " joined the game!");
                    // TODO: Update UI to show the new player
                });
            }
            
            @Override
            public void onPlayerLeft(String playerName) {
                System.out.println("👤 Player left: " + playerName);
                Platform.runLater(() -> {
                    notificationManager.showActionNotification("", playerName + " left the game.");
                    // TODO: Update UI to remove the player
                });
            }
            
            @Override
            public void onGameMove(String moveData) {
                System.out.println("🎯 Game move received:");
                System.out.println(moveData);
                Platform.runLater(() -> {
                    notificationManager.showActionNotification("", "Player made a move (see console)");
                    // TODO: Parse and apply the move to the game
                });
            }
            
            @Override
            public void onError(String error) {
                System.err.println("❌ WebSocket error: " + error);
                Platform.runLater(() -> {
                    notificationManager.showActionNotification("", "Connection error: " + error);
                });
            }
        });
        
        // Connect to WebSocket if we have a game ID
        if (gameId != null) {
            String username = SessionManager.getInstance().getCurrentUser().getUsername();
            
            System.out.println("=== CONNECTING TO WEBSOCKET ===");
            System.out.println("Game ID: " + gameId);
            System.out.println("Username: " + username);
            System.out.println("===============================");
            
            // Connect in a separate thread to avoid blocking UI
            new Thread(() -> {
                boolean connected = webSocketManager.connectAndJoinGame(gameId, username);
                if (!connected) {
                    Platform.runLater(() -> {
                        notificationManager.showActionNotification("", "Failed to connect to multiplayer server!");
                        currentTurnLabel.setText("CONNECTION FAILED");
                    });
                }
            }).start();
        } else {
            System.err.println("Cannot connect to WebSocket: No game ID available");
            notificationManager.showActionNotification("", "Error: No game ID for multiplayer connection");
        }
        
        System.out.println("Multiplayer waiting state initialized with WebSocket");
    }
} 