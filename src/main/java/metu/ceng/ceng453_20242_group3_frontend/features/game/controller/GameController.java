package metu.ceng.ceng453_20242_group3_frontend.features.game.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;

import metu.ceng.ceng453_20242_group3_frontend.config.AppConfig;
import metu.ceng.ceng453_20242_group3_frontend.features.game.model.Card;
import metu.ceng.ceng453_20242_group3_frontend.features.game.model.CardAction;
import metu.ceng.ceng453_20242_group3_frontend.features.game.model.CardColor;
import metu.ceng.ceng453_20242_group3_frontend.features.game.model.CardType;
import metu.ceng.ceng453_20242_group3_frontend.features.game.model.ComputerAIPlayer;
import metu.ceng.ceng453_20242_group3_frontend.features.game.model.Game;
import metu.ceng.ceng453_20242_group3_frontend.features.game.model.GameMode;
import metu.ceng.ceng453_20242_group3_frontend.features.game.model.Player;
import metu.ceng.ceng453_20242_group3_frontend.features.game.model.PlayerCount;
import metu.ceng.ceng453_20242_group3_frontend.features.game.model.Direction;
import metu.ceng.ceng453_20242_group3_frontend.features.common.util.SessionManager;
import metu.ceng.ceng453_20242_group3_frontend.features.game.view.CardRenderer;
import metu.ceng.ceng453_20242_group3_frontend.features.game.view.ColorSelectionDialog;
import metu.ceng.ceng453_20242_group3_frontend.features.game.view.ColorNotification;
import metu.ceng.ceng453_20242_group3_frontend.features.game.view.ActionNotification;

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
    
    // Game state using our Game model
    private Game game;
    private boolean isGameRunning = true;
    private boolean firstCardPlayed = false;
    
    // Legacy fields to be removed after full refactoring
    private List<ComputerAIPlayer> aiPlayers = new ArrayList<>();
    
    // Store player area animations for control
    private javafx.animation.Timeline[] playerAnimations;
    
    // Add a field to store the game table animation
    private javafx.animation.Timeline gameTableAnimation;
    
    @FXML
    private void initialize() {
        // Set local player name from session
        if (SessionManager.getInstance().isLoggedIn()) {
            String username = SessionManager.getInstance().getCurrentUser().getUsername();
            bottomPlayerNameLabel.setText(username);
        }
        
        // Set up exit game button
        exitGameButton.setOnAction(event -> exitGame());
        
        // Set up game table animations
        setupGameTableAnimations();
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

        // Create a new game instance
        this.game = new Game(
            "multiplayer".equalsIgnoreCase(gameMode) ? GameMode.MULTIPLAYER : GameMode.SINGLEPLAYER,
            playerCount
        );
        
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
        
        // Add AI players
        for (int i = 0; i < aiPlayerCount; i++) {
            String aiName = "Opponent " + (i+1);
            Player aiPlayer = new Player(aiName, true);
            game.addPlayer(aiPlayer);
            
            // Legacy AI players (to be removed in future)
            aiPlayers.add(new ComputerAIPlayer(aiName));
        }
        
        // Configure player areas based on player count
        setupPlayerAreas(game.getPlayerCount().getCount());
        
        // Start the game (this deals cards and initializes the discard pile)
        game.startGame();
        
        // When a new game starts, mark all cards as playable for the initial play
        for (Player player : game.getPlayers()) {
            for (Card card : player.getHand()) {
                card.setPlayable(true);
            }
        }
        
        // Set up the game table
        setupGameTableAnimations();

        // Update the UI with initial game state
        updateUI();
        
        // Add the test button for UNO indicators
        addTestButton();
        
        // Update UNO indicators based on card counts
        updateUnoIndicators();
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
        
        // Display player cards
        List<Player> players = game.getPlayers();
        if (!players.isEmpty()) {
            // Human player's cards (always the first player in our design)
            Player humanPlayer = players.get(0);
            for (Card card : humanPlayer.getHand()) {
                StackPane cardView = createCardView(card);
                cardView.setOnMouseClicked(event -> playCard(cardView, card));
                bottomPlayerCardsContainer.getChildren().add(cardView);
            }
            
            // Display AI cards (all other players)
            if (players.size() > 1) {
                // Top opponent (always exists if there are AI players)
                displayOpponentCards(1, topPlayerCardsContainer, false, 180);
                
                // Left opponent (if there are at least 3 players total)
                if (players.size() > 2) {
                    displayOpponentCards(2, leftPlayerCardsContainer, true, 90);
                }
                
                // Right opponent (if there are 4 players total)
                if (players.size() > 3) {
                    displayOpponentCards(3, rightPlayerCardsContainer, true, -90);
                }
            }
        }
        
        // Set up draw pile
        setupDrawPile();
        
        // Always set up discard pile - will show empty placeholder if no card has been played
        setupDiscardPile();
        
        // Update UNO indicators for all players
        updateUnoIndicators();

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
        
        Player humanPlayer = game.getPlayers().get(0);
        for (Card card : humanPlayer.getHand()) {
            StackPane cardView = createCardView(card);
            
            // Only enable click for playable cards
            if (card.isPlayable() && game.getCurrentPlayerIndex() == 0) {
                // Add glow effect to playable cards
                cardView.setEffect(new javafx.scene.effect.DropShadow(15, Color.GOLD));
                cardView.setStyle("-fx-cursor: hand;");
                cardView.setOnMouseClicked(event -> playCard(cardView, card));
            } else {
                // For unplayable cards, set a dimmed appearance
                cardView.setOpacity(0.8);
                // Still allow clicking but will show error message if attempted
                cardView.setOnMouseClicked(event -> playCard(cardView, card));
            }
            
            bottomPlayerCardsContainer.getChildren().add(cardView);
        }
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
            return;
        }
        
        // In the initial state (discard pile empty), all cards are playable
        if (game.isDiscardPileEmpty()) {
            System.out.println("Playing first card in the game");
            // Set the first card played flag (for initial card)
            firstCardPlayed = true;
            
            // For wild cards, handle color selection
            if (card.isWildCard()) {
                handleWildCardColorSelection(card, () -> {
                    finishCardPlay(cardView, card);
                });
            } else {
                // For regular cards, set the game color to the card's color
                if (card.getColor() != CardColor.MULTI) {
                    game.setCurrentColor(card.getColor());
                }
                finishCardPlay(cardView, card);
            }
            return;
        }
        
        // Check if the card is playable according to UNO rules
        if (!card.isPlayable()) {
            // Show a message that this card can't be played
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION
            );
            alert.setTitle("Invalid Move");
            alert.setHeaderText("Can't Play This Card");
            alert.setContentText("This card doesn't match the color or value of the top card.");
            alert.show();
            return;
        }
        
        // Set the first card played flag (for initial card)
        firstCardPlayed = true;
        
        // Check for wild cards that need color selection
        if (card.isWildCard()) {
            handleWildCardColorSelection(card, () -> {
                finishCardPlay(cardView, card);
            });
        } else {
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
        player.setHasCalledUno(true);
        
        // Show UNO call notification
        showUnoCallNotification(player.getName());
    }
    
    /**
     * Gets an action message based on the card type.
     * 
     * @param card The card that was played
     * @param targetPlayerName The name of the player affected by the action
     * @return A message describing the card's action or null if no notification should be shown
     */
    private String getActionMessage(Card card, String targetPlayerName) {
        // ONLY show notifications for action cards, NOT for number cards
        if (!card.isActionCard()) {
            return null; // Return null to indicate no notification should be shown
        }
        
        switch (card.getAction()) {
            case SKIP:
                return "plays " + card.getColor() + " Skip! " + targetPlayerName + "'s turn is skipped";
                
            case REVERSE:
                if (game.getPlayers().size() == 2) {
                    // In 2-player game, Reverse acts like Skip
                    return "plays " + card.getColor() + " Reverse! " + targetPlayerName + "'s turn is skipped";
                } else {
                    return "plays " + card.getColor() + " Reverse! Direction is changed";
                }
                
            case DRAW_TWO:
                return "plays " + card.getColor() + " Draw Two! " + targetPlayerName + " draws 2 cards";
                
            case WILD:
                // Color change notification is handled separately
                return null;
                
            case WILD_DRAW_FOUR:
                return "plays Wild Draw Four! " + targetPlayerName + " draws 4 cards, color is now " + game.getCurrentColor();
                
            default:
                return null; // Return null for any other cards that don't need notifications
        }
    }
    
    /**
     * Completes playing a card after any required user input (like wild card color selection).
     * 
     * @param cardView The card view to animate
     * @param card The card model to play
     */
    private void finishCardPlay(StackPane cardView, Card card) {
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
                showColorSelectionNotification(currentPlayer.getName(), game.getCurrentColor());
            }
            
            // ONLY show notifications for special actions
            String actionMessage = getActionMessage(card, nextPlayerName);
            if (actionMessage != null) {
                showActionNotification(currentPlayer.getName(), actionMessage);
            }
            
            // Check for UNO declaration when player will have 1 card left
            if (originalCardCount == 2 && currentPlayer.getCardCount() == 1) {
                // For human players, automatically declare UNO
                if (!currentPlayer.isAI()) {
                    declareUno();
                } else {
                    // AI players also automatically declare UNO
                    currentPlayer.setHasCalledUno(true);
                    showUnoCallNotification(currentPlayer.getName());
                }
            }
            
            // Now handle animation - but don't rely on the cardView which might be null after removal
            StackPane cardViewCopy = CardRenderer.createCardView(card);
            
            // Position the card view at the same position as the original card
            cardViewCopy.setLayoutX(cardView.getLayoutX());
            cardViewCopy.setLayoutY(cardView.getLayoutY());
            gamePane.getChildren().add(cardViewCopy);
            
            // Animate to discard pile
            animateCardToDiscardPile(cardViewCopy);
            
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
            handleAITurns();
        }
    }
    
    /**
     * Shows a notification about a player declaring UNO.
     * 
     * @param playerName The player's name
     */
    private void showUnoCallNotification(String playerName) {
        ActionNotification notification = ActionNotification.createUnoCallNotification(playerName);
        showNotification(notification);
    }
    
    /**
     * Shows a notification about a game action.
     * 
     * @param playerName The player's name
     * @param actionMessage The action message
     */
    private void showActionNotification(String playerName, String actionMessage) {
        ActionNotification notification = new ActionNotification(playerName, actionMessage);
        showNotification(notification);
    }
    
    /**
     * Displays a notification in the game pane.
     * 
     * @param notification The notification to display
     */
    private void showNotification(ActionNotification notification) {
        // Add the notification to the game pane
        StackPane notificationPane = notification.getNotificationPane();
        gamePane.getChildren().add(notificationPane);
        
        // Position the notification at the top center of the game pane
        notificationPane.setLayoutX((gamePane.getWidth() - notificationPane.getMaxWidth()) / 2);
        notificationPane.setLayoutY(50);
        
        // Show the notification
        notification.show();
    }
    
    /**
     * Handles AI turns when it's an AI player's turn
     */
    private void handleAITurns() {
        if (!isGameRunning || game == null) {
            return;
        }
        
        // Debug information
        System.out.println("Checking for AI turns, current player index: " + game.getCurrentPlayerIndex());
        
        // If it's AI's turn, trigger AI move with a small delay
        int currentPlayerIndex = game.getCurrentPlayerIndex();
        if (currentPlayerIndex > 0) { // Player at index 0 is always human
            // Create a delay so AI doesn't play immediately
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.millis(1000));
            pause.setOnFinished(e -> {
                int aiIndex = currentPlayerIndex;
                // Make sure it's still the same AI's turn after the delay
                if (game.getCurrentPlayerIndex() == aiIndex) {
                    System.out.println("AI player " + aiIndex + " is taking their turn");
                    simpleAITurn(aiIndex);
                }
            });
            pause.play();
        }
    }
    
    /**
     * Simulates a simple AI turn.
     *
     * @param aiIndex The index of the AI player in the game's player list
     */
    private void simpleAITurn(int aiIndex) {
        Player aiPlayer = game.getPlayers().get(aiIndex);
        
        // Make sure it's this AI's turn
        if (!game.getCurrentPlayer().equals(aiPlayer)) {
            System.out.println("Not AI player's turn anymore, skipping AI move");
            return;
        }
        
        System.out.println("AI player " + aiPlayer.getName() + " is checking for playable cards");
        
        // Update which cards are playable
        game.updatePlayableCards();
        
        // Find a playable card
        Card cardToPlay = null;
        for (Card card : aiPlayer.getHand()) {
            if (card.isPlayable()) {
                cardToPlay = card;
                System.out.println("AI will play: " + card);
                break;
            }
        }
        
        // Play the card or draw if no playable card
        if (cardToPlay != null) {
            // Get the corresponding AI instance
            ComputerAIPlayer aiInstance = aiPlayers.get(aiIndex - 1);
            
            final Card finalCardToPlay = cardToPlay; // Need final var for lambda
            
            // Handle wild card color selection
            if (cardToPlay.isWildCard()) {
                // Select a color
                CardColor selectedColor = aiInstance.selectWildCardColor();
                
                // Set the color in the game
                game.setCurrentColor(selectedColor);
                
                // Small delay before playing the card
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.millis(500));
                pause.setOnFinished(e -> {
                    playAICard(aiIndex, finalCardToPlay);
                });
                pause.play();
            } else {
                // Play non-wild card directly after a short delay
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.millis(500));
                pause.setOnFinished(e -> {
                    playAICard(aiIndex, finalCardToPlay);
                });
                pause.play();
            }
        } else {
            System.out.println("AI has no playable cards, will draw a card");
            
            // Delay before drawing
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.millis(500));
            pause.setOnFinished(e -> {
                // Draw a card
                Card drawnCard = game.drawCardForCurrentPlayer();
                System.out.println("AI drew: " + (drawnCard != null ? drawnCard.toString() : "null"));
                
                // Update the UI
                updateUI();
                updateTurnLabel();
                
                // If it's now the human player's turn, update playable cards
                if (!game.getCurrentPlayer().isAI()) {
                    updatePlayableCards();
                    
                    // Pulse the human player's area since it's their turn
                    updatePlayerAreaAnimations();
                } else {
                    // Otherwise, handle the next AI turn
                    handleAITurns();
                }
            });
            pause.play();
        }
    }
    
    /**
     * Plays a card for an AI player.
     * 
     * @param aiIndex The index of the AI player
     * @param card The card to play
     */
    private void playAICard(int aiIndex, Card card) {
        Player aiPlayer = game.getPlayers().get(aiIndex);
        
        // Make sure it's still this AI's turn
        if (!game.getCurrentPlayer().equals(aiPlayer)) {
            System.out.println("Not AI player's turn anymore, skipping card play");
            return;
        }
        
        int originalCardCount = aiPlayer.getCardCount();
        
        // Get the next player who will be affected by the action
        Player nextPlayer = game.getNextPlayer();
        String nextPlayerName = nextPlayer != null ? nextPlayer.getName() : "Unknown";
        
        // Play the card
        boolean success = game.playCard(card);
        System.out.println("AI played card: " + card + ", success: " + success);
        
        if (success) {
            // Show wild card color notification separately to ensure it's always displayed
            if (card.isWildCard() && game.getCurrentColor() != CardColor.MULTI) {
                showColorSelectionNotification(aiPlayer.getName(), game.getCurrentColor());
            }
            
            // ONLY show notifications for special actions
            String actionMessage = getActionMessage(card, nextPlayerName);
            if (actionMessage != null) {
                showActionNotification(aiPlayer.getName(), actionMessage);
            }
            
            // Check for UNO declaration when player will have 1 card left
            if (originalCardCount == 2 && aiPlayer.getCardCount() == 1) {
                // AI automatically declares UNO
                aiPlayer.setHasCalledUno(true);
                showUnoCallNotification(aiPlayer.getName());
            }
            
            // Update the UI
            updateUI();
            updateDirectionIndicator();
            updateTurnLabel();
            
            // Check for game over
            if (game.isGameEnded()) {
                handleGameEnd(false);
                return;
            }
            
            // If there are more AI turns, handle them
            if (game.getCurrentPlayer().isAI()) {
                handleAITurns();
            } else {
                // Otherwise, update which cards are playable for the human player
                updatePlayableCards();
                
                // Pulse the human player's area since it's their turn
                updatePlayerAreaAnimations();
            }
        } else {
            System.out.println("AI failed to play card: " + card);
            
            // If play failed (which shouldn't happen), try drawing instead
            Card drawnCard = game.drawCardForCurrentPlayer();
            updateUI();
            updateTurnLabel();
            
            // Continue game flow
            if (game.getCurrentPlayer().isAI()) {
                handleAITurns();
            } else {
                updatePlayableCards();
                updatePlayerAreaAnimations();
            }
        }
    }
    
    /**
     * Updates the direction indicator based on the game's direction
     */
    private void updateDirectionIndicator() {
        boolean isClockwise = game.getDirection() == Direction.CLOCKWISE;
        String imagePath = isClockwise 
            ? "/images/arrow-clockwise.png" 
            : "/images/arrow-counterclockwise.png";
        
        Image directionImage = new Image(getClass().getResourceAsStream(imagePath));
        directionIndicator.setImage(directionImage);
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
            currentTurnLabel.setStyle("-fx-background-color: rgba(0, 153, 51, 0.8);"); // Green for player's turn
        } else {
            currentTurnLabel.setText(currentPlayer.getName() + "'S TURN");
            currentTurnLabel.setStyle("-fx-background-color: rgba(217, 83, 79, 0.8);"); // Red for opponent's turn
        }
        
        // Update which player area is pulsing
        updatePlayerAreaAnimations();
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
            if (topCard.isWildCard() && game.getCurrentColor() != null && game.getCurrentColor() != CardColor.MULTI) {
                if (topCard.getAction() == CardAction.WILD) {
                    cardView = CardRenderer.createWildCardWithSelectedColor(topCard, game.getCurrentColor());
                } else {
                    cardView = CardRenderer.createWildDrawFourWithSelectedColor(topCard, game.getCurrentColor());
                }
                
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
        
        // Draw a card for the current player
        Card drawnCard = game.drawCardForCurrentPlayer();
        
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
                // Update the full UI after animation completes
                updateUI();
                
                // Update direction indicator
                updateDirectionIndicator();
                
                // Update turn label
                updateTurnLabel();
                
                // Check if an automatic move is needed for AI
                handleAITurns();
            });
            
            animation.play();
        } else {
            // If no card was drawn (e.g., draw pile is empty), still update the UI
            updateUI();
            updateDirectionIndicator();
            updateTurnLabel();
            handleAITurns();
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
     * Advances to the next player's turn
     */
    private void nextTurn() {
        if (!isGameRunning) {
            return;
        }
        
        // Update current player index by calling game.nextPlayer() method
        game.nextPlayer();
        
        // Update the turn label
        updateTurnLabel();
        
        // If it's AI's turn, simulate AI move
        if (game.getCurrentPlayerIndex() != 0) {
            simpleAITurn(game.getCurrentPlayerIndex() - 1);
        }
    }
    
    /**
     * Handles the end of the game
     * 
     * @param isPlayerWinner Whether the local player won the game
     */
    private void handleGameEnd(boolean isPlayerWinner) {
        isGameRunning = false;
        
        // Use Platform.runLater to show the dialog after animation completes
        Platform.runLater(() -> {
            // Create alert dialog
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION
            );
            
            alert.setTitle("Game Over");
            
            if (isPlayerWinner) {
                alert.setHeaderText("You Win!");
                alert.setContentText("Congratulations, you have won the game!");
            } else {
                alert.setHeaderText("You Lose!");
                alert.setContentText("Better luck next time!");
            }
            
            // Show dialog and return to main menu when closed
            alert.showAndWait().ifPresent(response -> exitGame());
        });
    }
    
    /**
     * Sets up subtle animations for the game players areas
     */
    private void setupGameTableAnimations() {
        // Create animation timelines for each player area
        javafx.animation.Timeline bottomPlayerAnimation = createPulseAnimation(bottomPlayerArea);
        javafx.animation.Timeline topPlayerAnimation = createPulseAnimation(topPlayerArea);
        javafx.animation.Timeline leftPlayerAnimation = createPulseAnimation(leftPlayerArea);
        javafx.animation.Timeline rightPlayerAnimation = createPulseAnimation(rightPlayerArea);
        
        // Store animations for later control
        playerAnimations = new javafx.animation.Timeline[] {
            bottomPlayerAnimation, topPlayerAnimation, leftPlayerAnimation, rightPlayerAnimation
        };
        
        // Find the game table element
        javafx.scene.Node gameTable = findGameTable();
        
        // Create the table animation if the table was found
        if (gameTable != null) {
            // Use the same animation timing as player animations for sync
            gameTableAnimation = createPulseAnimation(gameTable);
            gameTableAnimation.pause(); // Start paused
        }
    }
    
    /**
     * Finds the game table element in the scene graph
     * 
     * @return The game table node or null if not found
     */
    private javafx.scene.Node findGameTable() {
        // Look through the StackPane in the center of the grid
        for (javafx.scene.Node node : gamePane.getChildrenUnmodifiable()) {
            if (node instanceof javafx.scene.layout.GridPane) {
                javafx.scene.layout.GridPane gridPane = (javafx.scene.layout.GridPane) node;
                
                for (javafx.scene.Node child : gridPane.getChildren()) {
                    // Find the center stack pane
                    Integer colIndex = javafx.scene.layout.GridPane.getColumnIndex(child);
                    Integer rowIndex = javafx.scene.layout.GridPane.getRowIndex(child);
                    
                    if (colIndex != null && rowIndex != null && colIndex == 1 && rowIndex == 1) {
                        if (child instanceof javafx.scene.layout.StackPane) {
                            javafx.scene.layout.StackPane centerPane = (javafx.scene.layout.StackPane) child;
                            
                            // Find the game table inside the stack pane
                            for (javafx.scene.Node tableCandidate : centerPane.getChildren()) {
                                if (tableCandidate instanceof javafx.scene.layout.StackPane &&
                                    tableCandidate.getStyleClass().contains("game-table")) {
                                    return tableCandidate;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Creates a pulsing animation for a node
     * 
     * @param node The node to animate
     * @return The animation timeline
     */
    private javafx.animation.Timeline createPulseAnimation(javafx.scene.Node node) {
        // Gold color for the glow effect
        javafx.scene.paint.Color glowColor = javafx.scene.paint.Color.rgb(255, 215, 0, 0.7);
        
        // Create a pulsing glow effect with consistent timing for all elements
        javafx.animation.Timeline pulseAnimation = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.ZERO, 
                new javafx.animation.KeyValue(
                    node.effectProperty(),
                    new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.rgb(0, 0, 0, 0.7))
                )
            ),
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1.0), 
                new javafx.animation.KeyValue(
                    node.effectProperty(),
                    new javafx.scene.effect.DropShadow(20, glowColor)
                )
            ),
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(2.0), 
                new javafx.animation.KeyValue(
                    node.effectProperty(),
                    new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.rgb(0, 0, 0, 0.7))
                )
            )
        );
        
        pulseAnimation.setCycleCount(javafx.animation.Animation.INDEFINITE);
        // Start paused - will be activated only for current player
        pulseAnimation.pause();
        
        return pulseAnimation;
    }
    
    /**
     * Updates which player area is pulsing based on the current player.
     */
    private void updatePlayerAreaAnimations() {
        if (playerAnimations == null || game == null) {
            return;
        }
        
        // Stop all animations
        for (javafx.animation.Timeline animation : playerAnimations) {
            animation.pause();
        }
        
        // Stop game table animation
        if (gameTableAnimation != null) {
            gameTableAnimation.pause();
        }
        
        // Set basic shadow for all player areas
        bottomPlayerArea.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.rgb(0, 0, 0, 0.7)));
        topPlayerArea.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.rgb(0, 0, 0, 0.7)));
        leftPlayerArea.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.rgb(0, 0, 0, 0.7)));
        rightPlayerArea.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.rgb(0, 0, 0, 0.7)));
        
        // Start the animation for the current player's area
        int currentPlayerIndex = game.getCurrentPlayerIndex();
        if (currentPlayerIndex >= 0 && currentPlayerIndex < playerAnimations.length) {
            playerAnimations[currentPlayerIndex].play();
            
            // Also pulse the game table if it's the human player's turn (index 0)
            if (currentPlayerIndex == 0 && gameTableAnimation != null) {
                gameTableAnimation.play();
            }
        }
    }
    
    /**
     * Shows a notification about an AI player's color selection.
     * Only shown for wild cards.
     * 
     * @param playerName The AI player's name
     * @param selectedColor The selected color
     */
    private void showColorSelectionNotification(String playerName, CardColor selectedColor) {
        // Use the dedicated method for color change notifications
        ActionNotification notification = ActionNotification.createColorChangeNotification(playerName, selectedColor);
        showNotification(notification);
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
                    navigateToMainMenu();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Sets up the player areas based on total player count.
     * 
     * @param playerCount The total number of players (2-4)
     */
    private void setupPlayerAreas(int playerCount) {
        // Configure visibility based on player count
        switch (playerCount) {
            case 2: // 2 players: bottom and top
                topPlayerArea.setVisible(true);
                leftPlayerArea.setVisible(false);
                rightPlayerArea.setVisible(false);
                
                if (!aiPlayers.isEmpty()) {
                    topPlayerNameLabel.setText(aiPlayers.get(0).getName());
                } else {
                    topPlayerNameLabel.setText("Opponent 1");
                }
                break;
                
            case 3: // 3 players: bottom, top, and left
                topPlayerArea.setVisible(true);
                leftPlayerArea.setVisible(true);
                rightPlayerArea.setVisible(false);
                
                if (aiPlayers.size() >= 2) {
                    topPlayerNameLabel.setText(aiPlayers.get(0).getName());
                    leftPlayerNameLabel.setText(aiPlayers.get(1).getName());
                } else {
                    topPlayerNameLabel.setText("Opponent 1");
                    leftPlayerNameLabel.setText("Opponent 2");
                }
                break;
                
            case 4: // 4 players: all positions
                topPlayerArea.setVisible(true);
                leftPlayerArea.setVisible(true);
                rightPlayerArea.setVisible(true);
                
                if (aiPlayers.size() >= 3) {
                    topPlayerNameLabel.setText(aiPlayers.get(0).getName());
                    leftPlayerNameLabel.setText(aiPlayers.get(1).getName());
                    rightPlayerNameLabel.setText(aiPlayers.get(2).getName());
                } else {
                    topPlayerNameLabel.setText("Opponent 1");
                    leftPlayerNameLabel.setText("Opponent 2");
                    rightPlayerNameLabel.setText("Opponent 3");
                }
                break;
                
            default: // Default to 2 players if invalid count
                topPlayerArea.setVisible(true);
                leftPlayerArea.setVisible(false);
                rightPlayerArea.setVisible(false);
                
                topPlayerNameLabel.setText("Opponent 1");
                break;
        }
        
        // For grid layout - must keep areas managed but transparent
        leftPlayerArea.setManaged(true);
        rightPlayerArea.setManaged(true);
        
        // Make invisible areas transparent instead of completely hiding them
        if (!leftPlayerArea.isVisible()) {
            leftPlayerArea.setOpacity(0);
            leftPlayerNameLabel.setOpacity(0);
        } else {
            leftPlayerArea.setOpacity(1);
            leftPlayerNameLabel.setOpacity(1);
        }
        
        if (!rightPlayerArea.isVisible()) {
            rightPlayerArea.setOpacity(0);
            rightPlayerNameLabel.setOpacity(0);
        } else {
            rightPlayerArea.setOpacity(1);
            rightPlayerNameLabel.setOpacity(1);
        }
        
        // Ensure all labels are clearly visible
        topPlayerNameLabel.toFront();
        leftPlayerNameLabel.toFront();
        rightPlayerNameLabel.toFront();
        bottomPlayerNameLabel.toFront();
    }
    
    /**
     * Animates a card moving to the discard pile.
     * 
     * @param cardView The card view to animate
     */
    private void animateCardToDiscardPile(StackPane cardView) {
        // Calculate the target position (discard pile's center)
        double targetX = discardPileContainer.localToScene(discardPileContainer.getBoundsInLocal()).getCenterX() - 
                        gamePane.localToScene(gamePane.getBoundsInLocal()).getMinX() - 40; // Half of card width
        double targetY = discardPileContainer.localToScene(discardPileContainer.getBoundsInLocal()).getCenterY() - 
                        gamePane.localToScene(gamePane.getBoundsInLocal()).getMinY() - 60; // Half of card height
        
        // Create the animations
        javafx.animation.TranslateTransition moveCard = new javafx.animation.TranslateTransition(Duration.millis(300), cardView);
        moveCard.setToX(targetX - cardView.getLayoutX());
        moveCard.setToY(targetY - cardView.getLayoutY());
        
        javafx.animation.RotateTransition rotateCard = new javafx.animation.RotateTransition(Duration.millis(300), cardView);
        rotateCard.setToAngle(-5 + (Math.random() * 10)); // Random angle for natural look
        
        // Play both animations in parallel
        javafx.animation.ParallelTransition animation = new javafx.animation.ParallelTransition(moveCard, rotateCard);
        
        // When animation completes, update the UI
        animation.setOnFinished(e -> {
            // Remove the animated card
            gamePane.getChildren().remove(cardView);
            
            // Update the discard pile
            setupDiscardPile();
        });
        
        // Start the animation
        animation.play();
    }
    
    /**
     * Updates UNO indicators for all players based on their card count.
     */
    private void updateUnoIndicators() {
        // Remove any existing UNO indicators first
        removeExistingUnoIndicators();
        
        // Check each player's card count
        List<Player> players = game.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            
            // Show UNO indicator if player has exactly one card
            if (player.getCardCount() == 1) {
                switch (i) {
                    case 0: // Human player (bottom)
                        addUnoIndicatorToLabel(bottomPlayerNameLabel);
                        break;
                    case 1: // Top opponent
                        addUnoIndicatorToLabel(topPlayerNameLabel);
                        break;
                    case 2: // Left opponent (if present)
                        addUnoIndicatorToLabel(leftPlayerNameLabel);
                        break;
                    case 3: // Right opponent (if present)
                        addUnoIndicatorToLabel(rightPlayerNameLabel);
                        break;
                }
            }
        }
    }
    
    /**
     * Removes any existing UNO indicator badges from all player name labels.
     */
    private void removeExistingUnoIndicators() {
        // Remove graphics from all name labels
        for (Label nameLabel : new Label[]{bottomPlayerNameLabel, topPlayerNameLabel, leftPlayerNameLabel, rightPlayerNameLabel}) {
            if (nameLabel != null) {
                // Remove the UNO indicator graphic
                nameLabel.setGraphic(null);
                nameLabel.setGraphicTextGap(0);
                nameLabel.setContentDisplay(ContentDisplay.LEFT);
            }
        }
    }
    
    /**
     * Adds a UNO indicator badge DIRECTLY INSIDE the player name label.
     * 
     * @param nameLabel The name label to add the indicator to
     */
    private void addUnoIndicatorToLabel(Label nameLabel) {
        if (nameLabel == null) return;
        
        // Create the UNO indicator as a StackPane
        StackPane indicator = new StackPane();
        indicator.setMaxWidth(30);
        indicator.setMaxHeight(30);
        indicator.setPrefWidth(30);
        indicator.setPrefHeight(30);
        
        // Create the background circle
        Circle badge = new Circle(15);
        badge.setFill(Color.rgb(227, 35, 45)); // UNO red
        badge.setStroke(Color.WHITE);
        badge.setStrokeWidth(2);
        
        // Create the "UNO" text
        Label unoText = new Label("UNO");
        unoText.setTextFill(Color.WHITE);
        unoText.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");
        
        // Add components to the indicator
        indicator.getChildren().addAll(badge, unoText);
        
        // Add pulsing animation
        ScaleTransition pulse = new ScaleTransition(Duration.millis(800), indicator);
        pulse.setFromX(0.8);
        pulse.setFromY(0.8);
        pulse.setToX(1.2);
        pulse.setToY(1.2);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        
        // Set the indicator as the graphic of the name label
        // Position depends on whether it's a side player or top/bottom player
        if (nameLabel == leftPlayerNameLabel) {
            nameLabel.setGraphic(indicator);
            nameLabel.setGraphicTextGap(10);
            nameLabel.setContentDisplay(ContentDisplay.RIGHT); // Add graphic to the right of text
        } else if (nameLabel == rightPlayerNameLabel) {
            nameLabel.setGraphic(indicator);
            nameLabel.setGraphicTextGap(10);
            nameLabel.setContentDisplay(ContentDisplay.LEFT); // Add graphic to the left of text
        } else { // Top or bottom player
            nameLabel.setGraphic(indicator);
            nameLabel.setGraphicTextGap(10);
            nameLabel.setContentDisplay(ContentDisplay.RIGHT); // Add graphic to the right of text
        }
        
        // Start the animation
        pulse.play();
    }
    
    /**
     * Adds a test button to trigger UNO indicator testing.
     * This method is only for development/testing purposes.
     */
    private void addTestButton() {
        Button testButton = new Button("Test UNO Indicators");
        testButton.setLayoutX(10);
        testButton.setLayoutY(10);
        testButton.setStyle("-fx-background-color: #666; -fx-text-fill: white;");
        
        // Add click handler to test UNO indicators
        testButton.setOnAction(event -> {
            testUnoIndicators();
        });
        
        // Add to game pane
        gamePane.getChildren().add(testButton);
    }
    
    /**
     * Temporarily shows UNO indicators for all players to test positioning.
     * This method is only for development/testing purposes.
     */
    private void testUnoIndicators() {
        // First remove any existing indicators
        removeExistingUnoIndicators();
        
        // Then add indicators for all player positions
        addUnoIndicatorToLabel(bottomPlayerNameLabel);
        addUnoIndicatorToLabel(topPlayerNameLabel);
        
        // Only add for side players if they're in the game
        if (game.getPlayers().size() > 2) {
            addUnoIndicatorToLabel(leftPlayerNameLabel);
        }
        
        if (game.getPlayers().size() > 3) {
            addUnoIndicatorToLabel(rightPlayerNameLabel);
        }
        
        // Show a notification explaining the test
        showActionNotification("TESTING", "UNO indicators shown for all players");
    }
} 