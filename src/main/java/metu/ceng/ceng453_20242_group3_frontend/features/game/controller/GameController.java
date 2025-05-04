package metu.ceng.ceng453_20242_group3_frontend.features.game.controller;

import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
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
    
    // Sub-controllers
    private NotificationManager notificationManager;
    private UnoIndicatorManager unoIndicatorManager;
    private GameTableController gameTableController;
    private AIPlayerController aiPlayerController;
    private CardAnimationController cardAnimationController;
    
    @FXML
    private void initialize() {
        // Set local player name from session
        if (SessionManager.getInstance().isLoggedIn()) {
            String username = SessionManager.getInstance().getCurrentUser().getUsername();
            bottomPlayerNameLabel.setText(username);
        }
        
        // Set up exit game button
        exitGameButton.setOnAction(event -> exitGame());
        
        // Initialize managers
        notificationManager = new NotificationManager(gamePane);
        unoIndicatorManager = new UnoIndicatorManager(
            topPlayerNameLabel,
            leftPlayerNameLabel,
            rightPlayerNameLabel,
            bottomPlayerNameLabel
        );
        
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

        // Create a new game instance
        this.game = new Game(
            "multiplayer".equalsIgnoreCase(gameMode) ? GameMode.MULTIPLAYER : GameMode.SINGLEPLAYER,
            playerCount
        );
        
        // FORCE the direction to be COUNTER_CLOCKWISE
        if (game.getDirection() != Direction.COUNTER_CLOCKWISE) {
            System.out.println("!!! WARNING: Game direction was not counter-clockwise, forcing it now !!!");
            game.setDirection(Direction.COUNTER_CLOCKWISE);
        }
        
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
        
        // Ensure direction indicator is updated immediately
        updateDirectionIndicator();
        
        // When a new game starts, mark all cards as playable for the initial play
        for (Player player : game.getPlayers()) {
            for (Card card : player.getHand()) {
                card.setPlayable(true);
            }
        }

        // Update the UI with initial game state
        updateUI();
        
        // Add the test button for UNO indicators
        addTestButton();
        
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
            
            // Make sure WILD_DRAW_FOUR cards are checked again
            boolean isPlayable = card.isPlayable();
            
            // Extra validation for WILD_DRAW_FOUR to prevent UI bugs
            if (isPlayable && card.getAction() == CardAction.WILD_DRAW_FOUR) {
                // Double check if player has matching color cards
                for (Card playerCard : humanPlayer.getHand()) {
                    if (playerCard != card && 
                        playerCard.getColor() == game.getCurrentColor() && 
                        playerCard.getColor() != CardColor.MULTI) {
                        
                        isPlayable = false;
                        System.out.println("Disabling WILD_DRAW_FOUR in UI - player has matching color card: " + playerCard);
                        break;
                    }
                }
            }
            
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
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION
        );
        alert.setTitle("Invalid Move");
        alert.setHeaderText("Can't Play This Card");
        alert.setContentText(message);
        alert.show();
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
                // For WILD_DRAW_FOUR, check if it's valid (should be in initial state)
                if (card.getAction() == CardAction.WILD_DRAW_FOUR) {
                    // In the initial state, WILD_DRAW_FOUR should be playable
                    handleWildCardColorSelection(card, () -> {
                        finishCardPlay(cardView, card);
                    });
                } else {
                    // Regular wild card
                    handleWildCardColorSelection(card, () -> {
                        finishCardPlay(cardView, card);
                    });
                }
            } else {
                // For regular cards, set the game color to the card's color
                if (card.getColor() != CardColor.MULTI) {
                    game.setCurrentColor(card.getColor());
                }
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
            // Check if player has cards matching the current color
            boolean hasMatchingColorCard = false;
            for (Card playerCard : currentPlayer.getHand()) {
                if (playerCard != card && 
                    playerCard.getColor() == game.getCurrentColor() && 
                    playerCard.getColor() != CardColor.MULTI) {
                    
                    hasMatchingColorCard = true;
                    break;
                }
            }
            
            if (hasMatchingColorCard) {
                showCardUnplayableMessage("You can't play a Wild Draw Four when you have cards matching the current color.");
                return;
            }
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
        
        boolean isClockwise = gameDirection == Direction.CLOCKWISE;
        String imagePath = isClockwise 
            ? "/images/arrow-clockwise.png" 
            : "/images/arrow-counterclockwise.png";
        
        Image directionImage = new Image(getClass().getResourceAsStream(imagePath));
        directionIndicator.setImage(directionImage);
        
        // Add pulsing effect to make the direction more noticeable
        if (gameTableAnimation != null) {
            gameTableAnimation.stop();
        }
        
        // Create a pulsing effect
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
        } else {
            // Display the appropriate opponent name based on new counterclockwise layout
            String playerName = currentPlayer.getName();
            currentTurnLabel.setText(playerName + "'S TURN");
            currentTurnLabel.setStyle("-fx-background-color: rgba(217, 83, 79, 0.9); -fx-font-size: 20px; -fx-font-weight: bold; -fx-border-color: white; -fx-border-width: 2px; -fx-border-radius: 20px;"); // Red for opponent's turn
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
            if (topCard.isWildCard() && game.getCurrentColor() != null && game.getCurrentColor() != CardColor.MULTI) {
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
                aiPlayerController.handleAITurns();
            });
            
            animation.play();
        } else {
            // If no card was drawn (e.g., draw pile is empty), still update the UI
            updateUI();
            updateDirectionIndicator();
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
     * Updates UNO indicators for all players based on their card count.
     */
    private void updateUnoIndicators() {
        // Use the UNO indicator manager to update indicators
        unoIndicatorManager.updateUnoIndicators(game.getPlayers());
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
        // Show test indicators using the UNO indicator manager
        unoIndicatorManager.showTestUnoIndicators();
        
        // Show a notification explaining the test
        notificationManager.showActionNotification("TESTING", "UNO indicators shown for all players");
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
} 