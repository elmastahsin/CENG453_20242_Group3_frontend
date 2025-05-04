package metu.ceng.ceng453_20242_group3_frontend.features.game.view;

import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import metu.ceng.ceng453_20242_group3_frontend.features.game.model.Card;
import metu.ceng.ceng453_20242_group3_frontend.features.game.model.CardAction;
import metu.ceng.ceng453_20242_group3_frontend.features.game.model.CardColor;

/**
 * Manages game notifications and provides a centralized way to display notifications.
 */
public class NotificationManager {
    
    private final Pane gamePane;
    
    /**
     * Creates a new notification manager.
     * 
     * @param gamePane The parent pane where notifications will be displayed
     */
    public NotificationManager(Pane gamePane) {
        this.gamePane = gamePane;
    }
    
    /**
     * Shows a notification about a player declaring UNO.
     * 
     * @param playerName The player's name
     */
    public void showUnoCallNotification(String playerName) {
        ActionNotification notification = ActionNotification.createUnoCallNotification(playerName);
        showNotification(notification);
    }
    
    /**
     * Shows a notification about a color selection.
     * 
     * @param playerName The player's name
     * @param selectedColor The selected color
     */
    public void showColorSelectionNotification(String playerName, CardColor selectedColor) {
        ActionNotification notification = ActionNotification.createColorChangeNotification(playerName, selectedColor);
        showNotification(notification);
    }
    
    /**
     * Shows a notification about a game action.
     * 
     * @param playerName The player's name
     * @param actionMessage The action message
     */
    public void showActionNotification(String playerName, String actionMessage) {
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
     * Gets an action message based on the card type.
     * 
     * @param card The card that was played
     * @param playerName The name of the player who played the card
     * @param targetPlayerName The name of the player affected by the action
     * @return A message describing the card's action or null if no notification should be shown
     */
    public String getActionMessage(Card card, String playerName, String targetPlayerName) {
        // ONLY show notifications for action cards, NOT for number cards
        if (!card.isActionCard()) {
            return null; // Return null to indicate no notification should be shown
        }
        
        switch (card.getAction()) {
            case SKIP:
                return "plays " + card.getColor() + " Skip! " + targetPlayerName + "'s turn is skipped";
                
            case REVERSE:
                if (isTwoPlayerGame()) {
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
                return "plays Wild Draw Four! " + targetPlayerName + " draws 4 cards";
                
            default:
                return null; // Return null for any other cards that don't need notifications
        }
    }
    
    /**
     * Helper method to determine if we're in a two-player game.
     * This would typically be provided by the Game model.
     */
    private boolean isTwoPlayerGame() {
        // This is a placeholder - in real code, this would check the actual player count
        return false;
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
} 