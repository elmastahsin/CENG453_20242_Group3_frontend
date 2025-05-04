package metu.ceng.ceng453_20242_group3_frontend.features.game.view;

import javafx.application.Platform;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import metu.ceng.ceng453_20242_group3_frontend.features.game.model.Card;
import metu.ceng.ceng453_20242_group3_frontend.features.game.model.CardAction;
import metu.ceng.ceng453_20242_group3_frontend.features.game.model.CardColor;

/**
 * Manager for displaying game notifications
 */
public class NotificationManager {
    
    private final Pane parentPane;
    
    /**
     * Creates a new notification manager.
     * 
     * @param parentPane The parent pane to add notifications to
     */
    public NotificationManager(Pane parentPane) {
        this.parentPane = parentPane;
    }
    
    /**
     * Shows an action notification.
     * 
     * @param playerName The name of the player performing the action
     * @param message The action message to display
     */
    public void showActionNotification(String playerName, String message) {
        // Run on JavaFX thread to avoid threading issues
        Platform.runLater(() -> {
            // Create the notification
            ActionNotification notification = new ActionNotification(playerName, message);
            
            // Position in top-left corner of the screen
            StackPane notificationPane = notification.getNotificationPane();
            notificationPane.setLayoutX(20); // Fixed left margin
            notificationPane.setLayoutY(20); // Fixed top margin
            
            // Add to parent and show
            parentPane.getChildren().add(notificationPane);
            notification.show();
        });
    }
    
    /**
     * Shows a color selection notification.
     * 
     * @param playerName The name of the player who selected the color
     * @param selectedColor The selected color
     */
    public void showColorSelectionNotification(String playerName, CardColor selectedColor) {
        // Run on JavaFX thread to avoid threading issues
        Platform.runLater(() -> {
            // Create the notification
            ActionNotification notification = ActionNotification.createColorChangeNotification(playerName, selectedColor);
            
            // Position below any previous notification in top-left
            StackPane notificationPane = notification.getNotificationPane();
            notificationPane.setLayoutX(20); // Fixed left margin
            notificationPane.setLayoutY(100); // Position below general notifications
            
            // Add to parent and show
            parentPane.getChildren().add(notificationPane);
            notification.show();
        });
    }
    
    /**
     * Shows a UNO call notification.
     * 
     * @param playerName The name of the player calling UNO
     */
    public void showUnoCallNotification(String playerName) {
        // Run on JavaFX thread to avoid threading issues
        Platform.runLater(() -> {
            // Create the notification
            ActionNotification notification = ActionNotification.createUnoCallNotification(playerName);
            
            // Position in top-left but with priority (UNO calls are important)
            StackPane notificationPane = notification.getNotificationPane();
            notificationPane.setLayoutX(20); // Fixed left margin 
            notificationPane.setLayoutY(20); // Same as regular notifications
            
            // Add to parent and show
            parentPane.getChildren().add(notificationPane);
            notification.show();
        });
    }
    
    /**
     * Gets an action message based on the card type.
     * 
     * @param card The card that was played
     * @param playerName The name of the player who played the card
     * @param targetPlayerName The name of the player affected by the action
     * @return A message describing the card's action or null if no notification should be shown
     */
    public String getActionMessage(Card card, String playerName, String targetPlayerName) {
        if (card == null) {
            return null;
        }
        
        // Determine message based on card action
        switch (card.getAction()) {
            case SKIP:
                return "skips " + targetPlayerName + "'s turn";
                
            case REVERSE:
                return "reverses direction";
                
            case DRAW_TWO:
                return "makes " + targetPlayerName + " draw 2 cards";
                
            case WILD_DRAW_FOUR:
                return "makes " + targetPlayerName + " draw 4 cards";
                
            case WILD:
                if (card.getColor() == CardColor.MULTI) {
                    return "plays a WILD card";
                } else {
                    return "color is now " + card.getColor();
                }
                
            default:
                // Don't show notifications for regular number cards
                if (card.isNumberCard()) {
                    return null;
                }
                
                // For any other unknown action
                return "plays " + card.toString();
        }
    }
    
    /**
     * Shows notifications for a played card.
     * 
     * @param card The card that was played
     * @param playerName The name of the player who played the card
     * @param targetPlayerName The name of the player affected by the action
     * @param selectedColor The selected color (for wild cards)
     */
    public void showCardPlayNotifications(Card card, String playerName, String targetPlayerName, CardColor selectedColor) {
        // Show wild card color notification separately to ensure it's always displayed
        if (card.isWildCard() && selectedColor != CardColor.MULTI) {
            showColorSelectionNotification(playerName, selectedColor);
        }
        
        // Show action notification if needed
        String actionMessage = getActionMessage(card, playerName, targetPlayerName);
        if (actionMessage != null) {
            showActionNotification(playerName, actionMessage);
        }
    }
    
    /**
     * Shows a notification explaining why a card can't be played.
     * 
     * @param message The explanation message
     */
    public void showCardUnplayableNotification(String message) {
        ActionNotification notification = ActionNotification.createUnplayableCardNotification(message);
        notification.getNotificationPane().setStyle("-fx-background-color: rgba(211, 47, 47, 0.9);"); // Red background for errors
        
        // Add notification to the overlay
        AnchorPane.setTopAnchor(notification.getNotificationPane(), 120.0);
        AnchorPane.setRightAnchor(notification.getNotificationPane(), 10.0);
        parentPane.getChildren().add(notification.getNotificationPane());
        
        // Show the notification
        notification.show();
    }
} 