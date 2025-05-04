package metu.ceng.ceng453_20242_group3_frontend.features.game.view;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;

import metu.ceng.ceng453_20242_group3_frontend.features.game.model.Card;
import metu.ceng.ceng453_20242_group3_frontend.features.game.model.CardAction;
import metu.ceng.ceng453_20242_group3_frontend.features.game.model.CardColor;

/**
 * Utility class for rendering UNO cards.
 * Creates visual representations of different types of cards.
 */
public class CardRenderer {
    // Card dimensions
    private static final double CARD_WIDTH = 80;
    private static final double CARD_HEIGHT = 120;
    private static final double CARD_ARC = 15;
    
    // Standard UNO colors
    private static final Color RED_COLOR = Color.rgb(227, 35, 45);
    private static final Color GREEN_COLOR = Color.rgb(67, 176, 71);
    private static final Color BLUE_COLOR = Color.rgb(0, 122, 193);
    private static final Color YELLOW_COLOR = Color.rgb(243, 206, 37);
    private static final Color BLACK_COLOR = Color.rgb(30, 30, 30);
    private static final Color UNO_RED = Color.rgb(211, 47, 47);
    
    /**
     * Creates a visual representation of a card.
     *
     * @param card The card model to display
     * @return A StackPane containing the card visualization
     */
    public static StackPane createCardView(Card card) {
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
        StackPane cardView = createBaseCard(card.getColor());
        
        // Add white oval in the middle (UNO card style)
        Ellipse whiteEllipse = new Ellipse(30, 45);
        whiteEllipse.setFill(Color.WHITE);
        whiteEllipse.setRotate(30);
        whiteEllipse.setEffect(new InnerShadow(3, getColorFromCardColor(card.getColor()).darker()));
        
        cardView.getChildren().add(whiteEllipse);
        
        // Add card value in center
        String value = String.valueOf(card.getValue());
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-font-family: 'Arial Black';");
        valueLabel.setTextFill(getColorFromCardColor(card.getColor()));
        cardView.getChildren().add(valueLabel);
        
        // Add smaller value in top-left corner
        Label topLeftLabel = new Label(value);
        topLeftLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Arial Black';");
        topLeftLabel.setTextFill(Color.WHITE);
        topLeftLabel.setTranslateX(-25);
        topLeftLabel.setTranslateY(-45);
        cardView.getChildren().add(topLeftLabel);
        
        // Add reflected value in bottom-right corner
        Label bottomRightLabel = new Label(value);
        bottomRightLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Arial Black';");
        bottomRightLabel.setTextFill(Color.WHITE);
        bottomRightLabel.setTranslateX(25);
        bottomRightLabel.setTranslateY(45);
        bottomRightLabel.setRotate(180);
        cardView.getChildren().add(bottomRightLabel);
        
        setPlayableEffect(card, cardView);
        
        return cardView;
    }

    /**
     * Creates a visual representation of an action card.
     *
     * @param card The card model to display
     * @return A StackPane containing the card visualization
     */
    private static StackPane createActionCardView(Card card) {
        StackPane cardView = createBaseCard(card.getColor());
        Color cardColor = getColorFromCardColor(card.getColor());
        
        String actionSymbol = card.getAction().getSymbol();
        
        // Special handling for Draw Two cards to match the image
        if (card.getAction() == CardAction.DRAW_TWO) {
            // Create white oval background
            Ellipse whiteOval = new Ellipse(30, 45);
            whiteOval.setFill(Color.WHITE);
            whiteOval.setRotate(30);
            whiteOval.setEffect(new InnerShadow(3, cardColor.darker()));
            cardView.getChildren().add(whiteOval);
            
            // Create the +2 symbol like in the official UNO cards
            Label plusLabel = new Label("+2");
            plusLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-font-family: 'Arial Black';");
            plusLabel.setTextFill(cardColor);
            cardView.getChildren().add(plusLabel);
            
            // Add small +2 in top-left corner
            Label topLeftLabel = new Label("+2");
            topLeftLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Arial Black';");
            topLeftLabel.setTextFill(Color.WHITE);
            topLeftLabel.setTranslateX(-25);
            topLeftLabel.setTranslateY(-45);
            cardView.getChildren().add(topLeftLabel);
            
            // Add reflected +2 in bottom-right corner
            Label bottomRightLabel = new Label("+2");
            bottomRightLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Arial Black';");
            bottomRightLabel.setTextFill(Color.WHITE);
            bottomRightLabel.setTranslateX(25);
            bottomRightLabel.setTranslateY(45);
            bottomRightLabel.setRotate(180);
            cardView.getChildren().add(bottomRightLabel);
        } else {
            // For other action cards: Skip, Reverse
            // Add white oval in the middle (UNO card style)
            Ellipse whiteEllipse = new Ellipse(30, 45);
            whiteEllipse.setFill(Color.WHITE);
            whiteEllipse.setRotate(30);
            whiteEllipse.setEffect(new InnerShadow(3, cardColor.darker()));
            
            cardView.getChildren().add(whiteEllipse);
            
            // Add card action symbol and name
            String actionText = "";
            
            switch (card.getAction()) {
                case SKIP:
                    actionText = "SKIP";
                    break;
                case REVERSE:
                    actionText = "REV";
                    break;
                default:
                    actionText = card.getAction().name();
                    break;
            }
            
            // Create symbol
            Label symbolLabel = new Label(actionSymbol);
            symbolLabel.setStyle("-fx-font-size: 38px; -fx-font-weight: bold; -fx-font-family: 'Arial Black';");
            symbolLabel.setTextFill(cardColor);
            cardView.getChildren().add(symbolLabel);
            
            // Add text below symbol
            Label textLabel = new Label(actionText);
            textLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-font-family: 'Arial Black';");
            textLabel.setTextFill(cardColor);
            textLabel.setTranslateY(30);
            cardView.getChildren().add(textLabel);
            
            // Add smaller symbol in top-left corner
            Label topLeftLabel = new Label(actionSymbol);
            topLeftLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Arial Black';");
            topLeftLabel.setTextFill(Color.WHITE);
            topLeftLabel.setTranslateX(-25);
            topLeftLabel.setTranslateY(-45);
            cardView.getChildren().add(topLeftLabel);
            
            // Add reflected symbol in bottom-right corner
            Label bottomRightLabel = new Label(actionSymbol);
            bottomRightLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Arial Black';");
            bottomRightLabel.setTextFill(Color.WHITE);
            bottomRightLabel.setTranslateX(25);
            bottomRightLabel.setTranslateY(45);
            bottomRightLabel.setRotate(180);
            cardView.getChildren().add(bottomRightLabel);
        }
        
        setPlayableEffect(card, cardView);
        
        return cardView;
    }

