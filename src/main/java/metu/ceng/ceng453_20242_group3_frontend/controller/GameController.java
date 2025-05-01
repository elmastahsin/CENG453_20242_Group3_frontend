package metu.ceng.ceng453_20242_group3_frontend.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Pair;
import metu.ceng.ceng453_20242_group3_frontend.util.SessionManager;

/**
 * Controller for the game view.
 */
public class GameController {
    
    @FXML
    private BorderPane gamePane;
    
    @FXML
    private ImageView logoImageView;
    
    @FXML
    private Label opponentNameLabel;
    
    @FXML
    private HBox opponentCardsContainer;
    
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
    
    @FXML
    private Label discardLabel;
    
    @FXML
    private ImageView clockwiseIndicator;
    
    @FXML
    private ImageView counterClockwiseIndicator;
    
    @FXML
    private StackPane drawPileContainer;
    
    @FXML
    private StackPane discardPileContainer;
    
    @FXML
    private Label turnIndicatorLabel;
    
    @FXML
    private Label playerNameLabel;
    
    @FXML
    private VBox chatMessagesContainer;
    
    @FXML
    private Button sendMessageButton;
    
    @FXML
    private ListView<String> leaderboardListView;
    
    @FXML
    private HBox playerCardsContainer;
    
    // Card colors
    private final Color RED_COLOR = Color.rgb(227, 35, 45);
    private final Color GREEN_COLOR = Color.rgb(67, 176, 71);
    private final Color BLUE_COLOR = Color.rgb(0, 122, 193);
    private final Color YELLOW_COLOR = Color.rgb(243, 206, 37);
    
    // Direction
    private boolean isClockwise = true;
    
    // Will hold the references to our custom direction indicators
    private StackPane clockwisePane;
    private StackPane counterClockwisePane;
    
    @FXML
    private void initialize() {
        // Set player name from session
        if (SessionManager.getInstance().isLoggedIn()) {
            String username = SessionManager.getInstance().getCurrentUser().getUsername();
            playerNameLabel.setText(username);
        }
        
        // Set up action buttons
        setupActionButtons();
        
        // Create direction indicators
        setupDirectionIndicators();
        
        // Create draw and discard piles
        setupCardPiles();
        
        // Add sample player cards
        createPlayerCards();
        
        // Add sample opponent cards
        createOpponentCards();
        
        // Set up leaderboard
        setupLeaderboard();
        
        // Add sample chat messages
        addSampleChatMessages();
        
        // Set up send message button
        sendMessageButton.setOnAction(event -> sendChatMessage());
    }
    
    private void setupActionButtons() {
        skipButton.setOnAction(event -> playSpecialCard("Skip"));
        reverseButton.setOnAction(event -> {
            playSpecialCard("Reverse");
            toggleDirection();
        });
        drawTwoButton.setOnAction(event -> playSpecialCard("Draw Two"));
        wildButton.setOnAction(event -> playSpecialCard("Wild"));
        wildDrawFourButton.setOnAction(event -> playSpecialCard("Wild Draw Four"));
    }
    
