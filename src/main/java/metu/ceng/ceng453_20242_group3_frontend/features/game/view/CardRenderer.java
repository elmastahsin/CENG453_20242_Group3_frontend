package metu.ceng.ceng453_20242_group3_frontend.features.game.view;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.effect.DropShadow;

import metu.ceng.ceng453_20242_group3_frontend.features.game.model.Card;
import metu.ceng.ceng453_20242_group3_frontend.features.game.model.CardAction;
import metu.ceng.ceng453_20242_group3_frontend.features.game.model.CardColor;

/**
 * Utility class for rendering UNO cards.
 * Handles the visual creation of different types of cards.
 */
public class CardRenderer {
    // Card colors for card creation
    private static final Color RED_COLOR = Color.rgb(227, 35, 45);
    private static final Color GREEN_COLOR = Color.rgb(67, 176, 71);
    private static final Color BLUE_COLOR = Color.rgb(0, 122, 193);
    private static final Color YELLOW_COLOR = Color.rgb(243, 206, 37);
    
    /**
     * Creates a visual representation of a card.
     *
     * @param card The card model to display
     * @return A StackPane containing the card visualization
     */
    public static StackPane createCardView(Card card) {
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
    private static StackPane createNumberCardView(Card card) {
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
            cardView.setEffect(new DropShadow(15, Color.WHITE));
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
    private static StackPane createActionCardView(Card card) {
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
            cardView.setEffect(new DropShadow(15, Color.WHITE));
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
    private static StackPane createWildCardView(Card card) {
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
            cardView.setEffect(new DropShadow(15, Color.WHITE));
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
    private static StackPane createWildDrawFourCardView(Card card) {
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
            cardView.setEffect(new DropShadow(15, Color.WHITE));
            cardView.setStyle("-fx-cursor: hand;");
        }
        
        return cardView;
    }

    /**
     * Creates a card back for opponent cards.
     *
     * @return A StackPane representing the card back
     */
    public static StackPane createCardBackView() {
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
     * Creates an empty card placeholder for empty discard pile.
     *
     * @return A StackPane representing an empty card placeholder
     */
    public static StackPane createEmptyCardPlaceholder() {
        StackPane cardView = new StackPane();
        cardView.setPrefSize(80, 120);
        cardView.getStyleClass().add("uno-card");
        
        // Create an empty card placeholder with dashed border
        Rectangle emptyRect = new Rectangle(80, 120);
        emptyRect.setArcWidth(15);
        emptyRect.setArcHeight(15);
        emptyRect.setFill(Color.rgb(0, 50, 0, 0.5));
        emptyRect.setStroke(Color.WHITE);
        emptyRect.setStrokeWidth(2);
        emptyRect.getStrokeDashArray().addAll(5.0, 5.0);
        cardView.getChildren().add(emptyRect);
        
        // Add an "EMPTY" label
        Label emptyLabel = new Label("EMPTY");
        emptyLabel.setTextFill(Color.WHITE);
        emptyLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        cardView.getChildren().add(emptyLabel);
        
        return cardView;
    }
    
    /**
     * Helper method to convert CardColor to JavaFX Color.
     *
     * @param cardColor The CardColor enum value
     * @return The corresponding JavaFX Color
     */
    private static Color getColorFromCardColor(CardColor cardColor) {
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
     * Creates a visual representation of a wild card with a selected color.
     *
     * @param card The card model to display
     * @param selectedColor The color selected for the wild card
     * @return A StackPane containing the card visualization
     */
    public static StackPane createWildCardWithSelectedColor(Card card, CardColor selectedColor) {
        StackPane cardView = createWildCardView(card);
        
        // Create color indicator
        Rectangle colorIndicator = new Rectangle(30, 30);
        colorIndicator.setArcWidth(15);
        colorIndicator.setArcHeight(15);
        
        // Set color based on selection
        switch (selectedColor) {
            case RED:
                colorIndicator.setFill(Color.rgb(227, 35, 45));
                break;
            case BLUE:
                colorIndicator.setFill(Color.rgb(0, 122, 193));
                break;
            case GREEN:
                colorIndicator.setFill(Color.rgb(67, 176, 71));
                break;
            case YELLOW:
                colorIndicator.setFill(Color.rgb(243, 206, 37));
                break;
            default:
                // Should never happen for a wild card with selected color
                colorIndicator.setFill(Color.WHITE);
                break;
        }
        
        // Add a white border
        colorIndicator.setStroke(Color.WHITE);
        colorIndicator.setStrokeWidth(2);
        
        // Position at bottom of card
        colorIndicator.setTranslateY(35);
        
        // Add to the card view
        cardView.getChildren().add(colorIndicator);
        
        return cardView;
    }

    /**
     * Creates a visual representation of a wild draw four card with a selected color.
     *
     * @param card The card model to display
     * @param selectedColor The color selected for the wild card
     * @return A StackPane containing the card visualization
     */
    public static StackPane createWildDrawFourWithSelectedColor(Card card, CardColor selectedColor) {
        StackPane cardView = createWildDrawFourCardView(card);
        
        // Create color indicator
        Rectangle colorIndicator = new Rectangle(30, 30);
        colorIndicator.setArcWidth(15);
        colorIndicator.setArcHeight(15);
        
        // Set color based on selection
        switch (selectedColor) {
            case RED:
                colorIndicator.setFill(Color.rgb(227, 35, 45));
                break;
            case BLUE:
                colorIndicator.setFill(Color.rgb(0, 122, 193));
                break;
            case GREEN:
                colorIndicator.setFill(Color.rgb(67, 176, 71));
                break;
            case YELLOW:
                colorIndicator.setFill(Color.rgb(243, 206, 37));
                break;
            default:
                // Should never happen for a wild card with selected color
                colorIndicator.setFill(Color.WHITE);
                break;
        }
        
        // Add a white border
        colorIndicator.setStroke(Color.WHITE);
        colorIndicator.setStrokeWidth(2);
        
        // Position at bottom of card
        colorIndicator.setTranslateY(35);
        
        // Add to the card view
        cardView.getChildren().add(colorIndicator);
        
        return cardView;
    }
} 