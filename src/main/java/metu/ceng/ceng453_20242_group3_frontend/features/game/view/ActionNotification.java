package metu.ceng.ceng453_20242_group3_frontend.features.game.view;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import metu.ceng.ceng453_20242_group3_frontend.features.game.model.CardColor;

/**
 * A notification component that displays game action messages.
 */
public class ActionNotification {
    
    private final StackPane notificationPane;
    private final SequentialTransition animation;
    
    /**
     * Creates a notification with the specified urgency level.
     * 
     * @param playerName The name of the player performing the action
     * @param actionMessage The action message to display
     * @param isUrgent Whether the notification is urgent (affects timing)
     */
    public ActionNotification(String playerName, String actionMessage, boolean isUrgent) {
        notificationPane = new StackPane();
        notificationPane.setPrefWidth(320);
        notificationPane.setPrefHeight(60);
        notificationPane.setMaxWidth(320);
        notificationPane.setMaxHeight(60);
        notificationPane.setOpacity(0); // Start invisible for animation
        
        // Background with rounded corners
        Rectangle background = new Rectangle(320, 60);
        background.setArcWidth(15);
        background.setArcHeight(15);
        
        // Choose background color based on message type
        if (actionMessage.equals("calls UNO!")) {
            // Special UNO call background
            background.setFill(Color.rgb(211, 47, 47, 0.9)); // UNO Red with transparency
            background.setStroke(Color.WHITE);
            background.setStrokeWidth(2);
        } else if (actionMessage.contains("drew a card and can play it")) {
            // Special notification for AI drawing a playable card
            background.setFill(Color.rgb(67, 160, 71, 0.9)); // Green with transparency
            background.setStroke(Color.WHITE);
            background.setStrokeWidth(1);
        } else if (actionMessage.contains("can't play") || actionMessage.contains("not your turn") || actionMessage.contains("match")) {
            // Special notification for unplayable cards
            background.setFill(Color.rgb(211, 47, 47, 0.9)); // Red with transparency
            background.setStroke(Color.WHITE);
            background.setStrokeWidth(2);
        } else {
            // Default notification background
            background.setFill(Color.rgb(33, 33, 33, 0.9)); // Dark gray with transparency
            background.setStroke(Color.WHITE);
            background.setStrokeWidth(1);
        }
        
        // Create the notification content
        HBox content = new HBox(15);
        content.setAlignment(Pos.CENTER_LEFT); // Left-aligned for corner positioning
        content.setPadding(new Insets(10));
        
        // Create the notification text
        String displayText = playerName + " " + actionMessage;
        Label textLabel = new Label(displayText);
        
        // Style based on urgency
        if (isUrgent) {
            textLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        } else {
            textLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        }
        
        textLabel.setWrapText(true);
        textLabel.setTextAlignment(TextAlignment.LEFT); // Left-aligned text
        
        // Only add color indicator for UNO calls and color changes (wild cards)
        if (actionMessage.contains("UNO")) {
            // Create action icon for UNO calls
            Rectangle actionIcon = new Rectangle(40, 40);
            actionIcon.setArcWidth(8);
            actionIcon.setArcHeight(8);
            actionIcon.setFill(Color.RED);
            actionIcon.setStroke(Color.WHITE);
            actionIcon.setStrokeWidth(2);
            
            content.getChildren().addAll(actionIcon, textLabel);
        } else if (actionMessage.contains("color is now")) {
            // This is a wild card with color change notification
            String colorName = actionMessage.substring(actionMessage.lastIndexOf("color is now") + 13).trim();
            
            // Create color indicator for wild cards
            Rectangle colorIndicator = new Rectangle(40, 40);
            colorIndicator.setArcWidth(8);
            colorIndicator.setArcHeight(8);
            
            // Set color based on the specified color name
            switch (colorName) {
                case "RED":
                    colorIndicator.setFill(Color.rgb(227, 35, 35)); // UNO Red
                    break;
                case "GREEN":
                    colorIndicator.setFill(Color.rgb(12, 180, 87)); // UNO Green
                    break;
                case "BLUE":
                    colorIndicator.setFill(Color.rgb(35, 110, 235)); // UNO Blue
                    break;
                case "YELLOW":
                    colorIndicator.setFill(Color.rgb(240, 196, 0)); // UNO Yellow
                    break;
                default:
                    colorIndicator.setFill(Color.GRAY);
                    break;
            }
            
            colorIndicator.setStroke(Color.WHITE);
            colorIndicator.setStrokeWidth(1);
            
            content.getChildren().addAll(colorIndicator, textLabel);
        } else if (actionMessage.contains("drew a card and can play it")) {
            // Special notification for AI drawing a playable card
            Rectangle cardDrawnIcon = new Rectangle(40, 40);
            cardDrawnIcon.setArcWidth(8);
            cardDrawnIcon.setArcHeight(8);
            cardDrawnIcon.setFill(Color.rgb(50, 205, 50)); // Green for good luck/success
            cardDrawnIcon.setStroke(Color.WHITE);
            cardDrawnIcon.setStrokeWidth(2);
            
            content.getChildren().addAll(cardDrawnIcon, textLabel);
        } else {
            // For all other actions (Skip, Reverse, Draw), just show the text without an icon
            content.getChildren().add(textLabel);
        }
        
        // Add components to the notification pane
        notificationPane.getChildren().addAll(background, content);
        
        // Create animations
        // Urgent notifications use different animation timing
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), notificationPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        // Create a pause to show the notification
        PauseTransition pause = new PauseTransition(Duration.seconds(isUrgent ? 3.5 : 2.5));
        
