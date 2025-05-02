package metu.ceng.ceng453_20242_group3_frontend.controller;

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
import javafx.util.Pair;
import metu.ceng.ceng453_20242_group3_frontend.model.ComputerAIPlayer;
import metu.ceng.ceng453_20242_group3_frontend.util.SessionManager;
import javafx.util.Duration;

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
    
    // Game state
    private boolean isClockwise = true;
    private int playerCount = 2; // Default to 2 players
    private List<ComputerAIPlayer> aiPlayers = new ArrayList<>();
    private int currentPlayerIndex = 0; // 0 = local player, 1+ = AI opponents
    private boolean isGameRunning = true;
    
    // Card colors
    private final Color RED_COLOR = Color.rgb(227, 35, 45);
    private final Color GREEN_COLOR = Color.rgb(67, 176, 71);
    private final Color BLUE_COLOR = Color.rgb(0, 122, 193);
    private final Color YELLOW_COLOR = Color.rgb(243, 206, 37);
    
    @FXML
    private void initialize() {
        // Set local player name from session
        if (SessionManager.getInstance().isLoggedIn()) {
            String username = SessionManager.getInstance().getCurrentUser().getUsername();
            bottomPlayerNameLabel.setText(username);
        }
        
        // Set up exit game button
        exitGameButton.setOnAction(event -> exitGame());
        
        // Update direction indicator
        updateDirectionIndicator();
        
        // Initialize turn label
        updateTurnLabel();
        
        // Set up game table animations
        setupGameTableAnimations();
    }
    
    /**
     * Initializes the game with the specified parameters.
     * 
     * @param gameMode The game mode
     * @param aiPlayerCount Number of AI players
     * @param initialCardCount Number of initial cards for each player
     */
    public void initializeGame(String gameMode, int aiPlayerCount, int initialCardCount) {
        // Set player count (user + AI players)
        this.playerCount = aiPlayerCount + 1;
        
        // Initialize AI players
        this.aiPlayers.clear();
        for (int i = 0; i < aiPlayerCount; i++) {
            String opponentName = "Opponent " + (i+1);
            
            aiPlayers.add(new ComputerAIPlayer(opponentName));
        }
        
        // Configure player areas based on player count
        setupPlayerAreas(this.playerCount);
        
        // Generate cards
        generatePlayerCards(initialCardCount);
        generateOpponentCards(aiPlayerCount, initialCardCount);
        
        // Start with local player's turn
        this.currentPlayerIndex = 0;
        
        // Update direction indicator
        updateDirectionIndicator();
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
     * Generates cards for the local player.
     * 
     * @param cardCount Number of cards to generate
     */
    private void generatePlayerCards(int cardCount) {
        // Clear existing cards
        bottomPlayerCardsContainer.getChildren().clear();
        
        // Sample player cards - removed Wild card as requested
        List<Pair<String, Color>> playerCards = Arrays.asList(
            new Pair<>("7", BLUE_COLOR),
            new Pair<>("2", GREEN_COLOR),
            new Pair<>("4", GREEN_COLOR),
            new Pair<>("0", RED_COLOR),
            new Pair<>("2", RED_COLOR),
            new Pair<>("2", BLUE_COLOR),
            new Pair<>("5", YELLOW_COLOR)
        );
        
        // Add cards to player's hand
        for (int i = 0; i < Math.min(cardCount, playerCards.size()); i++) {
            Pair<String, Color> cardInfo = playerCards.get(i);
            StackPane card = createCard(cardInfo.getKey(), cardInfo.getValue());
            card.setOnMouseClicked(event -> playCard(card));
            bottomPlayerCardsContainer.getChildren().add(card);
        }
    }
    
    /**
     * Generates cards for all opponents.
     * 
     * @param opponentCount Number of opponents
     * @param cardCount Number of cards per opponent
     */
    private void generateOpponentCards(int opponentCount, int cardCount) {
        // Clear existing cards
        topPlayerCardsContainer.getChildren().clear();
        leftPlayerCardsContainer.getChildren().clear();
        rightPlayerCardsContainer.getChildren().clear();
        
        // Always generate top opponent cards (1+ opponents)
        for (int i = 0; i < cardCount; i++) {
            Rectangle cardBack = createCardBack();
            StackPane card = new StackPane(cardBack);
            card.getStyleClass().add("uno-card");
            card.setStyle("-fx-opacity: 1.0;"); // Make sure cards are fully visible
            topPlayerCardsContainer.getChildren().add(card);
        }
        
        // Generate left opponent cards (2+ opponents) with horizontal orientation
        for (int i = 0; i < cardCount; i++) {
            Rectangle cardBack = createCardBack();
            // Use original size but rotate the card container
            StackPane card = new StackPane(cardBack);
            card.getStyleClass().add("uno-card");
            card.setStyle("-fx-opacity: 1.0;"); // Make sure cards are fully visible
            
            // Apply rotation to make cards horizontal
            card.setRotate(90);
            
            leftPlayerCardsContainer.getChildren().add(card);
        }
        
        // Generate right opponent cards (3 opponents) with horizontal orientation
        for (int i = 0; i < cardCount; i++) {
            Rectangle cardBack = createCardBack();
            // Use original size but rotate the card container
            StackPane card = new StackPane(cardBack);
            card.getStyleClass().add("uno-card");
            card.setStyle("-fx-opacity: 1.0;"); // Make sure cards are fully visible
            
            // Apply rotation to make cards horizontal
            card.setRotate(-90);
            
            rightPlayerCardsContainer.getChildren().add(card);
        }
    }
    
    /**
     * Creates a card with the specified value and color.
     * 
     * @param value The value of the card
     * @param color The color of the card
     * @return A StackPane representing the card
     */
    private StackPane createCard(String value, Color color) {
        StackPane card = new StackPane();
        card.setPrefSize(80, 120);
        card.getStyleClass().add("uno-card");
        
        Rectangle cardBase = new Rectangle(80, 120);
        cardBase.setArcWidth(15);
        cardBase.setArcHeight(15);
        
        if (color != null) {
            // Regular number card or special card with color
            cardBase.setFill(color);
            cardBase.setStroke(Color.WHITE);
            cardBase.setStrokeWidth(2);
            
            card.getChildren().add(cardBase);
            
            // Add white oval in the middle (UNO card style)
            javafx.scene.shape.Ellipse whiteEllipse = new javafx.scene.shape.Ellipse(30, 45);
            whiteEllipse.setFill(Color.WHITE);
            whiteEllipse.setRotate(30);
            
            card.getChildren().add(whiteEllipse);
            
            // Add card value
            Label valueLabel = new Label(value);
            valueLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");
            valueLabel.setTextFill(color);
            card.getChildren().add(valueLabel);
            
            // Add smaller value in top-left corner
            Label cornerLabel = new Label(value);
            cornerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            cornerLabel.setTextFill(Color.WHITE);
            cornerLabel.setTranslateX(-25);
            cornerLabel.setTranslateY(-45);
            card.getChildren().add(cornerLabel);
            
        } else {
            // Wild card - create multicolored design
            Rectangle wildCardBase = new Rectangle(80, 120);
            wildCardBase.setArcWidth(15);
            wildCardBase.setArcHeight(15);
            wildCardBase.setFill(Color.BLACK);
            
            card.getChildren().add(wildCardBase);
            
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
            
            card.getChildren().addAll(redSegment, blueSegment, yellowSegment, greenSegment);
            
            // Add "Wild" text
            Label wildLabel = new Label("Wild");
            wildLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            wildLabel.setTextFill(Color.WHITE);
            wildLabel.setTranslateY(40);
            card.getChildren().add(wildLabel);
        }
        
        return card;
    }
    
    /**
     * Creates a card back for opponent cards.
     * 
     * @return A Rectangle representing the card back
     */
    private Rectangle createCardBack() {
        Rectangle cardBack = new Rectangle(80, 120);
        cardBack.setArcWidth(15);
        cardBack.setArcHeight(15);
        cardBack.setFill(Color.valueOf("#E74C3C")); // Bright red color for better visibility
        cardBack.setStroke(Color.WHITE);
        cardBack.setStrokeWidth(3);
        return cardBack;
    }
    
    /**
     * Handles a card being played by the user
     * 
     * @param card The card that was played
     */
    private void playCard(StackPane card) {
        if (currentPlayerIndex != 0) {
            // Not the player's turn
            return;
        }
        
        // Remove the card from the player's hand
        bottomPlayerCardsContainer.getChildren().remove(card);
        
        // Add animation for card being played
        javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(Duration.millis(300), card);
        tt.setToY(-100);
        tt.setOnFinished(event -> {
            // Clear existing cards but keep the label
            Label pileLabel = null;
            for (javafx.scene.Node node : discardPileContainer.getChildren()) {
                if (node instanceof Label) {
                    pileLabel = (Label) node;
                    break;
                }
            }
            discardPileContainer.getChildren().clear();
            
            // Add the played card to the discard pile
            discardPileContainer.getChildren().add(card);
            
            // Re-add the label to remain on top
            if (pileLabel != null) {
                discardPileContainer.getChildren().add(pileLabel);
            } else {
                // Create a new label if none exists
                Label newLabel = new Label("DISCARD PILE");
                newLabel.getStyleClass().add("card-pile-label");
                discardPileContainer.getChildren().add(newLabel);
            }
            
            // Check for game ending conditions (empty hand)
            if (bottomPlayerCardsContainer.getChildren().isEmpty()) {
                handleGameEnd(true); // Player wins
                return;
            }
            
            // Move to the next player's turn
            nextTurn();
        });
        tt.play();
    }
    
    /**
     * Toggles the game direction.
     */
    private void toggleDirection() {
        isClockwise = !isClockwise;
        updateDirectionIndicator();
    }
    
    /**
     * Updates the direction indicator image.
     */
    private void updateDirectionIndicator() {
        String imagePath = isClockwise 
            ? "/images/arrow-clockwise.png" 
            : "/images/arrow-counterclockwise.png";
        
        Image directionImage = new Image(getClass().getResourceAsStream(imagePath));
        directionIndicator.setImage(directionImage);
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
     * Updates the turn label to show which player's turn it is
     */
    private void updateTurnLabel() {
        if (currentPlayerIndex == 0) {
            // Local player's turn
            currentTurnLabel.setText("YOUR TURN");
            currentTurnLabel.setStyle("-fx-background-color: rgba(0, 153, 51, 0.8);"); // Green for player
        } else if (currentPlayerIndex > 0 && currentPlayerIndex <= aiPlayers.size()) {
            // AI player's turn
            String aiName = aiPlayers.get(currentPlayerIndex - 1).getName();
            currentTurnLabel.setText(aiName + "'S TURN");
            currentTurnLabel.setStyle("-fx-background-color: rgba(204, 51, 0, 0.8);"); // Red for opponent
        }
    }
    
    /**
     * Advances to the next player's turn
     */
    private void nextTurn() {
        if (!isGameRunning) {
            return;
        }
        
        // Update current player index based on game direction
        if (isClockwise) {
            currentPlayerIndex = (currentPlayerIndex + 1) % (playerCount);
        } else {
            currentPlayerIndex = (currentPlayerIndex - 1 + playerCount) % (playerCount);
        }
        
        // Update the turn label
        updateTurnLabel();
        
        // If it's AI's turn, simulate AI move
        if (currentPlayerIndex != 0) {
            simpleAITurn(currentPlayerIndex - 1);
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
} 