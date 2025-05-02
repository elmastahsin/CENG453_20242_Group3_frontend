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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

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
        
        // Update the UI
        updateUI();
        
        // Update direction indicator
        updateDirectionIndicator();
        
        // Update turn label
        updateTurnLabel();
        
        // Update player area animations
        updatePlayerAreaAnimations();
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
            // First click after game start should place initial card on discard pile
            if (!firstCardPlayed) {
                startFirstCardPlay();
            } else {
                drawCard();
            }
        });
        
        drawPileContainer.getChildren().add(cardBackView);
        
        // Add draw pile label
        Label pileLabel = new Label("DRAW PILE");
        pileLabel.getStyleClass().add("card-pile-label");
        drawPileContainer.getChildren().add(pileLabel);
    }
    
    /**
     * Places the first card in the discard pile to start the game
     */
    private void startFirstCardPlay() {
        Card initialCard = game.startFirstTurn();
        
        if (initialCard != null) {
            firstCardPlayed = true;
            
            // Create and animate the initial card being placed
            StackPane cardView = CardRenderer.createCardView(initialCard);
            cardView.setScaleX(0.1);
            cardView.setScaleY(0.1);
            cardView.setOpacity(0);
            
            // Clear and add to the discard pile container
            discardPileContainer.getChildren().clear();
            discardPileContainer.getChildren().add(cardView);
            
            // Add discard pile label
            Label pileLabel = new Label("DISCARD PILE");
            pileLabel.getStyleClass().add("card-pile-label");
            discardPileContainer.getChildren().add(pileLabel);
            
            // Animate the card appearing
            javafx.animation.ScaleTransition scaleX = new javafx.animation.ScaleTransition(Duration.millis(500), cardView);
            scaleX.setToX(1.0);
            
            javafx.animation.ScaleTransition scaleY = new javafx.animation.ScaleTransition(Duration.millis(500), cardView);
            scaleY.setToY(1.0);
            
            javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(Duration.millis(500), cardView);
            fade.setToValue(1.0);
            
            javafx.animation.ParallelTransition animation = new javafx.animation.ParallelTransition(scaleX, scaleY, fade);
            animation.setOnFinished(e -> {
                // Add rotation after animation completes
                cardView.setRotate(-5 + (Math.random() * 10));
                
                // Update playable cards
                updatePlayableCards();
                
                // Update the current turn indicator
                updateTurnLabel();
                
                // Update player animations
                updatePlayerAreaAnimations();
                
                // Also play AI turn if needed
                handleAITurns();
            });
            
            animation.play();
        }
    }
    
    /**
     * Updates which cards are playable based on the top discard card
     */
    private void updatePlayableCards() {
        Card topCard = game.getDiscardPile().peekCard();
        if (topCard == null) return;
        
        // Only update UI for human player (index 0)
        if (game.getCurrentPlayerIndex() == 0) {
            Player humanPlayer = game.getPlayers().get(0);
            
            // Reset all cards to non-playable
            for (Card card : humanPlayer.getHand()) {
                card.setPlayable(false);
            }
            
            // Set cards that can be played as playable
            for (Card card : humanPlayer.getHand()) {
                card.setPlayable(card.canPlayOn(topCard));
            }
            
            // Refresh the card visuals
            updatePlayerHandVisuals();
        }
    }
    
    /**
     * Updates the visual appearance of cards in the player's hand
     */
    private void updatePlayerHandVisuals() {
        bottomPlayerCardsContainer.getChildren().clear();
        
        if (game.getPlayers().isEmpty()) return;
        
        Player humanPlayer = game.getPlayers().get(0);
        for (Card card : humanPlayer.getHand()) {
            StackPane cardView = createCardView(card);
            cardView.setOnMouseClicked(event -> playCard(cardView, card));
            
            // Add glow effect to playable cards
            if (card.isPlayable() && game.getCurrentPlayerIndex() == 0) {
                cardView.setEffect(new javafx.scene.effect.DropShadow(10, Color.WHITE));
                cardView.setStyle("-fx-cursor: hand;");
            }
            
            bottomPlayerCardsContainer.getChildren().add(cardView);
        }
    }
    
    /**
     * Sets up the discard pile with the top card.
     */
    private void setupDiscardPile() {
        discardPileContainer.getChildren().clear();
        
        // Get the top card from the discard pile
        Card topCard = game.getDiscardPile().peekCard();
        
        if (topCard != null) {
            // Create card view for top card
            StackPane cardView = CardRenderer.createCardView(topCard);
            
            // Add a slight rotation for visual interest
            cardView.setRotate(-5 + (Math.random() * 10)); // Random rotation between -5 and 5 degrees
            
            // Add shadow for emphasis
            javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
            shadow.setColor(javafx.scene.paint.Color.BLACK);
            shadow.setRadius(10);
            cardView.setEffect(shadow);
            
            discardPileContainer.getChildren().add(cardView);
            
            // Log the top card for debugging
            System.out.println("Current discard top card: " + topCard);
        } else {
            // Create an empty placeholder if there's no card
            StackPane emptyPlaceholder = CardRenderer.createEmptyCardPlaceholder();
            discardPileContainer.getChildren().add(emptyPlaceholder);
            
            // Log empty discard pile for debugging
            System.out.println("Discard pile is empty");
        }
        
        // Add discard pile label
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
     * Handles playing a card from the player's hand
     *
     * @param cardView The card view in the UI
     * @param card The card model being played
     */
    private void playCard(StackPane cardView, Card card) {
        // Only allow playing cards on the player's turn
        if (game.getCurrentPlayerIndex() != 0 || !isGameRunning) {
            return;
        }
        
        // Try to play the card
        boolean playSuccessful = game.playCard(card);
        
        if (playSuccessful) {
            // Set the first card played flag
            firstCardPlayed = true;
            
            // Check if the game has ended
            if (game.isGameEnded()) {
                handleGameEnd(true); // Human player won
                return;
            }
            
            // Update the UI to show the new state
            updateUI();
            
            // Update direction indicator
            updateDirectionIndicator();
            
            // Update turn label
            updateTurnLabel();
            
            // Update player area animations
            updatePlayerAreaAnimations();
            
            // Check if it's AI's turn and handle it
            handleAITurns();
        }
    }
    
    /**
     * Handles AI turns when it's an AI player's turn
     */
    private void handleAITurns() {
        if (!isGameRunning || game == null) {
            return;
        }
        
        // If it's AI's turn, trigger AI move
        int currentPlayerIndex = game.getCurrentPlayerIndex();
        if (currentPlayerIndex > 0) { // Player at index 0 is always human
            simpleAITurn(currentPlayerIndex - 1); // -1 because AI index in aiPlayers starts from 0
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
     * Performs a simple AI turn for the specified AI player
     * 
     * @param aiIndex The index of the AI player in the aiPlayers list
     */
    private void simpleAITurn(int aiIndex) {
        if (!isGameRunning || game == null) {
            return;
        }
        
        // Wait a moment before AI plays (for better user experience)
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
        pause.setOnFinished(event -> {
            // Find the first playable card in the AI's hand
            boolean cardPlayed = false;
            Player aiPlayer = game.getCurrentPlayer();
            
            if (aiPlayer != null && aiPlayer.isAI()) {
                for (Card card : aiPlayer.getHand()) {
                    if (card.isPlayable()) {
                        game.playCard(card);
                        cardPlayed = true;
                        break;
                    }
                }
                
                // If no card can be played, draw a card
                if (!cardPlayed) {
                    game.drawCardForCurrentPlayer();
                }
                
                // Update UI
                updateUI();
                
                // Update direction indicator
                updateDirectionIndicator();
                
                // Update turn label
                updateTurnLabel();
                
                // Update player area animations
                updatePlayerAreaAnimations();
                
                // If the AI turn resulted in game end
                if (game.isGameEnded()) {
                    handleGameEnd(false); // AI player won
                    return;
                }
                
                // If the next player is also AI, continue the chain
                handleAITurns();
            }
        });
        
        pause.play();
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
} 