    /**
     * Creates a visual representation of a wild card.
     *
     * @param card The card model to display
     * @return A StackPane containing the card visualization
     */
    private static StackPane createWildCardView(Card card) {
        // Create a black base for wild cards
        StackPane cardView = createBaseCard(CardColor.MULTI);
        
        // Create the wild card circle with segments - UNO style
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
        
        // Add "W" in top-left corner
        Label topLeftLabel = new Label("W");
        topLeftLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Arial Black';");
        topLeftLabel.setTextFill(Color.WHITE);
        topLeftLabel.setTranslateX(-25);
        topLeftLabel.setTranslateY(-45);
        cardView.getChildren().add(topLeftLabel);
        
        // Add reflected "W" in bottom-right corner
        Label bottomRightLabel = new Label("W");
        bottomRightLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Arial Black';");
        bottomRightLabel.setTextFill(Color.WHITE);
        bottomRightLabel.setTranslateX(25);
        bottomRightLabel.setTranslateY(45);
        bottomRightLabel.setRotate(180);
        cardView.getChildren().add(bottomRightLabel);
        
        setPlayableEffect(card, cardView);
        
        return cardView;
    }
    
    /**
     * Creates a visual representation of a Wild Draw Four card.
     *
     * @param card The card model to display
     * @return A StackPane containing the card visualization
     */
    private static StackPane createWildDrawFourCardView(Card card) {
        // Create a black base for wild cards
        StackPane cardView = createBaseCard(CardColor.MULTI);
        
        // Create the wild card circle with segments - UNO style
        double centerX = 40;
        double centerY = 50;
        double radius = 25;
        
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
        Label plusFourLabel = new Label("+4");
        plusFourLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-font-family: 'Arial Black';");
        plusFourLabel.setTextFill(Color.WHITE);
        plusFourLabel.setTranslateY(20);
        cardView.getChildren().add(plusFourLabel);
        
        // Add "+4" in top-left corner
        Label topLeftLabel = new Label("+4");
        topLeftLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Arial Black';");
        topLeftLabel.setTextFill(Color.WHITE);
        topLeftLabel.setTranslateX(-25);
        topLeftLabel.setTranslateY(-45);
        cardView.getChildren().add(topLeftLabel);
        
        // Add reflected "+4" in bottom-right corner
        Label bottomRightLabel = new Label("+4");
        bottomRightLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Arial Black';");
        bottomRightLabel.setTextFill(Color.WHITE);
        bottomRightLabel.setTranslateX(25);
        bottomRightLabel.setTranslateY(45);
        bottomRightLabel.setRotate(180);
        cardView.getChildren().add(bottomRightLabel);
        
        setPlayableEffect(card, cardView);
        
        return cardView;
    }