    private void setupDirectionIndicators() {
        try {
            // Create direction arrows using shapes instead of images
            StackPane clockwiseArrow = createDirectionArrow(true);
            StackPane counterClockwiseArrow = createDirectionArrow(false);
            
            // Add arrows to the layout
            clockwisePane = new StackPane(clockwiseArrow);
            counterClockwisePane = new StackPane(counterClockwiseArrow);
            
            clockwisePane.setMaxSize(50, 50);
            counterClockwisePane.setMaxSize(50, 50);
            
            // Replace the ImageView with the custom arrows
            StackPane parentCW = (StackPane) clockwiseIndicator.getParent();
            StackPane parentCCW = (StackPane) counterClockwiseIndicator.getParent();
            
            int cwIndex = parentCW.getChildren().indexOf(clockwiseIndicator);
            int ccwIndex = parentCCW.getChildren().indexOf(counterClockwiseIndicator);
            
            if (cwIndex >= 0) {
                parentCW.getChildren().set(cwIndex, clockwisePane);
            } else {
                parentCW.getChildren().add(clockwisePane);
            }
            
            if (ccwIndex >= 0) {
                parentCCW.getChildren().set(ccwIndex, counterClockwisePane);
            } else {
                parentCCW.getChildren().add(counterClockwisePane);
            }
            
            // Show only the current direction
            updateDirectionIndicators();
            
        } catch (Exception e) {
            System.err.println("Error setting up direction indicators: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private StackPane createDirectionArrow(boolean clockwise) {
        StackPane arrowPane = new StackPane();
        arrowPane.setMaxSize(40, 40);
        
        // Create circle
        javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(20);
        circle.setFill(Color.ORANGE);
        circle.setStroke(Color.WHITE);
        circle.setStrokeWidth(2);
        
        // Create arrow path
        javafx.scene.shape.SVGPath arrow = new javafx.scene.shape.SVGPath();
        if (clockwise) {
            arrow.setContent("M10,5 L20,15 L10,25 M30,15 L15,15");
        } else {
            arrow.setContent("M30,5 L20,15 L30,25 M10,15 L25,15");
        }
        arrow.setFill(Color.TRANSPARENT);
        arrow.setStroke(Color.WHITE);
        arrow.setStrokeWidth(3);
        
        arrowPane.getChildren().addAll(circle, arrow);
        return arrowPane;
    }
    
    private void setupCardPiles() {
        // Create draw pile
        Rectangle drawCardBack = createCardBack();
        drawPileContainer.getChildren().add(drawCardBack);
        
        // Create discard pile with a sample card
        StackPane discardCard = createCard("3", RED_COLOR);
        discardPileContainer.getChildren().add(discardCard);
    }
    
    private void createPlayerCards() {
        // Sample player cards
        List<Pair<String, Color>> playerCards = Arrays.asList(
            new Pair<>("7", BLUE_COLOR),
            new Pair<>("1", RED_COLOR),
            new Pair<>("2", GREEN_COLOR),
            new Pair<>("Wild", null)
        );
        
        for (Pair<String, Color> cardInfo : playerCards) {
            StackPane card = createCard(cardInfo.getKey(), cardInfo.getValue());
            card.setOnMouseClicked(event -> playCard(card));
            playerCardsContainer.getChildren().add(card);
        }
    }
    
    private void createOpponentCards() {
        // Sample opponent cards (show card backs)
        for (int i = 0; i < 5; i++) {
            Rectangle cardBack = createCardBack();
            cardBack.setWidth(70); // Smaller cards for opponent
            cardBack.setHeight(100);
            opponentCardsContainer.getChildren().add(cardBack);
        }
    }
    
    private void setupLeaderboard() {
        // Sample leaderboard data
        leaderboardListView.getItems().addAll(
            "1 Emily",
            "2 Michael",
            "3 David"
        );
    }
    
    private void addSampleChatMessages() {
        addChatMessage("Hello!");
        addChatMessage("Good luck!");
        addChatMessage("gg!");
    }
    
    private void addChatMessage(String message) {
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(Double.MAX_VALUE);
        messageLabel.getStyleClass().add("chat-message");
        chatMessagesContainer.getChildren().add(messageLabel);
    }
    
    private void sendChatMessage() {
        // This would send a message to other players in a real implementation
        addChatMessage("You: Message sent");
    }
    
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
    
    private Rectangle createCardBack() {
        Rectangle cardBack = new Rectangle(80, 120);
        cardBack.setArcWidth(15);
        cardBack.setArcHeight(15);
        cardBack.setFill(Color.valueOf("#1A6877"));
        cardBack.setStroke(Color.WHITE);
        cardBack.setStrokeWidth(2);
        return cardBack;
    }
    
    private void playCard(StackPane card) {
        // Remove the selected card from player's hand
        playerCardsContainer.getChildren().remove(card);
        
        // Clear the discard pile and add the new card
        discardPileContainer.getChildren().clear();
        discardPileContainer.getChildren().add(card);
        
        // Reset the click handler
        card.setOnMouseClicked(null);
    }
    
    private void playSpecialCard(String type) {
        System.out.println("Playing special card: " + type);
        // In a real implementation, this would send the action to the game server
    }
    
    private void toggleDirection() {
        isClockwise = !isClockwise;
        updateDirectionIndicators();
    }
    
    private void updateDirectionIndicators() {
        if (clockwisePane != null && counterClockwisePane != null) {
            clockwisePane.setVisible(isClockwise);
            counterClockwisePane.setVisible(!isClockwise);
        }
    }
    
    /**
     * Navigates back to the main menu.
     */
    private void navigateToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metu/ceng/ceng453_20242_group3_frontend/main-menu-view.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) gamePane.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 