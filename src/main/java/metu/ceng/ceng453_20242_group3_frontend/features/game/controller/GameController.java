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
    
    // Card colors for legacy code - will be removed in future refactoring
    private final Color RED_COLOR = Color.rgb(227, 35, 45);
    private final Color GREEN_COLOR = Color.rgb(67, 176, 71);
    private final Color BLUE_COLOR = Color.rgb(0, 122, 193);
    private final Color YELLOW_COLOR = Color.rgb(243, 206, 37);
    
    // Action card icons
    private final String SKIP_ICON = "⊘";
    private final String REVERSE_ICON = "↺";
    private final String DRAW_TWO_ICON = "+2";
    private final String WILD_ICON = "★";
    private final String WILD_DRAW_FOUR_ICON = "+4";
    
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
        PlayerCount playerCount;
        switch (aiPlayerCount + 1) { // +1 for human player
            case 2:
                playerCount = PlayerCount.TWO;
                break;
            case 3:
                playerCount = PlayerCount.THREE;
                break;
            case 4:
                playerCount = PlayerCount.FOUR;
                break;
            default:
                playerCount = PlayerCount.TWO; // Default
        }
        
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
                displayOpponentCards(1, topPlayerCardsContainer, false, 0);
                
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
        
        // Set up discard pile if a card has been played
        if (firstCardPlayed) {
            setupDiscardPile();
        }
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
     * Sets up the draw pile in the UI
     */
    private void setupDrawPile() {
        // Clear existing content
        drawPileContainer.getChildren().clear();
        
        // Create card back for draw pile
        StackPane cardView = createCardBackView();
        drawPileContainer.getChildren().add(cardView);
        
        // Add draw pile label
        Label pileLabel = new Label("DRAW PILE");
        pileLabel.getStyleClass().add("card-pile-label");
        drawPileContainer.getChildren().add(pileLabel);
        
        // Add click handler for drawing cards
        cardView.setOnMouseClicked(event -> {
            // First click after game start should place initial card on discard pile
            if (!firstCardPlayed) {
                startFirstCardPlay();
            } else {
                drawCard();
            }
        });
    }
    
    /**
     * Places the first card in the discard pile to start the game
     */
    private void startFirstCardPlay() {
        Card initialCard = game.startFirstTurn();
        
        if (initialCard != null) {
            firstCardPlayed = true;
            
            // Update discard pile view
            setupDiscardPile();
            
            // Update playable cards
            updatePlayableCards();
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
     * Sets up the discard pile in the UI
     */
    private void setupDiscardPile() {
        // Clear existing content
        discardPileContainer.getChildren().clear();
        
        // Get the top card from the discard pile
        Card topCard = game.getDiscardPile().peekCard();
        
        if (topCard != null) {
            // Create and add the top card view
            StackPane cardView = createCardView(topCard);
            discardPileContainer.getChildren().add(cardView);
        } else {
            // Create an empty discard pile placeholder when no cards are present
            StackPane emptyPile = new StackPane();
            emptyPile.setPrefSize(80, 120);
            
            Rectangle emptyRect = new Rectangle(80, 120);
            emptyRect.setArcWidth(15);
            emptyRect.setArcHeight(15);
            emptyRect.setFill(Color.rgb(0, 50, 0, 0.5));
            emptyRect.setStroke(Color.WHITE);
            emptyRect.setStrokeWidth(2);
            emptyRect.getStrokeDashArray().addAll(5.0, 5.0);
            
            Label emptyLabel = new Label("EMPTY");
            emptyLabel.setTextFill(Color.WHITE);
            
            emptyPile.getChildren().addAll(emptyRect, emptyLabel);
            discardPileContainer.getChildren().add(emptyPile);
        }
        
        // Add discard pile label
        Label pileLabel = new Label("DISCARD PILE");
        pileLabel.getStyleClass().add("card-pile-label");
        discardPileContainer.getChildren().add(pileLabel);
    }
    
    /**
     * Handles the action of drawing a card from the draw pile
     */
    private void drawCard() {
        // Only allow drawing on the player's turn
        if (game.getCurrentPlayerIndex() != 0) {
            return;
        }
        
        // Draw a card for the current player
        Card drawnCard = game.drawCardForCurrentPlayer();
        
        if (drawnCard != null) {
            // Add the card to the UI
            StackPane cardView = createCardView(drawnCard);
            cardView.setOnMouseClicked(event -> playCard(cardView, drawnCard));
            
            // Animate the card being drawn
            cardView.setTranslateY(-50);
            cardView.setOpacity(0);
            
            bottomPlayerCardsContainer.getChildren().add(cardView);
            
            javafx.animation.ParallelTransition pt = new javafx.animation.ParallelTransition(
                new javafx.animation.TranslateTransition(Duration.millis(300), cardView),
                new javafx.animation.FadeTransition(Duration.millis(300), cardView)
            );
            
            ((javafx.animation.TranslateTransition)pt.getChildren().get(0)).setToY(0);
            ((javafx.animation.FadeTransition)pt.getChildren().get(1)).setToValue(1);
            
            pt.play();
            
            // Update turn label
            updateTurnLabel();
        }
    }
    
    /**
     * Creates a visual representation of a card.
     *
     * @param card The card model to display
     * @return A StackPane containing the card visualization
     */
    private StackPane createCardView(Card card) {
        // Always create cards programmatically for consistency
        if (card.isNumberCard()) {
            return createNumberCardView(card);
        } else if (card.isWildCard()) {
            if (card.getAction() == CardAction.WILD) {
                return createWildCardView(card);
            } else {
                return createWildDrawFourCardView(card);
            }
        } else {
            // Action cards: Skip, Reverse, Draw Two
            return createActionCardView(card);
        }
    }
    
    /**
     * Creates a visual representation of a number card.
     *
     * @param card The card model to display
     * @return A StackPane containing the card visualization
     */
    private StackPane createNumberCardView(Card card) {
        StackPane cardView = new StackPane();
        cardView.setPrefSize(80, 120);
        cardView.getStyleClass().add("uno-card");
        
        Rectangle cardBase = new Rectangle(80, 120);
        cardBase.setArcWidth(15);
        cardBase.setArcHeight(15);
        
        // Determine color based on card color
        Color cardColor = getColorFromCardColor(card.getColor());
        
        cardBase.setFill(cardColor);
        cardBase.setStroke(Color.WHITE);
        cardBase.setStrokeWidth(2);
        
        cardView.getChildren().add(cardBase);
        
        // Add white oval in the middle (UNO card style)
        javafx.scene.shape.Ellipse whiteEllipse = new javafx.scene.shape.Ellipse(30, 45);
        whiteEllipse.setFill(Color.WHITE);
        whiteEllipse.setRotate(30);
        
        cardView.getChildren().add(whiteEllipse);
        
        // Add card value in center
        String value = String.valueOf(card.getValue());
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");
        valueLabel.setTextFill(cardColor);
        cardView.getChildren().add(valueLabel);
        
        // Add smaller value in top-left corner
        Label topLeftLabel = new Label(value);
        topLeftLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        topLeftLabel.setTextFill(Color.WHITE);
        topLeftLabel.setTranslateX(-25);
        topLeftLabel.setTranslateY(-45);
        cardView.getChildren().add(topLeftLabel);
        
        // Add reflected value in bottom-right corner
        Label bottomRightLabel = new Label(value);
        bottomRightLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        bottomRightLabel.setTextFill(Color.WHITE);
        bottomRightLabel.setTranslateX(25);
        bottomRightLabel.setTranslateY(45);
        bottomRightLabel.setRotate(180); // Rotate to create reflection effect
        cardView.getChildren().add(bottomRightLabel);
        
        // Set appropriate style for playable cards
        if (card.isPlayable()) {
            cardView.setEffect(new javafx.scene.effect.DropShadow(15, Color.WHITE));
            cardView.setStyle("-fx-cursor: hand;");
        }
        
        return cardView;
    }
    
    /**
     * Creates a visual representation of an action card.
     *
     * @param card The card model to display
     * @return A StackPane containing the card visualization
     */
    private StackPane createActionCardView(Card card) {
        StackPane cardView = new StackPane();
        cardView.setPrefSize(80, 120);
        cardView.getStyleClass().add("uno-card");
        
        Rectangle cardBase = new Rectangle(80, 120);
        cardBase.setArcWidth(15);
        cardBase.setArcHeight(15);
        
        // Determine color based on card color
        Color cardColor = getColorFromCardColor(card.getColor());
        
        cardBase.setFill(cardColor);
        cardBase.setStroke(Color.WHITE);
        cardBase.setStrokeWidth(2);
        
        cardView.getChildren().add(cardBase);
        
        // Add white oval in the middle (UNO card style)
        javafx.scene.shape.Ellipse whiteEllipse = new javafx.scene.shape.Ellipse(30, 45);
        whiteEllipse.setFill(Color.WHITE);
        whiteEllipse.setRotate(30);
        
        cardView.getChildren().add(whiteEllipse);
        
        // Add card action name and icon
        String actionText = "";
        String actionIcon = card.getAction().getSymbol();
        
        switch (card.getAction()) {
            case SKIP:
                actionText = "SKIP";
                break;
            case REVERSE:
                actionText = "REV";
                break;
            case DRAW_TWO:
                actionText = "DRAW";
                break;
            default:
                actionText = card.getAction().name();
                break;
        }
        
        // Add icon
        Label iconLabel = new Label(actionIcon);
        iconLabel.setStyle("-fx-font-size: 38px; -fx-font-weight: bold;");
        iconLabel.setTextFill(cardColor);
        cardView.getChildren().add(iconLabel);
        
        // Add text below icon
        Label textLabel = new Label(actionText);
        textLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        textLabel.setTextFill(cardColor);
        textLabel.setTranslateY(30);
        cardView.getChildren().add(textLabel);
        
        // Add smaller icon in top-left corner
        Label topLeftLabel = new Label(actionIcon);
        topLeftLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        topLeftLabel.setTextFill(Color.WHITE);
        topLeftLabel.setTranslateX(-25);
        topLeftLabel.setTranslateY(-45);
        cardView.getChildren().add(topLeftLabel);
        
        // Add reflected icon in bottom-right corner
        Label bottomRightLabel = new Label(actionIcon);
        bottomRightLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        bottomRightLabel.setTextFill(Color.WHITE);
        bottomRightLabel.setTranslateX(25);
        bottomRightLabel.setTranslateY(45);
        bottomRightLabel.setRotate(180);
        cardView.getChildren().add(bottomRightLabel);
        
        // Set appropriate style for playable cards
        if (card.isPlayable()) {
            cardView.setEffect(new javafx.scene.effect.DropShadow(15, Color.WHITE));
            cardView.setStyle("-fx-cursor: hand;");
        }
        
        return cardView;
    }
    
    /**
     * Creates a visual representation of a wild card.
     *
     * @param card The card model to display
     * @return A StackPane containing the card visualization
     */
    private StackPane createWildCardView(Card card) {
        StackPane cardView = new StackPane();
        cardView.setPrefSize(80, 120);
        cardView.getStyleClass().add("uno-card");
        
        // Wild card - create multicolored design
        Rectangle wildCardBase = new Rectangle(80, 120);
        wildCardBase.setArcWidth(15);
        wildCardBase.setArcHeight(15);
        wildCardBase.setFill(Color.BLACK);
        
        cardView.getChildren().add(wildCardBase);
        
        // Create the wild card circle with segments
        double centerX = 40;
        double centerY = 60;
        double radius = 30;
        
        // Red segment (top-left)
        javafx.scene.shape.Arc redSegment = new javafx.scene.shape.Arc(centerX, centerY, radius, radius, 135, 90);
        redSegment.setType(javafx.scene.shape.ArcType.ROUND);
        redSegment.setFill(RED_COLOR);
        
        // Blue segment (top-right)
        javafx.scene.shape.Arc blueSegment = new javafx.scene.shape.Arc(centerX, centerY, radius, radius, 45, 90);
        blueSegment.setType(javafx.scene.shape.ArcType.ROUND);
        blueSegment.setFill(BLUE_COLOR);
        
        // Yellow segment (bottom-right)
        javafx.scene.shape.Arc yellowSegment = new javafx.scene.shape.Arc(centerX, centerY, radius, radius, -45, 90);
        yellowSegment.setType(javafx.scene.shape.ArcType.ROUND);
        yellowSegment.setFill(YELLOW_COLOR);
        
        // Green segment (bottom-left)
        javafx.scene.shape.Arc greenSegment = new javafx.scene.shape.Arc(centerX, centerY, radius, radius, 225, 90);
        greenSegment.setType(javafx.scene.shape.ArcType.ROUND);
        greenSegment.setFill(GREEN_COLOR);
        
        cardView.getChildren().addAll(redSegment, blueSegment, yellowSegment, greenSegment);
        
        // Add wild star icon
        Label iconLabel = new Label("★");
        iconLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");
        iconLabel.setTextFill(Color.WHITE);
        iconLabel.setTranslateY(-15);
        cardView.getChildren().add(iconLabel);
        
        // Add "WILD" text
        Label wildLabel = new Label("WILD");
        wildLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        wildLabel.setTextFill(Color.WHITE);
        wildLabel.setTranslateY(20);
        cardView.getChildren().add(wildLabel);
        
        // Add "W" in top-left corner
        Label topLeftLabel = new Label("W");
        topLeftLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        topLeftLabel.setTextFill(Color.WHITE);
        topLeftLabel.setTranslateX(-25);
        topLeftLabel.setTranslateY(-45);
        cardView.getChildren().add(topLeftLabel);
        
        // Add reflected "W" in bottom-right corner
        Label bottomRightLabel = new Label("W");
        bottomRightLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        bottomRightLabel.setTextFill(Color.WHITE);
        bottomRightLabel.setTranslateX(25);
        bottomRightLabel.setTranslateY(45);
        bottomRightLabel.setRotate(180);
        cardView.getChildren().add(bottomRightLabel);
        
        // Set appropriate style for playable cards
        if (card.isPlayable()) {
            cardView.setEffect(new javafx.scene.effect.DropShadow(15, Color.WHITE));
            cardView.setStyle("-fx-cursor: hand;");
        }
        
        return cardView;
    }
    
    /**
     * Creates a visual representation of a Wild Draw Four card.
     *
     * @param card The card model to display
     * @return A StackPane containing the card visualization
     */
    private StackPane createWildDrawFourCardView(Card card) {
        StackPane cardView = new StackPane();
        cardView.setPrefSize(80, 120);
        cardView.getStyleClass().add("uno-card");
        
        // Wild card - create multicolored design
        Rectangle wildCardBase = new Rectangle(80, 120);
        wildCardBase.setArcWidth(15);
        wildCardBase.setArcHeight(15);
        wildCardBase.setFill(Color.BLACK);
        
        cardView.getChildren().add(wildCardBase);
        
        // Create the wild card circle with segments
        double centerX = 40;
        double centerY = 60;
        double radius = 30;
        
        // Red segment (top-left)
        javafx.scene.shape.Arc redSegment = new javafx.scene.shape.Arc(centerX, centerY, radius, radius, 135, 90);
        redSegment.setType(javafx.scene.shape.ArcType.ROUND);
        redSegment.setFill(RED_COLOR);
        
        // Blue segment (top-right)
        javafx.scene.shape.Arc blueSegment = new javafx.scene.shape.Arc(centerX, centerY, radius, radius, 45, 90);
        blueSegment.setType(javafx.scene.shape.ArcType.ROUND);
        blueSegment.setFill(BLUE_COLOR);
        
        // Yellow segment (bottom-right)
        javafx.scene.shape.Arc yellowSegment = new javafx.scene.shape.Arc(centerX, centerY, radius, radius, -45, 90);
        yellowSegment.setType(javafx.scene.shape.ArcType.ROUND);
        yellowSegment.setFill(YELLOW_COLOR);
        
        // Green segment (bottom-left)
        javafx.scene.shape.Arc greenSegment = new javafx.scene.shape.Arc(centerX, centerY, radius, radius, 225, 90);
        greenSegment.setType(javafx.scene.shape.ArcType.ROUND);
        greenSegment.setFill(GREEN_COLOR);
        
        cardView.getChildren().addAll(redSegment, blueSegment, yellowSegment, greenSegment);
        
        // Add "+4" icon
        Label iconLabel = new Label("+4");
        iconLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");
        iconLabel.setTextFill(Color.WHITE);
        iconLabel.setTranslateY(-15);
        cardView.getChildren().add(iconLabel);
        
        // Add "WILD +4" text
        Label wildLabel = new Label("WILD +4");
        wildLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        wildLabel.setTextFill(Color.WHITE);
        wildLabel.setTranslateY(20);
        cardView.getChildren().add(wildLabel);
        
        // Add "+4" in top-left corner
        Label topLeftLabel = new Label("+4");
        topLeftLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        topLeftLabel.setTextFill(Color.WHITE);
        topLeftLabel.setTranslateX(-25);
        topLeftLabel.setTranslateY(-45);
        cardView.getChildren().add(topLeftLabel);
        
        // Add reflected "+4" in bottom-right corner
        Label bottomRightLabel = new Label("+4");
        bottomRightLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        bottomRightLabel.setTextFill(Color.WHITE);
        bottomRightLabel.setTranslateX(25);
        bottomRightLabel.setTranslateY(45);
        bottomRightLabel.setRotate(180);
        cardView.getChildren().add(bottomRightLabel);
        
        // Set appropriate style for playable cards
        if (card.isPlayable()) {
            cardView.setEffect(new javafx.scene.effect.DropShadow(15, Color.WHITE));
            cardView.setStyle("-fx-cursor: hand;");
        }
        
        return cardView;
    }
    
    /**
     * Creates a card back for opponent cards.
     *
     * @return A StackPane representing the card back
     */
    private StackPane createCardBackView() {
        StackPane cardView = new StackPane();
        cardView.setPrefSize(80, 120);
        cardView.getStyleClass().add("uno-card");
        
        // Create card back directly without trying to load images
        Rectangle cardBack = new Rectangle(80, 120);
        cardBack.setArcWidth(15);
        cardBack.setArcHeight(15);
        cardBack.setFill(Color.valueOf("#D32F2F")); // UNO red for card back
        cardBack.setStroke(Color.WHITE);
        cardBack.setStrokeWidth(3);
        cardView.getChildren().add(cardBack);
        
        // Create an oval for the UNO logo background
        javafx.scene.shape.Ellipse logoBackground = new javafx.scene.shape.Ellipse(35, 25);
        logoBackground.setFill(Color.WHITE);
        logoBackground.setRotate(-20);
        cardView.getChildren().add(logoBackground);
        
        // Add the UNO logo
        Label unoLabel = new Label("UNO");
        unoLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        unoLabel.setTextFill(Color.valueOf("#D32F2F")); // Match the card back color
        cardView.getChildren().add(unoLabel);
        
        // Add a border around the UNO text
        javafx.scene.shape.Ellipse logoBorder = new javafx.scene.shape.Ellipse(35, 25);
        logoBorder.setFill(Color.TRANSPARENT);
        logoBorder.setStroke(Color.BLACK);
        logoBorder.setStrokeWidth(1);
        logoBorder.setRotate(-20);
        cardView.getChildren().add(logoBorder);
        
        return cardView;
    }
    
    /**
     * Helper method to convert CardColor to JavaFX Color.
     *
     * @param cardColor The CardColor enum value
     * @return The corresponding JavaFX Color
     */
    private Color getColorFromCardColor(CardColor cardColor) {
        switch (cardColor) {
            case RED:
                return RED_COLOR;
            case GREEN:
                return GREEN_COLOR;
            case BLUE:
                return BLUE_COLOR;
            case YELLOW:
                return YELLOW_COLOR;
            case MULTI:
                return Color.BLACK;
            default:
                return Color.BLACK;
        }
    }
    
    /**
     * Handles a card being played by the user
     *
     * @param cardView The card view that was clicked
     * @param card The card model that was played
     */
    private void playCard(StackPane cardView, Card card) {
        // Check if a card has been placed on the discard pile to start the game
        if (!firstCardPlayed) {
            startFirstCardPlay();
            return;
        }
        
        // Check if it's the player's turn
        if (game.getCurrentPlayerIndex() != 0) {
            return;
        }
        
        // Check if the card can be played
        Card topCard = game.getDiscardPile().peekCard();
        if (topCard != null && !card.canPlayOn(topCard)) {
            // Card cannot be played, show feedback
            javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.8);
            cardView.setEffect(glow);
            
            javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(Duration.ZERO, 
                    new javafx.animation.KeyValue(glow.levelProperty(), 0.8)),
                new javafx.animation.KeyFrame(Duration.millis(200), 
                    new javafx.animation.KeyValue(glow.levelProperty(), 0)),
                new javafx.animation.KeyFrame(Duration.millis(400), 
                    new javafx.animation.KeyValue(glow.levelProperty(), 0.8)),
                new javafx.animation.KeyFrame(Duration.millis(600), 
                    new javafx.animation.KeyValue(glow.levelProperty(), 0))
            );
            
            timeline.play();
            return;
        }
        
        // Play the card through the game model
        boolean success = game.playCard(card);
        if (!success) {
            return; // Card couldn't be played due to game rules
        }
        
        // Remove the card from the player's hand in UI
        bottomPlayerCardsContainer.getChildren().remove(cardView);
        
        // Add animation for card being played
        javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(Duration.millis(300), cardView);
        tt.setToY(-100);
        tt.setOnFinished(event -> {
            // Update the discard pile
            setupDiscardPile();
            
            // Check for game ending conditions (empty hand)
            if (game.isGameEnded() && game.getWinner() != null) {
                handleGameEnd(game.getWinner().equals(game.getPlayers().get(0))); // True if human player won
                return;
            }
            
            // Update direction indicator if a reverse card was played
            updateDirectionIndicator();
            
            // Update the turn label
            updateTurnLabel();
            
            // Handle AI turns
            handleAITurns();
        });
        tt.play();
    }
    
    /**
     * Handles AI player turns
     */
    private void handleAITurns() {
        if (game.getCurrentPlayerIndex() > 0 && game.getCurrentPlayerIndex() < game.getPlayers().size()) {
            // It's an AI player's turn
            simpleAITurn(game.getCurrentPlayerIndex() - 1);
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
     * Updates the turn label to show which player's turn it is
     */
    private void updateTurnLabel() {
        int currentPlayerIndex = game.getCurrentPlayerIndex();
        List<Player> players = game.getPlayers();
        
        if (currentPlayerIndex >= 0 && currentPlayerIndex < players.size()) {
            Player currentPlayer = players.get(currentPlayerIndex);
            
            if (currentPlayerIndex == 0) {
                // Local player's turn
                currentTurnLabel.setText("YOUR TURN");
                currentTurnLabel.setStyle("-fx-background-color: rgba(0, 153, 51, 0.8);"); // Green for player
            } else {
                // AI player's turn
                currentTurnLabel.setText(currentPlayer.getName() + "'S TURN");
                currentTurnLabel.setStyle("-fx-background-color: rgba(204, 51, 0, 0.8);"); // Red for opponent
            }
        }
    }
    
    /**
     * Simulates an AI player's turn with a delay
     * 
     * @param aiIndex The index of the AI player
     */
    private void simpleAITurn(int aiIndex) {
        if (aiIndex < 0 || aiIndex >= aiPlayers.size()) {
            return;
        }
        
        ComputerAIPlayer ai = aiPlayers.get(aiIndex);
        
        // Get the AI thinking time
        int thinkTime = ai.takeTurn();
        
        // Use JavaFX animation to delay the turn
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(thinkTime));
        pause.setOnFinished(event -> {
            // AI completes turn
            ai.finishTurn();
            
            // Advance to next player
            nextTurn();
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
     * Sets up subtle animations for the game table to enhance visual appeal
     */
    private void setupGameTableAnimations() {
        // Find the game table element
        javafx.scene.Node gameTable = null;
        
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
                                    gameTable = tableCandidate;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Create pulsing animation for the game table
        if (gameTable != null) {
            javafx.scene.Node finalGameTable = gameTable;
            
            // Create a pulsing glow effect
            javafx.animation.Timeline pulseAnimation = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.ZERO, 
                    new javafx.animation.KeyValue(
                        finalGameTable.effectProperty(),
                        new javafx.scene.effect.DropShadow(15, javafx.scene.paint.Color.rgb(0, 0, 0, 0.7))
                    )
                ),
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1.5), 
                    new javafx.animation.KeyValue(
                        finalGameTable.effectProperty(),
                        new javafx.scene.effect.DropShadow(25, javafx.scene.paint.Color.rgb(255, 255, 255, 0.3))
                    )
                ),
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(3), 
                    new javafx.animation.KeyValue(
                        finalGameTable.effectProperty(),
                        new javafx.scene.effect.DropShadow(15, javafx.scene.paint.Color.rgb(0, 0, 0, 0.7))
                    )
                )
            );
            
            pulseAnimation.setCycleCount(javafx.animation.Animation.INDEFINITE);
            pulseAnimation.play();
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