    /**
     * Creates a card back for opponent cards.
     *
     * @return A StackPane representing the card back
     */
    public static StackPane createCardBackView() {
        StackPane cardView = new StackPane();
        cardView.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        cardView.getStyleClass().add("uno-card");
        
        // Create card back with UNO red color
        Rectangle cardBack = new Rectangle(CARD_WIDTH, CARD_HEIGHT);
        cardBack.setArcWidth(CARD_ARC);
        cardBack.setArcHeight(CARD_ARC);
        cardBack.setFill(BLACK_COLOR);
        cardBack.setStroke(Color.WHITE);
        cardBack.setStrokeWidth(3);
        cardView.getChildren().add(cardBack);
        
        // Create an oval for the UNO logo background
        Ellipse logoBackground = new Ellipse(35, 25);
        logoBackground.setFill(Color.WHITE);
        logoBackground.setRotate(-20);
        cardView.getChildren().add(logoBackground);
        
        // Add the UNO logo
        Label unoLabel = new Label("UNO");
        unoLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-font-family: 'Impact';");
        unoLabel.setTextFill(UNO_RED);
        cardView.getChildren().add(unoLabel);
        
        // Add a border around the UNO text
        Ellipse logoBorder = new Ellipse(35, 25);
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
        cardView.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        cardView.getStyleClass().add("uno-card");
        
        // Create an empty card placeholder with dashed border and semi-transparent fill
        Rectangle emptyRect = new Rectangle(CARD_WIDTH, CARD_HEIGHT);
        emptyRect.setArcWidth(CARD_ARC);
        emptyRect.setArcHeight(CARD_ARC);
        emptyRect.setFill(Color.rgb(20, 70, 20, 0.5));
        emptyRect.setStroke(Color.WHITE);
        emptyRect.setStrokeWidth(2);
        emptyRect.getStrokeDashArray().addAll(5.0, 5.0);
        cardView.getChildren().add(emptyRect);
        
        // Add an "EMPTY" label
        Label emptyLabel = new Label("EMPTY");
        emptyLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'Arial Black';");
        emptyLabel.setTextFill(Color.WHITE);
        cardView.getChildren().add(emptyLabel);
        
        return cardView;
    }
    
    /**
     * Creates a visual representation of a wild card with a selected color.
     *
     * @param card The card model to display
     * @param selectedColor The color selected for the wild card
     * @return A StackPane containing the card visualization
     */
    public static StackPane createWildCardWithSelectedColor(Card card, CardColor selectedColor) {
        StackPane cardView;
        if (card.getAction() == CardAction.WILD) {
            cardView = createWildCardView(card);
        } else {
            cardView = createWildDrawFourCardView(card);
        }
        
        // Create color indicator with simpler styling
        Rectangle colorIndicator = new Rectangle(30, 30);
        colorIndicator.setArcWidth(30);
        colorIndicator.setArcHeight(30);
        
        Color selectedColorFill = getColorFromCardColor(selectedColor);
        colorIndicator.setFill(selectedColorFill);
        
        // Add a white border
        colorIndicator.setStroke(Color.WHITE);
        colorIndicator.setStrokeWidth(2);
        
        // Position at bottom of card
        colorIndicator.setTranslateY(40);
        
        // Add to the card view
        cardView.getChildren().add(colorIndicator);
        
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
                return BLACK_COLOR;
            default:
                return BLACK_COLOR;
        }
    }

    /**
     * Creates a base card with the specified color.
     * 
     * @param color The color of the card
     * @return A StackPane with the base card
     */
    private static StackPane createBaseCard(CardColor color) {
        StackPane cardView = new StackPane();
        cardView.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        cardView.getStyleClass().add("uno-card");
        
        Rectangle cardBase = new Rectangle(CARD_WIDTH, CARD_HEIGHT);
        cardBase.setArcWidth(CARD_ARC);
        cardBase.setArcHeight(CARD_ARC);
        
        Color cardColor = getColorFromCardColor(color);
        
        // Use a very subtle gradient (almost solid) to avoid darkness
        Stop[] stops = new Stop[]{
            new Stop(0, cardColor),
            new Stop(1, cardColor.deriveColor(0, 1.0, 0.95, 1.0))  // Just slightly darker
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
        cardBase.setFill(gradient);
        
        cardBase.setStroke(Color.WHITE);
        cardBase.setStrokeWidth(2);
        
        cardView.getChildren().add(cardBase);
        return cardView;
    }
    
    /**
     * Sets appropriate visual effects for playable cards.
     * 
     * @param card The card model
     * @param cardView The card view to modify
     */
    private static void setPlayableEffect(Card card, StackPane cardView) {
        if (card.isPlayable()) {
            // Use a lighter shadow effect for better performance
            DropShadow highlight = new DropShadow(8, Color.GOLD);
            cardView.setEffect(highlight);
            cardView.setStyle("-fx-cursor: hand;");
            
            // Use a simpler highlight instead of scaling to improve performance
            cardView.setOpacity(1.0);
        } else {
            cardView.setOpacity(0.85);
        }
    }
} 