        // Create the disappearance animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), notificationPane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        
        // Create the sequential animation
        animation = new SequentialTransition(fadeIn, pause, fadeOut);
        animation.setOnFinished(e -> {
            // Remove the notification from its parent when animation is complete
            if (notificationPane.getParent() != null) {
                ((javafx.scene.layout.Pane) notificationPane.getParent()).getChildren().remove(notificationPane);
            }
        });
    }
    
    /**
     * Creates a new color change notification specifically for wild cards.
     * 
     * @param playerName The name of the player who played the wild card
     * @param newColor The new color selected
     * @return A configured action notification for color change
     */
    public static ActionNotification createColorChangeNotification(String playerName, CardColor newColor) {
        return new ActionNotification(playerName, "changes color to " + newColor, false);
    }
    
    /**
     * Creates a new action notification with standard urgency.
     * 
     * @param playerName The name of the player performing the action
     * @param actionMessage The action message to display
     */
    public ActionNotification(String playerName, String actionMessage) {
        this(playerName, actionMessage, false);
    }
    
    /**
     * Creates a special UNO call notification.
     * 
     * @param playerName The name of the player calling UNO
     * @return A configured action notification for UNO call
     */
    public static ActionNotification createUnoCallNotification(String playerName) {
        return new ActionNotification(playerName, "calls UNO!", true);
    }
    
    /**
     * Creates a special notification for when AI draws a playable card.
     * 
     * @param playerName The name of the AI player
     * @return A configured action notification for drawn playable card
     */
    public static ActionNotification createDrawnPlayableCardNotification(String playerName) {
        return new ActionNotification(playerName, "drew a card and can play it", true);
    }
    
    /**
     * Creates a special notification for unplayable cards.
     * 
     * @param message The error message explaining why the card can't be played
     * @return A configured action notification for unplayable card
     */
    public static ActionNotification createUnplayableCardNotification(String message) {
        return new ActionNotification("", message, true);
    }
    
    /**
     * Gets the notification pane.
     * 
     * @return The notification pane
     */
    public StackPane getNotificationPane() {
        return notificationPane;
    }
    
    /**
     * Shows the notification.
     */
    public void show() {
        animation.play();
    }
} 